package ch.frostnova.spring.boot.platform.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * Performance logging aspect, logs performance and result state (ok or exception) for (nested) service calls.<br> Activated by profile
 * <code>performance-logging</code>, the aspect will log performance for any
 * <ul>
 * <li>classes annotated with <code>@PerformanceLogging</code></li>
 * <li>Spring <code>@Service</code></li>
 * <li>Spring <code>@Controller</code></li>
 * <li>Spring <code>@RestController</code></li>
 * <li>Spring Data <code>Repository</code></li>
 * </ul>
 * Example log output:
 * <pre><code>
 * 15:12:22.316 INFO  [main] | PerformanceLoggingAspect - Test.a() -&gt; 211.20 ms, self: 51.30 ms
 * &nbsp;&nbsp;+ Test.b() -&gt; java.lang.IllegalArgumentException, 134.04 ms, self: 102.04 ms
 * &nbsp;&nbsp;&nbsp;&nbsp;+ 5x Test.c() -&gt; 51.94 ms
 * &nbsp;&nbsp;&nbsp;&nbsp;+ Test.d() -&gt; java.lang.ArithmeticException, 0.03 ms
 * &nbsp;&nbsp;+ Test.e() -&gt; 25.86 ms
 * 15:12:22.339 INFO  [main] | PerformanceLoggingAspect - Other.x() -&gt; 12.55 ms, self: 2.57 ms
 * &nbsp;&nbsp;+ Other.y() -&gt; 9.98 ms, self: 7.92 ms
 * &nbsp;&nbsp;&nbsp;&nbsp;+ Other.z() -&gt; 2.06 ms
 * </code></pre>
 *
 * @author Peter Walser
 * @since 2018-11-02
 */
@Aspect
@Component
@Profile("performance-logging")
public class PerformanceLoggingAspect {

    private final static String SYMBOL_SPACE = " ";
    private final static String SYMBOL_INDENTATION = ""; // unicode alternative: "\u2937"
    private final static String SYMBOL_RIGHT_ARROW = "->"; // unicode alternative: "\u2192"

    private static final Logger log = LoggerFactory.getLogger(PerformanceLoggingAspect.class);

    /**
     * Threshold (in nanoseconds) under which calls are no longer reported in detail.
     */
    private static final long DETAIL_THRESHOLD_NS = 1_000_000; // 1 ms

    /**
     * Bind aspect to any Spring @Service, @Controller, @RestController and Repository
     *
     * @param joinPoint aspect join point
     * @return invocation result
     * @throws Throwable invocation exception
     */
    @Around("!@within(ch.frostnova.spring.boot.platform.aspect.NoPerformanceLogging) && " +
            "(@within(ch.frostnova.spring.boot.platform.aspect.PerformanceLogging)" +
            "|| @within(org.springframework.stereotype.Service)" +
            "|| @within(org.springframework.stereotype.Controller)" +
            "|| @within(org.springframework.scheduling.annotation.Scheduled)" +
            "|| @within(org.springframework.web.bind.annotation.RestController))"
    )
    public static Object around(ProceedingJoinPoint joinPoint) {

        String invocation = joinPoint.getSignature().toShortString();
        return PerformanceLoggingContext.current().execute(invocation, () -> joinPoint.proceed());
    }

    /**
     * Bind aspect to intermediate invocations. Only log such invocations if we passed an entry point already.
     *
     * @param joinPoint aspect join point
     * @return invocation result
     * @throws Throwable invocation exception
     */
    @Around("this(org.springframework.data.repository.Repository)" +
            "|| @within(org.springframework.stereotype.Component)")
    public static Object aroundIntermediate(ProceedingJoinPoint joinPoint) throws Throwable {
        if (PerformanceLoggingContext.current().isIntermediateInvocation()) {
            return around(joinPoint);
        }
        return joinPoint.proceed();
    }

    public static class PerformanceLoggingContext {

        private final static ThreadLocal<PerformanceLoggingContext> current = new ThreadLocal<>();

        private final Deque<InvocationInfo> invocations = new LinkedList<>();
        private final Deque<InvocationInfo> invocationStack = new LinkedList<>();
        private final Deque<AtomicLong> nestedTime = new LinkedList<>();

        public static PerformanceLoggingContext current() {
            PerformanceLoggingContext context = current.get();
            if (context == null) {
                context = new PerformanceLoggingContext();
                current.set(context);
            }
            return context;
        }

        boolean isIntermediateInvocation() {
            return invocationStack.size() > 0;
        }

        private void enter(String invocation) {
            long time = System.nanoTime();
            InvocationInfo invocationInfo = new InvocationInfo(invocationStack.size(), invocation, time);
            invocations.add(invocationInfo);
            invocationStack.push(invocationInfo);
            nestedTime.push(new AtomicLong());
        }

        private void exit(Throwable t) {
            long time = System.nanoTime();
            if (invocationStack.isEmpty()) {
                throw new IllegalStateException("No invocation in progress");
            }

            InvocationInfo info = invocationStack.pop();
            info.done(time, t != null ? t.getClass().getName() : null);
            info.setNestedTimeNs(nestedTime.pop().get());

            Optional.ofNullable(nestedTime.peek()).ifPresent(x -> x.addAndGet(info.getElapsedTimeNs()));

            if (invocationStack.isEmpty()) {

                InvocationInfo previous = null;
                Iterator<InvocationInfo> iterator = invocations.iterator();
                while (iterator.hasNext()) {
                    InvocationInfo invocationInfo = iterator.next();
                    if (previous != null && previous.merge(invocationInfo)) {
                        iterator.remove();
                    } else {
                        previous = invocationInfo;
                    }
                }
                iterator = invocations.iterator();
                InvocationInfo skipFromLevel = null;
                while (iterator.hasNext()) {
                    InvocationInfo invocationInfo = iterator.next();
                    if (skipFromLevel != null) {
                        if (invocationInfo.level > skipFromLevel.level) {
                            iterator.remove();
                        } else {
                            skipFromLevel = null;
                        }
                    }
                    if (skipFromLevel == null && invocationInfo.getElapsedTimeNs() < PerformanceLoggingAspect.DETAIL_THRESHOLD_NS && invocationInfo.level > 1) {
                        skipFromLevel = invocationInfo;
                    }
                }

                PerformanceLoggingAspect.log.info(invocations.stream()
                        .map(InvocationInfo::toString)
                        .collect(Collectors.joining("\n")));
                invocations.clear();
                current.remove();
            }
        }


        /**
         * Run code inside the performance logging context
         *
         * @param invocationInfo invocationInfo
         * @param runnable       runnable to execute, required
         */
        public void execute(String invocationInfo, CheckedRunnable runnable) {
            enter(invocationInfo);

            Throwable error = null;
            try {
                runnable.run();
            } catch (RuntimeException ex) {
                error = ex;
                throw ex;
            } catch (Throwable ex) {
                error = ex;
                throw ex instanceof RuntimeException ? (RuntimeException) ex : new RuntimeException(ex);
            } finally {
                exit(error);
            }
        }

        /**
         * Run code inside the performance logging context
         *
         * @param invocationInfo invocationInfo
         * @param supplier       supplier to execute, required
         */
        public <T> T execute(String invocationInfo, CheckedSupplier<T> supplier) {
            enter(invocationInfo);

            Throwable error = null;
            try {
                return supplier.supply();
            } catch (RuntimeException ex) {
                error = ex;
                throw ex;
            } catch (Throwable ex) {
                error = ex;
                throw ex instanceof RuntimeException ? (RuntimeException) ex : new RuntimeException(ex);
            } finally {
                exit(error);
            }
        }
    }


    private static class InvocationInfo {

        private int mergeCount = 1;
        private final int level;
        private long startTimeNs;
        private long endTimeNs;
        private final String invocation;
        private String result;
        private long nestedTimeNs;

        InvocationInfo(int level, String invocation, long startTimeNs) {
            this.level = level;
            this.startTimeNs = startTimeNs;
            this.invocation = invocation;
        }

        void done(long endTimeNs, String result) {
            this.endTimeNs = endTimeNs;
            this.result = result;
        }

        long getElapsedTimeNs() {
            return endTimeNs - startTimeNs;
        }

        void setNestedTimeNs(long timeNs) {
            nestedTimeNs = timeNs;
        }

        boolean merge(InvocationInfo other) {
            if (other != null && nestedTimeNs == 0 && other.nestedTimeNs == 0 && level == other.level && invocation.equals(other.invocation)) {
                startTimeNs = Math.min(startTimeNs, other.startTimeNs);
                endTimeNs = Math.max(endTimeNs, other.endTimeNs);
                mergeCount++;
                return true;
            }
            return false;
        }

        @Override
        public String toString() {

            long durationNs = endTimeNs - startTimeNs;

            StringBuilder builder = new StringBuilder();
            if (level > 0) {
                for (int i = 0; i < level; i++) {
                    builder.append("  ");
                }
                builder.append(SYMBOL_INDENTATION);
                builder.append(SYMBOL_SPACE);
            }
            if (mergeCount > 1) {
                builder.append(mergeCount);
                builder.append("x ");
            }
            builder.append(invocation);
            builder.append(SYMBOL_SPACE);
            builder.append(SYMBOL_RIGHT_ARROW);
            builder.append(SYMBOL_SPACE);
            if (result != null) {
                builder.append(result);
                builder.append(", ");
            }
            builder.append(formatTimeMs(durationNs));
            if (nestedTimeNs > 0) {
                builder.append(", self: ");
                builder.append(formatTimeMs(durationNs - nestedTimeNs));
            }
            return builder.toString();
        }

        private static String formatTimeMs(long timeNs) {
            return BigDecimal.valueOf(timeNs * 0.000001).setScale(2, RoundingMode.HALF_EVEN) + " ms";
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
    }
}
