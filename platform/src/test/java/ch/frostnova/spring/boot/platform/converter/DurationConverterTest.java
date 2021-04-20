package ch.frostnova.spring.boot.platform.converter;

import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.time.Duration.parse;
import static java.time.temporal.ChronoUnit.DAYS;
import static java.time.temporal.ChronoUnit.HOURS;
import static java.time.temporal.ChronoUnit.MILLIS;
import static java.time.temporal.ChronoUnit.MINUTES;
import static java.time.temporal.ChronoUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class DurationConverterTest {

    private final DurationConverter durationConverter = new DurationConverter();

    @Test
    public void testEmpty() {
        assertThat(durationConverter.convert(null)).isNull();
    }

    @Test
    public void testTrivial() {
        assertThat(durationConverter.convert("")).isEqualTo(Duration.ZERO);
    }

    @Test
    public void testConversionSuccessful() {
        assertThat(durationConverter.convert("123d")).isEqualTo(parse("P123D"));
        assertThat(durationConverter.convert("-123d")).isEqualTo(parse("P-123D"));

        assertThat(durationConverter.convert("12H34M56S")).isEqualTo(parse("PT12H34M56S"));
        assertThat(durationConverter.convert("-12H34M56S")).isEqualTo(parse("PT-12H-34M-56S"));

        assertThat(durationConverter.convert("1d2h3m4s500ms")).isEqualTo(parse("P1DT2H3M4.5S"));
        assertThat(durationConverter.convert("-1d2h3m4s500ms")).isEqualTo(parse("P-1DT-2H-3M-4.5S"));
    }

    @Test
    public void testIllegalFormat() {
        for (String s : List.of("foo", "123", "5.5d", "5d-3h", "77t", "1s2m3d")) {
            assertThatThrownBy(() -> durationConverter.convert(s)).isInstanceOf(IllegalArgumentException.class);
        }
    }

    @RepeatedTest(100)
    public void testConversionSuccessfulMonteCarlo() {

        ThreadLocalRandom rnd = ThreadLocalRandom.current();
        Supplier<Integer> randomOrEmpty = () -> rnd.nextBoolean() ? rnd.nextInt(0, 100) : null;

        boolean negative = rnd.nextBoolean();
        Integer d = randomOrEmpty.get();
        Integer h = randomOrEmpty.get();
        Integer m = randomOrEmpty.get();
        Integer s = randomOrEmpty.get();
        Integer ms = randomOrEmpty.get();

        Duration expected = Duration.ZERO
                .plus(numberOrZero(d), DAYS)
                .plus(numberOrZero(h), HOURS)
                .plus(numberOrZero(m), MINUTES)
                .plus(numberOrZero(s), SECONDS)
                .plus(numberOrZero(ms), MILLIS);
        if (negative) {
            expected = Duration.ZERO.minus(expected);
        }

        String text = (negative ? "-" : "") +
                Stream.of(suffixed(d, "d"), suffixed(h, "h"), suffixed(m, "m"), suffixed(s, "s"), suffixed(ms, "ms"))
                        .filter(Objects::nonNull)
                        .collect(Collectors.joining());

        assertThat(durationConverter.convert(text)).isEqualTo(expected);
    }

    private int numberOrZero(Integer n) {
        return Optional.ofNullable(n).orElse(0);
    }

    private String suffixed(Integer n, String suffix) {
        return n != null ? n + suffix : "";
    }
}
