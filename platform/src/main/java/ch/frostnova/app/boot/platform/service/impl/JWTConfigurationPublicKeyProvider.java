package ch.frostnova.app.boot.platform.service.impl;

import ch.frostnova.app.boot.platform.config.JWTProperties;
import ch.frostnova.app.boot.platform.security.KeyPairUtil;
import ch.frostnova.app.boot.platform.service.JWTPublicKeyProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.net.URL;
import java.security.PublicKey;

@Component
@ConditionalOnProperty(prefix = "ch.frostnova.platform.security.jwt", value = "public-key")
@Primary
public class JWTConfigurationPublicKeyProvider implements JWTPublicKeyProvider {

    private transient PublicKey publicKey;

    @Autowired
    private JWTProperties jwtProperties;

    @PostConstruct
    public void init() throws Exception {
        String resourcePath = "/" + jwtProperties.getPublicKey();
        URL resource = getClass().getResource(resourcePath);
        if (resource == null) {
            throw new IOException("Resource not found: " + resourcePath);
        }
        publicKey = KeyPairUtil.loadPublicKey(jwtProperties.getKeyType(), resource);
    }

    @Override
    public PublicKey getPublicKey() {
        return publicKey;
    }
}
