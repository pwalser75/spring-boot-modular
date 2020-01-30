package ch.frostnova.module1.web;


import ch.frostnova.app.boot.platform.model.UserInfo;
import ch.frostnova.app.boot.platform.service.TokenAuthenticator;
import ch.frostnova.common.service.scope.TaskScope;
import ch.frostnova.module1.api.model.Note;
import ch.frostnova.module1.web.client.NoteClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.actuate.metrics.MetricsEndpoint;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.NotFoundException;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

import static ch.frostnova.app.boot.platform.model.UserInfo.aUserInfo;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

/**
 * Note endpoint test
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles({"test", "performance-logging"})
public class NoteEndpointTest {

    private final Logger log = LoggerFactory.getLogger(getClass());

    @LocalServerPort
    private int port;

    @MockBean
    private MetricsEndpoint metricsEndpoint;

    @MockBean
    private TokenAuthenticator tokenAuthenticator;

    private String baseURL;

    private String testToken;

    @BeforeEach
    public void setup() {
        TaskScope.init();
        baseURL = "https://localhost:" + port;
        log.info("BASE URL: " + baseURL);

        testToken = UUID.randomUUID().toString();
        UserInfo testUser = aUserInfo().tenant("test-tenant").login("test-user").role("A").role("B").role("C").build();
        when(tokenAuthenticator.authenticate(Mockito.eq(testToken))).thenReturn(testUser);
    }

    @AfterEach
    public void cleanup() {
        TaskScope.destroy();
    }

    @Test
    public void testCRUD() {

        try (NoteClient noteClient = new NoteClient(baseURL, testToken)) {

            // create

            Note note = new Note();
            note.setText("Aloha");

            Note created = noteClient.create(note);
            assertNotNull(created);
            assertNotNull(created.getId());
            assertEquals(note.getText(), created.getText());
            String id = created.getId();
            note = created;

            // read

            Note loaded = noteClient.get(id);
            assertNotNull(loaded);
            assertNotNull(loaded.getId());
            assertEquals(note.getText(), loaded.getText());

            // list

            assertTrue(noteClient.list().stream().anyMatch(p -> Objects.equals(p.getId(), id)));

            // update

            note.setText("Lorem ipsum dolor sit amet");
            noteClient.save(note);

            loaded = noteClient.get(id);
            assertNotNull(loaded);
            assertEquals(note.getId(), loaded.getId());
            assertEquals(note.getText(), loaded.getText());

            // delete

            noteClient.delete(id);

            // delete again - must not result in an exception
            noteClient.delete(id);

            // must not be found afterwards
            assertFalse(noteClient.list().stream().anyMatch(p -> Objects.equals(p.getId(), id)));

            Assertions.assertThrows(NotFoundException.class, () -> noteClient.get(id));
        }
    }

    @Test
    public void testFind() {
        try (NoteClient noteClient = new NoteClient(baseURL, testToken)) {

            // create

            Note note = new Note();
            String time = LocalDateTime.now().toString();
            String random = UUID.randomUUID().toString();
            note.setText("Welcome to Switzerland " + time + ", " + random);
            note = noteClient.create(note);

            assertTrue(noteClient.find("welcome").contains(note));
            assertFalse(noteClient.find("aloha").contains(note));

            assertTrue(noteClient.find(time).contains(note));
            assertTrue(noteClient.find(random).contains(note));
            assertTrue(noteClient.find(note.getText()).contains(note));
            assertTrue(noteClient.find("\"" + note.getText().substring(0, note.getText().length() / 2) + "\"").contains(note));

            assertFalse(noteClient.find(UUID.randomUUID().toString()).contains(note));
        }
    }

    @Test
    public void testValidation() {
        try (NoteClient noteClient = new NoteClient(baseURL, testToken)) {
            assertThrows(BadRequestException.class, () -> noteClient.create(new Note()));
        }
    }
}
