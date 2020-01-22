package ch.frostnova.app.boot.platform.config;

import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Optional;
import java.util.regex.Pattern;

@Component
@ConfigurationProperties("ch.frostnova.platform.security.signing")
public class SigningConfig {

    private SigningKeyType keyType;
    private String publicKey;
    private String privateKey;
    private PrivateKey resolvedPrivateKey;
    private PublicKey resolvedPublicKey;

    @PostConstruct
    private void init() throws IOException {
        if (privateKey != null) {
            resolvedPrivateKey = loadPrivateKey(requireKeyType().getKeyType(), getResource(privateKey));
        }
        if (publicKey != null) {
            resolvedPublicKey = loadPublicKey(requireKeyType().getKeyType(), getResource(publicKey));
        }
    }

    private URL getResource(String resourcePath) throws IOException {

        // attempt to locate Java resource
        URL resource = getClass().getResource("/" + resourcePath);
        if (resource != null) {
            return resource;
        }

        // attempt to locate file
        File file = new File(resourcePath);
        if (file.exists()) {
            if (file.isFile() && !file.canRead()) {
                throw new IOException("File is not readable: " + resourcePath);
            }
            return file.toURI().toURL();
        }
        throw new FileNotFoundException(String.format("Resource '%s' not found", resourcePath));
    }

    public SigningKeyType getKeyType() {
        return keyType;
    }

    public void setKeyType(SigningKeyType keyType) {
        this.keyType = keyType;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    public String getPrivateKey() {
        return privateKey;
    }

    public void setPrivateKey(String privateKey) {
        this.privateKey = privateKey;
    }

    public SigningKeyType requireKeyType() {
        return Optional.ofNullable(keyType).orElseThrow(() -> new IllegalStateException("Missing key type in signing config"));
    }

    public PrivateKey requirePrivateKey() {
        return Optional.ofNullable(resolvedPrivateKey).orElseThrow(() -> new UnsupportedOperationException("Signing not possible - no private key configured"));
    }

    public PublicKey requirePublicKey() {
        return Optional.ofNullable(resolvedPublicKey).orElseThrow(() -> new UnsupportedOperationException("Verification not possible - no public key configured"));
    }

    public enum SigningKeyType {
        RSA("RSA", SignatureAlgorithm.RS256),
        EC("EC", SignatureAlgorithm.ES256);

        private final String keyType;
        private final SignatureAlgorithm signatureAlgorithm;


        SigningKeyType(String keyType, SignatureAlgorithm signatureAlgorithm) {
            this.keyType = keyType;
            this.signatureAlgorithm = signatureAlgorithm;
        }

        public SignatureAlgorithm getSignatureAlgorithm() {
            return signatureAlgorithm;
        }

        public String getKeyType() {
            return keyType;
        }
    }

    public static PrivateKey loadPrivateKey(String keyType, URL resource) {
        try {
            if (keyType == null) {
                throw new IllegalArgumentException("keyType is required");
            }
            if (resource == null) {
                throw new IllegalArgumentException("resource is required");
            }
            KeyFactory kf = KeyFactory.getInstance(keyType);
            return kf.generatePrivate(new PKCS8EncodedKeySpec(loadPEM(resource)));
        } catch (Exception ex) {
            throw new RuntimeException("Unable to load private key: " + resource, ex);
        }
    }

    public static PublicKey loadPublicKey(String keyType, URL resource) {
        try {
            if (keyType == null) {
                throw new IllegalArgumentException("keyType is required");
            }
            if (resource == null) {
                throw new IllegalArgumentException("resource is required");
            }
            KeyFactory kf = KeyFactory.getInstance(keyType);
            return kf.generatePublic(new X509EncodedKeySpec(loadPEM(resource)));
        } catch (Exception ex) {
            throw new RuntimeException("Unable to load public key: " + resource, ex);
        }
    }

    private static byte[] loadPEM(URL resource) throws IOException {
        try (InputStream in = resource.openStream()) {
            String pem = new String(in.readAllBytes(), StandardCharsets.ISO_8859_1);
            Pattern parse = Pattern.compile("(?m)(?s)^---*BEGIN.*---*$(.*)^---*END.*---*$.*");
            String encoded = parse.matcher(pem).replaceFirst("$1");
            return Base64.getMimeDecoder().decode(encoded);
        } catch (Exception ex) {
            throw new IOException("Could not read PEM from " + resource, ex);
        }
    }
}
