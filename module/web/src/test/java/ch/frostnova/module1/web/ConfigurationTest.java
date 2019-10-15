package ch.frostnova.module1.web;

import ch.frostnova.module1.web.config.ExampleProperties;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test for {@link ExampleProperties}
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = ExampleProperties.class)
@EnableConfigurationProperties
public class ConfigurationTest {

    private final static double EPSILON = 1e-23;

    @Autowired
    private ExampleProperties exampleProperties;

    @Test
    public void testConfigurationProperties() {

        assertNotNull(exampleProperties);
        assertEquals(123, exampleProperties.getA());
        assertEquals(123.456, exampleProperties.getB(), ConfigurationTest.EPSILON);
        assertTrue(exampleProperties.isC());
        assertEquals("Test", exampleProperties.getD());

        assertNotNull(exampleProperties.getE());
        assertEquals(3, exampleProperties.getE().size());
        assertTrue(exampleProperties.getE().contains("ONE"));
        assertTrue(exampleProperties.getE().contains("TWO"));
        assertTrue(exampleProperties.getE().contains("THREE"));

        assertNotNull(exampleProperties.getF());
        assertEquals(3, exampleProperties.getF().size());
        assertEquals("first", exampleProperties.getF().get(0));
        assertEquals("second", exampleProperties.getF().get(1));
        assertEquals("third", exampleProperties.getF().get(2));
    }

    @Test
    public void testDefaultProperties() {

        assertNotNull(exampleProperties);
        assertEquals(5, exampleProperties.getX());
        assertEquals(6.7, exampleProperties.getY(), ConfigurationTest.EPSILON);
        assertEquals("Aloha", exampleProperties.getZ());
    }
}
