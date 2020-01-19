package ch.frostnova.app.boot.platform.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;

public interface JWTVerificationService {

    Jws<Claims> verify(String token);
}
