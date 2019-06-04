package ch.frostnova.app.boot.platform.web.error;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;

import javax.validation.ConstraintViolation;
import javax.validation.Path;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Validation error
 */
public class ValidationError {

    private final static Pattern STANDARD_MESSAGE = Pattern.compile("\\{javax.validation.constraints.(\\w+).message\\}");

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

        Matcher matcher = ValidationError.STANDARD_MESSAGE.matcher(constraintViolation.getMessageTemplate());
        errorCode = matcher.matches() ? matcher.group(1) : constraintViolation.getMessageTemplate();

        message = constraintViolation.getMessage();

        String type = constraintViolation.getLeafBean().getClass().getSimpleName();
        String field = null;
        for (Path.Node node : constraintViolation.getPropertyPath()) {
            field = node.getName();
        }
        path = type + "." + field;

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
