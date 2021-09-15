package ch.frostnova.spring.boot.platform.web.error;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.util.LinkedList;
import java.util.List;

/**
 * Validation errors
 */
public class ValidationErrors {

    @JsonProperty("errors")
    private final List<ValidationError> errors = new LinkedList<>();

    public ValidationErrors(ConstraintViolationException ex) {
        for (ConstraintViolation<?> error : ex.getConstraintViolations()) {
            errors.add(new ValidationError(error));
        }
    }

    public ValidationErrors(BindingResult bindingResult) {
        for (ObjectError error : bindingResult.getAllErrors()) {
            errors.add(new ValidationError(error));
        }
    }

    public List<ValidationError> getErrors() {
        return errors;
    }
}
