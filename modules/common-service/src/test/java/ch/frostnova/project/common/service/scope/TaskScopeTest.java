package ch.frostnova.project.common.service.scope;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {TaskScopeConfiguration.class, TaskScopedComponent.class})
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
        assertThrows(BeanCreationException.class, () -> taskScopedComponent.getUuid());
    }

    @Test
    public void testScopeActive() {
        TaskScope.init();
        try {
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

    @Test
    public void testRunInContext() {
        assertFalse(TaskScope.isActive());
        TaskScope.runInContext(() -> {
            assertNotNull(taskScopedComponent);
            assertNotNull(taskScopedComponent.getUuid());
            System.out.println(taskScopedComponent.getUuid());
        });
        assertFalse(TaskScope.isActive());
    }

    @Test
    public void testRunInContextWithException() {
        assertFalse(TaskScope.isActive());
        assertThrows(ArithmeticException.class, () ->
                TaskScope.runInContext(() -> {
                    throw new ArithmeticException();
                }));
        assertFalse(TaskScope.isActive());
    }

    @Test
    public void testInheritInThread() throws ExecutionException, InterruptedException {
        TaskScope.init();
        final String uuid = taskScopedComponent.getUuid();

        ExecutorService executorService = Executors.newFixedThreadPool(8);
        List<Future<?>> futures = new LinkedList<>();
        Map<String, String> threadComponentInstances = Collections.synchronizedMap(new HashMap<>());

        for (int i = 0; i < 100; i++) {
            futures.add(executorService.submit(TaskScope.scoped(() -> {
                assertTrue(TaskScope.isActive());
                String threadInfo = Thread.currentThread().getId() + ": " + Thread.currentThread().getName();
                threadComponentInstances.put(threadInfo, taskScopedComponent.getUuid());
                try {
                    Thread.sleep(10);
                } catch (InterruptedException ex) {
                    throw new RuntimeException(ex);
                }
            })));
        }
        for (Future<?> future : futures) {
            future.get();
        }
        assertTrue(TaskScope.isActive());
        // same component in each task
        assertTrue(threadComponentInstances.size() > 1);
        threadComponentInstances.forEach((tci, result) -> assertEquals(uuid, result));
    }

    @Test
    public void testScopeInParallelStream() {

        assertFalse(TaskScope.isActive());
        TaskScope.CheckedFunction<Object, String> scoped = TaskScope.scoped(x -> taskScopedComponent.getUuid());

        List<String> results = IntStream.range(0, 100)
                .parallel()
                .peek(x -> assertFalse(TaskScope.isActive()))
                .mapToObj(x -> TaskScope.runInContext(taskScopedComponent::getUuid))
                .peek(x -> assertFalse(TaskScope.isActive()))
                .collect(Collectors.toList());

        assertFalse(TaskScope.isActive());
        // distinct components per task
        assertEquals(results.size(), results.stream().distinct().count());
    }

    @Test
    public void testInheritInParallelStream() {
        TaskScope.init();

        final String uuid = taskScopedComponent.getUuid();
        TaskScope.CheckedFunction<Object, String> scoped = TaskScope.scoped(x -> taskScopedComponent.getUuid());

        List<String> results = IntStream.range(0, 100)
                .parallel()
                .mapToObj(scoped::applyUnchecked)
                .collect(Collectors.toList());

        assertTrue(TaskScope.isActive());
        // same component in each task
        results.forEach(result -> assertEquals(uuid, result));
    }
}
