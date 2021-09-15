package ch.frostnova.common.service;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.auditing.DateTimeProvider;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.security.Principal;
import java.time.OffsetDateTime;
import java.util.Optional;

import static ch.frostnova.common.service.PersistenceConfig.AUDITOR_PROVIDER_ID;

@EnableTransactionManagement
@EnableJpaAuditing(dateTimeProviderRef = PersistenceConfig.OFFSET_DATE_TIME_PROVIDER_ID, auditorAwareRef = AUDITOR_PROVIDER_ID)
@Configuration
public class PersistenceConfig {

    protected final static String OFFSET_DATE_TIME_PROVIDER_ID = "offset-date-time-provider";
    protected final static String AUDITOR_PROVIDER_ID = "internal-auditor-provider";
    protected final static String ANONYMOUS_USER_NAME = "anonymous";

    private static String resolveUserName() {

        return Optional.ofNullable(SecurityContextHolder.getContext())
                .map(SecurityContext::getAuthentication)
                .map(Principal::getName)
                .orElse(ANONYMOUS_USER_NAME);
    }

    @Bean(OFFSET_DATE_TIME_PROVIDER_ID)
    public DateTimeProvider dateTimeProvider() {
        return () -> Optional.of(OffsetDateTime.now());
    }

    @Bean(AUDITOR_PROVIDER_ID)
    public AuditorAware<String> auditorProvider() {
        return () -> Optional.of(resolveUserName());
    }
}
