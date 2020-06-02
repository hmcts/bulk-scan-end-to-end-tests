package uk.gov.hmcts.reform.bulkscan.endtoendtests.utils;

import com.typesafe.config.ConfigFactory;
import io.restassured.RestAssured;
import org.apache.http.HttpHeaders;

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
            .get("/token/" + jurisdiction)
            .then()
            .statusCode(200)
            .extract()
            .body()
            .toString();
    }

    private SasTokenRetriever() {
    }
}
