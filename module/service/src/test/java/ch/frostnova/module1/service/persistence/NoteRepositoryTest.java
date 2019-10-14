package ch.frostnova.module1.service.persistence;

import ch.frostnova.module1.service.TestConfig;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.NoSuchElementException;

import static org.junit.Assert.*;

/**
 * Test JPA repository
 */
@RunWith(SpringRunner.class)
@DataJpaTest
@ContextConfiguration(classes = TestConfig.class)
public class NoteRepositoryTest {

    @Autowired
    private NoteRepository repository;

    @Test
    public void testCRUD() {

        // create
        NoteEntity note = new NoteEntity();
        note.setText("Aloha");

        assertFalse(note.isPersistent());
        OffsetDateTime before = OffsetDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        note = repository.save(note);
        assertTrue(note.isPersistent());
        assertNotNull(note.getId());
        assertNotNull(note.getCreatedOn());
        assertFalse(note.getCreatedOn().isBefore(before));
        assertFalse(note.getCreatedOn().isAfter(OffsetDateTime.now()));
        assertNotNull(note.getLastUpdatedOn());
        assertFalse(note.getLastUpdatedOn().isBefore(before));
        assertFalse(note.getLastUpdatedOn().isAfter(OffsetDateTime.now()));
        OffsetDateTime creationDate = note.getCreatedOn();

        // read
        note = repository.findById(note.getId()).orElseThrow(NoSuchElementException::new);
        assertEquals("Aloha", note.getText());

        // update
        note.setText("Lorem ipsum dolor sit amet");
        note = repository.save(note);
        before = OffsetDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        assertEquals(creationDate, note.getCreatedOn());
        assertNotNull(note.getLastUpdatedOn());
        assertFalse(note.getLastUpdatedOn().isBefore(before));
        assertFalse(note.getLastUpdatedOn().isAfter(OffsetDateTime.now()));

        // delete
        repository.deleteById(note.getId());
        note = repository.findById(note.getId()).orElse(null);
        assertNull(note);
    }

    @Test
    public void testFind() {

        NoteEntity note = new NoteEntity();
        note.setText("Lorem ipsum dolor sit amet");
        repository.save(note);

        for (String positive : Arrays.asList("Lorem", "IPSUM", "oLo", "dolor ips", "sit, lo", "'sit amet'", "'em IP")) {
            assertTrue("query: " + positive, repository.findAll(NoteRepository.fulltextSearch(positive)).contains(note));
        }

        for (String negative : Arrays.asList("L0rem", "QUIPSUM", "foo", "dolor ups", "sit# lo", "'sitamet'", "'lorem dolor")) {
            assertFalse("query: " + negative, repository.findAll(NoteRepository.fulltextSearch(negative)).contains(note));
        }
    }
}
