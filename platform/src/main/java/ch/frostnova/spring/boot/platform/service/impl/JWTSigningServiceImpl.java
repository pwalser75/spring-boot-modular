package ch.frostnova.spring.boot.platform.service.impl;

import ch.frostnova.spring.boot.platform.config.SigningConfig;
import ch.frostnova.spring.boot.platform.model.UserInfo;
import ch.frostnova.spring.boot.platform.service.JWTSigningService;
import io.jsonwebtoken.Jwts;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@ConditionalOnProperty(value = "ch.frostnova.platform.security.signing.private-key")
public class JWTSigningServiceImpl implements JWTSigningService {

    private final static String CLAIM_TENANT = "tenant";
    private final static String CLAIM_SCOPE = "scope";

    @Autowired
    private Logger logger;

    @Autowired
    private SigningConfig signingConfig;

    @Value("${info.app.name:spring}")
    private String appName;

    @PostConstruct
    public void init() throws Exception {

        signingConfig.requirePublicKey();
        logger.warn("{} is activated, service can issue self-signed JWT security tokens - do not use in production", getClass().getSimpleName());
    }

    @Override
    public String createJWT(UserInfo userInfo, OffsetDateTime validFrom, Duration validity) {

        return Jwts.builder()
                .setIssuer(appName)
                .setId(UUID.randomUUID().toString())
                .setIssuedAt(Date.from(validFrom.toInstant()))
                .setNotBefore(Date.from(validFrom.toInstant()))
                .setExpiration(Date.from(validFrom.plus(validity).toInstant()))
                .claim(CLAIM_TENANT, userInfo.getTenant())
                .setSubject(userInfo.getLogin())
                .claim(CLAIM_SCOPE, Optional.ofNullable(userInfo.getRoles()).map(TreeSet::new).orElse(null))
                .addClaims(userInfo.getAdditionalClaims().entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)))
                .signWith(signingConfig.getKeyType().getSignatureAlgorithm(), signingConfig.requirePrivateKey())
                .compact();
    }
}
