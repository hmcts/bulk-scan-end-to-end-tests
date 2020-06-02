package uk.gov.hmcts.reform.bulkscan.endtoendtests.helper;

import com.google.common.io.Resources;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

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

    public static byte[] createZipArchive(
        List<String> pdfFiles,
        String metadataFile,
        String zipFileName
    ) throws Exception {
        String metadataContent = updateMetadataWithFileNameAndDcns(metadataFile, zipFileName);

        byte[] zipContents = createZipArchiveWithDocumentsAndMetadata(pdfFiles, metadataContent);

        var outputStream = new ByteArrayOutputStream();
        try (var zos = new ZipOutputStream(outputStream)) {
            zos.putNextEntry(new ZipEntry(ENVELOPE_ZIPFILE_NAME));
            zos.write(zipContents);
            zos.closeEntry();

            // add signature
            zos.putNextEntry(new ZipEntry(SIGNATURE_FILE_NAME));
            zos.write(Resources.toByteArray(Resources.getResource(SIGNATURE_FILE_NAME)));
            zos.closeEntry();
        }
        return outputStream.toByteArray();
    }

    public static String randomFileName() {
        return String.format(
            "%s_%s.test.zip",
            ThreadLocalRandom.current().nextInt(0, Integer.MAX_VALUE),
            LocalDateTime.now().format(FILE_NAME_DATE_TIME_FORMAT)
        );
    }

    private static byte[] createZipArchiveWithDocumentsAndMetadata(
        List<String> pdfFiles, String metadataContent
    ) throws Exception {
        var outputStream = new ByteArrayOutputStream();
        try (var zos = new ZipOutputStream(outputStream)) {
            for (String pdf : pdfFiles) {
                zos.putNextEntry(new ZipEntry(pdf));
                zos.write(Resources.toByteArray(Resources.getResource(pdf)));
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
            Resources.toString(Resources.getResource(metadataFile), StandardCharsets.UTF_8);

        return metadataTemplate
            .replace("$$zip_file_name$$", zipFileName)
            .replace("$$dcn1$$", generateDcnNumber());
    }

    private static String generateDcnNumber() {
        return Long.toString(System.currentTimeMillis()) + Math.abs(RANDOM.nextInt());
    }
}
