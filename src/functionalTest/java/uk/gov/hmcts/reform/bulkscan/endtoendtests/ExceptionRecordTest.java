package uk.gov.hmcts.reform.bulkscan.endtoendtests;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.bulkscan.endtoendtests.helper.Await;
import uk.gov.hmcts.reform.bulkscan.endtoendtests.helper.Container;
import uk.gov.hmcts.reform.bulkscan.endtoendtests.helper.StorageHelper;
import uk.gov.hmcts.reform.bulkscan.endtoendtests.helper.ZipFileHelper;
import uk.gov.hmcts.reform.bulkscan.endtoendtests.utils.ProcessorEnvelopeResult;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.bulkscan.endtoendtests.utils.ProcessorEnvelopeStatusChecker.getZipFileStatus;

public class ExceptionRecordTest {

    @Test
    public void should_upload_blob_and_create_exception_record() throws Exception {
        var zipArchive = ZipFileHelper.createZipArchive(
            singletonList("test-data/exception/1111002.pdf"),
            "test-data/exception/metadata.json",
            Container.BULKSCAN
        );

        StorageHelper.uploadZipFile(Container.BULKSCAN, zipArchive);

        Await.envelopeDispatched(zipArchive.fileName);
        Await.envelopeCompleted(zipArchive.fileName);
    }

    @Test
    public void should_dispatch_blob_and_create_exception_record_for_supplementary_evidence_with_ocr_classification()
        throws Exception {

        var zipArchive = ZipFileHelper.createZipArchive(
            singletonList("test-data/supplementary_evidence_with_ocr/1111002.pdf"),
            "test-data/supplementary_evidence_with_ocr/metadata.json",
            Container.BULKSCAN
        );

        StorageHelper.uploadZipFile(Container.BULKSCAN, zipArchive);

        Await.envelopeDispatched(zipArchive.fileName);
        Await.envelopeCompleted(zipArchive.fileName);

        //get the process result again and assert
        assertCompletedProcessorResult(zipArchive.fileName);
    }

    @Test
    public void should_dispatch_blob_and_create_exception_record_for_supplementary_evidence()
        throws Exception {

        var zipArchive = ZipFileHelper.createZipArchive(
            singletonList("test-data/supplementary_evidence/1111002.pdf"),
            "test-data/supplementary_evidence/metadata.json",
            Container.BULKSCAN
        );

        StorageHelper.uploadZipFile(Container.BULKSCAN, zipArchive);

        Await.envelopeDispatched(zipArchive.fileName);
        Await.envelopeCompleted(zipArchive.fileName);

        //get the process result again and assert
        assertCompletedProcessorResult(zipArchive.fileName);
    }


    @Test
    public void should_dispatch_blob_and_create_exception_record_for_new_application_classification()
        throws Exception {

        var zipArchive = ZipFileHelper.createZipArchive(
            singletonList("test-data/new_application/1111002.pdf"),
            "test-data/new_application/metadata.json",
            Container.BULKSCAN
        );

        StorageHelper.uploadZipFile(Container.BULKSCAN, zipArchive);

        Await.envelopeDispatched(zipArchive.fileName);
        Await.envelopeCompleted(zipArchive.fileName);

        //get the process result again and assert
        assertCompletedProcessorResult(zipArchive.fileName);
    }

    private void assertCompletedProcessorResult(String zipFileName) {
        ProcessorEnvelopeResult processorEnvelopeResult = getZipFileStatus(zipFileName);
        assertThat(processorEnvelopeResult.ccdId).isNotBlank();
        assertThat(processorEnvelopeResult.container).isEqualTo(Container.BULKSCAN.name);
        assertThat(processorEnvelopeResult.envelopeCcdAction).isEqualTo("EXCEPTION_RECORD");
        assertThat(processorEnvelopeResult.id).isNotBlank();
        assertThat(processorEnvelopeResult.status).isEqualTo("COMPLETED");
    }
}
