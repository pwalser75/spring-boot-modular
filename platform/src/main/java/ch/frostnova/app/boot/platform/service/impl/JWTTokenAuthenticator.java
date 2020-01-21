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

import java.util.List;
import java.util.stream.Collectors;

@Component
@ConditionalOnProperty(value = "ch.frostnova.platform.security.auth", havingValue = "jwt")
public class JWTTokenAuthenticator implements TokenAuthenticator {

    @Autowired
    private Logger logger;

    @Autowired
    private JWTVerificationService jwtVerificationService;

    @Override
    public UserInfo authenticate(String token) throws SecurityException {

        logger.debug("Token: {}", token);
        Jws<Claims> claims = jwtVerificationService.verify(token);
        logger.debug("Authenticated as: {}", claims.getBody());

        List<?> scopes = claims.getBody().get("scope", List.class);

        return UserInfo.aUserInfo()
                .tenant(claims.getBody().get("tenant", String.class))
                .login(claims.getBody().getSubject())
                .roles(scopes.stream().map(String::valueOf).collect(Collectors.toSet()))
                //TODO: map additional claims
                .build();
    }
}
