package uk.gov.hmcts.reform.bulkscan.endtoendtests;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.bulkscan.endtoendtests.helper.Await;
import uk.gov.hmcts.reform.bulkscan.endtoendtests.helper.StorageHelper;
import uk.gov.hmcts.reform.bulkscan.endtoendtests.helper.ZipFileHelper;

import static java.util.Collections.singletonList;

public class NewApplicationPaymentsTest {

    private static final String FILE_NAME_PREFIX = "new_appl_paym_";

    @Test
    public void should_upload_blob_and_create_exception_record() throws Exception {

        var zipArchive = ZipFileHelper.createZipArchive(
            FILE_NAME_PREFIX,
            singletonList("test-data/new-application-payments/1111002.pdf"),
            "test-data/new-application-payments/metadata.json"
        );

        StorageHelper.uploadZipFile("bulkscan", zipArchive);

        Await.envelopeDispatched(zipArchive.fileName);
        Await.envelopeCompleted(zipArchive.fileName);
    }
}
