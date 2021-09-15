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
import java.util.Collection;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@SpringBootTest(classes = {PlatformConfig.class})
@EnableConfigurationProperties
public class JWTServiceTest {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private JWTSigningService jwtSigningService;

    @Autowired
    private JWTVerificationService jwtVerificationService;

    @Autowired(required = false)
    private CacheManager cacheManager;

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
                .additionalClaim("loginDeviceId", UUID.randomUUID().toString())
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

        assertThat(issuer).isEqualTo("frostnova-platform");
        assertThat(tenant).isEqualTo("test-tenant");
        assertThat(subject).isEqualTo("test-user");

        assertThat(issuedAt.isBefore(validFrom.toInstant())).isFalse();
        assertThat(issuedAt.isAfter(validFrom.toInstant())).isFalse();
        assertThat(notBefore.isBefore(validFrom.toInstant())).isFalse();
        assertThat(notBefore.isAfter(validFrom.toInstant())).isFalse();
        assertThat(notAfter.isBefore(validFrom.toInstant().plus(duration))).isFalse();
        assertThat(notAfter.isAfter(validFrom.toInstant().plus(duration))).isFalse();
        assertThat(scopes).isNotNull();

        assertThat(userInfo.getRoles()).allSatisfy(role -> assertThat(scopes.contains(role)).isTrue());
        assertThat(userInfo.getAdditionalClaims()).allSatisfy((k, v) -> assertThat(body.get(k, v.getClass())).isEqualTo(v));
    }

    @Test
    public void testJWTExpiration() throws Exception {

        UserInfo userInfo = UserInfo.aUserInfo().tenant("test-tenant").login("test-user").build();
        String token = jwtSigningService.createJWT(userInfo, OffsetDateTime.now(), Duration.of(2, ChronoUnit.SECONDS));
        jwtVerificationService.verify(token);

        Thread.sleep(2000);
        assertThatThrownBy(() -> jwtVerificationService.verify(token)).isInstanceOf(ExpiredJwtException.class);
    }

    @Test
    public void testJWTCache() {

        UserInfo userInfo = UserInfo.aUserInfo().tenant("test-tenant").login("test-user").build();
        String token = jwtSigningService.createJWT(userInfo, OffsetDateTime.now(), Duration.of(1, ChronoUnit.HOURS));
        Jws<Claims> value = jwtVerificationService.verify(token);

        Assert.isTrue(value == jwtVerificationService.verify(token));
    }
}
