package ch.frostnova.spring.boot.platform.service;

import ch.frostnova.spring.boot.platform.model.UserInfo;

import java.time.Duration;
import java.time.OffsetDateTime;

public interface JWTSigningService {

    String createJWT(UserInfo userInfo, OffsetDateTime validFrom, Duration validity);
}
