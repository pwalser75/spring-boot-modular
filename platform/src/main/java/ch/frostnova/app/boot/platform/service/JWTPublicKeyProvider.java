package ch.frostnova.app.boot.platform.service;

import java.security.PublicKey;

public interface JWTPublicKeyProvider {

    PublicKey getPublicKey();
}
