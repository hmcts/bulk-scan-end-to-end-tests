package uk.gov.hmcts.reform.bulkscan.endtoendtests;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.warrenstrange.googleauth.GoogleAuthenticator;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.bulkscan.endtoendtests.helper.Await;
import uk.gov.hmcts.reform.bulkscan.endtoendtests.helper.Container;
import uk.gov.hmcts.reform.bulkscan.endtoendtests.helper.StorageHelper;
import uk.gov.hmcts.reform.bulkscan.endtoendtests.helper.ZipFileHelper;
import uk.gov.hmcts.reform.bulkscan.endtoendtests.utils.ProcessorEnvelopeResult;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static java.lang.String.format;
import static org.apache.http.HttpHeaders.CONTENT_TYPE;
import static org.apache.http.HttpStatus.SC_OK;
import static org.apache.http.entity.ContentType.APPLICATION_FORM_URLENCODED;
import static org.apache.http.entity.ContentType.APPLICATION_JSON;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.bulkscan.endtoendtests.utils.ProcessorEnvelopeStatusChecker.getZipFileStatus;

public class PaymentsTest {


    private static final String REDIRECT_URI = "http://localhost/receiver";
    public static final String BEARER = "Bearer ";


    private Config conf = ConfigFactory.load();

    private ObjectMapper objectMapper = new ObjectMapper();

    @Test
    public void should_upload_blob_and_create_exception_record() throws Exception {

        var zipArchive = ZipFileHelper.createZipArchive("test-data/new-application-payments", Container.BULKSCAN);

        StorageHelper.uploadZipFile(Container.BULKSCAN, zipArchive);

        Await.envelopeDispatched(zipArchive.fileName);
        Await.envelopeCompleted(zipArchive.fileName);

        //get the process result again and assert
        String ccdId = assertCompletedProcessorResultAndRetrieveCcdId(zipArchive.fileName);

        String accessToken = getAccessToken();

        String s2sToken = getS2SToken();

        Map<?, ?> caseData = getCaseData(accessToken, s2sToken, ccdId);

        String awaitingPaymentDCNProcessing = (String)caseData.get("awaitingPaymentDCNProcessing");
        String containsPayments = (String)caseData.get("containsPayments");
        assertThat(containsPayments).isEqualTo("Yes");
        assertThat(awaitingPaymentDCNProcessing).isEqualTo("No");
    }

    private String getAccessToken() throws com.fasterxml.jackson.core.JsonProcessingException {
        String idamApiUrl = conf.getString("idam-api-url");
        String idamClientSecret = conf.getString("idam-client-secret");
        String username = conf.getString("idam-users-bulkscan-username");
        String password = conf.getString("idam-users-bulkscan-password");
        Response idamResponse = RestAssured
            .given()
            .relaxedHTTPSValidation()
            .baseUri(idamApiUrl)
            .header(CONTENT_TYPE, APPLICATION_FORM_URLENCODED.getMimeType())
            .formParam("grant_type", "password")
            .formParam("redirect_uri", REDIRECT_URI)
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

    private String getS2SToken() throws IOException {
        String s2sUrl = conf.getString("s2s-url");
        String s2sSecret = conf.getString("s2s-secret");
        final String oneTimePassword = format("%06d", new GoogleAuthenticator().getTotpPassword(s2sSecret));
        Map<String, String> signInDetails = new HashMap<>();
        signInDetails.put("microservice", "bulk_scan_orchestrator");
        signInDetails.put("oneTimePassword", oneTimePassword);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        objectMapper.writeValue(baos, signInDetails);
        String signInDetailsStr = baos.toString();

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

    private Map<?, ?> getCaseData(
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
            .header("Authorization", BEARER + accessToken)
            .header("ServiceAuthorization", BEARER + s2sToken)
            .get("/cases/" + ccdId);

        assertThat(caseResponse.getStatusCode()).isEqualTo(SC_OK);

        Map<?, ?> c = objectMapper.readValue(caseResponse.getBody().print(), Map.class);
        return (Map<?, ?>) c.get("data");
    }

    private String assertCompletedProcessorResultAndRetrieveCcdId(String zipFileName) {
        assertThat(getZipFileStatus(zipFileName)).hasValueSatisfying(env -> {
            assertThat(env.ccdId).isNotBlank();
            assertThat(env.container).isEqualTo(Container.BULKSCAN.name);
            assertThat(env.envelopeCcdAction).isEqualTo("EXCEPTION_RECORD");
            assertThat(env.id).isNotBlank();
            assertThat(env.status).isEqualTo("COMPLETED");
        });
        ProcessorEnvelopeResult processorEnvelopeResult = getZipFileStatus(zipFileName).get();
        return processorEnvelopeResult.ccdId;
    }
}
