package ch.frostnova.app.boot.platform.service.impl;

import ch.frostnova.app.boot.platform.config.SigningConfig;
import ch.frostnova.app.boot.platform.service.JWTVerificationService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service
@ConditionalOnProperty(value = "ch.frostnova.platform.security.signing.public-key")
public class JWTVerificationServiceImpl implements JWTVerificationService {

    @Autowired
    private SigningConfig signingConfig;

    @PostConstruct
    public void init() {
        signingConfig.requirePublicKey();
    }

    @Override
    public Jws<Claims> verify(String token) {
        return Jwts.parser().setSigningKey(signingConfig.requirePublicKey()).parseClaimsJws(token);
    }
}
