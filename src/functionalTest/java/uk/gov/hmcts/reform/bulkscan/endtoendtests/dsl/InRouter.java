package uk.gov.hmcts.reform.bulkscan.endtoendtests.dsl;

import java.util.Objects;

import static com.jayway.awaitility.Awaitility.await;
import static java.util.concurrent.TimeUnit.SECONDS;
import static uk.gov.hmcts.reform.bulkscan.endtoendtests.utils.RouterEnvelopesStatusChecker.checkStatus;

public final class InRouter {

    public static void awaitDispatching(String fileName) {
        await("File " + fileName + " should be dispatched")
            .atMost(60, SECONDS)
            .pollInterval(1, SECONDS)
            .until(() -> Objects.equals(checkStatus(fileName), "DISPATCHED"));
    }
}
