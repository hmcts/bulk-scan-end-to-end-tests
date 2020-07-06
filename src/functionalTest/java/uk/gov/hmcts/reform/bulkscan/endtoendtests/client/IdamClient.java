package uk.gov.hmcts.reform.bulkscan.endtoendtests.client;

import io.restassured.RestAssured;
import io.restassured.path.json.JsonPath;

import java.util.HashMap;
import java.util.Map;

import static org.apache.http.HttpHeaders.AUTHORIZATION;
import static org.apache.http.HttpHeaders.CONTENT_TYPE;
import static org.apache.http.HttpStatus.SC_OK;
import static org.apache.http.entity.ContentType.APPLICATION_FORM_URLENCODED;
import static uk.gov.hmcts.reform.bulkscan.endtoendtests.config.TestConfig.IDAM_API_URL;
import static uk.gov.hmcts.reform.bulkscan.endtoendtests.config.TestConfig.IDAM_CLIENT_REDIRECT_URI;
import static uk.gov.hmcts.reform.bulkscan.endtoendtests.config.TestConfig.IDAM_CLIENT_SECRET;


public class IdamClient {

    private static final String BEARER_PREFIX = "Bearer ";
    private static Map<String, String> idamTokenUserIdMap = new HashMap<>();
    private static Map<String, String> idamUserAccessTokenMap = new HashMap<>();


    private IdamClient() {
    }

    public static String getIdamToken(String idamUserName, String idamPassword) {
        return idamUserAccessTokenMap
            .computeIfAbsent(idamUserName, i -> IdamClient.retrieveIdamToken(idamUserName, idamPassword));
    }

    private static String retrieveIdamToken(String idamUserName, String idamPassword) {
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
            .formParam("username", idamUserName)
            .formParam("password", idamPassword)
            .post("/o/token")
            .then()
            .assertThat()
            .statusCode(SC_OK)
            .extract()
            .jsonPath();

        return idamResponse.getString("access_token");
    }

    public static String getUserId(String idamToken) {
        return idamTokenUserIdMap.computeIfAbsent(idamToken, IdamClient::retrieveUserId);
    }

    private static String retrieveUserId(String idamToken) {

        JsonPath idamResponse = RestAssured
            .given()
            .relaxedHTTPSValidation()
            .baseUri(IDAM_API_URL)
            .header(AUTHORIZATION, BEARER_PREFIX + idamToken)
            .get("/details")
            .then()
            .assertThat()
            .statusCode(SC_OK)
            .extract()
            .jsonPath();

        return idamResponse.getString("id");
    }
}
