package ch.frostnova.module1.web;

import ch.frostnova.app.boot.platform.PlatformConfig;
import ch.frostnova.module1.service.Module1ServiceConfig;
import ch.frostnova.project.common.service.CommonServiceConfig;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

/**
 * @author pwalser
 * @since 12.03.2018.
 */
@SpringBootApplication
@Import({PlatformConfig.class, Module1ServiceConfig.class, CommonServiceConfig.class})
class TestApp {

}
