package uk.gov.hmcts.reform.bulkscan.endtoendtests.config;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

public class TestConfig {

    private static Config conf = ConfigFactory.load();

    public static final String IDAM_API_URL = conf.getString("idam-api-url");
    public static final String IDAM_CLIENT_REDIRECT_URI = conf.getString("idam-client-redirect-uri");
    public static final String IDAM_CLIENT_SECRET = conf.getString("idam-client-secret");
    public static final String BULKSCAN_IDAM_USER_NAME = conf.getString("idam-users-bulkscan-username");
    public static final String BULKSCAN_IDAM_PASSWORD = conf.getString("idam-users-bulkscan-password");

    public static final String CMC_IDAM_USER_NAME = conf.getString("idam-users-cmc-username");
    public static final String CMC_IDAM_PASSWORD = conf.getString("idam-users-cmc-password");

    public static final String DIVORCE_IDAM_USER_NAME = conf.getString("idam-users-div-username");
    public static final String DIVORCE_IDAM_PASSWORD = conf.getString("idam-users-div-password");

    public static final String PROBATE_IDAM_USER_NAME = conf.getString("idam-users-probate-username");
    public static final String PROBATE_IDAM_PASSWORD = conf.getString("idam-users-probate-password");

    public static final String SSCS_IDAM_USER_NAME = conf.getString("idam-users-sscs-username");
    public static final String SSCS_IDAM_PASSWORD = conf.getString("idam-users-sscs-password");

    public static final String PUBLICLAW_IDAM_USER_NAME = conf.getString("idam-users-publiclaw-username");
    public static final String PUBLICLAW_IDAM_PASSWORD = conf.getString("idam-users-publiclaw-password");

    public static final String S2S_URL = conf.getString("s2s-url");
    public static final String S2S_SECRET = conf.getString("s2s-secret");

    public static final String CCD_API_URL = conf.getString("core-case-data-api-url");

    public static final String STORAGE_URL = conf.getString("storage-account-url");
    public static final String PROXY_HOST = conf.getString("proxy-host");
    public static final int PROXY_PORT = conf.getInt("proxy-port");

    public static final String KEY_BASE_64_FORMAT = conf.getString("signing-key-der-base64");

    public static final String BLOB_ROUTER_URL = conf.getString("blob-router-url");

    public static final String PROCESSOR_URL = conf.getString("processor-url");

    private TestConfig() {
    }
}
