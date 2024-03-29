package ch.frostnova.app.boot;

import ch.frostnova.common.service.CommonServiceConfig;
import ch.frostnova.module1.service.Module1ServiceConfig;
import ch.frostnova.module1.web.Module1WebConfig;
import ch.frostnova.spring.boot.platform.PlatformConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

/**
 * Spring boot application main class
 */
@SpringBootApplication
@Import({PlatformConfig.class, CommonServiceConfig.class,
        Module1ServiceConfig.class, Module1WebConfig.class})
public class SpringBootProject {

    public static void main(String[] args) {
        SpringApplication.run(SpringBootProject.class, args);
    }
}
