package ch.frostnova.app.boot.platform.web.error;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;

import javax.validation.ConstraintViolation;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Validation error
 */
public class ValidationError {

    @JsonProperty("path")
    private final String path;

    @JsonProperty("code")
    private final String errorCode;

    @JsonProperty("message")
    private final String message;

    public ValidationError(ObjectError objectError) {

        String model = objectError.getObjectName();
        String field = objectError instanceof FieldError ? ((FieldError) objectError).getField() : null;

        path = Stream.of(model, field).filter(Objects::nonNull).collect(Collectors.joining(":"));
        errorCode = objectError.getCode();
        message = objectError.getDefaultMessage();
    }

    public ValidationError(ConstraintViolation<?> constraintViolation) {

        path = constraintViolation.getPropertyPath().toString();
        errorCode = constraintViolation.getMessageTemplate();
        message = constraintViolation.getMessage();

    }

    public String getPath() {
        return path;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public String getMessage() {
        return message;
    }
}
