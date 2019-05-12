package ch.frostnova.module1.api.exception;

/**
 * Exception indicating that a resource was not found
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException() {
        this("Not found");
    }

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
