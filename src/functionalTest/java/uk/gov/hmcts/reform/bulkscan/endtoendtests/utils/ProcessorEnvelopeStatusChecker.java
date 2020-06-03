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

    public static boolean checkEnvelope(String fileName, String status, String ccdAction) {
        var responseBody = RestAssured
            .given()
            .relaxedHTTPSValidation()
            .baseUri(processorUrl)
            .queryParam("name", fileName)
            .get("/zip-files")
            .andReturn()
            .body();

        if (responseBody.jsonPath().getList("envelopes").isEmpty()) {
            return false;
        } else {
            return responseBody.jsonPath().getString("envelopes[0].status").equals(status)
                && responseBody.jsonPath().getString("envelopes[0].envelopeCcdAction").equals(status)
                && !responseBody.jsonPath().getString("envelopes[0].ccd_id").isBlank();
        }
    }

    private ProcessorEnvelopeStatusChecker() {
    }
}
