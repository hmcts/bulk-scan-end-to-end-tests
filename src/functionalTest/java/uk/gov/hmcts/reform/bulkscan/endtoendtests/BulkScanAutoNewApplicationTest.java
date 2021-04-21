package uk.gov.hmcts.reform.bulkscan.endtoendtests;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.bulkscan.endtoendtests.helper.Await;
import uk.gov.hmcts.reform.bulkscan.endtoendtests.helper.Container;
import uk.gov.hmcts.reform.bulkscan.endtoendtests.helper.StorageHelper;
import uk.gov.hmcts.reform.bulkscan.endtoendtests.helper.ZipFileHelper;
import uk.gov.hmcts.reform.bulkscan.endtoendtests.model.Classification;
import uk.gov.hmcts.reform.bulkscan.endtoendtests.utils.ProcessorEnvelopeResult;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.bulkscan.endtoendtests.helper.Container.BULKSCAN_AUTO;
import static uk.gov.hmcts.reform.bulkscan.endtoendtests.utils.ProcessorEnvelopeStatusChecker.getZipFileStatus;

public class BulkScanAutoNewApplicationTest {

    @Test
    public void should_create_case_automatically()
        throws Exception {

        var zipArchive = ZipFileHelper.createZipArchive("test-data/new_application", BULKSCAN_AUTO);

        StorageHelper.uploadZipFile(BULKSCAN_AUTO, zipArchive);

        Await.envelopeDispatched(zipArchive.fileName);
        Await.envelopeCompleted(zipArchive.fileName);

        //get the process result again and assert
        assertCompletedAutoCaseResult(getZipFileStatus(zipArchive.fileName));
    }

    @Test
    public void should_create_case_automatically_and_attach_supplementary_evidence()
        throws Exception {

        var zipArchiveCase = ZipFileHelper.createZipArchive("test-data/new_application", BULKSCAN_AUTO);

        StorageHelper.uploadZipFile(BULKSCAN_AUTO, zipArchiveCase);

        Await.envelopeDispatched(zipArchiveCase.fileName);
        Await.envelopeCompleted(zipArchiveCase.fileName);
        // case created automatically
        //get the process result again and assert
        Optional<ProcessorEnvelopeResult> optEnvelopeResult
            = getZipFileStatus(zipArchiveCase.fileName);
        assertCompletedAutoCaseResult(optEnvelopeResult);

        //attach supplementary evidence

        var zipArchiveSupp = ZipFileHelper.createZipArchive(
            "test-data/" + Classification.SUPPLEMENTARY_EVIDENCE, Container.BULKSCAN);

        StorageHelper.uploadZipFile(Container.BULKSCAN, zipArchiveSupp);

        Await.envelopeDispatched(zipArchiveSupp.fileName);
        Await.envelopeCompleted(zipArchiveSupp.fileName);

        //get the process result again and assert
        assertCompletedSupplementaryResult(
            zipArchiveSupp.fileName,
            optEnvelopeResult.get().ccdId
        );

    }

    private void assertCompletedSupplementaryResult(String zipFileName, String ccdId) {
        assertThat(getZipFileStatus(zipFileName)).hasValueSatisfying(env -> {
            assertThat(env.ccdId).isEqualTo(ccdId);
            assertThat(env.container).isEqualTo(Container.BULKSCAN.name);
            assertThat(env.envelopeCcdAction).isEqualTo("AUTO_ATTACHED_TO_CASE");
            assertThat(env.id).isNotBlank();
            assertThat(env.status).isEqualTo("COMPLETED");
        });
    }

    private void assertCompletedAutoCaseResult(Optional<ProcessorEnvelopeResult> optEnv) {
        assertThat(optEnv).hasValueSatisfying(env -> {
            assertThat(env.ccdId).isNotBlank();
            assertThat(env.container).isEqualTo(BULKSCAN_AUTO.name);
            assertThat(env.envelopeCcdAction).isEqualTo("AUTO_CREATED_CASE");
            assertThat(env.id).isNotBlank();
            assertThat(env.status).isEqualTo("COMPLETED");
        });
    }
}
