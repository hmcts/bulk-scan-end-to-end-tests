package uk.gov.hmcts.reform.bulkscan.endtoendtests;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.bulkscan.endtoendtests.client.CcdClient;
import uk.gov.hmcts.reform.bulkscan.endtoendtests.client.IdamClient;
import uk.gov.hmcts.reform.bulkscan.endtoendtests.client.S2SClient;
import uk.gov.hmcts.reform.bulkscan.endtoendtests.helper.Await;
import uk.gov.hmcts.reform.bulkscan.endtoendtests.helper.Container;
import uk.gov.hmcts.reform.bulkscan.endtoendtests.helper.StorageHelper;
import uk.gov.hmcts.reform.bulkscan.endtoendtests.helper.ZipFileHelper;
import uk.gov.hmcts.reform.bulkscan.endtoendtests.utils.ProcessorEnvelopeResult;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.bulkscan.endtoendtests.utils.ProcessorEnvelopeStatusChecker.getZipFileStatus;

public class PaymentsTest {

    private IdamClient idamClient = new IdamClient();

    private S2SClient s2SClient = new S2SClient();

    private CcdClient ccdClient = new CcdClient();

    @Test
    public void should_create_exception_record_with_payments_when_envelope_contains_payments() throws Exception {

        var zipArchive = ZipFileHelper.createZipArchive("test-data/new-application-payments", Container.BULKSCAN);

        StorageHelper.uploadZipFile(Container.BULKSCAN, zipArchive);

        Await.envelopeDispatched(zipArchive.fileName);
        Await.envelopeCompleted(zipArchive.fileName);

        //get the process result again and assert
        assertCompletedProcessorResult(zipArchive.fileName);

        final String ccdId = retrieveCcdId(zipArchive.fileName);

        final String idamToken = idamClient.getIdamToken();
        final String s2sToken = s2SClient.getS2SToken();

        Await.paymentsProcessed(ccdClient, idamToken, s2sToken, ccdId);
    }

    private void assertCompletedProcessorResult(String zipFileName) {
        assertThat(getZipFileStatus(zipFileName)).hasValueSatisfying(env -> {
            assertThat(env.ccdId).isNotBlank();
            assertThat(env.container).isEqualTo(Container.BULKSCAN.name);
            assertThat(env.envelopeCcdAction).isEqualTo("EXCEPTION_RECORD");
            assertThat(env.id).isNotBlank();
            assertThat(env.status).isEqualTo("COMPLETED");
        });
    }

    private String retrieveCcdId(String zipFileName) {
        Optional<ProcessorEnvelopeResult> processorEnvelopeResult = getZipFileStatus(zipFileName);

        assertThat(processorEnvelopeResult.isPresent()).isTrue();

        return processorEnvelopeResult.get().ccdId;
    }
}
