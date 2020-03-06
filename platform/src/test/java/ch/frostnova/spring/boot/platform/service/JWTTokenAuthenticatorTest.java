package ch.frostnova.spring.boot.platform.service;

import ch.frostnova.spring.boot.platform.PlatformConfig;
import ch.frostnova.spring.boot.platform.model.UserInfo;
import ch.frostnova.spring.boot.platform.service.impl.JWTTokenAuthenticator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@SpringBootTest(classes = {PlatformConfig.class})
@EnableConfigurationProperties
public class JWTTokenAuthenticatorTest {

    @Autowired
    private JWTTokenAuthenticator jwtTokenAuthenticator;

    @Autowired
    private JWTSigningService jwtSigningService;

    @Test
    public void test() {

        UserInfo request = UserInfo.aUserInfo()
                .tenant("test-tenant")
                .login("test-login")
                .role("RoleA")
                .role("RoleB")
                .additionalClaim("loginDeviceId", "device-001")
                .additionalClaim("accessChannel", "web")
                .build();

        String token = jwtSigningService.createJWT(request, OffsetDateTime.now(), Duration.of(2, ChronoUnit.HOURS));

        UserInfo userInfo = jwtTokenAuthenticator.authenticate(token);

        assertEquals("test-tenant", userInfo.getTenant());
        assertEquals("test-login", userInfo.getLogin());
        assertEquals(2, userInfo.getRoles().size());
        assertTrue(userInfo.getRoles().contains("RoleA"));
        assertTrue(userInfo.getRoles().contains("RoleB"));
        assertEquals("device-001", userInfo.getAdditionalClaims().get("loginDeviceId"));
        assertEquals("web", userInfo.getAdditionalClaims().get("accessChannel"));
    }
}
