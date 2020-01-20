package ch.frostnova.app.boot.platform.service;

import ch.frostnova.app.boot.platform.PlatformConfig;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
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

    @Autowired
    private JWTSigningService jwtSigningService;

    @Autowired
    private JWTVerificationService jwtVerificationService;

    @RepeatedTest(10)
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

        String issuer = jwt.getBody().getIssuer();
        Instant issuedAt = jwt.getBody().getIssuedAt().toInstant();
        String tenant = jwt.getBody().get("tenant", String.class);
        String subject = jwt.getBody().getSubject();
        Instant notBefore = jwt.getBody().getNotBefore().toInstant();
        Instant notAfter = jwt.getBody().getExpiration().toInstant();
        Collection<String> scopes = jwt.getBody().get("scope", Collection.class);

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
        additionalClaims.forEach((k, v) -> assertEquals(v, jwt.getBody().get(k, v.getClass())));
    }
}
