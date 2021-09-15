package ch.frostnova.common.service.persistence;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test for {@link IdGenerator}
 *
 * @author pwalser
 * @since 27.05.2018.
 */
public class IdGeneratorTest {

    @Test
    public void testIdGenerator() {

        IdGenerator idGenerator = new IdGenerator();

        int numberOfIdsToGenerate = 1000;

        long numberOfDistinctIds = IntStream.range(0, numberOfIdsToGenerate)
                .mapToObj(i -> idGenerator.generate(null, null))
                .peek(System.out::println)
                .peek(Assertions::assertNotNull)
                .peek(id -> assertTrue(id.toString().length() >= 10))
                .distinct()
                .count();
        assertThat(numberOfDistinctIds).isEqualTo(numberOfIdsToGenerate);
    }
}
