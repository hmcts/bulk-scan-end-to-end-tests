package uk.gov.hmcts.reform.bulkscan.endtoendtests;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.bulkscan.endtoendtests.helper.Await;
import uk.gov.hmcts.reform.bulkscan.endtoendtests.helper.StorageHelper;
import uk.gov.hmcts.reform.bulkscan.endtoendtests.helper.ZipFileHelper;
import uk.gov.hmcts.reform.bulkscan.endtoendtests.utils.ProcessorEnvelopeResult;
import uk.gov.hmcts.reform.bulkscan.endtoendtests.utils.ProcessorEnvelopeStatusChecker;
import uk.gov.hmcts.reform.bulkscan.endtoendtests.utils.RouterEnvelopesStatusChecker;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.bulkscan.endtoendtests.utils.ProcessorEnvelopeStatusChecker.getCompletedEnvelope;

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
            singletonList("1111002.pdf"),
            "supplementary_evidence_with_ocr_metadata.json",
            zipFileName
        );

        await("File " + zipFileName + " should be dispatched")
            .atMost(60, TimeUnit.SECONDS)
            .pollInterval(500, TimeUnit.MILLISECONDS)
            .until(() -> Objects.equals(RouterEnvelopesStatusChecker.checkStatus(zipFileName), "DISPATCHED"));

        ProcessorEnvelopeResult[] processorEnvelopeResult = new ProcessorEnvelopeResult[1];
        await("Exception record is created for " + zipFileName)
            .atMost(60, TimeUnit.SECONDS)
            .pollInterval(500, TimeUnit.MILLISECONDS)
            .until(() -> (processorEnvelopeResult[0] = getCompletedEnvelope(zipFileName)) != null);

        assertThat(processorEnvelopeResult[0].ccdId).isNotBlank();
        assertThat(processorEnvelopeResult[0].container).isEqualTo("bulkscan");
        assertThat(processorEnvelopeResult[0].envelopeCcdAction).isEqualTo("EXCEPTION_RECORD");
        assertThat(processorEnvelopeResult[0].id).isNotBlank();
        assertThat(processorEnvelopeResult[0].status).isEqualTo("COMPLETED");
    }
}
