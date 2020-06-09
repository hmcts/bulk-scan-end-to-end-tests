package uk.gov.hmcts.reform.bulkscan.endtoendtests;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.apache.http.HttpHeaders;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.bulkscan.endtoendtests.helper.Await;
import uk.gov.hmcts.reform.bulkscan.endtoendtests.helper.Container;
import uk.gov.hmcts.reform.bulkscan.endtoendtests.helper.StorageHelper;
import uk.gov.hmcts.reform.bulkscan.endtoendtests.helper.ZipFileHelper;
import uk.gov.hmcts.reform.bulkscan.endtoendtests.utils.ProcessorEnvelopeResult;

import java.util.Map;

import static org.apache.http.entity.ContentType.APPLICATION_FORM_URLENCODED;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.bulkscan.endtoendtests.utils.ProcessorEnvelopeStatusChecker.getZipFileStatus;

public class NewApplicationPaymentsTest {

    private Config conf = ConfigFactory.load();

    private static final String IDAM_API_URL = "https://idam-api.aat.platform.hmcts.net";
    private static final String OPEN_ID_TOKEN_PATH = "/o/token";
    private static final String REDIRECT_URI = "http://localhost/receiver";

    private static final String CORE_CASE_DATA_API_URL = "http://ccd-data-store-api-aat.service.core-compute-aat.internal";

    @Test
    public void should_upload_blob_and_create_exception_record() throws Exception {

        var zipArchive = ZipFileHelper.createZipArchive("test-data/new-application-payments", Container.BULKSCAN);

        StorageHelper.uploadZipFile(Container.BULKSCAN, zipArchive);

        Await.envelopeDispatched(zipArchive.fileName);
        Await.envelopeCompleted(zipArchive.fileName);

        //get the process result again and assert
        assertCompletedProcessorResult(zipArchive.fileName);

        String username = conf.getString("idam-users-bulkscan-username");
        String password = conf.getString("idam-users-bulkscan-password");
        String secret = conf.getString("idam-client-secret");

        Response response = RestAssured
            .given()
            .relaxedHTTPSValidation()
            .proxy("proxyout.reform.hmcts.net", 8080)
            .baseUri(IDAM_API_URL)
            .header(HttpHeaders.CONTENT_TYPE, APPLICATION_FORM_URLENCODED.getMimeType())
            .formParam("grant_type", "password")
            .formParam("redirect_uri", REDIRECT_URI)
            .formParam("client_id", "bsp")
            .formParam("client_secret", secret)
            .formParam("scope", "openid")
            .formParam("username", username)
            .formParam("password", password)
            .post(OPEN_ID_TOKEN_PATH);
        ObjectMapper m = new ObjectMapper();
        Map<?, ?> r = m.readValue(response.getBody().print(), Map.class);
        String accessToken = (String)r.get("access_token");
        String idToken = (String)r.get("id_token");
        String refreshToken = (String)r.get("refresh_token");

        String ccdId = retrieveCcdId(zipArchive.fileName);

        Response caseResponse = RestAssured
            .given()
            .relaxedHTTPSValidation()
            .proxy("proxyout.reform.hmcts.net", 8080)
            .baseUri(CORE_CASE_DATA_API_URL)
            .header("experimental", true)
            .header("Authorization", idToken)
            .header("ServiceAuthorization", accessToken)
            .get("/cases/" + ccdId);

        assertThat(caseResponse.getStatusCode()).isEqualTo(200);
    }

    private void assertCompletedProcessorResult(String zipFileName) {
        ProcessorEnvelopeResult processorEnvelopeResult = getZipFileStatus(zipFileName);
        assertThat(processorEnvelopeResult.ccdId).isNotBlank();
        assertThat(processorEnvelopeResult.container).isEqualTo(Container.BULKSCAN.name);
        assertThat(processorEnvelopeResult.envelopeCcdAction).isEqualTo("EXCEPTION_RECORD");
        assertThat(processorEnvelopeResult.id).isNotBlank();
        assertThat(processorEnvelopeResult.status).isEqualTo("COMPLETED");
    }

    private String retrieveCcdId(String zipFileName) {
        ProcessorEnvelopeResult processorEnvelopeResult = getZipFileStatus(zipFileName);
        return processorEnvelopeResult.ccdId;
    }
}
