package ch.frostnova.app.boot.platform.service;

import java.security.PrivateKey;

public interface JWTPrivateKeyProvider {

    PrivateKey getPrivateKey();
}
