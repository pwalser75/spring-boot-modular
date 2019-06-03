package ch.frostnova.module1.service.persistence;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.NoSuchElementException;

/**
 * Test JPA repository
 */
@RunWith(SpringRunner.class)
@SpringBootTest
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
}
