package uk.gov.hmcts.reform.bulkscan.endtoendtests.helper;

import java.security.KeyFactory;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;

import static uk.gov.hmcts.reform.bulkscan.endtoendtests.config.TestConfig.KEY_BASE_64_FORMAT;

public final class SigningHelper {

    public static byte[] sign(byte[] input) throws Exception {
        byte[] keyBytes = Base64.getDecoder().decode(KEY_BASE_64_FORMAT);
        var signature = Signature.getInstance("SHA256withRSA");
        signature.initSign(KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(keyBytes)));
        signature.update(input);
        return signature.sign();
    }

    private SigningHelper() {
        // util class
    }
}
