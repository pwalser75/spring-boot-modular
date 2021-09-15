package ch.frostnova.module1.web.client;

import ch.frostnova.module1.web.config.ResponseExceptionMapper;
import ch.frostnova.module1.web.config.RestClientConfig;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.Invocation;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.Response;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

/**
 * Note client API
 */
public class LoginClient implements AutoCloseable {

    private final static String PATH = "/login";

    private final String baseURL;
    private final Client client;

    public LoginClient(String baseURL) {
        this.baseURL = baseURL + PATH;
        client = RestClientConfig.clientBuilder().build();
    }

    @Override
    public void close() {
        client.close();
    }

    /**
     * List all notes
     *
     * @return list of notes (never null)
     */
    public String login(String tenant, String login, Set<String> roles) {
        Invocation invocation = client
                .target(String.join("/", baseURL, tenant, login))
                .queryParam("roles", Optional.ofNullable(roles).orElseGet(HashSet::new).toArray())
                .request()
                .buildGet();

        Response response = ResponseExceptionMapper.check(invocation.invoke(), 200);
        return response.readEntity(new GenericType<>() {
        });
    }
}
