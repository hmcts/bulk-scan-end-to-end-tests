package uk.gov.hmcts.reform.bulkscan.endtoendtests.helper;

import com.azure.storage.blob.BlobContainerClientBuilder;
import com.typesafe.config.ConfigFactory;
import uk.gov.hmcts.reform.bulkscan.endtoendtests.utils.SasTokenRetriever;

import java.io.ByteArrayInputStream;

public final class StorageHelper {

    private static final String storageUrl = ConfigFactory.load().getString("storage-account-url");

    private StorageHelper() {
        // utility class
    }

    public static void uploadZipFile(String container, String fileName, byte[] zipFileContent) {
        String sasToken = SasTokenRetriever.getTokenFor(container);

        new BlobContainerClientBuilder()
            .endpoint(storageUrl + "/" + container)
            .sasToken(sasToken)
            .buildClient()
            .getBlobClient(fileName)
            .upload(new ByteArrayInputStream(zipFileContent), zipFileContent.length);
    }
}
