package uk.gov.hmcts.reform.bulkscan.endtoendtests.helper;

import com.google.common.collect.ImmutableMap;
import com.google.common.io.Resources;
import org.apache.commons.io.FilenameUtils;
import uk.gov.hmcts.reform.bulkscan.endtoendtests.utils.ContainerJurisdictionPoBoxMapper;
import uk.gov.hmcts.reform.bulkscan.endtoendtests.utils.OcrDataEncoder;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static com.google.common.io.Resources.getResource;
import static com.google.common.io.Resources.toByteArray;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;

public final class ZipFileHelper {

    public static final String ENVELOPE_ZIPFILE_NAME = "envelope.zip";
    public static final String SIGNATURE_FILE_NAME = "signature";

    private static final DateTimeFormatter FILE_NAME_DATE_TIME_FORMAT =
        DateTimeFormatter.ofPattern("dd-MM-yyyy-HH-mm-ss");

    private ZipFileHelper() {
        // utility class
    }

    public static ZipArchive createZipArchive(
        String dirName,
        Container container
    ) throws Exception {
        var ocrData = OcrDataEncoder.encodeDefaultOcrData(container);
        return createZipArchive(dirName, container, "", ocrData);
    }

    public static ZipArchive createZipArchive(
        String dirName,
        Container container,
        String caseNumber,
        String ocrData
    ) throws Exception {
        List<String> files =
            Stream.of(new File(getResource(dirName).getPath()).listFiles())
                .map(e -> dirName + "/" + e.getName())
                .collect(toList());

        return createZipArchive(
            files.stream().filter(f -> f.endsWith(".pdf")).collect(toList()),
            files.stream().filter(f -> f.endsWith(".json")).collect(toList()).get(0),
            container,
            caseNumber,
            ocrData
        );
    }

    private static ZipArchive createZipArchive(
        List<String> pdfFiles,
        String metadataFile,
        Container container,
        String caseNumber,
        String ocrData
    ) throws Exception {
        String zipFileName = String.format(
            "%s_%s.test.zip",
            ThreadLocalRandom.current().nextInt(0, Integer.MAX_VALUE),
            LocalDateTime.now().format(FILE_NAME_DATE_TIME_FORMAT)
        );
        var containerMapping = ContainerJurisdictionPoBoxMapper.getMappedContainerData(container);

        String metadataContent = updateMetadata(
            metadataFile,
            zipFileName,
            caseNumber,
            containerMapping.jurisdiction,
            containerMapping.poBox,
            containerMapping.formType,
            ocrData
        );

        byte[] zipContents = createZipArchiveWithDocumentsAndMetadata(pdfFiles, metadataContent);

        var outputStream = new ByteArrayOutputStream();
        try (var zos = new ZipOutputStream(outputStream)) {
            zos.putNextEntry(new ZipEntry(ENVELOPE_ZIPFILE_NAME));
            zos.write(zipContents);
            zos.closeEntry();

            // add signature
            zos.putNextEntry(new ZipEntry(SIGNATURE_FILE_NAME));
            zos.write(SigningHelper.sign(zipContents));
            zos.closeEntry();
        }

        return new ZipArchive(
            zipFileName,
            outputStream.toByteArray()
        );
    }

    private static byte[] createZipArchiveWithDocumentsAndMetadata(
        List<String> pdfFiles,
        String metadataContent
    ) throws Exception {
        var outputStream = new ByteArrayOutputStream();
        try (var zos = new ZipOutputStream(outputStream)) {
            for (String pdf : pdfFiles) {
                zos.putNextEntry(new ZipEntry(FilenameUtils.getName(pdf)));
                zos.write(toByteArray(getResource(pdf)));
                zos.closeEntry();
            }

            // add metadata
            zos.putNextEntry(new ZipEntry("metadata.json"));
            zos.write(metadataContent.getBytes());
            zos.closeEntry();
        }
        return outputStream.toByteArray();
    }

    private static String updateMetadata(
        String metadataFile,
        String zipFileName,
        String caseNumber,
        String jurisdiction,
        String poBox,
        String formType,
        String ocrData
    ) throws Exception {
        assertThat(metadataFile).isNotBlank();

        String metadataTemplate =
            Resources.toString(getResource(metadataFile), StandardCharsets.UTF_8);

        var replacements = ImmutableMap
            .<String, String>builder()
            .put("$$zip_file_name$$", zipFileName)
            .put("$$dcn1$$", generateDocumentDcnNumber())
            .put("$$payment_dcn$$", generatePaymentDcnNumber())
            .put("$$case_number$$", caseNumber)
            .put("$$jurisdiction$$", jurisdiction)
            .put("$$po_box$$", poBox)
            .put("$$form_type$$", formType)
            .put("$$ocr_data$$", ocrData)
            .build();

        return replacements
            .entrySet()
            .stream()
            .reduce(
                metadataTemplate,
                (metadata, entry) -> entry.getValue() == null
                    ? metadata
                    : metadata.replace(entry.getKey(), entry.getValue()),
                (metadata1, metadata2) -> metadata2 // return newly replaced string
            );
    }

    private static String generateDocumentDcnNumber() {
        return generateDcnNumber(17);
    }

    private static String generatePaymentDcnNumber() {
        return generateDcnNumber(21);
    }

    private static String generateDcnNumber(int length) {
        return (Long.toString(System.nanoTime()) + System.nanoTime()).substring(0, length);
    }

    public static class ZipArchive {
        public final String fileName;
        public final byte[] content;

        public ZipArchive(String fileName, byte[] content) {
            this.fileName = fileName;
            this.content = content;
        }
    }
}
