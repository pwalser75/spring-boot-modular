package ch.frostnova.spring.boot.platform.web.error;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import javax.validation.ConstraintViolationException;
import java.util.NoSuchElementException;

/**
 * Exception handler advice.
 */
@ControllerAdvice
public class GeneralExceptionHandlerAdvice extends ResponseEntityExceptionHandler {


    private final static Logger logger = LoggerFactory.getLogger(GeneralExceptionHandlerAdvice.class);

    @ExceptionHandler(NoSuchElementException.class)
    protected ResponseEntity<Object> handleNotFound(NoSuchElementException ex, WebRequest request) {
        return handleExceptionInternal(ex, ex.getMessage(), new HttpHeaders(), HttpStatus.NOT_FOUND, request);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    protected ResponseEntity<Object> handleBadRequest(IllegalArgumentException ex, WebRequest request) {
        return handleExceptionInternal(ex, ex.getMessage(), new HttpHeaders(), HttpStatus.BAD_REQUEST, request);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    protected ResponseEntity<Object> handleBadRequest(ConstraintViolationException ex, WebRequest request) {
        ValidationErrors errors = new ValidationErrors(ex);
        return handleExceptionInternal(ex, errors.getErrors(), new HttpHeaders(), HttpStatus.BAD_REQUEST, request);
    }

    @ExceptionHandler(AuthenticationException.class)
    protected ResponseEntity<Object> handleAuthenticationException(AuthenticationException ex, WebRequest request) {
        return handleExceptionInternal(ex, ex.getMessage(), new HttpHeaders(), HttpStatus.FORBIDDEN, request);
    }

    @ExceptionHandler(Exception.class)
    protected ResponseEntity<Object> handleUnknownException(Exception ex, WebRequest request) {
        return handleExceptionInternal(ex, ex.getMessage(), new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR, request);
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                                  HttpHeaders headers, HttpStatus status, WebRequest request) {
        ValidationErrors errors = new ValidationErrors(ex.getBindingResult());
        return handleExceptionInternal(ex, errors.getErrors(), new HttpHeaders(), HttpStatus.BAD_REQUEST, request);
    }

    @Override
    protected ResponseEntity<Object> handleExceptionInternal(Exception ex, Object body, HttpHeaders headers,
                                                             HttpStatus status, WebRequest request) {
        ErrorResponse errorResponse = new ErrorResponse(status.name(), ex, body);
        String logMessage = ex.getClass().getSimpleName() + ": " + ex.getMessage();
        if (status.is5xxServerError()) {
            logger.error(logMessage, ex);
        }
        if (status.is4xxClientError()) {
            if (logger.isDebugEnabled()) {
                logger.debug(logMessage, ex);
            } else {
                logger.info(logMessage);
            }
        }
        return super.handleExceptionInternal(ex, errorResponse, headers, status, request);
    }
}
