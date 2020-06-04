package uk.gov.hmcts.reform.bulkscan.endtoendtests;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.bulkscan.endtoendtests.helper.Await;
import uk.gov.hmcts.reform.bulkscan.endtoendtests.helper.StorageHelper;
import uk.gov.hmcts.reform.bulkscan.endtoendtests.helper.ZipFileHelper;
import uk.gov.hmcts.reform.bulkscan.endtoendtests.utils.ProcessorEnvelopeResult;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.bulkscan.endtoendtests.utils.ProcessorEnvelopeStatusChecker.getZipFileStatus;

public class ExceptionRecordTest {

    @Test
    public void should_upload_blob_and_create_exception_record() throws Exception {
        String zipFileName = ZipFileHelper.randomFileName();

        var zipArchive = ZipFileHelper.createZipArchive(
            singletonList("test-data/exception/1111002.pdf"),
            "test-data/exception/exception_metadata.json",
            zipFileName
        );

        StorageHelper.uploadZipFile("bulkscan", zipFileName, zipArchive);

        Await.envelopeDispatched(zipFileName);
        Await.envelopeCompleted(zipFileName);
    }

    @Test
    public void should_dispatch_blob_and_create_exception_record_for_supplementary_evidence_with_ocr_classification()
        throws Exception {
        String zipFileName = ZipFileHelper.randomFileName();

        var zipArchive = ZipFileHelper.createZipArchive(
            singletonList("test-data/exception/1111002.pdf"),
            "test-data/exception/supplementary_evidence_with_ocr_metadata.json",
            zipFileName
        );

        StorageHelper.uploadZipFile("bulkscan", zipFileName, zipArchive);

        Await.envelopeDispatched(zipFileName);
        Await.envelopeCompleted(zipFileName);

        //get the process result again to assert
        ProcessorEnvelopeResult processorEnvelopeResult = getZipFileStatus(zipFileName);
        assertThat(processorEnvelopeResult.ccdId).isNotBlank();
        assertThat(processorEnvelopeResult.container).isEqualTo("bulkscan");
        assertThat(processorEnvelopeResult.envelopeCcdAction).isEqualTo("EXCEPTION_RECORD");
        assertThat(processorEnvelopeResult.id).isNotBlank();
        assertThat(processorEnvelopeResult.status).isEqualTo("COMPLETED");
    }
}
