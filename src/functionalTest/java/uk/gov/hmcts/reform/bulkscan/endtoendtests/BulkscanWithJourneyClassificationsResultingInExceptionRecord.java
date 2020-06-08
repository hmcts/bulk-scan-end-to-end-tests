package uk.gov.hmcts.reform.bulkscan.endtoendtests;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import uk.gov.hmcts.reform.bulkscan.endtoendtests.helper.Await;
import uk.gov.hmcts.reform.bulkscan.endtoendtests.helper.Container;
import uk.gov.hmcts.reform.bulkscan.endtoendtests.helper.StorageHelper;
import uk.gov.hmcts.reform.bulkscan.endtoendtests.helper.ZipFileHelper;
import uk.gov.hmcts.reform.bulkscan.endtoendtests.model.Classification;
import uk.gov.hmcts.reform.bulkscan.endtoendtests.utils.ProcessorEnvelopeResult;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.bulkscan.endtoendtests.utils.ProcessorEnvelopeStatusChecker.getZipFileStatus;

public class BulkscanWithJourneyClassificationsResultingInExceptionRecord {

    @ParameterizedTest
    @EnumSource(Classification.class)
    public void should_dispatch_blob_and_create_exception_record_for_classification(Classification classification)
        throws Exception {

        var zipArchive = ZipFileHelper.createZipArchive("test-data/" + classification, Container.BULKSCAN);

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
