package ch.frostnova.app.boot.config;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * Test for {@link ExampleProperties}
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class ConfigurationTest {

    private final static double EPSILON = 1e-23;

    @Autowired
    private ExampleProperties exampleProperties;

    @Test
    public void testConfigurationProperties() {

        Assert.assertNotNull(exampleProperties);
        Assert.assertEquals(123, exampleProperties.getA());
        Assert.assertEquals(123.456, exampleProperties.getB(), ConfigurationTest.EPSILON);
        Assert.assertTrue(exampleProperties.isC());
        Assert.assertEquals("Test", exampleProperties.getD());

        Assert.assertNotNull(exampleProperties.getE());
        Assert.assertEquals(3, exampleProperties.getE().size());
        Assert.assertTrue(exampleProperties.getE().contains("ONE"));
        Assert.assertTrue(exampleProperties.getE().contains("TWO"));
        Assert.assertTrue(exampleProperties.getE().contains("THREE"));

        Assert.assertNotNull(exampleProperties.getF());
        Assert.assertEquals(3, exampleProperties.getF().size());
        Assert.assertEquals("first", exampleProperties.getF().get(0));
        Assert.assertEquals("second", exampleProperties.getF().get(1));
        Assert.assertEquals("third", exampleProperties.getF().get(2));
    }

    @Test
    public void testDefaultProperties() {

        Assert.assertNotNull(exampleProperties);
        Assert.assertEquals(5, exampleProperties.getX());
        Assert.assertEquals(6.7, exampleProperties.getY(), ConfigurationTest.EPSILON);
        Assert.assertEquals("Aloha", exampleProperties.getZ());
    }
}
