package uk.gov.hmcts.reform.bulkscan.endtoendtests.utils;

public class EnvelopeAction {

    public final String envelopeId;
    public final String action;

    public EnvelopeAction(
        String envelopeId,
        String action
    ) {
        this.envelopeId = envelopeId;
        this.action = action;
    }
}
