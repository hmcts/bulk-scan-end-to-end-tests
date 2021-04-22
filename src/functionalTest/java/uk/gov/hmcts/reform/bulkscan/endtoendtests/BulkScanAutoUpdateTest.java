package uk.gov.hmcts.reform.bulkscan.endtoendtests;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.bulkscan.endtoendtests.client.CcdClient;
import uk.gov.hmcts.reform.bulkscan.endtoendtests.helper.Await;
import uk.gov.hmcts.reform.bulkscan.endtoendtests.helper.StorageHelper;
import uk.gov.hmcts.reform.bulkscan.endtoendtests.helper.ZipFileHelper;
import uk.gov.hmcts.reform.bulkscan.endtoendtests.utils.EnvelopeAction;
import uk.gov.hmcts.reform.bulkscan.endtoendtests.utils.ProcessorEnvelopeResult;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.bulkscan.endtoendtests.client.CcdClient.assertCaseEnvelopes;
import static uk.gov.hmcts.reform.bulkscan.endtoendtests.helper.Container.BULKSCAN_AUTO;
import static uk.gov.hmcts.reform.bulkscan.endtoendtests.utils.ProcessorEnvelopeStatusChecker.getZipFileStatus;

public class BulkScanAutoUpdateTest {

    @Test
    public void should_create_and_update_case_automatically()
        throws Exception {

        var zipArchiveCreate = ZipFileHelper.createZipArchive("test-data/new_application", BULKSCAN_AUTO);

        StorageHelper.uploadZipFile(BULKSCAN_AUTO, zipArchiveCreate);

        Await.envelopeDispatched(zipArchiveCreate.fileName);
        Await.envelopeCompleted(zipArchiveCreate.fileName);

        ProcessorEnvelopeResult envCreate = getZipFileStatus(zipArchiveCreate.fileName).get();
        assertCompletedProcessorResult(envCreate, "AUTO_CREATED_CASE");

        String ccdId = envCreate.ccdId;
        Map<String, Object> caseDataCreated =
            CcdClient.getCaseData(ccdId, BULKSCAN_AUTO.idamUserName, BULKSCAN_AUTO.idamPassword);
        assertCaseFields(caseDataCreated, "Name", "Surname", "e2e@test.dev");
        assertCaseEnvelopes(
            caseDataCreated,
            new EnvelopeAction[]{
                new EnvelopeAction(envCreate.id, "create")
            }
        );

        var zipArchiveUpdate = ZipFileHelper.createZipArchive(
            "test-data/auto-update",
            BULKSCAN_AUTO,
            ccdId,
            "bulkscanautoupdate"
        );

        StorageHelper.uploadZipFile(BULKSCAN_AUTO, zipArchiveUpdate);

        Await.envelopeDispatched(zipArchiveUpdate.fileName);
        Await.envelopeCompleted(zipArchiveUpdate.fileName);

        ProcessorEnvelopeResult envUpdate = getZipFileStatus(zipArchiveUpdate.fileName).get();
        assertCompletedProcessorResult(envUpdate, "AUTO_UPDATED_CASE");

        Map<String, Object> caseDataUpdated =
            CcdClient.getCaseData(ccdId, BULKSCAN_AUTO.idamUserName, BULKSCAN_AUTO.idamPassword);
        assertCaseFields(caseDataUpdated, "Name1", "Surname1", "e2e1@test.dev");
        assertCaseEnvelopes(
            caseDataUpdated,
            new EnvelopeAction[]{
                new EnvelopeAction(envCreate.id, "create"),
                new EnvelopeAction(envUpdate.id, "update")
            }
        );
    }

    private void assertCompletedProcessorResult(
        ProcessorEnvelopeResult env,
        String ccdAction
    ) {
        assertThat(env.ccdId).isNotBlank();
        assertThat(env.container).isEqualTo(BULKSCAN_AUTO.name);
        assertThat(env.envelopeCcdAction).isEqualTo(ccdAction);
        assertThat(env.id).isNotBlank();
        assertThat(env.status).isEqualTo("COMPLETED");
    }

    private void assertCaseFields(
        Map<String, Object> caseDetails,
        String name,
        String surname,
        String email
    ) {
        assertThat(caseDetails.get("firstName")).isEqualTo(name);
        assertThat(caseDetails.get("lastName")).isEqualTo(surname);
        assertThat(caseDetails.get("email")).isEqualTo(email);
    }
}
