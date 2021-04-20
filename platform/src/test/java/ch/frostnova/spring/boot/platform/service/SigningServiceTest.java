package ch.frostnova.spring.boot.platform.service;

import ch.frostnova.spring.boot.platform.PlatformConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.concurrent.ThreadLocalRandom;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@SpringBootTest(classes = {PlatformConfig.class})
@EnableConfigurationProperties
public class SigningServiceTest {

    @Autowired
    private SigningService signingService;

    @Test
    public void testSigningAndVerification() throws Exception {
        byte[] data = new byte[12345];
        ThreadLocalRandom.current().nextBytes(data);

        byte[] signature = signingService.sign(data);
        assertThat(signature).isNotNull();
        assertThat(signature.length).isGreaterThan(0);
        assertThat(signingService.verify(data, signature)).isTrue();

        byte[] fakeSignature = new byte[signature.length];
        System.arraycopy(signature, 0, fakeSignature, 0, signature.length);
        fakeSignature[0] = (byte) ~signature[0];
        assertThat(signingService.verify(data, fakeSignature)).isFalse();
    }
}
