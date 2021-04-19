package ch.frostnova.module1.web.client;

import ch.frostnova.module1.api.model.Note;
import ch.frostnova.module1.web.config.ResponseExceptionMapper;
import ch.frostnova.module1.web.config.RestClientConfig;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.Invocation;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.Response;

import java.util.List;

/**
 * Note client API
 */
public class NoteClient implements AutoCloseable {

    private final static String PATH = "/api/notes";

    private final String baseURL;
    private final String authToken;
    private final Client client;

    public NoteClient(String baseURL, String authToken) {
        this.baseURL = baseURL + PATH;
        this.authToken = authToken;
        client = RestClientConfig.clientBuilder().build();
    }

    @Override
    public void close() {
        client.close();
    }

    private Invocation.Builder authenticated(Invocation.Builder builder) {
        return (authToken != null) ? builder.header("Authorization", "Bearer " + authToken) : builder;
    }

    /**
     * List all notes
     *
     * @return list of notes (never null)
     */
    public List<Note> list() {
        Invocation invocation = authenticated(client
                .target(baseURL)
                .request())
                .buildGet();

        Response response = ResponseExceptionMapper.check(invocation.invoke(), 200);
        return response.readEntity(new GenericType<>() {
        });
    }

    /**
     * Find notes
     *
     * @return search result
     */
    public List<Note> find(String query) {
        Invocation invocation = authenticated(client
                .target(baseURL)
                .queryParam("query", query)
                .request())
                .buildGet();

        Response response = ResponseExceptionMapper.check(invocation.invoke(), 200);
        return response.readEntity(new GenericType<>() {
        });
    }

    /**
     * Get a note by id. Throws a {@link NotFoundException} if the note wasn't found.
     *
     * @param id id
     * @return note.
     */
    public Note get(String id) {

        Invocation invocation = authenticated(client
                .target(baseURL + "/" + id)
                .request())
                .buildGet();

        Response response = ResponseExceptionMapper.check(invocation.invoke(), 200);
        return response.readEntity(Note.class);
    }

    /**
     * Create a new note with the provided data
     *
     * @param note data
     * @return created note
     */
    public Note create(Note note) {
        Invocation invocation = authenticated(client
                .target(baseURL)
                .request())
                .buildPost(Entity.json(note));

        Response response = ResponseExceptionMapper.check(invocation.invoke(), 201);
        return response.readEntity(Note.class);
    }

    /**
     * Update a note
     *
     * @param note note (whose id is required)
     */
    public void save(Note note) {

        if (note.getId() == null) {
            throw new IllegalArgumentException("Not yet persisted, use the create() method instead");
        }

        Invocation invocation = authenticated(client
                .target(baseURL + "/" + note.getId())
                .request())
                .buildPut(Entity.json(note));

        ResponseExceptionMapper.check(invocation.invoke(), 204);
    }

    /**
     * Delete the note with the given id, if it exists (no error thrown otherwise).
     *
     * @param id id of the record
     */
    public void delete(String id) {

        Invocation invocation = authenticated(client
                .target(baseURL + "/" + id)
                .request())
                .buildDelete();

        ResponseExceptionMapper.check(invocation.invoke(), 204);
    }

}
