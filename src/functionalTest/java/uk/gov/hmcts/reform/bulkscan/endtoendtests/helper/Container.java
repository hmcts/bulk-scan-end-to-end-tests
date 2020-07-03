package uk.gov.hmcts.reform.bulkscan.endtoendtests.helper;

import static uk.gov.hmcts.reform.bulkscan.endtoendtests.config.TestConfig.BULKSCAN_IDAM_PASSWORD;
import static uk.gov.hmcts.reform.bulkscan.endtoendtests.config.TestConfig.BULKSCAN_IDAM_USER_NAME;
import static uk.gov.hmcts.reform.bulkscan.endtoendtests.config.TestConfig.CMC_IDAM_PASSWORD;
import static uk.gov.hmcts.reform.bulkscan.endtoendtests.config.TestConfig.CMC_IDAM_USER_NAME;
import static uk.gov.hmcts.reform.bulkscan.endtoendtests.config.TestConfig.DIVORCE_IDAM_PASSWORD;
import static uk.gov.hmcts.reform.bulkscan.endtoendtests.config.TestConfig.DIVORCE_IDAM_USER_NAME;
import static uk.gov.hmcts.reform.bulkscan.endtoendtests.config.TestConfig.PROBATE_IDAM_PASSWORD;
import static uk.gov.hmcts.reform.bulkscan.endtoendtests.config.TestConfig.PROBATE_IDAM_USER_NAME;
import static uk.gov.hmcts.reform.bulkscan.endtoendtests.config.TestConfig.PUBLICLAW_IDAM_PASSWORD;
import static uk.gov.hmcts.reform.bulkscan.endtoendtests.config.TestConfig.PUBLICLAW_IDAM_USER_NAME;
import static uk.gov.hmcts.reform.bulkscan.endtoendtests.config.TestConfig.SSCS_IDAM_PASSWORD;
import static uk.gov.hmcts.reform.bulkscan.endtoendtests.config.TestConfig.SSCS_IDAM_USER_NAME;

public enum Container {

    BULKSCAN("bulkscan", BULKSCAN_IDAM_USER_NAME, BULKSCAN_IDAM_PASSWORD),
    CMC("cmc", CMC_IDAM_USER_NAME, CMC_IDAM_PASSWORD),
    DIVORCE("divorce", DIVORCE_IDAM_USER_NAME, DIVORCE_IDAM_PASSWORD),
    FINREM("finrem", DIVORCE_IDAM_USER_NAME, DIVORCE_IDAM_PASSWORD),
    PROBATE("probate", PROBATE_IDAM_USER_NAME, PROBATE_IDAM_PASSWORD),
    PUBLICLAW("publiclaw", PUBLICLAW_IDAM_USER_NAME, PUBLICLAW_IDAM_PASSWORD),
    SSCS("sscs", SSCS_IDAM_USER_NAME, SSCS_IDAM_PASSWORD);

    public final String name;
    public final String idamUserName;
    public final String idamPassword;

    Container(String name, String idamUserName, String idamPassword) {
        this.name = name;
        this.idamUserName = idamUserName;
        this.idamPassword = idamPassword;
    }

    @Override
    public String toString() {
        return name;
    }
}
