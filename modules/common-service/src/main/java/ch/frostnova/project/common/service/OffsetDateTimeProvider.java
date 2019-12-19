package ch.frostnova.project.common.service;

import org.springframework.data.auditing.DateTimeProvider;

import java.time.OffsetDateTime;
import java.time.temporal.TemporalAccessor;
import java.util.Optional;

/**
 * Provider for offset date/time to allow using  OffsetDateTime instead of LocalDateTime on audit dates (created-on, updated-on).
 */
public class OffsetDateTimeProvider implements DateTimeProvider {

    @Override
    public Optional<TemporalAccessor> getNow() {
        return Optional.of(OffsetDateTime.now());
    }
}
