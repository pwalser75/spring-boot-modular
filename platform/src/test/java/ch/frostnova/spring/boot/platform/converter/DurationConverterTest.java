package ch.frostnova.spring.boot.platform.converter;

import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

public class DurationConverterTest {

    private final DurationConverter durationConverter = new DurationConverter();

    @Test
    public void testEmpty() {
        assertNull(durationConverter.convert(null));

    }

    @Test
    public void testTrivial() {
        assertEquals(Duration.ZERO, durationConverter.convert(""));
    }

    @Test
    public void testConversionSuccessful() {
        assertEquals(Duration.parse("P123D"), durationConverter.convert("123d"));
        assertEquals(Duration.parse("P-123D"), durationConverter.convert("-123d"));

        assertEquals(Duration.parse("PT12H34M56S"), durationConverter.convert("12H34M56S"));
        assertEquals(Duration.parse("PT-12H-34M-56S"), durationConverter.convert("-12H34M56S"));

        assertEquals(Duration.parse("P1DT2H3M4.5S"), durationConverter.convert("1d2h3m4s500ms"));
        assertEquals(Duration.parse("P-1DT-2H-3M-4.5S"), durationConverter.convert("-1d2h3m4s500ms"));
    }

    @Test
    public void testIllegalFormat() {
        for (String s : List.of("foo", "123", "5.5d", "5d-3h", "77t", "1s2m3d")) {
            assertThrows(IllegalArgumentException.class, () -> durationConverter.convert(s));
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
                .plus(numberOrZero(d), ChronoUnit.DAYS)
                .plus(numberOrZero(h), ChronoUnit.HOURS)
                .plus(numberOrZero(m), ChronoUnit.MINUTES)
                .plus(numberOrZero(s), ChronoUnit.SECONDS)
                .plus(numberOrZero(ms), ChronoUnit.MILLIS);
        if (negative) {
            expected = Duration.ZERO.minus(expected);
        }

        String text = (negative ? "-" : "") +
                Stream.of(suffixed(d, "d"), suffixed(h, "h"), suffixed(m, "m"), suffixed(s, "s"), suffixed(ms, "ms"))
                        .filter(Objects::nonNull)
                        .collect(Collectors.joining());

        assertEquals(expected, durationConverter.convert(text));
    }

    private int numberOrZero(Integer n) {
        return Optional.ofNullable(n).orElse(0);
    }

    private String suffixed(Integer n, String suffix) {
        return n != null ? n + suffix : "";
    }

    @Test
    public void foo() {
        System.out.println(OffsetDateTime.now());
    }
}
