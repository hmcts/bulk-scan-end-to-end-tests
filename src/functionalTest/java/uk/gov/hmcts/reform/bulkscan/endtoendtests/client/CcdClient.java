package uk.gov.hmcts.reform.bulkscan.endtoendtests.client;

import io.restassured.RestAssured;
import io.restassured.specification.RequestSpecification;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.bulkscan.endtoendtests.helper.Container;
import uk.gov.hmcts.reform.bulkscan.endtoendtests.utils.ContainerJurisdictionPoBoxMapper;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.Event;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;

import java.util.Locale;
import java.util.Map;

import static uk.gov.hmcts.reform.bulkscan.endtoendtests.config.TestConfig.CCD_API_URL;

public class CcdClient {

    private static final String BEARER_TOKEN_PREFIX = "Bearer ";
    public static final String CASE_TYPE = "ExceptionRecord";
    private static final String EVENT_TYPE_ID = "rejectRecord";
    private static final String EVENT_SUMMARY = "Reject test an exception record";

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

    public void startRejectEventAndSubmit(
        String accessToken,
        String s2sToken,
        String userId,
        String ccdId,
        Container container
    ) {
        Map<String, Object> caseData = getCaseData(accessToken, s2sToken, ccdId);

        System.out.println("caseData  " + caseData);

        String caseTypeId = container.name.toUpperCase(Locale.getDefault()) + "_" + CASE_TYPE;
        var containerMapping = ContainerJurisdictionPoBoxMapper.getMappedContainerData(container);

        StartEventResponse startEventResponse =
            startEvent(accessToken, s2sToken, userId, containerMapping.jurisdiction, caseTypeId,
                EVENT_TYPE_ID);

        System.out.println("startEventResponse token  " + startEventResponse.getToken());

        CaseDataContent newCaseDataContent = CaseDataContent.builder()
            .eventToken(startEventResponse.getToken())
            .event(Event.builder()
                .id(EVENT_TYPE_ID)
                .summary(EVENT_SUMMARY)
                .build())
            .data(caseData)
            .build();

        submitEvent(
            accessToken,
            s2sToken,
            userId,
            containerMapping.jurisdiction,
            caseTypeId,
            newCaseDataContent
        );
    }

    private StartEventResponse startEvent(
        String accessToken,
        String s2sToken,
        String userId,
        String jurisdictionId,
        String caseType,
        String eventId
    ) {
        return getRequestSpecification(accessToken, s2sToken)
            .pathParam("userId", userId)
            .pathParam("jurisdictionId", jurisdictionId)
            .pathParam("caseType", caseType)
            .pathParam("eventId", eventId)
            .get(
                "/caseworkers/{userId}"
                    + "/jurisdictions/{jurisdictionId}"
                    + "/case-types/{caseType}"
                    + "/event-triggers/{eventId}/token"
            )
            .then()
            .assertThat()
            .statusCode(HttpStatus.OK.value())
            .extract()
            .as(StartEventResponse.class);
    }

    private CaseDetails submitEvent(
        String accessToken,
        String s2sToken,
        String userId,
        String jurisdictionId,
        String caseType,
        CaseDataContent caseDataContent
    ) {
        return getRequestSpecification(accessToken, s2sToken)
            .pathParam("userId", userId)
            .pathParam("jurisdictionId", jurisdictionId)
            .pathParam("caseType", caseType)
            .body(caseDataContent)
            .post(
                "/caseworkers/{userId}"
                    + "/jurisdictions/{jurisdictionId}"
                    + "/case-types/{caseType}"
                    + "/cases?ignoreWarning=true"
            )
            .then()
            .assertThat()
            .statusCode(HttpStatus.OK.value())
            .extract()
            .as(CaseDetails.class);
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
