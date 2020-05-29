package uk.gov.hmcts.reform.bulkscan.endtoendtests.utils;

import com.typesafe.config.ConfigFactory;
import io.restassured.RestAssured;

public final class RouterEnvelopesStatusChecker {

    private static final String blobRouterUrl = ConfigFactory.load().getString("blob-router-url");

    /**
     * Checks the status of envelope with given file name in blob-router.
     */
    public static String checkStatus(String fileName) {
        var responseBody = RestAssured
            .given()
            .relaxedHTTPSValidation()
            .baseUri(blobRouterUrl)
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
