package ch.frostnova.module1.service;

import ch.frostnova.common.service.CommonServiceConfig;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Metrics;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.validation.beanvalidation.MethodValidationPostProcessor;

/**
 * Test config
 *
 * @author pwalser
 * @since 04.06.2019
 */
@Configuration
@Import({Module1ServiceConfig.class, CommonServiceConfig.class})
public class TestConfig {

    @Bean
    public MethodValidationPostProcessor bean() {
        return new MethodValidationPostProcessor();
    }

    @Bean
    @ConditionalOnMissingBean
    public MeterRegistry meterRegistry() {
        return Metrics.globalRegistry;
    }
}
