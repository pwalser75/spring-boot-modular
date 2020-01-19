package ch.frostnova.app.boot.platform.service.impl;

import ch.frostnova.app.boot.platform.service.JWTPublicKeyProvider;
import ch.frostnova.app.boot.platform.service.JWTVerificationService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnBean(JWTPublicKeyProvider.class)
public class JWTVerificationServiceImpl implements JWTVerificationService {

    @Autowired
    private JWTPublicKeyProvider jwtPublicKeyProvider;

    @Override
    public Jws<Claims> verify(String token) {
        return Jwts.parser().setSigningKey(jwtPublicKeyProvider.getPublicKey()).parseClaimsJws(token);
    }
}
