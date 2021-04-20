package ch.frostnova.module1.web;


import ch.frostnova.common.service.scope.TaskScope;
import ch.frostnova.module1.api.model.Note;
import ch.frostnova.module1.web.client.NoteClient;
import ch.frostnova.spring.boot.platform.model.UserInfo;
import ch.frostnova.spring.boot.platform.service.TokenAuthenticator;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
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

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

import static ch.frostnova.spring.boot.platform.model.UserInfo.aUserInfo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
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
            assertThat(created).isNotNull();
            assertThat(created.getId()).isNotNull();
            assertThat(created.getText()).isEqualTo(note.getText());
            String id = created.getId();
            note = created;

            // read

            Note loaded = noteClient.get(id);
            assertThat(loaded).isNotNull();
            assertThat(loaded.getId()).isNotNull();
            assertThat(loaded.getText()).isEqualTo(note.getText());

            // list

            assertTrue(noteClient.list().stream().anyMatch(p -> Objects.equals(p.getId(), id)));

            // update

            note.setText("Lorem ipsum dolor sit amet");
            noteClient.save(note);

            loaded = noteClient.get(id);
            assertThat(loaded).isNotNull();
            assertThat(loaded.getId()).isEqualTo(note.getId());
            assertThat(loaded.getText()).isEqualTo(note.getText());

            // delete

            noteClient.delete(id);

            // delete again - must not result in an exception
            noteClient.delete(id);

            // must not be found afterwards
            assertFalse(noteClient.list().stream().anyMatch(p -> Objects.equals(p.getId(), id)));

            Assertions.assertThatThrownBy(() -> noteClient.get(id)).isInstanceOf(NotFoundException.class);
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

            assertThat(noteClient.find("welcome").contains(note)).isTrue();
            assertThat(noteClient.find("aloha").contains(note)).isFalse();

            assertThat(noteClient.find(time).contains(note)).isTrue();
            assertThat(noteClient.find(random).contains(note)).isTrue();
            assertThat(noteClient.find(note.getText()).contains(note)).isTrue();
            assertTrue(noteClient.find("\"" + note.getText().substring(0, note.getText().length() / 2) + "\"").contains(note));

            assertThat(noteClient.find(UUID.randomUUID().toString()).contains(note)).isFalse();
        }
    }

    @Test
    public void testValidation() {
        try (NoteClient noteClient = new NoteClient(baseURL, testToken)) {
            assertThatThrownBy(() -> noteClient.create(new Note())).isInstanceOf(BadRequestException.class);
        }
    }
}
