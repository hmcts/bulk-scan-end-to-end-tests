package uk.gov.hmcts.reform.bulkscan.endtoendtests.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
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

public class S2SClient {

    private Config conf = ConfigFactory.load();

    private ObjectMapper objectMapper = new ObjectMapper();

    public String getS2SToken() throws IOException {
        String s2sSecret = conf.getString("s2s-secret");
        final String oneTimePassword = format("%06d", new GoogleAuthenticator().getTotpPassword(s2sSecret));
        Map<String, String> signInDetails = new HashMap<>();
        signInDetails.put("microservice", "bulk_scan_orchestrator");
        signInDetails.put("oneTimePassword", oneTimePassword);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        objectMapper.writeValue(baos, signInDetails);
        String signInDetailsStr = baos.toString();

        String s2sUrl = conf.getString("s2s-url");
        Response s2sResponse = RestAssured
            .given()
            .relaxedHTTPSValidation()
            .baseUri(s2sUrl)
            .header(CONTENT_TYPE, APPLICATION_JSON.getMimeType())
            .body(signInDetailsStr)
            .post("/lease");

        assertThat(s2sResponse.getStatusCode()).isEqualTo(SC_OK);

        return s2sResponse.getBody().print();
    }
}
