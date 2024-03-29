package ch.frostnova.module1.service.metrics;

import ch.frostnova.module1.service.persistence.NoteRepository;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * Metrics for the notes
 *
 * @author pwalser
 * @since 2021-04-19
 */
@Component
@Lazy(value = false)
public class NoteMetrics {

    private final static String NOTE_COUNT_GAUGE_NAME = "business.metrics.notes.count";

    private final Logger logger = LoggerFactory.getLogger(NoteMetrics.class);

    @Autowired
    private MeterRegistry meterRegistry;

    @Autowired
    private NoteRepository repository;

    private Long notesCount;

    @PostConstruct
    public void init() {
        logger.debug("registering notes gauge");
        update();
        Gauge.builder(NOTE_COUNT_GAUGE_NAME, this, noteMetrics -> notesCount)
                .description("Number of notes in the DB")
                .register(meterRegistry);
        logger.info("notes gauge registered");
    }

    @Scheduled(fixedDelay = 5000)
    public void update() {
        logger.trace("updating notes gauge");
        notesCount = repository.count();
    }
}
