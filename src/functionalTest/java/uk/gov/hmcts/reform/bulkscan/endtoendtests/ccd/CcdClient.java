package uk.gov.hmcts.reform.bulkscan.endtoendtests.ccd;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

@Service
public class CcdClient {

    private static final Logger log = LoggerFactory.getLogger(CcdClient.class);

    private static final String AWAITING_DCN_PROCESSING_FIELD_NAME = "awaitingPaymentDCNProcessing";
    private static final String COMPLETE_AWAITING_DCN_PROCESSING_EVENT_ID = "completeAwaitingPaymentDCNProcessing";

    private final CoreCaseDataApi ccdApi;
    private final CcdAuthenticatorFactory authenticatorFactory;

    public CcdClient(
        CoreCaseDataApi ccdApi,
        CcdAuthenticatorFactory authenticatorFactory
    ) {
        this.ccdApi = ccdApi;
        this.authenticatorFactory = authenticatorFactory;
    }

    public CaseDetails getCase(
        String exceptionRecordCcdId,
        String jurisdiction
    ) {
        CcdAuthenticator authenticator = authenticatorFactory.createForJurisdiction(jurisdiction);

        return ccdApi.getCase(
            authenticator.getUserToken(),
            authenticator.getServiceToken(),
            exceptionRecordCcdId
        );
    }
}
