package ch.frostnova.common.service.scope;

import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.config.Scope;
import org.springframework.web.context.annotation.RequestScope;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;

/**
 * A scope for beans living withing a task context. Contrary to the {@link RequestScope}, the task scope is not limited to HTTP requests, but can be used for:
 * <ul>
 *     <li>HTTP requests (each request is a task)</li>
 *     <li>Asynchronous and parallel task execution with shared or dedicated task scopes</li>
 *     <li>Dispatching of messages from topics and queues (processing of a message is a task)</li>
 *     <li>Unit or integration testing (test or test suite is a task)</li>
 * </ul>
 * The task scope can be manually started and stopped. For Servlet requests, the task scope is automatically
 * opened and closed (by the {@link TaskScopeRequestScopeFilter}), making this scope behave like the {@link RequestScope}.
 * Task scopes are thread-bound, but nesting of task scopes within the same thread is possible using the {@link ExecutionContext} functionality.
 * <p>
 *
 * @author pwalser
 * @since 2019-11-01
 */
public class TaskScope implements Scope {

    public final static String NAME = "task";

    private final static ThreadLocal<ScopeInstance> scopeInstance = new ThreadLocal<>();

    /**
     * Initialize the scope and bind it to the current thread.
     * Throws an {@link IllegalStateException} if the scope is already active on that thread.
     */
    public static void init() {
        checkScopeNotActive();
        scopeInstance.set(new ScopeInstance());
    }

    /**
     * Destroy the scope and unbind it from the current thread.
     * Throws an {@link IllegalStateException} if the scope was not active on that thread.
     */
    public static void destroy() {
        checkScopeActive();
        scopeInstance.get().destroy();
        scopeInstance.remove();
    }

    /**
     * Check if the scope is active on the current thread.
     *
     * @return active
     */
    public static boolean isActive() {
        return Optional.ofNullable(scopeInstance.get()).map(ScopeInstance::isActive).orElse(false);
    }

    /**
     * Returns the current conversation id (if active), or null if the scope is not active.
     *
     * @return current conversation id
     */
    public static String currentConversationId() {
        return Optional.ofNullable(scopeInstance.get()).map(ScopeInstance::conversationId).orElse(null);
    }

    private static void checkScopeActive() {
        if (!isActive()) {
            throw new IllegalStateException("Test scope not active (activate with TestScope.init(), deactivate with TestScope.destroy()");
        }
    }

    private static void checkScopeNotActive() {
        if (isActive()) {
            throw new IllegalStateException("Test scope already initialized");
        }
    }

    private static ScopeInstance scopeInstance() {
        checkScopeActive();
        return scopeInstance.get();
    }

    @Override
    public Object get(String name, ObjectFactory<?> objectFactory) {
        TaskScope.checkScopeActive();
        return scopeInstance().scopedObjects().computeIfAbsent(name, x -> objectFactory.getObject());
    }

    @Override
    public Object remove(String name) {
        scopeInstance().destructionCallbacks().remove(name);
        return scopeInstance().scopedObjects().remove(name);
    }

    @Override
    public void registerDestructionCallback(String name, Runnable callback) {
        scopeInstance().destructionCallbacks().put(name, callback);
    }

    @Override
    public Object resolveContextualObject(String key) {
        return null;
    }

    @Override
    public String getConversationId() {
        return scopeInstance().conversationId();
    }

    /**
     * The scope instance contains the registry of scoped objects, and the destruction callbacks
     * for those objects, to be called when the scope is destroyed.
     * Calling the destruction callbacks will have the dependency injection framework invoke
     * the pre-destroy lifecycle hooks before the scoped objects are disposed of.
     */
    private static class ScopeInstance {
        private final Map<String, Object> scopedObjects = new HashMap<>();
        private final Map<String, Runnable> destructionCallbacks = new HashMap<>();
        private String conversationId = UUID.randomUUID().toString();

        private void destroy() {
            scopedObjects.keySet().forEach(name ->
                    Optional.ofNullable(destructionCallbacks.get(name))
                            .ifPresent(Runnable::run));
            scopedObjects.clear();
            destructionCallbacks.clear();
            conversationId = null;
        }

        private boolean isActive() {
            return conversationId != null;
        }

        private String conversationId() {
            return conversationId;
        }

        private Map<String, Object> scopedObjects() {
            return scopedObjects;
        }

        private Map<String, Runnable> destructionCallbacks() {
            return destructionCallbacks;
        }
    }

    /**
     * Creates an {@link ExecutionContext} which executes code withing the current task scope.
     *
     * @return new execution context which runs code in the current task scope.
     * @throws IllegalStateException when no current task scope is active.
     */
    public static ExecutionContext currentExecutionContext() {
        return new ExecutionContext(scopeInstance());
    }

    /**
     * Creates a new {@link ExecutionContext} which executes code within a dedicated task scope (per execution).
     *
     * @return new execution context with dedicated task scopes per execution.
     */
    public static ExecutionContext newExecutionContext() {
        return new ExecutionContext();
    }

    /**
     * An execution context which can execute code fragments ({@link Runnable}, {@link Callable},
     * {@link CheckedRunnable} or {@link CheckedSupplier}).
     * Particularly useful for asynchronous or parallel processing (using {@link ExecutorService},
     * or parallel streams), where code can be executed in a shared or dedicated task scope.
     */
    public static class ExecutionContext {

        private final ScopeInstance scope;

        private ExecutionContext() {
            this(null);
        }

        private ExecutionContext(ScopeInstance scope) {
            this.scope = scope;
            checkScopeActive();
        }

        private void checkScopeActive() {
            if (scope != null && !scope.isActive()) {
                throw new IllegalStateException("Scope is no longer active");
            }
        }

        /**
         * Execute a {@link CheckedRunnable} (compatible with {@link Runnable} in the task scope of the execution context.
         *
         * @param runnable runnable, required
         */
        public void execute(CheckedRunnable runnable) {
            if (runnable == null) {
                throw new IllegalArgumentException("Runnable is required");
            }
            execute(() -> {
                runnable.runUnchecked();
                return null;
            });
        }

        /**
         * Execute a {@link CheckedSupplier} (compatible with {@link Callable}) in the task scope of the execution context.
         *
         * @param supplier supplier, required
         */
        public <T> T execute(CheckedSupplier<T> supplier) {
            if (supplier == null) {
                throw new IllegalArgumentException("Supplier is required");
            }
            checkScopeActive();
            ScopeInstance backupScope = scopeInstance.get();
            ScopeInstance executionScope = scope != null ? scope : new ScopeInstance();

            TaskScope.scopeInstance.set(executionScope);
            try {
                return supplier.supplyUnchecked();
            } finally {
                TaskScope.scopeInstance.set(backupScope);
                if (scope == null) {
                    executionScope.destroy();
                }
            }
        }
    }

    /**
     * Functional interface for a runnable which can throw a checked exception.
     */
    @FunctionalInterface
    public interface CheckedRunnable {

        /**
         * Functional contract
         *
         * @throws Exception optional exception
         */
        void run() throws Throwable;


        /**
         * Unchecked execution: execute checked and rethrow any exception as {@link RuntimeException}.
         */
        default void runUnchecked() {
            try {
                run();
            } catch (RuntimeException ex) {
                throw ex;
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            } catch (Throwable ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    /**
     * Functional interface for a supplier which can throw a checked exception. (same as {@link Callable}).
     */
    @FunctionalInterface
    public interface CheckedSupplier<T> {

        /**
         * Functional contract
         *
         * @return return value
         * @throws Exception optional exception
         */
        T supply() throws Throwable;

        /**
         * Unchecked execution: execute checked and rethrow any exception as {@link RuntimeException}.
         */
        default T supplyUnchecked() {
            try {
                return supply();
            } catch (RuntimeException ex) {
                throw ex;
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            } catch (Throwable ex) {
                throw new RuntimeException(ex);
            }
        }
    }
}
