package ch.frostnova.module1.web.client;

import ch.frostnova.module1.web.config.ResponseExceptionMapper;
import ch.frostnova.module1.web.config.RestClientConfig;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
                .target(Stream.of(baseURL, tenant, login).collect(Collectors.joining("/")))
                .queryParam("roles", Optional.ofNullable(roles).orElseGet(HashSet::new).toArray())
                .request()
                .buildGet();

        Response response = ResponseExceptionMapper.check(invocation.invoke(), 200);
        return response.readEntity(new GenericType<>() {
        });
    }
}
