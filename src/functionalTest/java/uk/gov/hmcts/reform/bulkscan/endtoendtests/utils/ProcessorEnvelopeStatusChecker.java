package uk.gov.hmcts.reform.bulkscan.endtoendtests.utils;

import com.typesafe.config.ConfigFactory;
import io.restassured.RestAssured;
import io.restassured.path.json.JsonPath;
import io.restassured.response.ResponseBody;

import java.util.Optional;

public final class ProcessorEnvelopeStatusChecker {

    private static final String processorUrl = ConfigFactory.load().getString("processor-url");

    private ProcessorEnvelopeStatusChecker() {
    }

    public static Optional<ProcessorEnvelopeResult> getZipFileStatus(String fileName) {
        JsonPath jsonPath = getZipFileStatusResponse(fileName).jsonPath();
        if (jsonPath.getList("envelopes").isEmpty()) {
            return Optional.empty();
        } else {
            return Optional.of(new ProcessorEnvelopeResult(
                jsonPath.getString("envelopes[0].id"),
                jsonPath.getString("envelopes[0].container"),
                jsonPath.getString("envelopes[0].status"),
                jsonPath.getString("envelopes[0].ccd_id"),
                jsonPath.getString("envelopes[0].envelope_ccd_action")
            ));
        }
    }

    private static ResponseBody getZipFileStatusResponse(String fileName) {
        return RestAssured
            .given()
            .relaxedHTTPSValidation()
            .baseUri(processorUrl)
            .queryParam("name", fileName)
            .get("/zip-files")
            .andReturn()
            .body();
    }
}
