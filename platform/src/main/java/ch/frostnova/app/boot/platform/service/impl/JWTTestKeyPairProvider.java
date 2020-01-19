package ch.frostnova.app.boot.platform.service.impl;

import ch.frostnova.app.boot.platform.service.JWTPrivateKeyProvider;
import ch.frostnova.app.boot.platform.service.JWTPublicKeyProvider;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;

@Component
@Profile("test")
public class JWTTestKeyPairProvider implements JWTPublicKeyProvider, JWTPrivateKeyProvider {

    private final static String KEY_TYPE = "RSA";
    private final static int KEY_LENGTH = 2048;

    @Autowired
    private Logger logger;

    private PublicKey publicKey;
    private PrivateKey privateKey;

    @PostConstruct
    public void init() throws Exception {
        logger.warn("Using built-in test key pair provider for JWT signing/verification - do not use in production");
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance(KEY_TYPE);
        keyGen.initialize(KEY_LENGTH);
        KeyPair keyPair = keyGen.generateKeyPair();
        publicKey = keyPair.getPublic();
        privateKey = keyPair.getPrivate();
    }

    @Override
    public PublicKey getPublicKey() {
        return publicKey;
    }

    @Override
    public PrivateKey getPrivateKey() {
        return privateKey;
    }
}
