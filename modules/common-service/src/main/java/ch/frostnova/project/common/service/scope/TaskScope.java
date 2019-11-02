package ch.frostnova.project.common.service.scope;

import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.config.Scope;
import org.springframework.web.context.annotation.RequestScope;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

/**
 * A scope for beans living withing a task context. Contrary to the {@link RequestScope}, the task scope is not limited to HTTP requests, but can be used for:
 * <ul>
 *     <li>HTTP requests (request is a task)</li>
 *     <li>Dispatching of messages from topics and queues (processing of a message is a task)</li>
 *     <li>Batch processing jobs (each job is a task)</li>
 *     <li>Unit or integration testing (test or test suite is a task)</li>
 *     <li>Whatever other tasks you can immagine</li>
 * </ul>
 * The task scope can be manually started and stopped. For Servlet requests, the task scope is automatically
 * opened and closed, making it behave like the {@link RequestScope}.
 * Within one thread, no more than one task scope can be active.
 * <p>
 * //ServletRequestListener
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
        return scopeInstance.get() != null;
    }

    /**
     * Returns the current conversation id (if active), or null if the scope is not active.
     *
     * @return current conversation id
     */
    public static String currentConversationId() {
        return Optional.ofNullable(scopeInstance.get()).map(ScopeInstance::conversationId).orElse(null);
    }

    private ScopeInstance scopeInstance() {
        checkScopeActive();
        return scopeInstance.get();
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

    public static void runInContext(CheckedRunnable runnable) {
        if (runnable == null) {
            throw new IllegalArgumentException("Runnable is required");
        }
        runInContext(() -> {
            runnable.runUnchecked();
            return null;
        });
    }

    public static <T> T runInContext(CheckedSupplier<T> supplier) {
        if (supplier == null) {
            throw new IllegalArgumentException("Supplier is required");
        }
        if (isActive()) {
            return supplier.supplyUnchecked();
        }
        try {
            init();
            return supplier.supplyUnchecked();
        } finally {
            if (isActive()) {
                destroy();
            }
        }
    }

    public static Runnable scoped(CheckedRunnable runnable) {
        if (runnable == null) {
            throw new IllegalArgumentException("Runnable is required");
        }
        return scoped(x -> {
            runnable.runUnchecked();
            return null;
        }).asRunnable();
    }

    public static <T> Supplier<T> scoped(CheckedSupplier<T> supplier) {
        if (supplier == null) {
            throw new IllegalArgumentException("Supplier is required");
        }
        return scoped(x -> supplier.supplyUnchecked()).asSupplier();
    }

    public static <T, R> CheckedFunction<T, R> scoped(CheckedFunction<T, R> function) {
        if (function == null) {
            throw new IllegalArgumentException("Function is required");
        }
        ScopeInstance scopeInstance = TaskScope.scopeInstance.get();
        return input -> {
            ScopeInstance currentThreadScopeInstance = TaskScope.scopeInstance.get();
            // running in active scope?
            if (currentThreadScopeInstance != null) {
                return function.apply(input);
            }
            // inherited scope?
            if (scopeInstance != null) {
                TaskScope.scopeInstance.set(scopeInstance);
                try {
                    return function.apply(input);
                } finally {
                    TaskScope.scopeInstance.remove();
                }
            }
            // new scope
            try {
                TaskScope.init();
                return function.apply(input);
            } finally {
                TaskScope.destroy();
            }
        };
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

    private static class ScopeInstance {
        private final Map<String, Object> scopedObjects = new HashMap<>();
        private final Map<String, Runnable> destructionCallbacks = new HashMap<>();
        private final String conversationId = UUID.randomUUID().toString();

        public void destroy() {
            scopedObjects.keySet().forEach(name -> {
                Optional.ofNullable(destructionCallbacks.get(name)).ifPresent(Runnable::run);
            });
            scopedObjects.clear();
            destructionCallbacks.clear();
        }

        public String conversationId() {
            return conversationId;
        }

        public Map<String, Object> scopedObjects() {
            return scopedObjects;
        }

        public Map<String, Runnable> destructionCallbacks() {
            return destructionCallbacks;
        }
    }

    @FunctionalInterface
    public interface CheckedRunnable {
        void run() throws Exception;

        default void runUnchecked() {
            try {
                run();
            } catch (RuntimeException ex) {
                throw ex;
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    @FunctionalInterface
    public interface CheckedSupplier<T> {
        T supply() throws Exception;

        default T supplyUnchecked() {
            try {
                return supply();
            } catch (RuntimeException ex) {
                throw ex;
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }

        default Runnable asRunnable() {
            return () -> supplyUnchecked();
        }
    }

    @FunctionalInterface
    public interface CheckedFunction<T, R> {
        R apply(T input) throws Exception;

        default R applyUnchecked(T input) {
            try {
                return apply(input);
            } catch (RuntimeException ex) {
                throw ex;
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }

        default Supplier<R> asSupplier() {
            return () -> applyUnchecked(null);
        }

        default Runnable asRunnable() {
            return () -> applyUnchecked(null);
        }
    }
}
