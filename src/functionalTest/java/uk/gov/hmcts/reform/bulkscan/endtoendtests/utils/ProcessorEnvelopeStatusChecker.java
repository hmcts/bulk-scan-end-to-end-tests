package uk.gov.hmcts.reform.bulkscan.endtoendtests.utils;

import com.typesafe.config.ConfigFactory;
import io.restassured.RestAssured;
import io.restassured.path.json.JsonPath;

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

    public static ProcessorEnvelopeResult getCompletedEnvelope(String fileName) {
        var responseBody = RestAssured
            .given()
            .relaxedHTTPSValidation()
            .baseUri(processorUrl)
            .queryParam("name", fileName)
            .get("/zip-files")
            .andReturn()
            .body();
        JsonPath jsonPath = responseBody.jsonPath();
        if (jsonPath.getList("envelopes").isEmpty()
            || !responseBody.jsonPath().getString("envelopes[0].status").equals("COMPLETED")) {
            return null;
        } else {
            return new ProcessorEnvelopeResult(
                jsonPath.getString("envelopes[0].id"),
                jsonPath.getString("envelopes[0].container"),
                jsonPath.getString("envelopes[0].status"),
                jsonPath.getString("envelopes[0].ccd_id"),
                jsonPath.getString("envelopes[0].envelope_ccd_action")
            );
        }
    }

    private ProcessorEnvelopeStatusChecker() {
    }
}
