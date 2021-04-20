package ch.frostnova.module1.service;

import ch.frostnova.common.api.exception.ResourceNotFoundException;
import ch.frostnova.common.service.scope.TaskScope;
import ch.frostnova.module1.api.model.Note;
import ch.frostnova.module1.api.service.NoteService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.validation.ValidationException;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(SpringExtension.class)
@DataJpaTest
@ActiveProfiles("test")
@ContextConfiguration(classes = TestConfig.class)
public class NoteServiceTest {

    @Autowired
    private NoteService noteService;

    @BeforeEach
    public void setup() {
        TaskScope.init();
    }

    @AfterEach
    public void cleanup() {
        TaskScope.destroy();
    }

    @Test
    public void testCreate() {

        String text = UUID.randomUUID().toString();
        Note note = new Note(text);

        Note saved = noteService.save(note);
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getCreated()).isNotNull();
        assertThat(saved.getCreated().isAfter(OffsetDateTime.now())).isFalse();
        assertThat(saved.getUpdated()).isNotNull();
        assertThat(saved.getUpdated().isAfter(OffsetDateTime.now())).isFalse();
        assertThat(saved.getUpdated().isAfter(saved.getCreated())).isFalse();

        assertThat(saved.getText()).isEqualTo(note.getText());
    }

    @Test
    public void testValidateTextRequired() {

        Note note = new Note();
        assertThatThrownBy(() -> noteService.save(note)).isInstanceOf(ValidationException.class);
    }

    @Test
    public void testValidateTextNotBlank() {

        Note note = new Note("");
        assertThatThrownBy(() -> noteService.save(note)).isInstanceOf(ValidationException.class);
    }

    @Test
    public void testValidateTextTooLong() {

        String text = ThreadLocalRandom.current()
                .ints('a', 'z' + 1)
                .limit(3000)
                .mapToObj(c -> (char) c)
                .map(String::valueOf)
                .collect(Collectors.joining());
        Note note = new Note(text);
        assertThatThrownBy(() -> noteService.save(note)).isInstanceOf(ValidationException.class);
    }

    @Test
    public void testLoad() {

        Note note = new Note(UUID.randomUUID().toString());
        note = noteService.save(note);

        Note loaded = noteService.get(note.getId());

        assertThat(loaded).isEqualTo(note);
        assertThat(loaded.getText()).isEqualTo(note.getText());
    }

    @Test
    public void testList() {

        Note note = new Note(UUID.randomUUID().toString());
        note = noteService.save(note);

        List<Note> list = noteService.list();
        assertThat(list.contains(note)).isTrue();
    }

    @Test
    public void testUpdate() {

        Note note = new Note(UUID.randomUUID().toString());
        note = noteService.save(note);

        note.setText(UUID.randomUUID().toString());
        noteService.save(note);

        Note updated = noteService.get(note.getId());
        assertThat(updated).isEqualTo(note);
        assertThat(updated.getText()).isEqualTo(note.getText());
    }

    @Test
    public void testDelete() {

        Note note = new Note(UUID.randomUUID().toString());
        String id = noteService.save(note).getId();

        noteService.delete(id);

        assertThatThrownBy(() -> noteService.get(id)).isInstanceOf(ResourceNotFoundException.class);
        assertThat(noteService.list().stream().anyMatch(n -> n.getId().equals(id))).isFalse();
    }

    @Test
    public void testFind() {

        Note note = noteService.save(new Note("Lorem ipsum dolor sit amet"));

        for (String positive : Arrays.asList("Lorem", "IPSUM", "oLo", "dolor ips", "sit, lo", "'sit amet'", "'em IP")) {
            assertTrue(noteService.find(positive).contains(note), "query: " + positive);
        }

        for (String negative : Arrays.asList("L0rem", "QUIPSUM", "foo", "dolor ups", "sit# lo", "'sitamet'", "'lorem dolor")) {
            assertFalse(noteService.find(negative).contains(note), "query: " + negative);
        }
    }
}
