package uk.gov.hmcts.reform.bulkscan.endtoendtests.helper;

import uk.gov.hmcts.reform.bulkscan.endtoendtests.utils.RouterEnvelopesStatusChecker;

import java.util.Objects;

import static com.jayway.awaitility.Awaitility.await;
import static java.util.concurrent.TimeUnit.SECONDS;
import static uk.gov.hmcts.reform.bulkscan.endtoendtests.utils.ProcessorEnvelopeStatusChecker.getZipFileStatus;

public final class Await {

    public static void envelopeDispatched(String zipFileName) {
        await("File " + zipFileName + " should be dispatched from router")
            .atMost(60, SECONDS)
            .pollInterval(1, SECONDS)
            .until(() -> Objects.equals(RouterEnvelopesStatusChecker.checkStatus(zipFileName), "DISPATCHED"));
    }

    public static void envelopeCompleted(String zipFileName) {
        await("File " + zipFileName + " should be completed in processor")
            .atMost(100, SECONDS)
            .pollInterval(1, SECONDS)
            .until(() -> getZipFileStatus(zipFileName).filter(s -> s.status.equals("COMPLETED")).isPresent());
    }

    private Await() {
        // util class
    }
}
