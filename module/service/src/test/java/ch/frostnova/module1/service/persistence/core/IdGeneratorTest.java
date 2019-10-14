package ch.frostnova.module1.service.persistence.core;

import org.junit.Test;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

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

        Set<Serializable> generatedIds = new HashSet<>();
        for (int i = 0; i < 1000; i++) {

            Serializable id = idGenerator.generate(null, null);
            assertNotNull(id);
            assertFalse(generatedIds.contains(id));
            generatedIds.add(id);
            System.out.println(id);
        }
    }
}
