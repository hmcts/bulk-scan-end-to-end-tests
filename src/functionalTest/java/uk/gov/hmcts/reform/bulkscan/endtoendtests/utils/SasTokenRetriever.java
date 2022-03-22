package uk.gov.hmcts.reform.bulkscan.endtoendtests.utils;

import io.restassured.RestAssured;
import org.apache.http.HttpHeaders;

import static uk.gov.hmcts.reform.bulkscan.endtoendtests.config.TestConfig.BLOB_ROUTER_URL;

public final class SasTokenRetriever {
    public static final String SYNTHETIC_TEST_SOURCE = "SyntheticTest-Source";

    /**
     * Retrieves SAS token for given service/jurisdiction.
     */
    public static String getTokenFor(String jurisdiction) {
        return RestAssured
            .given()
            .relaxedHTTPSValidation()
            .baseUri(BLOB_ROUTER_URL)
            .header(HttpHeaders.CONTENT_TYPE, "application/json")
            .header(SYNTHETIC_TEST_SOURCE, "Bulk Scan E2E test")
            .get("/token/" + jurisdiction)
            .then()
            .log().body() // debug
            .statusCode(200)
            .extract()
            .body()
            .jsonPath()
            .getString("sas_token");
    }

    private SasTokenRetriever() {
    }
}
