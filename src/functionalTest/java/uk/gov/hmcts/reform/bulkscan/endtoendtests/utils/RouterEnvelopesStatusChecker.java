package uk.gov.hmcts.reform.bulkscan.endtoendtests.utils;

import io.restassured.RestAssured;

import static uk.gov.hmcts.reform.bulkscan.endtoendtests.config.TestConfig.BLOB_ROUTER_URL;

public final class RouterEnvelopesStatusChecker {

    /**
     * Checks the status of envelope with given file name in blob-router.
     */
    public static String checkStatus(String fileName) {
        var responseBody = RestAssured
            .given()
            .relaxedHTTPSValidation()
            .baseUri(BLOB_ROUTER_URL)
            .queryParam("file_name", fileName)
            .get("/envelopes")
            .andReturn()
            .body();

        if (responseBody.jsonPath().getList("data").isEmpty()) {
            return null;
        } else {
            return responseBody.jsonPath().getString("data[0].status");
        }
    }

    private RouterEnvelopesStatusChecker() {
    }
}
