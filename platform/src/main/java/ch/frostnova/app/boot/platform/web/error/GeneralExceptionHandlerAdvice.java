package ch.frostnova.app.boot.platform.web.error;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import javax.validation.ConstraintViolationException;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Exception handler advice.
 */
@ControllerAdvice
public class GeneralExceptionHandlerAdvice extends ResponseEntityExceptionHandler {

    private final static Pattern exceptionNamePattern = Pattern.compile("([A-Z][a-z0-9]+)");

    private final static Logger logger = LoggerFactory.getLogger(GeneralExceptionHandlerAdvice.class);

    public static void main(String[] args) {

        System.out.println(toErrorCode(new NullPointerException()));
        System.out.println(toErrorCode(new ArrayIndexOutOfBoundsException()));
        System.out.println(toErrorCode(new OutOfMemoryError()));
    }

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
    @Nullable
    protected ResponseEntity<Object> handleExceptionInternal(Exception ex, @Nullable Object body, HttpHeaders headers,
                                                             HttpStatus status, @NonNull WebRequest request) {
        ErrorResponse errorResponse = new ErrorResponse(status.name(), toErrorCode(ex), ex.getLocalizedMessage(), body);
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

    private static String toErrorCode(Throwable ex) {
        String simpleName = ex.getClass().getSimpleName();
        Matcher matcher = exceptionNamePattern.matcher(simpleName);
        List<String> tokens = new LinkedList<>();
        while (matcher.find()) {
            tokens.add(matcher.group(0));
        }
        if (tokens.isEmpty()) {
            return simpleName;
        }
        return tokens.stream().map(String::toUpperCase).collect(Collectors.joining("_"));
    }
}
