package uk.gov.hmcts.reform.bulkscan.endtoendtests.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import io.restassured.RestAssured;
import io.restassured.response.Response;

import java.util.Map;

import static org.apache.http.HttpHeaders.CONTENT_TYPE;
import static org.apache.http.HttpStatus.SC_OK;
import static org.apache.http.entity.ContentType.APPLICATION_FORM_URLENCODED;
import static org.assertj.core.api.Assertions.assertThat;

public class IdamClient {

    private Config conf = ConfigFactory.load();

    private ObjectMapper objectMapper = new ObjectMapper();

    public String getIdamToken() throws JsonProcessingException {
        String idamApiUrl = conf.getString("idam-api-url");
        String idamClientRedirectUri = conf.getString("idam-client-redirect-uri");
        String idamClientSecret = conf.getString("idam-client-secret");
        String username = conf.getString("idam-users-bulkscan-username");
        String password = conf.getString("idam-users-bulkscan-password");
        Response idamResponse = RestAssured
            .given()
            .relaxedHTTPSValidation()
            .baseUri(idamApiUrl)
            .header(CONTENT_TYPE, APPLICATION_FORM_URLENCODED.getMimeType())
            .formParam("grant_type", "password")
            .formParam("redirect_uri", idamClientRedirectUri)
            .formParam("client_id", "bsp")
            .formParam("client_secret", idamClientSecret)
            .formParam("scope", "openid profile roles")
            .formParam("username", username)
            .formParam("password", password)
            .post("/o/token");

        assertThat(idamResponse.getStatusCode()).isEqualTo(SC_OK);

        Map<?, ?> r = objectMapper.readValue(idamResponse.getBody().print(), Map.class);
        return (String)r.get("access_token");
    }
}
