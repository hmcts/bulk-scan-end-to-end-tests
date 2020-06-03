package uk.gov.hmcts.reform.bulkscan.endtoendtests.helper;

import com.azure.core.http.ProxyOptions;
import com.azure.core.http.netty.NettyAsyncHttpClientBuilder;
import com.azure.storage.blob.BlobContainerClientBuilder;
import com.typesafe.config.ConfigFactory;
import uk.gov.hmcts.reform.bulkscan.endtoendtests.utils.SasTokenRetriever;

import java.io.ByteArrayInputStream;
import java.net.InetSocketAddress;

public final class StorageHelper {

    private static final String storageUrl = ConfigFactory.load().getString("storage-account-url");

    private static final String proxyHost = ConfigFactory.load().getString("proxy-host");

    private static final int proxyPort = ConfigFactory.load().getInt("proxy-port");

    private StorageHelper() {
        // utility class
    }

    public static void uploadZipFile(String container, String fileName, byte[] zipFileContent) {
        String sasToken = SasTokenRetriever.getTokenFor(container);

        new BlobContainerClientBuilder()
            .endpoint(storageUrl + "/" + container)
            .sasToken(sasToken)
            .httpClient(new NettyAsyncHttpClientBuilder()
                            .proxy(new ProxyOptions(
                                       ProxyOptions.Type.HTTP,
                                       new InetSocketAddress(proxyHost, proxyPort)
                                   )
                            )
                            .build())
            .buildClient()
            .getBlobClient(fileName)
            .upload(new ByteArrayInputStream(zipFileContent), zipFileContent.length);
    }
}
