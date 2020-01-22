package ch.frostnova.app.boot.platform.service.impl;

import ch.frostnova.app.boot.platform.config.SigningConfig;
import ch.frostnova.app.boot.platform.service.JWTVerificationService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.time.Instant;
import java.util.Optional;

@Service
@ConditionalOnProperty(value = "ch.frostnova.platform.security.signing.public-key")
public class JWTVerificationServiceImpl implements JWTVerificationService {

    private final String CACHE_NAME = "jwt-cache";

    @Autowired
    private Logger logger;

    @Autowired
    private Optional<CacheManager> cacheManager;

    @Autowired
    private SigningConfig signingConfig;

    @PostConstruct
    public void init() {
        signingConfig.requirePublicKey();
        logger.info("JWT caching {}", cacheManager.map(Object::getClass)
                .map(Class::getName)
                .map(n -> "enabled")
                .orElse("disabled"));
    }

    @Override
    public Jws<Claims> verify(String token) {

        // check if we have cached claims for that token
        Optional<Jws<Claims>> cachedToken = getCache()
                .map(cache -> cache.get(token))
                .map(Cache.ValueWrapper::get)
                .map(Jws.class::cast);

        if (cachedToken.isPresent()) {
            // check if token is still valid
            Jws<Claims> jws = cachedToken.get();
            Instant expiresAt = jws.getBody().getExpiration().toInstant();
            if (expiresAt.isBefore(Instant.now())) {
                // remove expired token from cache and throw exception
                getCache().get().evict(token);
                throw new ExpiredJwtException(jws.getHeader(), jws.getBody(), "Token is expired");
            }
            // return valid cached token
            return jws;
        }
        // parse and validate token
        Jws<Claims> result = Jwts.parser().setSigningKey(signingConfig.requirePublicKey()).parseClaimsJws(token);

        // cache token
        getCache().ifPresent(cache -> cache.put(token, result));
        return result;
    }

    private Optional<Cache> getCache() {
        return cacheManager.map(cm -> cm.getCache(CACHE_NAME));
    }
}
