package uk.gov.hmcts.reform.bulkscan.endtoendtests;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import uk.gov.hmcts.reform.bulkscan.endtoendtests.client.CcdClient;
import uk.gov.hmcts.reform.bulkscan.endtoendtests.helper.Await;
import uk.gov.hmcts.reform.bulkscan.endtoendtests.helper.Container;
import uk.gov.hmcts.reform.bulkscan.endtoendtests.helper.StorageHelper;
import uk.gov.hmcts.reform.bulkscan.endtoendtests.helper.ZipFileHelper;
import uk.gov.hmcts.reform.bulkscan.endtoendtests.model.Classification;

import static uk.gov.hmcts.reform.bulkscan.endtoendtests.model.Classification.EXCEPTION;
import static uk.gov.hmcts.reform.bulkscan.endtoendtests.model.Classification.NEW_APPLICATION;
import static uk.gov.hmcts.reform.bulkscan.endtoendtests.utils.ProcessorEnvelopeStatusChecker.retrieveCcdId;

class AllServicesTest {

    @ParameterizedTest
    @MethodSource("serviceWithClassification")
    void should_create_case_for_service(Container container, Classification classification) throws Exception {
        // given
        var zipArchive = ZipFileHelper.createZipArchive("test-data/" + classification, container);

        // when
        StorageHelper.uploadZipFile(container, zipArchive);

        // then
        Await.envelopeDispatched(zipArchive.fileName);
        Await.envelopeCompleted(zipArchive.fileName);

        final String ccdId = retrieveCcdId(zipArchive.fileName);
        CcdClient.rejectException(ccdId, container);
    }

    private static Object[][] serviceWithClassification() {
        return new Object[][] {
            new Object[] { Container.CMC, EXCEPTION },
            new Object[] { Container.DIVORCE, NEW_APPLICATION },
            new Object[] { Container.FINREM, NEW_APPLICATION },
            new Object[] { Container.PROBATE, NEW_APPLICATION },
            new Object[] { Container.PUBLICLAW, EXCEPTION },
            new Object[] { Container.SSCS, NEW_APPLICATION }
        };
    }
}
