package uk.gov.hmcts.reform.bulkscan.endtoendtests;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.bulkscan.endtoendtests.client.CcdClient;
import uk.gov.hmcts.reform.bulkscan.endtoendtests.helper.Await;
import uk.gov.hmcts.reform.bulkscan.endtoendtests.helper.StorageHelper;
import uk.gov.hmcts.reform.bulkscan.endtoendtests.helper.ZipFileHelper;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.bulkscan.endtoendtests.helper.Container.BULKSCAN_AUTO;
import static uk.gov.hmcts.reform.bulkscan.endtoendtests.utils.ProcessorEnvelopeStatusChecker.getZipFileStatus;

public class BulkScanAutoUpdateTest {

    @Test
    public void should_dispatch_blob_and_create_exception_record_for_classification()
        throws Exception {

        var zipArchiveCreate = ZipFileHelper.createZipArchive("test-data/new_application", BULKSCAN_AUTO);

        StorageHelper.uploadZipFile(BULKSCAN_AUTO, zipArchiveCreate);

        Await.envelopeDispatched(zipArchiveCreate.fileName);
        Await.envelopeCompleted(zipArchiveCreate.fileName);

        //get the process result again and assert
        assertCompletedProcessorResult(zipArchiveCreate.fileName);

        String ccdId = getZipFileStatus(zipArchiveCreate.fileName).get().ccdId;

        Map<String, Object> caseData =
            CcdClient.getCaseData(ccdId, BULKSCAN_AUTO.idamUserName, BULKSCAN_AUTO.idamPassword);
        assertThat(caseData.get("firstName")).isEqualTo("Name");
        assertThat(caseData.get("lastName")).isEqualTo("Surname");
        assertThat(caseData.get("email")).isEqualTo("e2e@test.dev");

        var zipArchiveUpdate = ZipFileHelper.createZipArchive(
            "test-data/auto-update",
            BULKSCAN_AUTO,
            ccdId,
            "bulkscanautoupdate"
        );

        StorageHelper.uploadZipFile(BULKSCAN_AUTO, zipArchiveUpdate);

        Await.envelopeDispatched(zipArchiveUpdate.fileName);
        Await.envelopeCompleted(zipArchiveUpdate.fileName);

        //get the process result again and assert
        assertCompletedProcessorResult(zipArchiveUpdate.fileName);

        Map<String, Object> caseDataUpdated =
            CcdClient.getCaseData(ccdId, BULKSCAN_AUTO.idamUserName, BULKSCAN_AUTO.idamPassword);
        assertThat(caseDataUpdated.get("firstName")).isEqualTo("Name1");
        assertThat(caseDataUpdated.get("lastName")).isEqualTo("Surname1");
        assertThat(caseDataUpdated.get("email")).isEqualTo("e2e1@test.dev");
    }

    private void assertCompletedProcessorResult(String zipFileName) {
        assertThat(getZipFileStatus(zipFileName)).hasValueSatisfying(env -> {
            assertThat(env.ccdId).isNotBlank();
            assertThat(env.container).isEqualTo(BULKSCAN_AUTO.name);
            assertThat(env.envelopeCcdAction).isEqualTo("CASE_CREATED");
            assertThat(env.id).isNotBlank();
            assertThat(env.status).isEqualTo("COMPLETED");
        });
    }
}
