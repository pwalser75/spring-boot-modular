package ch.frostnova.app.boot.platform.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Example application properties
 */
@Component
@ConfigurationProperties("ch.frostnova.platform.security.jwt")
public class JWTProperties {


}
