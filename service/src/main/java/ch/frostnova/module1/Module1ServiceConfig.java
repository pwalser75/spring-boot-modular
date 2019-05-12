package ch.frostnova.module1;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@ComponentScan
@EntityScan
@EnableJpaRepositories
@EnableTransactionManagement
@Configuration
public class Module1ServiceConfig {
}
