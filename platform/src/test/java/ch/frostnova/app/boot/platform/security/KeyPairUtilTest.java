package ch.frostnova.app.boot.platform.security;

import org.junit.jupiter.api.Test;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.concurrent.ThreadLocalRandom;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test for {@link KeyPairUtil}.
 */
public class KeyPairUtilTest {

    private final static String SIGNATURE_SPEC = "SHA256withRSA";
    private final static String KEY_TYPE = KeyPairUtil.KEY_TYPE_RSA;
    private final static int KEY_LENGTH = 2048;

    @Test
    public void testWithGeneratedKeyPair() throws Exception {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance(KEY_TYPE);
        keyGen.initialize(KEY_LENGTH);
        KeyPair keyPair = keyGen.generateKeyPair();
        verifyKeyPair(keyPair.getPublic(), keyPair.getPrivate());
    }

    @Test
    public void testWithLoadedKeyPair() throws Exception {
        PrivateKey privateKey = KeyPairUtil.loadPrivateKey(KEY_TYPE, getClass().getResource("/jwt.pem"));
        PublicKey publicKey = KeyPairUtil.loadPublicKey(KEY_TYPE, getClass().getResource("/jwt.pub.pem"));
        verifyKeyPair(publicKey, privateKey);
    }

    private void verifyKeyPair(PublicKey publicKey, PrivateKey privateKey) throws Exception {

        byte[] secret = new byte[12345];
        ThreadLocalRandom.current().nextBytes(secret);

        byte[] signature = KeyPairUtil.sign(privateKey, SIGNATURE_SPEC, secret);
        assertTrue(KeyPairUtil.verify(publicKey, SIGNATURE_SPEC, secret, signature));
    }

}
