package uk.gov.hmcts.reform.bulkscan.endtoendtests.helper;

import com.typesafe.config.ConfigFactory;

import java.security.KeyFactory;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;

public final class SigningHelper {

    private static final String keyBase64Format = ConfigFactory.load().getString("signing-key-der-base64");

    public static byte[] sign(byte[] input) throws Exception {
        byte[] keyBytes = Base64.getDecoder().decode(keyBase64Format);
        var signature = Signature.getInstance("SHA256withRSA");
        signature.initSign(KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(keyBytes)));
        signature.update(input);
        return signature.sign();
    }

    private SigningHelper() {
        // util class
    }
}
