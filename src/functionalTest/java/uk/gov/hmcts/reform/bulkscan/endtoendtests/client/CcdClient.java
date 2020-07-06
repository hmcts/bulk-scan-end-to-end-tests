package uk.gov.hmcts.reform.bulkscan.endtoendtests.client;

import io.restassured.RestAssured;
import io.restassured.specification.RequestSpecification;
import uk.gov.hmcts.reform.bulkscan.endtoendtests.helper.Container;
import uk.gov.hmcts.reform.bulkscan.endtoendtests.utils.ContainerJurisdictionPoBoxMapper;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.Event;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;

import java.io.IOException;
import java.util.Locale;
import java.util.Map;

import static org.apache.http.HttpHeaders.CONTENT_TYPE;
import static org.apache.http.HttpStatus.SC_OK;
import static org.apache.http.entity.ContentType.APPLICATION_JSON;
import static uk.gov.hmcts.reform.bulkscan.endtoendtests.client.IdamClient.getIdamToken;
import static uk.gov.hmcts.reform.bulkscan.endtoendtests.client.IdamClient.getUserId;
import static uk.gov.hmcts.reform.bulkscan.endtoendtests.client.S2SClient.getS2SToken;
import static uk.gov.hmcts.reform.bulkscan.endtoendtests.config.TestConfig.CCD_API_URL;

public class CcdClient {

    private static final String BEARER_TOKEN_PREFIX = "Bearer ";
    public static final String CASE_TYPE = "ExceptionRecord";
    private static final String REJECT_EVENT_TYPE_ID = "rejectRecord";
    private static final String REJECT_EVENT_SUMMARY = "Reject the test exception record";

    private CcdClient() {
    }

    public static Map<String, Object> getCaseData(
        String ccdId,
        String idamUserName,
        String idamPassword
    ) throws IOException {
        CaseDetails caseResponse = getRequestSpecification(getIdamToken(idamUserName, idamPassword), getS2SToken())
            .pathParam("ccdId", ccdId)
            .get("/cases/{ccdId}")
            .then()
            .assertThat()
            .statusCode(SC_OK)
            .extract()
            .as(CaseDetails.class);

        return caseResponse.getData();
    }

    public static void rejectException(
        String caseId,
        Container container
    ) throws IOException {

        String idamToken = getIdamToken(container.idamUserName, container.idamPassword);
        String s2sToken = getS2SToken();
        String userId = getUserId(idamToken);

        String caseTypeId = container.name.toUpperCase(Locale.getDefault()) + "_" + CASE_TYPE;
        var containerMapping = ContainerJurisdictionPoBoxMapper.getMappedContainerData(container);

        StartEventResponse startEventResponse =
            startEvent(
                idamToken,
                s2sToken,
                userId,
                containerMapping.jurisdiction,
                caseTypeId,
                caseId,
                REJECT_EVENT_TYPE_ID
            );


        Map<String, Object> caseData = startEventResponse.getCaseDetails().getData();

        CaseDataContent newCaseDataContent = CaseDataContent.builder()
            .eventToken(startEventResponse.getToken())
            .event(Event.builder()
                .id(REJECT_EVENT_TYPE_ID)
                .summary(REJECT_EVENT_SUMMARY)
                .build())
            .data(caseData)
            .build();

        submitEvent(
            idamToken,
            s2sToken,
            userId,
            containerMapping.jurisdiction,
            caseTypeId,
            caseId,
            newCaseDataContent
        );
    }

    private static StartEventResponse startEvent(
        String idamToken,
        String s2sToken,
        String userId,
        String jurisdictionId,
        String caseType,
        String caseId,
        String eventId
    ) {
        return getRequestSpecification(idamToken, s2sToken)
            .header(CONTENT_TYPE, APPLICATION_JSON.getMimeType())
            .pathParam("userId", userId)
            .pathParam("jurisdictionId", jurisdictionId)
            .pathParam("caseType", caseType)
            .pathParam("caseId", caseId)
            .pathParam("eventId", eventId)
            .get(
                "/caseworkers/{userId}"
                    + "/jurisdictions/{jurisdictionId}"
                    + "/case-types/{caseType}"
                    + "/cases/{caseId}"
                    + "/event-triggers/{eventId}/token"
            )
            .then()
            .assertThat()
            .statusCode(SC_OK)
            .extract()
            .as(StartEventResponse.class);
    }

    private static CaseDetails submitEvent(
        String idamToken,
        String s2sToken,
        String userId,
        String jurisdictionId,
        String caseType,
        String caseId,
        CaseDataContent caseDataContent
    ) {
        return getRequestSpecification(idamToken, s2sToken)
            .header(CONTENT_TYPE, APPLICATION_JSON.getMimeType())
            .pathParam("userId", userId)
            .pathParam("jurisdictionId", jurisdictionId)
            .pathParam("caseType", caseType)
            .pathParam("caseId", caseId)
            .body(caseDataContent)
            .post(
                "/caseworkers/{userId}"
                    + "/jurisdictions/{jurisdictionId}"
                    + "/case-types/{caseType}"
                    + "/cases/{caseId}"
                    + "/events?ignoreWarning=true"
            )
            .then()
            .assertThat()
            .statusCode(SC_OK)
            .extract()
            .as(CaseDetails.class);
    }

    private static RequestSpecification getRequestSpecification(String idamToken, String s2sToken) {
        return RestAssured
            .given()
            .relaxedHTTPSValidation()
            .baseUri(CCD_API_URL)
            .header("experimental", true)
            .header("Authorization", BEARER_TOKEN_PREFIX + idamToken)
            .header("ServiceAuthorization", BEARER_TOKEN_PREFIX + s2sToken);
    }
}
