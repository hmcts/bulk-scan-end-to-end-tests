package uk.gov.hmcts.reform.bulkscan.endtoendtests.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import io.restassured.RestAssured;
import io.restassured.response.Response;

import java.util.Map;

import static org.apache.http.HttpStatus.SC_OK;
import static org.assertj.core.api.Assertions.assertThat;

public class CcdClient {

    private static final String BEARER_TOKEN_PREFIX = "Bearer ";

    private Config conf = ConfigFactory.load();

    private ObjectMapper objectMapper = new ObjectMapper();

    public Map<?, ?> getCaseData(
        String accessToken,
        String s2sToken,
        String ccdId
    ) throws JsonProcessingException {
        String coreCaseDataApiUrl = conf.getString("core-case-data-api-url");
        Response caseResponse = RestAssured
            .given()
            .relaxedHTTPSValidation()
            .baseUri(coreCaseDataApiUrl)
            .header("experimental", true)
            .header("Authorization", BEARER_TOKEN_PREFIX + accessToken)
            .header("ServiceAuthorization", BEARER_TOKEN_PREFIX + s2sToken)
            .get("/cases/" + ccdId);

        assertThat(caseResponse.getStatusCode()).isEqualTo(SC_OK);

        Map<?, ?> c = objectMapper.readValue(caseResponse.getBody().print(), Map.class);
        return (Map<?, ?>) c.get("data");
    }
}
