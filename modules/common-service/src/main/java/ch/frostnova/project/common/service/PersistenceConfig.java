package ch.frostnova.project.common.service;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.auditing.DateTimeProvider;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.Optional;

import static ch.frostnova.project.common.service.PersistenceConfig.AUDITOR_PROVIDER_ID;

@EnableTransactionManagement
@EnableJpaAuditing(dateTimeProviderRef = PersistenceConfig.OFFSET_DATE_TIME_PROVIDER_ID, auditorAwareRef = AUDITOR_PROVIDER_ID)
@Configuration
public class PersistenceConfig {

    protected final static String OFFSET_DATE_TIME_PROVIDER_ID = "offset-date-time-provider";
    protected final static String AUDITOR_PROVIDER_ID = "internal-auditor-provider";
    protected final static String ANONYMOUS_USER_NAME = "anonymous";

    @Bean(OFFSET_DATE_TIME_PROVIDER_ID)
    public DateTimeProvider dateTimeProvider() {
        return () -> Optional.of(OffsetDateTime.now());
    }

    @Bean(AUDITOR_PROVIDER_ID)
    public AuditorAware<String> auditorProvider() {
        return () -> Optional.of(resolveUserName());
    }

    private static String resolveUserName() {
         /*
          if you are using spring security, you can get the currently logged username with following code segment.
          SecurityContextHolder.getContext().getAuthentication().getName()
         */
        return Optional.ofNullable(System.getProperty("user.name"))
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> s.length() > 0)
                .orElse(ANONYMOUS_USER_NAME);
    }
}
