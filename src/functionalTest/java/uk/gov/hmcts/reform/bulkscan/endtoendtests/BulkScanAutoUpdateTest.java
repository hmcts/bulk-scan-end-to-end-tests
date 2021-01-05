package uk.gov.hmcts.reform.bulkscan.endtoendtests;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.bulkscan.endtoendtests.client.CcdClient;
import uk.gov.hmcts.reform.bulkscan.endtoendtests.helper.Await;
import uk.gov.hmcts.reform.bulkscan.endtoendtests.helper.StorageHelper;
import uk.gov.hmcts.reform.bulkscan.endtoendtests.helper.ZipFileHelper;

import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toMap;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.bulkscan.endtoendtests.helper.Container.BULKSCAN_AUTO;
import static uk.gov.hmcts.reform.bulkscan.endtoendtests.utils.ProcessorEnvelopeStatusChecker.getZipFileStatus;

public class BulkScanAutoUpdateTest {

    @Test
    public void should_dispatch_blob_and_create_exception_record_for_classification()
        throws Exception {

        var zipArchive = ZipFileHelper.createZipArchive("test-data/new_application", BULKSCAN_AUTO);

        StorageHelper.uploadZipFile(BULKSCAN_AUTO, zipArchive);

        Await.envelopeDispatched(zipArchive.fileName);
        Await.envelopeCompleted(zipArchive.fileName);

        //get the process result again and assert
        assertCompletedProcessorResult(zipArchive.fileName);

        String ccdId = getZipFileStatus(zipArchive.fileName).get().ccdId;

        Map<String, Object> caseData =
            CcdClient.getCaseData(ccdId, BULKSCAN_AUTO.idamUserName, BULKSCAN_AUTO.idamPassword);
        Map<String, String> ocrData = getOcrData(caseData);
        assertThat(ocrData.get("firstName")).isEqualTo("Name");
        assertThat(ocrData.get("lastName")).isEqualTo("Surname");
        assertThat(ocrData.get("email")).isEqualTo("e2e@test.dev");
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

    @SuppressWarnings("unchecked")
    private Map<String, String> getOcrData(Map<String, Object> caseData) {
        List<Map<String, Object>> ccdOcrData =
            (List<Map<String, Object>>) caseData.get("scanOCRData");

        return ccdOcrData
            .stream()
            .map(items -> items.get("value"))
            .filter(item -> item instanceof Map)
            .map(item -> (Map<String, String>) item)
            .collect(
                toMap(
                    map -> map.get("key"),
                    map -> map.get("value")
                )
            );
    }
}
