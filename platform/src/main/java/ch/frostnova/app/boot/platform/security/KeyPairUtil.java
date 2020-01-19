package ch.frostnova.app.boot.platform.security;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.regex.Pattern;

public final class KeyPairUtil {

    public static final String KEY_TYPE_RSA = "RSA";

    private KeyPairUtil() {

    }

    public static PrivateKey loadPrivateKey(String keyType, URL resource) throws NoSuchAlgorithmException, IOException, InvalidKeySpecException {
        if (keyType == null) {
            throw new IllegalArgumentException("keyType is required");
        }
        if (resource == null) {
            throw new IllegalArgumentException("resource is required");
        }
        KeyFactory kf = KeyFactory.getInstance(keyType);
        return kf.generatePrivate(new PKCS8EncodedKeySpec(loadPEM(resource)));
    }

    public static PublicKey loadPublicKey(String keyType, URL resource) throws NoSuchAlgorithmException, IOException, InvalidKeySpecException {
        if (keyType == null) {
            throw new IllegalArgumentException("keyType is required");
        }
        if (resource == null) {
            throw new IllegalArgumentException("resource is required");
        }
        KeyFactory kf = KeyFactory.getInstance(keyType);
        return kf.generatePublic(new X509EncodedKeySpec(loadPEM(resource)));
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

    public static byte[] sign(PrivateKey privateKey, String signatureSpec, byte[] data) throws Exception {
        if (privateKey == null) {
            throw new IllegalArgumentException("privateKey is required");
        }
        if (signatureSpec == null) {
            throw new IllegalArgumentException("signatureSpec is required");
        }
        if (data == null) {
            throw new IllegalArgumentException("data is required");
        }
        Signature signature = Signature.getInstance(signatureSpec);
        signature.initSign(privateKey);
        signature.update(data);
        return signature.sign();
    }

    public static boolean verify(PublicKey publicKey, String signatureSpec, byte[] data, byte[] signatureBytes) throws Exception {
        if (publicKey == null) {
            throw new IllegalArgumentException("publicKey is required");
        }
        if (signatureSpec == null) {
            throw new IllegalArgumentException("signatureSpec is required");
        }
        if (data == null) {
            throw new IllegalArgumentException("data is required");
        }
        if (signatureBytes == null) {
            throw new IllegalArgumentException("signatureBytes is required");
        }
        Signature signature = Signature.getInstance(signatureSpec);
        signature.initVerify(publicKey);
        signature.update(data);
        return signature.verify(signatureBytes);
    }
}
