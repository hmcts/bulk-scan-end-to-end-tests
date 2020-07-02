package uk.gov.hmcts.reform.bulkscan.endtoendtests.helper;

import com.azure.core.http.ProxyOptions;
import com.azure.core.http.netty.NettyAsyncHttpClientBuilder;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.storage.blob.BlobContainerClientBuilder;
import uk.gov.hmcts.reform.bulkscan.endtoendtests.helper.ZipFileHelper.ZipArchive;
import uk.gov.hmcts.reform.bulkscan.endtoendtests.utils.SasTokenRetriever;

import java.io.ByteArrayInputStream;
import java.net.InetSocketAddress;

import static uk.gov.hmcts.reform.bulkscan.endtoendtests.config.TestConfig.PROXY_HOST;
import static uk.gov.hmcts.reform.bulkscan.endtoendtests.config.TestConfig.PROXY_PORT;
import static uk.gov.hmcts.reform.bulkscan.endtoendtests.config.TestConfig.STORAGE_URL;

public final class StorageHelper {

    private StorageHelper() {
        // utility class
    }

    public static void uploadZipFile(Container container, ZipArchive zipArchive) {
        String sasToken = SasTokenRetriever.getTokenFor(container.name);

        new BlobContainerClientBuilder()
            .endpoint(STORAGE_URL + "/" + container.name)
            .sasToken(sasToken)
            .httpClient(new NettyAsyncHttpClientBuilder()
                            .proxy(new ProxyOptions(
                                       ProxyOptions.Type.HTTP,
                                       new InetSocketAddress(PROXY_HOST, PROXY_PORT)
                                   )
                            )
                            .build())
            .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS))
            .buildClient()
            .getBlobClient(zipArchive.fileName)
            .getBlockBlobClient()
            .upload(new ByteArrayInputStream(zipArchive.content), zipArchive.content.length);
    }
}
