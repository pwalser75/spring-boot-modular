package ch.frostnova.spring.boot.platform.web.error;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * General error response
 *
 * @author wap
 * @since 07.06.2019
 */
public class ErrorResponse {

    private final static Pattern exceptionNamePattern = Pattern.compile("([A-Z][a-z0-9]+)");

    @JsonProperty("timestamp")
    private final OffsetDateTime timestamp;

    @JsonProperty("errorCode")
    private final String errorCode;

    @JsonProperty("error")
    private final String error;

    @JsonProperty("message")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private final String message;

    @JsonProperty("reason")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private final Object details;

    public ErrorResponse(String errorCode, Exception ex, Object details) {
        timestamp = OffsetDateTime.now().truncatedTo(ChronoUnit.MILLIS);
        this.errorCode = errorCode;
        this.details = details;
        error = toErrorCode(ex);
        message = ex.getLocalizedMessage();
    }

    public static String toErrorCode(Throwable ex) {
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

    public OffsetDateTime getTimestamp() {
        return timestamp;
    }

    public String getError() {
        return error;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public String getMessage() {
        return message;
    }

    public Object getDetails() {
        return details;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{" +
                "timestamp=" + timestamp +
                ", error='" + error + '\'' +
                ", errorCode='" + errorCode + '\'' +
                ", message='" + message + '\'' +
                ", details=" + details +
                '}';
    }
}
