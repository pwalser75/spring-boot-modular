package ch.frostnova.module1;

import ch.frostnova.app.boot.platform.PlatformConfig;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

/**
 * @author pwalser
 * @since 12.03.2018.
 */
@SpringBootApplication
@Import(PlatformConfig.class)
public class TestApp {

}
