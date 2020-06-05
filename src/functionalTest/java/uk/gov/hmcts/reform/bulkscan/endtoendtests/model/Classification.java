package uk.gov.hmcts.reform.bulkscan.endtoendtests.model;

public enum Classification {

    EXCEPTION("exception"),
    NEW_APPLICATION("new_application"),
    SUPPLEMENTARY_EVIDENCE("supplementary_evidence"),
    SUPPLEMENTARY_EVIDENCE_WITH_OCR("supplementary_evidence_with_ocr");

    private final String value;

    /**
     * Validation in blob processor service uses lowercase.
     * Applying such feature through this value and {@link Classification#toString()} method here.
     * @param value The classification type
     */
    Classification(final String value) {
        this.value = value.toLowerCase();
    }

    @Override
    public String toString() {
        return value;
    }

}
