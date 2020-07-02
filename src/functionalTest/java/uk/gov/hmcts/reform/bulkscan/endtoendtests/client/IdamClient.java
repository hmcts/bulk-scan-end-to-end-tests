package uk.gov.hmcts.reform.bulkscan.endtoendtests.client;

import io.restassured.RestAssured;
import io.restassured.path.json.JsonPath;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;

import static org.apache.http.HttpHeaders.CONTENT_TYPE;
import static org.apache.http.entity.ContentType.APPLICATION_FORM_URLENCODED;
import static uk.gov.hmcts.reform.bulkscan.endtoendtests.config.TestConfig.IDAM_API_URL;
import static uk.gov.hmcts.reform.bulkscan.endtoendtests.config.TestConfig.IDAM_CLIENT_REDIRECT_URI;
import static uk.gov.hmcts.reform.bulkscan.endtoendtests.config.TestConfig.IDAM_CLIENT_SECRET;
import static uk.gov.hmcts.reform.bulkscan.endtoendtests.config.TestConfig.IDAM_PASSWORD;
import static uk.gov.hmcts.reform.bulkscan.endtoendtests.config.TestConfig.IDAM_USER_NAME;

public class IdamClient {

    private static final String BEARER_PREFIX = "Bearer ";

    public String getIdamToken() {
        JsonPath idamResponse = RestAssured
            .given()
            .relaxedHTTPSValidation()
            .baseUri(IDAM_API_URL)
            .header(CONTENT_TYPE, APPLICATION_FORM_URLENCODED.getMimeType())
            .formParam("grant_type", "password")
            .formParam("redirect_uri", IDAM_CLIENT_REDIRECT_URI)
            .formParam("client_id", "bsp")
            .formParam("client_secret", IDAM_CLIENT_SECRET)
            .formParam("scope", "openid profile roles")
            .formParam("username", IDAM_USER_NAME)
            .formParam("password", IDAM_PASSWORD)
            .post("/o/token")
            .then()
            .assertThat()
            .statusCode(HttpStatus.OK.value())
            .extract()
            .jsonPath();;


        return idamResponse.getString("access_token");
    }

    public String getUserId(String idamToken) {
        JsonPath idamResponse = RestAssured
            .given()
            .relaxedHTTPSValidation()
            .baseUri(IDAM_API_URL)
            .header(HttpHeaders.AUTHORIZATION, BEARER_PREFIX + idamToken)
            .get("/details")
            .then()
            .assertThat()
            .statusCode(HttpStatus.OK.value())
            .extract()
            .jsonPath();

        return idamResponse.getString("id");
    }
}
