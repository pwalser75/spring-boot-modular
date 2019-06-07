package ch.frostnova.app.boot.platform;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;

import java.util.Locale;

@ComponentScan
@Configuration
public class PlatformConfig {

    private final static Locale SERVER_LOCALE = Locale.US;

    @Bean
    public LocaleResolver localeResolver() {
        SessionLocaleResolver localeResolver = new SessionLocaleResolver();
        localeResolver.setDefaultLocale(SERVER_LOCALE);
        return localeResolver;
    }
}
