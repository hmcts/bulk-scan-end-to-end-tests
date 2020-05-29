package uk.gov.hmcts.reform.bulkscan.endtoendtests;

import com.typesafe.config.ConfigFactory;
import io.restassured.RestAssured;
import org.apache.http.HttpHeaders;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class SasTokenTest {

    private static String blobRouterUrl = ConfigFactory.load().getString("storage-account-url");

    @Test
    public void testSasToken() {
        assertThat(blobRouterUrl).isNotEmpty();

        RestAssured
            .given()
            .relaxedHTTPSValidation()
            .baseUri(blobRouterUrl)
            .header(HttpHeaders.CONTENT_TYPE, "application/json")
            .get("/token/bulkscan")
            .then()
            .statusCode(200);
    }

}
