package ch.frostnova.common.service.scope;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {TaskScopeConfig.class, TaskScopedComponent.class})
public class TaskScopeTest {

    @Autowired
    private TaskScopedComponent taskScopedComponent;

    @BeforeEach
    @AfterEach
    public void prepareScopeInactive() {
        if (TaskScope.isActive()) {
            TaskScope.destroy();
        }
        assertFalse(TaskScope.isActive());
    }

    @Test
    public void testScopeNotActive() {
        assertFalse(TaskScope.isActive());
        assertNull(TaskScope.currentConversationId());
        assertThrows(BeanCreationException.class, () -> taskScopedComponent.getUuid());
    }

    @Test
    public void testScopeActive() {
        TaskScope.init();
        try {
            assertTrue(TaskScope.isActive());
            assertNotNull(TaskScope.currentConversationId());

            assertNotNull(taskScopedComponent);
            assertNotNull(taskScopedComponent.getUuid());

            System.out.println(taskScopedComponent.getUuid());
        } finally {
            TaskScope.destroy();
        }
    }

    @Test
    public void testScopeDoubleInit() {
        TaskScope.init();
        assertThrows(IllegalStateException.class, () -> TaskScope.init());
    }

    @Test
    public void testDestroyNotActive() {
        assertFalse(TaskScope.isActive());
        assertThrows(IllegalStateException.class, () -> TaskScope.destroy());
    }

    @Test
    public void testScopeComponentLifecycle() {
        assertFalse(TaskScope.isActive());
        TaskScope.init();
        assertTrue(TaskScope.isActive());

        AtomicInteger callbackCount = new AtomicInteger();
        try {
            assertTrue(taskScopedComponent.isPostConstructed());
            taskScopedComponent.setPreDestroyCallback(() -> callbackCount.incrementAndGet());
            assertEquals(0, callbackCount.get());

        } finally {
            assertTrue(TaskScope.isActive());
            TaskScope.destroy();
            assertFalse(TaskScope.isActive());
            assertEquals(1, callbackCount.get());
        }
    }
}
