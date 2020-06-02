package uk.gov.hmcts.reform.bulkscan.endtoendtests.helper;

import java.security.KeyFactory;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;

import static com.google.common.io.Resources.getResource;
import static com.google.common.io.Resources.toByteArray;

public final class SigningHelper {

    public static byte[] sign(byte[] input) throws Exception {
        byte[] keyBytes = toByteArray(getResource("test_private_key.der"));
        Signature signature = Signature.getInstance("SHA256withRSA");
        signature.initSign(KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(keyBytes)));
        signature.update(input);
        return signature.sign();
    }

    private SigningHelper() {
        // util class
    }
}
