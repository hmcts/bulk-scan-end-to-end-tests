package uk.gov.hmcts.reform.bulkscan.endtoendtests.utils;

import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobContainerClientBuilder;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import java.io.ByteArrayInputStream;

public final class StorageHelper {

    private static Config config = ConfigFactory.load();

    private static String storageUrl = config.getString("storage-account-url");

    private StorageHelper() {
        // utility class
    }

    public static void uploadZipFle(String container, String fileName, byte[] zipFileContent) {
        String sasToken = ""; // TODO: use sas token helper

        BlobContainerClient blobContainerClient =
            new BlobContainerClientBuilder()
                .endpoint(storageUrl + "/" + container)
                .sasToken(sasToken)
                .buildClient();

        blobContainerClient
            .getBlobClient(fileName)
            .upload(new ByteArrayInputStream(zipFileContent), zipFileContent.length);

    }
}
