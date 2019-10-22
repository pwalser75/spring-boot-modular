package ch.frostnova.module1.service;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.validation.beanvalidation.MethodValidationPostProcessor;

/**
 * Test config
 *
 * @author wap
 * @since 04.06.2019
 */
@Configuration
@Import(Module1ServiceConfig.class)
public class TestConfig {

    @Bean
    public MethodValidationPostProcessor bean() {
        return new MethodValidationPostProcessor();
    }
}
