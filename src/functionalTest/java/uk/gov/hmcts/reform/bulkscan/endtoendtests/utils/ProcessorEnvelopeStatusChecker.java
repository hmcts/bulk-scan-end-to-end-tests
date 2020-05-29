package uk.gov.hmcts.reform.bulkscan.endtoendtests.utils;

import com.typesafe.config.ConfigFactory;
import io.restassured.RestAssured;

public final class ProcessorEnvelopeStatusChecker {

    private static final String processorUrl = ConfigFactory.load().getString("processor-url");

    /**
     * Checks the status of envelope with given file name in processor service.
     */
    public static String checkStatus(String fileName) {
        var responseBody = RestAssured
            .given()
            .relaxedHTTPSValidation()
            .baseUri(processorUrl)
            .queryParam("name", fileName)
            .get("/zip-files")
            .andReturn()
            .body();

        if (responseBody.jsonPath().getList("envelopes").isEmpty()) {
            return null;
        } else {
            return responseBody.jsonPath().getString("envelopes[0].status");
        }
    }

    private ProcessorEnvelopeStatusChecker() {
    }
}
