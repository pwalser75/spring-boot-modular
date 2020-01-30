package ch.frostnova.module1.service;

import ch.frostnova.common.service.PersistenceConfig;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@ComponentScan
@EntityScan
@EnableJpaRepositories
@Configuration
@Import(PersistenceConfig.class)
public class Module1ServiceConfig {
}
