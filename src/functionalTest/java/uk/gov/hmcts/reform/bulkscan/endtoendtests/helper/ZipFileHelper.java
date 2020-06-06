package uk.gov.hmcts.reform.bulkscan.endtoendtests.helper;

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
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static com.google.common.io.Resources.getResource;
import static com.google.common.io.Resources.toByteArray;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;

public final class ZipFileHelper {

    private static final Random RANDOM = new Random();
    public static final String ENVELOPE_ZIPFILE_NAME = "envelope.zip";
    public static final String SIGNATURE_FILE_NAME = "signature";

    private static final DateTimeFormatter FILE_NAME_DATE_TIME_FORMAT =
        DateTimeFormatter.ofPattern("dd-MM-yyyy-HH-mm-ss");

    private ZipFileHelper() {
        // utility class
    }

    public static ZipArchive createZipArchive(String dirName, Container container) throws Exception {
        List<String> files =
            Stream.of(new File(getResource(dirName).getPath()).listFiles())
                .map(e -> dirName + "/" + e.getName())
                .collect(toList());

        return createZipArchive(
            files.stream().filter(f -> f.endsWith(".pdf")).collect(toList()),
            files.stream().filter(f -> f.endsWith(".json")).collect(toList()).get(0),
            container
        );
    }

    public static ZipArchive createZipArchive(
        List<String> pdfFiles,
        String metadataFile,
        Container container
    ) throws Exception {
        String zipFileName = String.format(
            "%s_%s.test.zip",
            ThreadLocalRandom.current().nextInt(0, Integer.MAX_VALUE),
            LocalDateTime.now().format(FILE_NAME_DATE_TIME_FORMAT)
        );
        var containerMapping = ContainerJurisdictionPoBoxMapper.getMappedContainerData(container);
        var ocrData = OcrDataEncoder.encodeDefaultOcrData(container);

        String metadataContent = updateMetadata(
            metadataFile,
            zipFileName,
            containerMapping.jurisdiction,
            containerMapping.poBox,
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
        String jurisdiction,
        String poBox,
        String ocrData
    ) throws Exception {
        assertThat(metadataFile).isNotBlank();

        String metadataTemplate =
            Resources.toString(getResource(metadataFile), StandardCharsets.UTF_8);

        return metadataTemplate
            .replace("$$zip_file_name$$", zipFileName)
            .replace("$$dcn1$$", generateDcnNumber())
            .replace("$$jurisdiction$$", jurisdiction)
            .replace("$$po_box$$", poBox)
            .replace("$$ocr_data$$", ocrData);
    }

    private static String generateDcnNumber() {
        return Long.toString(System.currentTimeMillis()) + Math.abs(RANDOM.nextInt());
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
