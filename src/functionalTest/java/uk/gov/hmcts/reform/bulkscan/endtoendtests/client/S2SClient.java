package uk.gov.hmcts.reform.bulkscan.endtoendtests.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.warrenstrange.googleauth.GoogleAuthenticator;
import io.restassured.RestAssured;
import io.restassured.response.Response;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static java.lang.String.format;
import static org.apache.http.HttpHeaders.CONTENT_TYPE;
import static org.apache.http.HttpStatus.SC_OK;
import static org.apache.http.entity.ContentType.APPLICATION_JSON;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.bulkscan.endtoendtests.config.TestConfig.S2S_SECRET;
import static uk.gov.hmcts.reform.bulkscan.endtoendtests.config.TestConfig.S2S_URL;

public class S2SClient {

    private static ObjectMapper objectMapper = new ObjectMapper();
    private static String s2sToken = null;

    private S2SClient() {
    }

    public static String getS2SToken() throws IOException {
        if (s2sToken == null) {
            s2sToken = retrieveS2SToken();
        }
        return s2sToken;
    }

    private static String retrieveS2SToken() throws IOException {
        final String oneTimePassword = format("%06d", new GoogleAuthenticator().getTotpPassword(S2S_SECRET));
        Map<String, String> signInDetails = new HashMap<>();
        signInDetails.put("microservice", "bulk_scan_orchestrator");
        signInDetails.put("oneTimePassword", oneTimePassword);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        objectMapper.writeValue(baos, signInDetails);
        String signInDetailsStr = baos.toString();

        Response s2sResponse = RestAssured
            .given()
            .relaxedHTTPSValidation()
            .baseUri(S2S_URL)
            .header(CONTENT_TYPE, APPLICATION_JSON.getMimeType())
            .body(signInDetailsStr)
            .post("/lease");

        assertThat(s2sResponse.getStatusCode()).isEqualTo(SC_OK);

        return s2sResponse.getBody().print();
    }
}
