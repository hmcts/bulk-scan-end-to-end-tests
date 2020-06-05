package uk.gov.hmcts.reform.bulkscan.endtoendtests.helper;

import com.google.common.io.Resources;
import org.apache.commons.io.FilenameUtils;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static com.google.common.io.Resources.getResource;
import static com.google.common.io.Resources.toByteArray;
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

    public static ZipArchive createZipArchive(
        List<String> pdfFiles,
        String metadataFile
    ) throws Exception {
        String zipFileName = String.format(
            "%s_%s.test.zip",
            ThreadLocalRandom.current().nextInt(0, Integer.MAX_VALUE),
            LocalDateTime.now().format(FILE_NAME_DATE_TIME_FORMAT)
        );

        String metadataContent = updateMetadataWithFileNameAndDcns(metadataFile, zipFileName);

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

    public static String updateMetadataWithFileNameAndDcns(
        String metadataFile, String zipFileName
    ) throws Exception {
        assertThat(metadataFile).isNotBlank();

        String metadataTemplate =
            Resources.toString(getResource(metadataFile), StandardCharsets.UTF_8);

        return metadataTemplate
            .replace("$$zip_file_name$$", zipFileName)
            .replace("$$dcn1$$", generateDcnNumber());
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
