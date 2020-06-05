package uk.gov.hmcts.reform.bulkscan.endtoendtests.helper;

public enum Container {

    BULKSCAN("bulkscan");

    public final String name;

    Container(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
