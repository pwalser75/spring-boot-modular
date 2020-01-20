package ch.frostnova.app.boot.platform.service;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.Set;

public interface JWTSigningService {

    String createJWT(String tenant, String login, Set<String> roles, Map<String, Object> additionalClaims, OffsetDateTime validFrom, Duration validity);
}
