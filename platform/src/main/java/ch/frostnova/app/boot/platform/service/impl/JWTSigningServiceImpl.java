package ch.frostnova.app.boot.platform.service.impl;

import ch.frostnova.app.boot.platform.service.JWTPrivateKeyProvider;
import ch.frostnova.app.boot.platform.service.JWTSigningService;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.*;

@Service
@ConditionalOnBean(JWTPrivateKeyProvider.class)
public class JWTSigningServiceImpl implements JWTSigningService {

    private final static String CLAIM_TENANT = "tenant";
    private final static String CLAIM_SCOPE = "scope";

    @Autowired
    private Logger logger;

    @Autowired
    private JWTPrivateKeyProvider jwtPrivateKeyProvider;

    @Value("${info.app.name:spring}")
    private String appName;

    @PostConstruct
    public void init() throws Exception {
        logger.warn("{} is activated, service can issue self-signed JWT security tokens - do not use in production", getClass().getSimpleName());
    }

    @Override
    public String createJWT(String tenant, String login, Set<String> roles, Map<String, Object> additionalClaims, Duration validity) {
        OffsetDateTime now = OffsetDateTime.now();
        return Jwts.builder()
                .setIssuer(appName)
                .setIssuedAt(Date.from(now.toInstant()))
                .setNotBefore(Date.from(now.toInstant()))
                .setExpiration(Date.from(now.plus(validity).toInstant()))
                .claim(CLAIM_TENANT, tenant)
                .setSubject(login)
                .claim(CLAIM_SCOPE, Optional.ofNullable(roles).map(TreeSet::new).orElse(null))
                .addClaims(additionalClaims)
                .signWith(SignatureAlgorithm.RS256, jwtPrivateKeyProvider.getPrivateKey())
                .compact();

    }
}
