package ch.frostnova.module1.web;


import ch.frostnova.module1.api.model.Note;
import ch.frostnova.module1.web.client.NoteClient;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.NotFoundException;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Note endpoint test
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("performance-logging")
public class NoteEndpointTest {

    private final Logger log = LoggerFactory.getLogger(getClass());

    @LocalServerPort
    private int port;

    @Test
    public void testCRUD() {

        final String baseURL = "https://localhost:" + port + "/api/notes";
        log.info("BASE URL: " + baseURL);
        try (final NoteClient noteClient = new NoteClient(baseURL)) {

            // create

            Note note = new Note();
            note.setText("Aloha");

            Note created = noteClient.create(note);
            Assert.assertNotNull(created);
            Assert.assertNotNull(created.getId());
            Assert.assertEquals(note.getText(), created.getText());
            String id = created.getId();
            note = created;

            // read

            Note loaded = noteClient.get(id);
            Assert.assertNotNull(loaded);
            Assert.assertNotNull(loaded.getId());
            Assert.assertEquals(note.getText(), loaded.getText());

            // list

            Assert.assertTrue(noteClient.list().stream().anyMatch(p -> Objects.equals(p.getId(), id)));

            // update

            note.setText("Lorem ipsum dolor sit amet");
            noteClient.save(note);

            loaded = noteClient.get(id);
            Assert.assertNotNull(loaded);
            Assert.assertEquals(note.getId(), loaded.getId());
            Assert.assertEquals(note.getText(), loaded.getText());

            // delete

            noteClient.delete(id);

            // delete again - must not result in an exception
            noteClient.delete(id);

            // must not be found afterwards
            Assert.assertFalse(noteClient.list().stream().anyMatch(p -> Objects.equals(p.getId(), id)));

            try {
                noteClient.get(id);
                Assert.fail("Expected: NotFoundException");
            } catch (NotFoundException expected) {
                //
            }
        }
    }

    @Test
    public void testFind() {
        final String baseURL = "https://localhost:" + port + "/api/notes";
        log.info("BASE URL: " + baseURL);
        try (final NoteClient noteClient = new NoteClient(baseURL)) {

            // create

            Note note = new Note();
            String time = LocalDateTime.now().toString();
            String random = UUID.randomUUID().toString();
            note.setText("Welcome to Switzerland " + time + ", " + random);
            note = noteClient.create(note);

            Assert.assertTrue(noteClient.find("welcome").contains(note));
            Assert.assertFalse(noteClient.find("aloha").contains(note));

            Assert.assertTrue(noteClient.find(time).contains(note));
            Assert.assertTrue(noteClient.find(random).contains(note));
            Assert.assertTrue(noteClient.find(note.getText()).contains(note));
            Assert.assertTrue(noteClient.find("\"" + note.getText().substring(0, note.getText().length() / 2) + "\"").contains(note));

            Assert.assertFalse(noteClient.find(UUID.randomUUID().toString()).contains(note));
        }
    }

    @Test(expected = BadRequestException.class)
    public void testValidation() {
        final String baseURL = "https://localhost:" + port + "/api/notes";
        log.info("BASE URL: " + baseURL);
        try (final NoteClient noteClient = new NoteClient(baseURL)) {
            noteClient.create(new Note());
        }
    }
}
