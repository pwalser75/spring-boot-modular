package ch.frostnova.app.boot.platform.service.impl;

import ch.frostnova.app.boot.platform.config.JWTProperties;
import ch.frostnova.app.boot.platform.security.KeyPairUtil;
import ch.frostnova.app.boot.platform.service.JWTPrivateKeyProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.net.URL;
import java.security.PrivateKey;

@Component
@ConditionalOnProperty(prefix = "ch.frostnova.platform.security.jwt", value = "private-key")
@Primary
public class JWTConfigurationPrivateKeyProvider implements JWTPrivateKeyProvider {

    private transient PrivateKey privateKey;

    @Autowired
    private JWTProperties jwtProperties;

    @PostConstruct
    public void init() throws Exception {
        String resourcePath = "/" + jwtProperties.getPrivateKey();
        URL resource = getClass().getResource(resourcePath);
        if (resource == null) {
            throw new IOException("Resource not found: " + resourcePath);
        }
        privateKey = KeyPairUtil.loadPrivateKey(jwtProperties.getKeyType(), resource);
    }

    @Override
    public PrivateKey getPrivateKey() {
        return privateKey;
    }
}
