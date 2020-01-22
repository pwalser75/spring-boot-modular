package ch.frostnova.app.boot.platform.service;

import ch.frostnova.app.boot.platform.PlatformConfig;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.lang.Assert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@SpringBootTest(classes = {PlatformConfig.class})
@EnableConfigurationProperties
public class JWTServiceTest {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private JWTSigningService jwtSigningService;

    @Autowired
    private JWTVerificationService jwtVerificationService;

    @Autowired
    private Optional<CacheManager> cacheManager;

    @Test
    public void testGenerateJWT() {

        Duration duration = Duration.of(42, ChronoUnit.MINUTES);
        OffsetDateTime validFrom = OffsetDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        Set<String> roles = Set.of("ROLE1", "ROLE2", "ROLE3");
        Map<String, Object> additionalClaims = new HashMap<>();
        additionalClaims.put("login-device-id", UUID.randomUUID().toString());
        additionalClaims.put("channel", "mobile");

        String token = jwtSigningService.createJWT("test-tenant", "test-user", roles, additionalClaims, validFrom, duration);
        System.out.println(token);

        Jws<Claims> jwt = jwtVerificationService.verify(token);

        Claims body = jwt.getBody();
        String issuer = body.getIssuer();
        Instant issuedAt = body.getIssuedAt().toInstant();
        String tenant = body.get("tenant", String.class);
        String subject = body.getSubject();
        Instant notBefore = body.getNotBefore().toInstant();
        Instant notAfter = body.getExpiration().toInstant();
        Collection<String> scopes = body.get("scope", Collection.class);

        assertEquals("frostnova-platform", issuer);
        assertEquals("test-tenant", tenant);
        assertEquals("test-user", subject);

        assertFalse(issuedAt.isBefore(validFrom.toInstant()));
        assertFalse(issuedAt.isAfter(validFrom.toInstant()));
        assertFalse(notBefore.isBefore(validFrom.toInstant()));
        assertFalse(notBefore.isAfter(validFrom.toInstant()));
        assertFalse(notAfter.isBefore(validFrom.toInstant().plus(duration)));
        assertFalse(notAfter.isAfter(validFrom.toInstant().plus(duration)));
        assertNotNull(scopes);

        roles.forEach(role -> assertTrue(scopes.contains(role)));
        additionalClaims.forEach((k, v) -> assertEquals(v, body.get(k, v.getClass())));
    }

    @Test
    public void testJWTExpiration() throws Exception {

        String token = jwtSigningService.createJWT("test-tenant", "test-user", null, null, OffsetDateTime.now(), Duration.of(2, ChronoUnit.SECONDS));
        jwtVerificationService.verify(token);

        Thread.sleep(2000);
        assertThrows(ExpiredJwtException.class, () -> jwtVerificationService.verify(token));
    }

    @Test
    public void testJWTCache() {

        String token = jwtSigningService.createJWT("test-tenant", "test-user", null, null, OffsetDateTime.now(), Duration.of(1, ChronoUnit.HOURS));
        Jws<Claims> value = jwtVerificationService.verify(token);

        Assert.isTrue(value == jwtVerificationService.verify(token));
    }
}
