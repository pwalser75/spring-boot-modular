package ch.frostnova.module1.service.persistence;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

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
        note = repository.save(note);
        Assert.assertTrue(note.isPersistent());
        Assert.assertNotNull(note.getId());

        // read
        note = repository.findById(note.getId()).orElseThrow(NoSuchElementException::new);
        Assert.assertEquals("Aloha", note.getText());

        // update
        note.setText("Lorem ipsum dolor sit amet");
        note = repository.save(note);

        // delete
        repository.deleteById(note.getId());
        note = repository.findById(note.getId()).orElse(null);
        Assert.assertNull(note);
    }
}
