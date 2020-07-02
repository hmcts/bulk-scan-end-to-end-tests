package uk.gov.hmcts.reform.bulkscan.endtoendtests.client;

import io.restassured.RestAssured;
import io.restassured.specification.RequestSpecification;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.util.Map;

import static uk.gov.hmcts.reform.bulkscan.endtoendtests.config.TestConfig.CCD_API_URL;

public class CcdClient {

    private static final String BEARER_TOKEN_PREFIX = "Bearer ";

    public Map<String, Object> getCaseData(
        String accessToken,
        String s2sToken,
        String ccdId
    ) {
        CaseDetails caseResponse = getRequestSpecification(accessToken, s2sToken)
            .pathParam("ccdId", ccdId)
            .get("/cases/{ccdId}")
            .then()
            .assertThat()
            .statusCode(HttpStatus.OK.value())
            .extract()
            .as(CaseDetails.class);

        return caseResponse.getData();
    }

    private RequestSpecification getRequestSpecification(String accessToken, String s2sToken) {
        return RestAssured
            .given()
            .relaxedHTTPSValidation()
            .baseUri(CCD_API_URL)
            .header("experimental", true)
            .header("Authorization", BEARER_TOKEN_PREFIX + accessToken)
            .header("ServiceAuthorization", BEARER_TOKEN_PREFIX + s2sToken);
    }
}
