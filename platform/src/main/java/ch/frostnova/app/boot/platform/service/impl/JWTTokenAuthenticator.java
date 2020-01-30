package ch.frostnova.app.boot.platform.service.impl;

import ch.frostnova.app.boot.platform.model.UserInfo;
import ch.frostnova.app.boot.platform.service.JWTVerificationService;
import ch.frostnova.app.boot.platform.service.TokenAuthenticator;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Component
@ConditionalOnProperty(value = "ch.frostnova.platform.security.auth", havingValue = "jwt")
public class JWTTokenAuthenticator implements TokenAuthenticator {

    private final static Set<String> RESERVED_CLAIMS = Set.of("sub", "tenant", "scope");

    @Autowired
    private Logger logger;

    @Autowired
    private JWTVerificationService jwtVerificationService;


    @Override
    public UserInfo authenticate(String token) throws SecurityException {

        logger.debug("Token: {}", token);
        Jws<Claims> claims = jwtVerificationService.verify(token);
        Claims body = claims.getBody();

        logger.debug("Authenticated as: {}", body);

        List<?> scopes = body.get("scope", List.class);
        Map<String, String> additionalClaims = new HashMap<>();
        body.forEach((key, value) -> {
            if (!RESERVED_CLAIMS.contains(key)) {
                additionalClaims.put(key, toString(value));
            }
        });

        return UserInfo.aUserInfo()
                .tenant(body.get("tenant", String.class))
                .login(body.getSubject())
                .roles(scopes.stream().map(String::valueOf).collect(Collectors.toSet()))
                .additionalClaims(additionalClaims)
                .build();
    }

    private String toString(Object obj) {
        if (obj == null) {
            return null;
        }
        if (obj instanceof Iterable<?>) {
            Iterable<?> iterable = (Iterable<?>) obj;
            return StreamSupport.stream(iterable.spliterator(), false)
                    .map(this::toString)
                    .collect(Collectors.joining(","));
        }
        return String.valueOf(obj);
    }
}
