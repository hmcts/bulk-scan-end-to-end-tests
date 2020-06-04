package uk.gov.hmcts.reform.bulkscan.endtoendtests.helper;

import uk.gov.hmcts.reform.bulkscan.endtoendtests.utils.ProcessorEnvelopeResult;
import uk.gov.hmcts.reform.bulkscan.endtoendtests.utils.RouterEnvelopesStatusChecker;

import java.util.Objects;

import static com.jayway.awaitility.Awaitility.await;
import static java.util.concurrent.TimeUnit.SECONDS;
import static uk.gov.hmcts.reform.bulkscan.endtoendtests.utils.ProcessorEnvelopeStatusChecker.getCompletedEnvelope;

public final class Await {

    public static void envelopeDispatched(String zipFileName) {
        await("File " + zipFileName + " should be dispatched from router")
            .atMost(60, SECONDS)
            .pollInterval(1, SECONDS)
            .until(() -> Objects.equals(RouterEnvelopesStatusChecker.checkStatus(zipFileName), "DISPATCHED"));
    }

    public static ProcessorEnvelopeResult envelopeCompleted(String zipFileName) {
        //Variable used in lambda expression should be final or effectively final,
        //Used array to get value from lambda
        final ProcessorEnvelopeResult[] processorEnvelopeResult = new ProcessorEnvelopeResult[1];

        await("File " + zipFileName + " should be completed in processor")
            .atMost(60, SECONDS)
            .pollInterval(1, SECONDS)
            .until(() -> (processorEnvelopeResult[0] = getCompletedEnvelope(zipFileName)) != null);

        return processorEnvelopeResult[0];
    }

    private Await() {
        // util class
    }
}
