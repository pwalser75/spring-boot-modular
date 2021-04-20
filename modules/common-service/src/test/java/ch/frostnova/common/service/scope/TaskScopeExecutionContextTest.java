package ch.frostnova.common.service.scope;

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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
        assertThat(TaskScope.isActive()).isFalse();
    }

    @Test
    public void testRunInContext() {
        assertThat(TaskScope.isActive()).isFalse();

        TaskScope.newExecutionContext().execute(() -> {
            assertThat(taskScopedComponent).isNotNull();
            assertThat(taskScopedComponent.getUuid()).isNotNull();
            System.out.println(taskScopedComponent.getUuid());
        });

        assertThat(TaskScope.isActive()).isFalse();
    }

    @Test
    public void testRunInContextWithException() {
        assertThat(TaskScope.isActive()).isFalse();

        assertThrows(ArithmeticException.class, () ->
                TaskScope.newExecutionContext().execute(() -> {
                    throw new ArithmeticException();
                }));
        assertThat(TaskScope.isActive()).isFalse();
    }

    @Test
    public void testInThread() throws ExecutionException, InterruptedException {

        TaskScope.ExecutionContext executionContext = TaskScope.newExecutionContext();

        ExecutorService executorService = Executors.newFixedThreadPool(8);
        List<Future<String>> futures = new LinkedList<>();


        for (int i = 0; i < 100; i++) {
            futures.add(executorService.submit(() -> executionContext.execute(() -> {
                assertThat(TaskScope.isActive()).isTrue();
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
        assertThat(TaskScope.isActive()).isFalse();

        // distinct components per task
        assertThat(results.stream().distinct().count()).isEqualTo(results.size());
    }

    @Test
    public void testInheritInThread() throws ExecutionException, InterruptedException {
        TaskScope.init();
        String uuid = taskScopedComponent.getUuid();
        TaskScope.ExecutionContext executionContext = TaskScope.currentExecutionContext();

        ExecutorService executorService = Executors.newFixedThreadPool(8);
        List<Future<String>> futures = new LinkedList<>();

        for (int i = 0; i < 100; i++) {
            futures.add(executorService.submit(() -> executionContext.execute(() -> {
                assertThat(TaskScope.isActive()).isTrue();
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
        assertThat(TaskScope.isActive()).isTrue();

        // same component in each task
        assertThat(results).allSatisfy(result -> assertThat(result).isEqualTo(uuid));
    }

    @Test
    public void testScopeInParallelStream() {

        assertThat(TaskScope.isActive()).isFalse();

        TaskScope.ExecutionContext executionContext = TaskScope.newExecutionContext();

        List<String> results = IntStream.range(0, 100)
                .parallel()
                .mapToObj(x -> executionContext.execute(taskScopedComponent::getUuid))
                .collect(Collectors.toList());

        assertThat(TaskScope.isActive()).isFalse();
        // distinct components per task
        assertThat(results.stream().distinct().count()).isEqualTo(results.size());
    }

    @Test
    public void testInheritInParallelStream() {
        TaskScope.init();

        String uuid = taskScopedComponent.getUuid();
        TaskScope.ExecutionContext executionContext = TaskScope.currentExecutionContext();

        List<String> results = IntStream.range(0, 100)
                .parallel()
                .mapToObj(x -> executionContext.execute(taskScopedComponent::getUuid))
                .collect(Collectors.toList());

        assertThat(TaskScope.isActive()).isTrue();
        assertThat(taskScopedComponent.getUuid()).isEqualTo(uuid);
        // same component in each task
        assertThat(results).allSatisfy(result -> assertThat(result).isEqualTo(uuid));
    }

    @Test
    public void testCurrentExecutionContextFailsWhenNoneActive() {
        assertThatThrownBy(() -> TaskScope.currentExecutionContext()).isInstanceOf(IllegalStateException.class);
    }

    @Test
    public void testCurrentExecutionContextGone() {
        TaskScope.init();
        TaskScope.ExecutionContext executionContext = TaskScope.currentExecutionContext();

        executionContext.execute(() -> taskScopedComponent.getUuid());
        TaskScope.destroy();

        assertThatThrownBy(() -> executionContext.execute(() -> taskScopedComponent.getUuid())).isInstanceOf(IllegalStateException.class);
    }

    @Test
    public void testCurrentExecutionContextChanged() {
        TaskScope.init();
        TaskScope.ExecutionContext executionContext = TaskScope.currentExecutionContext();
        TaskScope.destroy();

        TaskScope.init();
        assertThatThrownBy(() -> executionContext.execute(() -> taskScopedComponent.getUuid())).isInstanceOf(IllegalStateException.class);
    }

    @Test
    public void testExecuteInActiveContextThread() {

        TaskScope.init();
        String uuid1 = taskScopedComponent.getUuid();
        String uuid2 = TaskScope.currentExecutionContext().execute(() -> taskScopedComponent.getUuid());
        String uuid3 = taskScopedComponent.getUuid();

        assertThat(uuid2).isEqualTo(uuid1);
        assertThat(uuid3).isEqualTo(uuid1);
    }

    @Test
    public void testNestedTaskScopes() {

        Set<String> preDestroyed = new HashSet<>();

        TaskScope.init();
        String uuid1 = taskScopedComponent.getUuid();
        taskScopedComponent.setPreDestroyCallback(() -> preDestroyed.add(uuid1));

        TaskScope.newExecutionContext().execute(() -> {
            String uuid2 = taskScopedComponent.getUuid();
            assertNotEquals(uuid1, uuid2);
            taskScopedComponent.setPreDestroyCallback(() -> preDestroyed.add(uuid2));

            TaskScope.newExecutionContext().execute(() -> {
                String uuid3 = taskScopedComponent.getUuid();
                assertNotEquals(uuid1, uuid3);
                assertNotEquals(uuid2, uuid3);
                taskScopedComponent.setPreDestroyCallback(() -> preDestroyed.add(uuid3));
            });
            assertThat(taskScopedComponent.getUuid()).isEqualTo(uuid2);
            assertThat(preDestroyed.size()).isEqualTo(1);
        });
        assertThat(taskScopedComponent.getUuid()).isEqualTo(uuid1);
        assertThat(preDestroyed.size()).isEqualTo(2);
        TaskScope.destroy();
        assertThat(preDestroyed.size()).isEqualTo(3);
    }

    @Test
    public void testRequiresRunnable() {
        TaskScope.CheckedRunnable runnable = null;
        assertThatThrownBy(() -> TaskScope.newExecutionContext().execute(runnable)).isInstanceOf(IllegalArgumentException.class);
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
        TaskScope.CheckedSupplier<String> supplier = null;
        assertThatThrownBy(() -> TaskScope.newExecutionContext().execute(supplier)).isInstanceOf(IllegalArgumentException.class);
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
