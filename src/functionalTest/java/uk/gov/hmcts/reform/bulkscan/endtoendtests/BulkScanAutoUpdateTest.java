package uk.gov.hmcts.reform.bulkscan.endtoendtests;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.bulkscan.endtoendtests.client.CcdClient;
import uk.gov.hmcts.reform.bulkscan.endtoendtests.helper.Await;
import uk.gov.hmcts.reform.bulkscan.endtoendtests.helper.StorageHelper;
import uk.gov.hmcts.reform.bulkscan.endtoendtests.helper.ZipFileHelper;

import java.io.IOException;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
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

        assertCompletedProcessorResult(zipArchiveCreate.fileName, "CASE_CREATED");

        String ccdId = getZipFileStatus(zipArchiveCreate.fileName).get().ccdId;
        assertCaseFields(ccdId, "Name", "Surname", "e2e@test.dev");

        var zipArchiveUpdate = ZipFileHelper.createZipArchive(
            "test-data/auto-update",
            BULKSCAN_AUTO,
            ccdId,
            "bulkscanautoupdate"
        );

        StorageHelper.uploadZipFile(BULKSCAN_AUTO, zipArchiveUpdate);

        Await.envelopeDispatched(zipArchiveUpdate.fileName);
        Await.envelopeCompleted(zipArchiveUpdate.fileName);

        assertCompletedProcessorResult(zipArchiveUpdate.fileName, "AUTO_UPDATED_CASE");

        assertCaseFields(ccdId, "Name1", "Surname1", "e2e1@test.dev");
    }

    private void assertCompletedProcessorResult(String zipFileName, String ccdAction) {
        assertThat(getZipFileStatus(zipFileName)).hasValueSatisfying(env -> {
            assertThat(env.ccdId).isNotBlank();
            assertThat(env.container).isEqualTo(BULKSCAN_AUTO.name);
            assertThat(env.envelopeCcdAction).isEqualTo(ccdAction);
            assertThat(env.id).isNotBlank();
            assertThat(env.status).isEqualTo("COMPLETED");
        });
    }

    private void assertCaseFields(
        String ccdId,
        String name,
        String surname,
        String email
    ) throws IOException {
        Map<String, Object> caseDataUpdated =
            CcdClient.getCaseData(ccdId, BULKSCAN_AUTO.idamUserName, BULKSCAN_AUTO.idamPassword);
        assertThat(caseDataUpdated.get("firstName")).isEqualTo(name);
        assertThat(caseDataUpdated.get("lastName")).isEqualTo(surname);
        assertThat(caseDataUpdated.get("email")).isEqualTo(email);
    }
}
