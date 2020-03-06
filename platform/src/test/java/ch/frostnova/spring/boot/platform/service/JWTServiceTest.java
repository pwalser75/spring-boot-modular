package ch.frostnova.spring.boot.platform.service;

import ch.frostnova.spring.boot.platform.PlatformConfig;
import ch.frostnova.spring.boot.platform.model.UserInfo;
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

        UserInfo userInfo = UserInfo.aUserInfo()
                .tenant("test-tenant")
                .login("test-user")
                .role("RoleA")
                .role("RoleB")
                .role("RoleC")
                .additionalClaim("loginDeviceId",UUID.randomUUID().toString())
                .additionalClaim("accessChannel", "mobile")
                .build();

        String token = jwtSigningService.createJWT(userInfo, validFrom, duration);
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

        userInfo.getRoles().forEach(role -> assertTrue(scopes.contains(role)));
        userInfo.getAdditionalClaims().forEach((k, v) -> assertEquals(v, body.get(k, v.getClass())));
    }

    @Test
    public void testJWTExpiration() throws Exception {

        UserInfo userInfo = UserInfo.aUserInfo().tenant("test-tenant").login("test-user").build();
        String token = jwtSigningService.createJWT(userInfo, OffsetDateTime.now(), Duration.of(2, ChronoUnit.SECONDS));
        jwtVerificationService.verify(token);

        Thread.sleep(2000);
        assertThrows(ExpiredJwtException.class, () -> jwtVerificationService.verify(token));
    }

    @Test
    public void testJWTCache() {

        UserInfo userInfo = UserInfo.aUserInfo().tenant("test-tenant").login("test-user").build();
        String token = jwtSigningService.createJWT(userInfo, OffsetDateTime.now(), Duration.of(1, ChronoUnit.HOURS));
        Jws<Claims> value = jwtVerificationService.verify(token);

        Assert.isTrue(value == jwtVerificationService.verify(token));
    }
}
