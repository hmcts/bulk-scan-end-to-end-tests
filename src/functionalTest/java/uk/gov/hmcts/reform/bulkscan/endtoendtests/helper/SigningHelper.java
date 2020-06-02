package uk.gov.hmcts.reform.bulkscan.endtoendtests.helper;

import com.typesafe.config.ConfigFactory;

import java.security.KeyFactory;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;

public final class SigningHelper {

    private static final String key = ConfigFactory.load().getString("signing-key-der");

    public static byte[] sign(byte[] input) throws Exception {

        var signature = Signature.getInstance("SHA256withRSA");
        signature.initSign(KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(key.getBytes())));
        signature.update(input);
        return signature.sign();
    }

    private SigningHelper() {
        // util class
    }
}
