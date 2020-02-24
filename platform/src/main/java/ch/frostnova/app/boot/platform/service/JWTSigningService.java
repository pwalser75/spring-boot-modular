package ch.frostnova.app.boot.platform.service;

import ch.frostnova.app.boot.platform.model.UserInfo;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.Set;

public interface JWTSigningService {

    String createJWT(UserInfo userInfo, OffsetDateTime validFrom, Duration validity);
}
