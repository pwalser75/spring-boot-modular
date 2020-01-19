package ch.frostnova.app.boot.platform.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InjectionPoint;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

/**
 * Injectable Logger config
 */
@Configuration
public class LoggerConfig {

    @Bean
    @Scope(BeanDefinition.SCOPE_PROTOTYPE)
    Logger logger(InjectionPoint injectionPoint) {
        return LoggerFactory.getLogger(injectionPoint.getField().getDeclaringClass());
    }
}
