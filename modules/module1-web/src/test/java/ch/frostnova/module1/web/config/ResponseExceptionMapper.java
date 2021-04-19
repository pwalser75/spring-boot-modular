package ch.frostnova.module1.web.config;

import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.InternalServerErrorException;
import jakarta.ws.rs.NotAllowedException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

/**
 * Exception mapper which maps the HTTP error status codes (4xx, 5xx) to appropriate exceptions.
 */
public final class ResponseExceptionMapper {

    private ResponseExceptionMapper() {

    }

    /**
     * Check the response: check if the status is one of the expected stati, and map error status codes to exceptions.
     *
     * @param response       response from web service call
     * @param expectedStatus expected status (or stati)
     * @return response
     * @throws WebApplicationException if the response status indicates an error (400 or higher)
     * @throws IllegalStateException   when the status is not one of the expected stati
     */
    public static Response check(Response response, int... expectedStatus) throws WebApplicationException {

        ResponseExceptionMapper.check(response);
        for (int status : expectedStatus) {
            if (response.getStatus() == status) {
                return response;
            }
        }
        throw new IllegalStateException("Unexpected return status: " + response.getStatus());
    }

    /**
     * Check the response: map error status codes to exceptions.
     *
     * @param response response from web service call
     * @throws WebApplicationException if the response status indicates an error (400 or higher).
     */
    private static void check(Response response) throws WebApplicationException {
        if (response == null) {
            throw new NullPointerException("response must not be null");
        }
        int status = response.getStatus();
        if (status >= 400) {
            if (status == 400) {
                throw new BadRequestException(response.readEntity(String.class));
            }
            if (status == 401) {
                throw new RuntimeException("Unauthorized: " + response.readEntity(String.class));
            }
            if (status == 403) {
                throw new ForbiddenException(response.readEntity(String.class));
            }
            if (status == 404) {
                throw new NotFoundException(response.readEntity(String.class));
            }
            if (status == 405) {
                throw new NotAllowedException(response.readEntity(String.class));
            }
            if (status == 500) {
                throw new InternalServerErrorException(response.readEntity(String.class));
            }
            throw new RuntimeException("Unexpected response: " + status + ", " + response.readEntity(String.class));
        }
    }
}
