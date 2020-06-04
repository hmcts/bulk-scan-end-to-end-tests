package uk.gov.hmcts.reform.bulkscan.endtoendtests.utils;

public class ProcessorEnvelopeResult {

    public final String id;
    public final String container;
    public final String status;
    public final String ccdId;
    public final String envelopeCcdAction;

    public ProcessorEnvelopeResult(
        String id,
        String container,
        String status,
        String ccdId,
        String envelopeCcdAction
    ) {
        this.id = id;
        this.container = container;
        this.status = status;
        this.ccdId = ccdId;
        this.envelopeCcdAction = envelopeCcdAction;
    }
}
