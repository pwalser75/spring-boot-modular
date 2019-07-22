package ch.frostnova.module1.service.persistence;

import ch.frostnova.module1.service.TestConfig;
import org.junit.Assert;
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

        Assert.assertFalse(note.isPersistent());
        OffsetDateTime before = OffsetDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        note = repository.save(note);
        Assert.assertTrue(note.isPersistent());
        Assert.assertNotNull(note.getId());
        Assert.assertNotNull(note.getCreatedOn());
        Assert.assertFalse(note.getCreatedOn().isBefore(before));
        Assert.assertFalse(note.getCreatedOn().isAfter(OffsetDateTime.now()));
        Assert.assertNotNull(note.getLastUpdatedOn());
        Assert.assertFalse(note.getLastUpdatedOn().isBefore(before));
        Assert.assertFalse(note.getLastUpdatedOn().isAfter(OffsetDateTime.now()));
        OffsetDateTime creationDate = note.getCreatedOn();

        // read
        note = repository.findById(note.getId()).orElseThrow(NoSuchElementException::new);
        Assert.assertEquals("Aloha", note.getText());

        // update
        note.setText("Lorem ipsum dolor sit amet");
        note = repository.save(note);
        before = OffsetDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        Assert.assertEquals(creationDate, note.getCreatedOn());
        Assert.assertNotNull(note.getLastUpdatedOn());
        Assert.assertFalse(note.getLastUpdatedOn().isBefore(before));
        Assert.assertFalse(note.getLastUpdatedOn().isAfter(OffsetDateTime.now()));

        // delete
        repository.deleteById(note.getId());
        note = repository.findById(note.getId()).orElse(null);
        Assert.assertNull(note);
    }

    @Test
    public void testFind() {

        NoteEntity note = new NoteEntity();
        note.setText("Lorem ipsum dolor sit amet");
        repository.save(note);

        for (String positive : Arrays.asList("Lorem", "IPSUM", "oLo", "dolor ips", "sit, lo", "'sit amet'", "'em IP")) {
            Assert.assertTrue("query: " + positive, repository.findAll(NoteRepository.fulltextSearch(positive)).contains(note));
        }

        for (String negative : Arrays.asList("L0rem", "QUIPSUM", "foo", "dolor ups", "sit# lo", "'sitamet'", "'lorem dolor")) {
            Assert.assertFalse("query: " + negative, repository.findAll(NoteRepository.fulltextSearch(negative)).contains(note));
        }
    }
}
