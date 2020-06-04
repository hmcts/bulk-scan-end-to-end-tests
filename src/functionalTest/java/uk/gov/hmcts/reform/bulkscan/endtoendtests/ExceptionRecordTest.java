package uk.gov.hmcts.reform.bulkscan.endtoendtests;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.bulkscan.endtoendtests.dsl.Await;
import uk.gov.hmcts.reform.bulkscan.endtoendtests.helper.StorageHelper;
import uk.gov.hmcts.reform.bulkscan.endtoendtests.helper.ZipFileHelper;

import static java.util.Collections.singletonList;

public class ExceptionRecordTest {

    @Test
    public void should_upload_blob_and_create_exception_record() throws Exception {
        String zipFileName = ZipFileHelper.randomFileName();

        // create zip file
        var zipArchive = ZipFileHelper.createZipArchive(
            singletonList("test-data/exception/1111002.pdf"),
            "test-data/exception/exception_metadata.json",
            zipFileName
        );

        // upload zip file
        StorageHelper.uploadZipFile("bulkscan", zipFileName, zipArchive);

        Await.envelopeDispatched(zipFileName);
        Await.envelopeCompleted(zipFileName);
    }
}
