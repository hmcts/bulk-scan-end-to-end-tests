package uk.gov.hmcts.reform.bulkscan.endtoendtests.utils;

import io.restassured.RestAssured;
import io.restassured.path.json.JsonPath;
import io.restassured.response.ResponseBody;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.bulkscan.endtoendtests.config.TestConfig.PROCESSOR_URL;

public final class ProcessorEnvelopeStatusChecker {

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

    public static String retrieveCcdId(String zipFileName) {
        Optional<ProcessorEnvelopeResult> processorEnvelopeResult = getZipFileStatus(zipFileName);

        assertThat(processorEnvelopeResult.isPresent()).isTrue();

        return processorEnvelopeResult.get().ccdId;
    }

    private static ResponseBody getZipFileStatusResponse(String fileName) {
        return RestAssured
            .given()
            .relaxedHTTPSValidation()
            .baseUri(PROCESSOR_URL)
            .queryParam("name", fileName)
            .get("/zip-files")
            .andReturn()
            .body();
    }
}
