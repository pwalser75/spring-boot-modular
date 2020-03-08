package ch.frostnova.common.api.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class BaseResourceTest {

    @Test
    public void testEqualsWithNewResources() {

        BaseResource newResource = new ExampleResource();
        BaseResource otherNewResource = new ExampleResource();
        BaseResource newResourceDifferentType = new OtherExampleResource();
        BaseResource existingResource = new ExampleResource(123L);

        assertEqualsAndHashcode(newResource, newResource);
        assertNotEqualsAndHashcode(newResource, otherNewResource);
        assertNotEqualsAndHashcode(newResource, existingResource);
        assertNotEqualsAndHashcode(newResource, newResourceDifferentType);
    }

    @Test
    public void testEqualsWithExistingResources() {

        BaseResource resource = new ExampleResource(123L);
        BaseResource sameResource = new ExampleResource(123L);
        BaseResource otherResourceSameTypeDifferentId = new ExampleResource(456L);
        BaseResource otherResourceSameIdDifferentType = new OtherExampleResource(123L);
        BaseResource newResource = new ExampleResource();

        assertEqualsAndHashcode(resource, resource);
        assertEqualsAndHashcode(resource, sameResource);

        assertNotEqualsAndHashcode(resource, newResource);
        assertNotEqualsAndHashcode(resource, otherResourceSameTypeDifferentId);
        assertNotEqualsAndHashcode(resource, otherResourceSameIdDifferentType);
    }

    private static class ExampleResource extends BaseResource<Long> {

        public ExampleResource() {
        }

        public ExampleResource(Long id) {
            setId(id);
        }
    }

    private static class OtherExampleResource extends BaseResource<Long> {

        public OtherExampleResource() {
        }

        public OtherExampleResource(Long id) {
            setId(id);
        }
    }

    private void assertEqualsAndHashcode(Object a, Object b) {
        assertNotNull(a);
        assertNotNull(b);
        assertTrue(a.equals(b));
        assertTrue(b.equals(a));
        assertTrue(a.hashCode() == b.hashCode());
    }

    private void assertNotEqualsAndHashcode(Object a, Object b) {
        if (a != null) {
            assertFalse(a.equals(b));
        }
        if (b != null) {
            assertFalse(b.equals(a));
        }
    }
}
