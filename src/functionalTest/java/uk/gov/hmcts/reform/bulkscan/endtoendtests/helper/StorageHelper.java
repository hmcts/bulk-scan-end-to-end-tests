package uk.gov.hmcts.reform.bulkscan.endtoendtests.helper;

import com.azure.core.http.ProxyOptions;
import com.azure.core.http.netty.NettyAsyncHttpClientBuilder;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.util.Context;
import com.azure.storage.blob.BlobContainerClientBuilder;
import com.azure.storage.blob.models.BlobHttpHeaders;
import com.azure.storage.blob.models.BlobRequestConditions;
import com.azure.storage.common.implementation.Constants;
import com.typesafe.config.ConfigFactory;
import uk.gov.hmcts.reform.bulkscan.endtoendtests.helper.ZipFileHelper.ZipArchive;
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

    public static void uploadZipFile(Container container, ZipArchive zipArchive) {
        String sasToken = SasTokenRetriever.getTokenFor(container.name);
        BlobRequestConditions blobRequestConditions = new BlobRequestConditions();
        blobRequestConditions.setIfNoneMatch(Constants.HeaderConstants.ETAG_WILDCARD);

        BlobHttpHeaders headers = new BlobHttpHeaders();

        new BlobContainerClientBuilder()
            .endpoint(storageUrl + "/" + container.name)
            .sasToken(sasToken)
            .httpClient(new NettyAsyncHttpClientBuilder()
                            .proxy(new ProxyOptions(
                                       ProxyOptions.Type.HTTP,
                                       new InetSocketAddress(proxyHost, proxyPort)
                                   )
                            )
                            .build())
            .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS))
            .buildClient()
            .getBlobClient(zipArchive.fileName)
            .uploadWithResponse(
                new ByteArrayInputStream(zipArchive.content),
                zipArchive.content.length,
                null,
                headers.setContentType("application/zip"),
                null,
                null,
                blobRequestConditions,
                null,
                Context.NONE
            );

    }
}
