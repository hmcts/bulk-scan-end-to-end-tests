package uk.gov.hmcts.reform.bulkscan.endtoendtests;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.bulkscan.endtoendtests.helper.StorageHelper;
import uk.gov.hmcts.reform.bulkscan.endtoendtests.helper.ZipFileHelper;
import uk.gov.hmcts.reform.bulkscan.endtoendtests.utils.ProcessorEnvelopeStatusChecker;
import uk.gov.hmcts.reform.bulkscan.endtoendtests.utils.RouterEnvelopesStatusChecker;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static com.jayway.awaitility.Awaitility.await;
import static java.util.Collections.singletonList;

public class ExceptionRecordTest {

    @Test
    public void should_upload_blob_and_create_exception_record() throws Exception {
        String zipFileName = ZipFileHelper.randomFileName();

        // create zip file
        var zipArchive = ZipFileHelper.createZipArchive(
            singletonList("1111002.pdf"),
            "exception_metadata.json",
            zipFileName
        );

        // upload zip file
        StorageHelper.uploadZipFile("bulkscan", zipFileName, zipArchive);

        await("File " + zipFileName + " should be dispatched")
            .atMost(60, TimeUnit.SECONDS)
            .pollInterval(500, TimeUnit.MILLISECONDS)
            .until(() -> Objects.equals(RouterEnvelopesStatusChecker.checkStatus(zipFileName), "DISPATCHED"));

        await("Exception record is created for " + zipFileName)
            .atMost(60, TimeUnit.SECONDS)
            .pollInterval(500, TimeUnit.MILLISECONDS)
            .until(() -> Objects.equals(ProcessorEnvelopeStatusChecker.checkStatus(zipFileName), "COMPLETED"));
    }
}
