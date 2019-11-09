package ch.frostnova.project.common.service.scope;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {TaskScopeConfig.class, TaskScopedComponent.class})
public class TaskScopeExecutionContextTest {

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
    public void testRunInContext() {
        assertFalse(TaskScope.isActive());

        TaskScope.newExecutionContext().execute(() -> {
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
                TaskScope.newExecutionContext().execute(() -> {
                    throw new ArithmeticException();
                }));
        assertFalse(TaskScope.isActive());
    }

    @Test
    public void testInThread() throws ExecutionException, InterruptedException {

        TaskScope.ExecutionContext executionContext = TaskScope.newExecutionContext();

        ExecutorService executorService = Executors.newFixedThreadPool(8);
        List<Future<String>> futures = new LinkedList<>();


        for (int i = 0; i < 100; i++) {
            futures.add(executorService.submit(() -> executionContext.execute(() -> {
                assertTrue(TaskScope.isActive());
                try {
                    Thread.sleep(10);
                } catch (InterruptedException ex) {
                    throw new RuntimeException(ex);
                }
                return taskScopedComponent.getUuid();
            })));
        }
        List<String> results = new LinkedList<>();
        for (Future<String> future : futures) {
            results.add(future.get());
        }
        executorService.shutdownNow();
        assertFalse(TaskScope.isActive());

        // distinct components per task
        assertEquals(results.size(), results.stream().distinct().count());
    }

    @Test
    public void testInheritInThread() throws ExecutionException, InterruptedException {
        TaskScope.init();
        final String uuid = taskScopedComponent.getUuid();
        TaskScope.ExecutionContext executionContext = TaskScope.currentExecutionContext();

        ExecutorService executorService = Executors.newFixedThreadPool(8);
        List<Future<String>> futures = new LinkedList<>();

        for (int i = 0; i < 100; i++) {
            futures.add(executorService.submit(() -> executionContext.execute(() -> {
                assertTrue(TaskScope.isActive());
                try {
                    Thread.sleep(10);
                } catch (InterruptedException ex) {
                    throw new RuntimeException(ex);
                }
                return taskScopedComponent.getUuid();
            })));
        }
        List<String> results = new LinkedList<>();
        for (Future<String> future : futures) {
            results.add(future.get());
        }
        executorService.shutdownNow();
        assertTrue(TaskScope.isActive());

        // same component in each task
        results.forEach(result -> assertEquals(uuid, result));
    }

    @Test
    public void testScopeInParallelStream() {

        assertFalse(TaskScope.isActive());

        TaskScope.ExecutionContext executionContext = TaskScope.newExecutionContext();

        List<String> results = IntStream.range(0, 100)
                .parallel()
                .mapToObj(x -> executionContext.execute(taskScopedComponent::getUuid))
                .collect(Collectors.toList());

        assertFalse(TaskScope.isActive());
        // distinct components per task
        assertEquals(results.size(), results.stream().distinct().count());
    }

    @Test
    public void testInheritInParallelStream() {
        TaskScope.init();

        final String uuid = taskScopedComponent.getUuid();
        TaskScope.ExecutionContext executionContext = TaskScope.currentExecutionContext();

        List<String> results = IntStream.range(0, 100)
                .parallel()
                .mapToObj(x -> executionContext.execute(taskScopedComponent::getUuid))
                .collect(Collectors.toList());

        assertTrue(TaskScope.isActive());
        assertEquals(uuid, taskScopedComponent.getUuid());
        // same component in each task
        results.forEach(result -> assertEquals(uuid, result));
    }

    @Test
    public void testCurrentExecutionContextFailsWhenNoneActive() {
        assertThrows(IllegalStateException.class, () -> TaskScope.currentExecutionContext());
    }

    @Test
    public void testCurrentExecutionContextGone() {
        TaskScope.init();
        TaskScope.ExecutionContext executionContext = TaskScope.currentExecutionContext();

        executionContext.execute(() -> taskScopedComponent.getUuid());
        TaskScope.destroy();

        assertThrows(IllegalStateException.class, () -> executionContext.execute(() -> taskScopedComponent.getUuid()));
    }

    @Test
    public void testCurrentExecutionContextChanged() {
        TaskScope.init();
        TaskScope.ExecutionContext executionContext = TaskScope.currentExecutionContext();
        TaskScope.destroy();

        TaskScope.init();
        assertThrows(IllegalStateException.class, () -> executionContext.execute(() -> taskScopedComponent.getUuid()));
    }

    @Test
    public void testExecuteInActiveContextThread() {

        TaskScope.init();
        final String uuid1 = taskScopedComponent.getUuid();
        final String uuid2 = TaskScope.currentExecutionContext().execute(() -> taskScopedComponent.getUuid());
        final String uuid3 = taskScopedComponent.getUuid();

        assertEquals(uuid1, uuid2);
        assertEquals(uuid1, uuid3);
    }

    @Test
    public void testNestedTaskScopes() {

        final Set<String> preDestroyed = new HashSet<>();

        TaskScope.init();
        final String uuid1 = taskScopedComponent.getUuid();
        taskScopedComponent.setPreDestroyCallback(() -> preDestroyed.add(uuid1));

        TaskScope.newExecutionContext().execute(() -> {
            final String uuid2 = taskScopedComponent.getUuid();
            assertNotEquals(uuid1, uuid2);
            taskScopedComponent.setPreDestroyCallback(() -> preDestroyed.add(uuid2));

            TaskScope.newExecutionContext().execute(() -> {
                final String uuid3 = taskScopedComponent.getUuid();
                assertNotEquals(uuid1, uuid3);
                assertNotEquals(uuid2, uuid3);
                taskScopedComponent.setPreDestroyCallback(() -> preDestroyed.add(uuid3));
            });
            assertEquals(uuid2, taskScopedComponent.getUuid());
            assertEquals(1, preDestroyed.size());
        });
        assertEquals(uuid1, taskScopedComponent.getUuid());
        assertEquals(2, preDestroyed.size());
        TaskScope.destroy();
        assertEquals(3, preDestroyed.size());
    }

    @Test
    public void testRequiresRunnable() {
        final TaskScope.CheckedRunnable runnable = null;
        assertThrows(IllegalArgumentException.class, () -> TaskScope.newExecutionContext().execute(runnable));
    }

    @Test
    public void testRunnableExecutionWithRuntimeException() {
        assertThrows(ArithmeticException.class, () -> TaskScope.newExecutionContext().execute(() -> {
            if (1 > 0) {
                throw new ArithmeticException();
            }
            return;
        }));
    }

    @Test
    public void testRunnableExecutionWithCheckedException() {
        assertThrows(RuntimeException.class, () -> TaskScope.newExecutionContext().execute(() -> {
            if (1 > 0) {
                throw new IOException();
            }
            return;
        }));
    }

    @Test
    public void testRequiresSupplier() {
        final TaskScope.CheckedSupplier<String> supplier = null;
        assertThrows(IllegalArgumentException.class, () -> TaskScope.newExecutionContext().execute(supplier));
    }

    @Test
    public void testSupplierExecutionWithRuntimeException() {
        assertThrows(ArithmeticException.class, () -> TaskScope.newExecutionContext().execute(() -> {
            if (1 > 0) {
                throw new ArithmeticException();
            }
            return null;
        }));
    }

    @Test
    public void testSupplierExecutionWithCheckedException() {
        assertThrows(RuntimeException.class, () -> TaskScope.newExecutionContext().execute(() -> {
            if (1 > 0) {
                throw new IOException();
            }
            return null;
        }));
    }
}
