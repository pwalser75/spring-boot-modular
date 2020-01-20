package ch.frostnova.app.boot.platform.converter;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.time.Duration.ZERO;
import static java.time.temporal.ChronoUnit.*;

@Component
public class DurationConverter implements Converter<String, Duration> {

    private final static Pattern FORMAT = Pattern.compile("(-)?(?:([0-9]+)d)?(?:([0-9]+)h)?(?:([0-9]+)m)?(?:([0-9]+)s)?(?:([0-9]+)ms)?", Pattern.CASE_INSENSITIVE);

    @Override
    public Duration convert(String source) {
        if (source == null) {
            return null;
        }
        Matcher matcher = FORMAT.matcher(source);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Invalid duration format: " + source);
        }
        boolean negative = matcher.group(1) != null;
        int days = parseInt(matcher.group(2));
        int hours = parseInt(matcher.group(3));
        int minutes = parseInt(matcher.group(4));
        int seconds = parseInt(matcher.group(5));
        int milliseconds = parseInt(matcher.group(6));


        Duration duration = ZERO
                .plus(days, DAYS)
                .plus(hours, HOURS)
                .plus(minutes, MINUTES)
                .plus(seconds, SECONDS)
                .plus(milliseconds, MILLIS);

        return negative ? ZERO.minus(duration) : duration;
    }

    private int parseInt(String number) {
        if (number == null) {
            return 0;
        }
        number = number.trim();
        if (number.length() == 0) {
            return 0;
        }
        return Integer.parseInt(number);
    }
}
