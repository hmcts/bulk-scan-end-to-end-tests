package uk.gov.hmcts.reform.bulkscan.endtoendtests;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.bulkscan.endtoendtests.helper.Await;
import uk.gov.hmcts.reform.bulkscan.endtoendtests.helper.StorageHelper;
import uk.gov.hmcts.reform.bulkscan.endtoendtests.helper.ZipFileHelper;

import static java.util.Collections.singletonList;

public class ExceptionRecordTest {

    @Test
    public void should_upload_blob_and_create_exception_record() throws Exception {
        var zipArchive = ZipFileHelper.createZipArchive(
            singletonList("test-data/exception/1111002.pdf"),
            "test-data/exception/exception_metadata.json"
        );

        StorageHelper.uploadZipFile("bulkscan", zipArchive);

        Await.envelopeDispatched(zipArchive.fileName);
        Await.envelopeCompleted(zipArchive.fileName);
    }
}
