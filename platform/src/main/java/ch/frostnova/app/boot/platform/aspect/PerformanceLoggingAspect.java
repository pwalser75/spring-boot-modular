package ch.frostnova.app.boot.platform.aspect;

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
 * 15:12:22.316 INFO  [main] | PerformanceLoggingAspect - Test.a() &rarr; 211.20 ms, self: 51.30 ms
 * &nbsp;&nbsp;&lfloor; Test.b() &rarr; java.lang.IllegalArgumentException, 134.04 ms, self: 102.04 ms
 * &nbsp;&nbsp;&nbsp;&nbsp;&lfloor; 5x Test.c() &rarr; 51.94 ms
 * &nbsp;&nbsp;&nbsp;&nbsp;&lfloor; Test.d() &rarr; java.lang.ArithmeticException, 0.03 ms
 * &nbsp;&nbsp;&lfloor; Test.e() &rarr; 25.86 ms
 * 15:12:22.339 INFO  [main] | PerformanceLoggingAspect - Other.x() &rarr; 12.55 ms, self: 2.57 ms
 * &nbsp;&nbsp;&lfloor; Other.y() &rarr; 9.98 ms, self: 7.92 ms
 * &nbsp;&nbsp;&nbsp;&nbsp;&lfloor; Other.z() &rarr; 2.06 ms
 * </code></pre>
 */
@Aspect
@Component
@Profile("performance-logging")
public class PerformanceLoggingAspect {

    private static Logger log = LoggerFactory.getLogger(PerformanceLoggingAspect.class);

    private final static String SYMBOL_INDENTATION = "+"; // unicode alternative: "\u2937"
    private final static String SYMBOL_RIGHT_ARROW = "->"; // unicode alternative: "\u2192"

    /**
     * Bind aspect to any Spring @Service, @Controller, @RestController and Repository
     *
     * @param joinPoint aspect join point
     * @return invocation result
     * @throws Throwable invocation exception
     */
    @Around("@within(org.springframework.stereotype.Service) " +
            "|| @within(org.springframework.stereotype.Controller) " +
            "|| @within(org.springframework.web.bind.annotation.RestController) " +
            "|| this(org.springframework.data.repository.Repository)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {

        String invocation = joinPoint.getSignature().toShortString();
        PerformanceLoggingContext.current().enter(invocation);

        Throwable error = null;
        try {
            return joinPoint.proceed();
        } catch (Throwable t) {
            throw error = t;
        } finally {
            PerformanceLoggingContext.current().exit(error);
        }
    }

    static class PerformanceLoggingContext {

        private final static ThreadLocal<PerformanceLoggingContext> current = new ThreadLocal<>();

        private final Deque<InvocationInfo> invocations = new LinkedList<>();
        private final Deque<InvocationInfo> invocationStack = new LinkedList<>();
        private final Deque<AtomicLong> nestedTime = new LinkedList<>();

        public static PerformanceLoggingContext current() {
            PerformanceLoggingContext context = PerformanceLoggingContext.current.get();
            if (context == null) {
                context = new PerformanceLoggingContext();
                PerformanceLoggingContext.current.set(context);
            }
            return context;
        }

        public void enter(String invocation) {
            long time = System.nanoTime();
            InvocationInfo invocationInfo = new InvocationInfo(invocationStack.size(), invocation, time);
            invocations.add(invocationInfo);
            invocationStack.push(invocationInfo);
            nestedTime.push(new AtomicLong());
        }

        public void exit() {
            exit(null);
        }

        public void exit(Throwable t) {
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

                PerformanceLoggingAspect.log.info(invocations.stream().map(InvocationInfo::toString).collect(Collectors.joining("\n")));
                invocations.clear();
                PerformanceLoggingContext.current.remove();
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
                builder.append(PerformanceLoggingAspect.SYMBOL_INDENTATION);
                builder.append(" ");
            }
            if (mergeCount > 1) {
                builder.append(mergeCount);
                builder.append("x ");
            }
            builder.append(invocation);
            builder.append(" ");
            builder.append(PerformanceLoggingAspect.SYMBOL_RIGHT_ARROW);
            builder.append(" ");
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

        private String formatTimeMs(long timeNs) {
            return BigDecimal.valueOf(timeNs * 0.000001).setScale(2, RoundingMode.HALF_EVEN) + " ms";
        }
    }
}
