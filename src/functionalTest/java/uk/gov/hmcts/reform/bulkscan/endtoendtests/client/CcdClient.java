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
    private static final String REJECT_EVENT_TYPE_ID = "rejectRecord";
    private static final String REJECT_EVENT_SUMMARY = "Reject the test exception record";

    public Map<String, Object> getCaseData(
        String idamToken,
        String s2sToken,
        String ccdId
    ) {
        CaseDetails caseResponse = getRequestSpecification(idamToken, s2sToken)
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
        String idamToken,
        String s2sToken,
        String userId,
        String caseId,
        Container container
    ) {

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

        System.out.println("startEventResponse token  " + startEventResponse.getToken());

        Map<String, Object> caseData = startEventResponse.getCaseDetails().getData();
        System.out.println("caseData  " + caseData);

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
            newCaseDataContent
        );
    }

    private StartEventResponse startEvent(
        String idamToken,
        String s2sToken,
        String userId,
        String jurisdictionId,
        String caseType,
        String caseId,
        String eventId
    ) {
        return getRequestSpecification(idamToken, s2sToken)
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
            .statusCode(HttpStatus.OK.value())
            .extract()
            .as(StartEventResponse.class);
    }

    private CaseDetails submitEvent(
        String idamToken,
        String s2sToken,
        String userId,
        String jurisdictionId,
        String caseType,
        CaseDataContent caseDataContent
    ) {
        return getRequestSpecification(idamToken, s2sToken)
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

    private RequestSpecification getRequestSpecification(String idamToken, String s2sToken) {
        return RestAssured
            .given()
            .relaxedHTTPSValidation()
            .baseUri(CCD_API_URL)
            .header("experimental", true)
            .header("Authorization", BEARER_TOKEN_PREFIX + idamToken)
            .header("ServiceAuthorization", BEARER_TOKEN_PREFIX + s2sToken);
    }
}
