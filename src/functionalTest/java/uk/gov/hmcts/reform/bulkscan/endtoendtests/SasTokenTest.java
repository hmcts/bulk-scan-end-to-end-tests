package uk.gov.hmcts.reform.bulkscan.endtoendtests;

import com.azure.storage.common.Utility;
import com.azure.storage.common.implementation.StorageImplUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.typesafe.config.ConfigFactory;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.apache.http.HttpHeaders;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.Map;

import static java.time.OffsetDateTime.now;
import static org.assertj.core.api.Assertions.assertThat;

public class SasTokenTest {

    private static String blobRouterUrl = ConfigFactory.load().getString("storage-account-url");

    @Test
    public void testSasToken() throws Exception {
        assertThat(blobRouterUrl).isNotEmpty();

        Response sasTokenResponse = RestAssured
            .given()
            .relaxedHTTPSValidation()
            .baseUri(blobRouterUrl)
            .header(HttpHeaders.CONTENT_TYPE, "application/json")
            .when().get("/token/bulkscan")
            .andReturn();

        verifySasTokenProperties(sasTokenResponse);
    }

    private void verifySasTokenProperties(Response tokenResponse) throws Exception {
        assertThat(tokenResponse.getStatusCode()).isEqualTo(200);

        final ObjectNode node = new ObjectMapper().readValue(tokenResponse.getBody().asString(), ObjectNode.class);
        final String sasToken = node.get("sas_token").asText();

        Map<String, String[]> queryParams = StorageImplUtils.parseQueryStringSplitValues(Utility.urlDecode(sasToken));

        assertThat(queryParams.get("sig")).isNotNull(); // this is a generated hash of the resource string
        assertThat(queryParams.get("sv")).contains("2019-07-07"); // azure api version is latest
        OffsetDateTime expiresAt = OffsetDateTime.parse(queryParams.get("se")[0]); // expiry datetime for the signature
        assertThat(expiresAt).isBetween(now(), now().plusSeconds(300));
        assertThat(queryParams.get("sp")).contains("cl"); // access permissions(create-c,list-l)
        assertThat(queryParams.get("spr")).containsExactlyInAnyOrder("https", "http");
    }
}
