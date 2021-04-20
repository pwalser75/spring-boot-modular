package ch.frostnova.module1.service.persistence;

import ch.frostnova.module1.service.TestConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test JPA repository
 */
@ExtendWith(SpringExtension.class)
@DataJpaTest
@ActiveProfiles("test")
@ContextConfiguration(classes = TestConfig.class)
public class NoteRepositoryTest {

    @Autowired
    private NoteRepository repository;

    @Test
    public void testCRUD() {

        // create
        NoteEntity note = new NoteEntity();
        note.setText("Aloha");

        assertThat(note.isPersistent()).isFalse();
        OffsetDateTime before = OffsetDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        note = repository.save(note);
        assertThat(note.isPersistent()).isTrue();
        assertThat(note.getId()).isNotNull();

        assertThat(note.getCreatedOn()).isNotNull();
        assertThat(note.getCreatedBy()).isNotNull();
        assertThat(note.getCreatedOn().isBefore(before)).isFalse();
        assertThat(note.getCreatedOn().isAfter(OffsetDateTime.now())).isFalse();

        assertThat(note.getLastUpdatedOn()).isNotNull();
        assertThat(note.getLastUpdatedBy()).isNotNull();
        assertThat(note.getLastUpdatedOn().isBefore(before)).isFalse();
        assertThat(note.getLastUpdatedOn().isAfter(OffsetDateTime.now())).isFalse();
        OffsetDateTime creationDate = note.getCreatedOn();

        // read
        note = repository.findById(note.getId()).orElseThrow(NoSuchElementException::new);
        assertThat(note.getText()).isEqualTo("Aloha");

        // update
        before = OffsetDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        note.setText("Lorem ipsum dolor sit amet");
        note = repository.save(note);
        assertThat(note.getCreatedOn()).isEqualTo(creationDate);
        assertThat(note.getLastUpdatedOn()).isNotNull();
        assertThat(note.getLastUpdatedOn().isBefore(before)).isFalse();
        assertThat(note.getLastUpdatedOn().isAfter(OffsetDateTime.now())).isFalse();

        // delete
        repository.deleteById(note.getId());
        note = repository.findById(note.getId()).orElse(null);
        assertThat(note).isNull();
    }

    @Test
    public void testFind() {
        NoteEntity note = new NoteEntity();
        note.setText("Lorem ipsum dolor sit amet");
        repository.save(note);

        for (String positive : Arrays.asList("Lorem", "IPSUM", "oLo", "dolor ips", "sit, lo", "'sit amet'", "'em IP")) {
            assertTrue(repository.findAll(NoteRepository.fulltextSearch(positive)).contains(note), "query: " + positive);
        }

        for (String negative : Arrays.asList("L0rem", "QUIPSUM", "foo", "dolor ups", "sit# lo", "'sitamet'", "'lorem dolor")) {
            assertFalse(repository.findAll(NoteRepository.fulltextSearch(negative)).contains(note), "query: " + negative);
        }
    }
}
