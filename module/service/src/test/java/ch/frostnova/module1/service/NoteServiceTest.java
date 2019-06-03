package ch.frostnova.module1.service;

import ch.frostnova.module1.api.exception.ResourceNotFoundException;
import ch.frostnova.module1.api.model.Note;
import ch.frostnova.module1.api.service.NoteService;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.validation.ValidationException;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@RunWith(SpringRunner.class)
@SpringBootTest
public class NoteServiceTest {

    @Autowired
    private NoteService noteService;

    @Test
    public void testCreate() {

        String text = UUID.randomUUID().toString();
        Note note = new Note(text);

        Note saved = noteService.save(note);
        Assert.assertNotNull(saved.getId());
        Assert.assertNotNull(saved.getCreatedOn());
        Assert.assertFalse(saved.getCreatedOn().isAfter(OffsetDateTime.now()));
        Assert.assertNotNull(saved.getUpdatedOn());
        Assert.assertFalse(saved.getUpdatedOn().isAfter(OffsetDateTime.now()));
        Assert.assertFalse(saved.getUpdatedOn().isAfter(saved.getCreatedOn()));

        Assert.assertEquals(note.getText(), saved.getText());
    }

    @Test(expected = ValidationException.class)
    public void testValidateTextRequired() {

        Note note = new Note();
        noteService.save(note);
    }

    @Test(expected = ValidationException.class)
    public void testValidateTextNotBlank() {

        Note note = new Note("");
        noteService.save(note);
    }

    @Test(expected = ValidationException.class)
    public void testValidateTextTooLong() {

        String text = ThreadLocalRandom.current()
                .ints('a', 'z' + 1)
                .limit(3000)
                .mapToObj(c -> (char) c)
                .map(String::valueOf)
                .collect(Collectors.joining());
        Note note = new Note(text);
        noteService.save(note);
    }

    @Test
    public void testLoad() {

        Note note = new Note(UUID.randomUUID().toString());
        note = noteService.save(note);

        Note loaded = noteService.get(note.getId());

        Assert.assertEquals(note, loaded);
        Assert.assertEquals(note.getText(), loaded.getText());
    }

    @Test
    public void testList() {

        Note note = new Note(UUID.randomUUID().toString());
        note = noteService.save(note);

        List<Note> list = noteService.list();
        Assert.assertTrue(list.contains(note));
    }

    @Test
    public void testUpdate() {

        Note note = new Note(UUID.randomUUID().toString());
        note = noteService.save(note);

        note.setText(UUID.randomUUID().toString());
        noteService.save(note);

        Note updated = noteService.get(note.getId());
        Assert.assertEquals(note, updated);
        Assert.assertEquals(note.getText(), updated.getText());
    }

    @Test
    public void testDelete() {

        Note note = new Note(UUID.randomUUID().toString());
        Long id = noteService.save(note).getId();

        noteService.delete(id);
        try {
            noteService.get(id);
            Assert.fail("Expected " + ResourceNotFoundException.class.getName());
        } catch (ResourceNotFoundException expected) {

        }
        Assert.assertFalse(noteService.list().stream().anyMatch(n -> n.getId().equals(id)));
    }


}
