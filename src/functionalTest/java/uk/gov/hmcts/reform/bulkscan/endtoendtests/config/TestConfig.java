package uk.gov.hmcts.reform.bulkscan.endtoendtests.config;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

public class TestConfig {

    private static Config conf = ConfigFactory.load();

    public static final String IDAM_API_URL = conf.getString("idam-api-url");
    public static final String IDAM_CLIENT_REDIRECT_URI = conf.getString("idam-client-redirect-uri");
    public static final String IDAM_CLIENT_SECRET = conf.getString("idam-client-secret");
    public static final String IDAM_USER_NAME = conf.getString("idam-users-bulkscan-username");
    public static final String IDAM_PASSWORD = conf.getString("idam-users-bulkscan-password");

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
