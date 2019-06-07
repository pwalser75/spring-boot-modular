package ch.frostnova.app.boot.platform.web.error;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;

/**
 * General error response
 *
 * @author wap
 * @since 07.06.2019
 */
public class ErrorResponse {

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

    public ErrorResponse(String errorCode, String error, String message, Object details) {
        this.timestamp = OffsetDateTime.now().truncatedTo(ChronoUnit.MILLIS);
        this.errorCode = errorCode;
        this.error = error;
        this.message = message;
        this.details = details;
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
