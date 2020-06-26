package uk.gov.hmcts.reform.bulkscan.endtoendtests.utils;

import com.typesafe.config.ConfigFactory;
import io.restassured.RestAssured;
import org.apache.http.HttpHeaders;
import uk.gov.hmcts.reform.logging.appinsights.SyntheticHeaders;

public final class SasTokenRetriever {

    private static final String blobRouterUrl = ConfigFactory.load().getString("blob-router-url");

    /**
     * Retrieves SAS token for given service/jurisdiction.
     */
    public static String getTokenFor(String jurisdiction) {
        return RestAssured
            .given()
            .relaxedHTTPSValidation()
            .baseUri(blobRouterUrl)
            .header(HttpHeaders.CONTENT_TYPE, "application/json")
            .header(SyntheticHeaders.SYNTHETIC_TEST_SOURCE, "Bulk Scan E2E test")
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
