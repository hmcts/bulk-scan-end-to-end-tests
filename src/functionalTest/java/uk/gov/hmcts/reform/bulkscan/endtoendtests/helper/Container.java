package uk.gov.hmcts.reform.bulkscan.endtoendtests.helper;

public enum Container {

    BULKSCAN("bulkscan"),
    CMC("cmc"),
    DIVORCE("divorce"),
    FINREM("finrem"),
    PROBATE("probate"),
    PUBLICLAW("publiclaw"),
    SSCS("sscs");

    public final String name;

    Container(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
