package ch.frostnova.spring.boot.platform.util;

import org.junit.jupiter.api.Test;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import java.util.regex.Pattern;

public class CipherSuitesTest {

    private final Pattern RECOMMENDED_CIPHER_SUITES_PATTERN = Pattern.compile("TLS_(ECDHE|DHE)_(ECDSA|RSA)_WITH_(CHACHA20_POLY1305|AES_(128|256)_(GCM|CBC))_SHA(|256|384|512)");

    @Test
    public void listDefaultCipherSuites() throws Exception {

        SSLContext context = SSLContext.getDefault();
        SSLSocketFactory socketFactory = context.getSocketFactory();
        String[] defaultCipherSuites = socketFactory.getDefaultCipherSuites();
        for (String cipherSuite : defaultCipherSuites) {
            System.out.println((isRecommended(cipherSuite) ? "x " : "  ") + cipherSuite);
        }
    }

    @Test
    public void listRecommendedCipherSuites() throws Exception {

        SSLContext context = SSLContext.getDefault();
        SSLSocketFactory socketFactory = context.getSocketFactory();
        String[] supportedCipherSuites = socketFactory.getSupportedCipherSuites();
        for (String cipherSuite : supportedCipherSuites) {
            if (isRecommended(cipherSuite)) {
                System.out.println(cipherSuite);
            }
        }

    }

    private boolean isRecommended(String cipherSuite) {
        return RECOMMENDED_CIPHER_SUITES_PATTERN.matcher(cipherSuite).matches();
    }
}
