package ch.frostnova.app.boot.platform.aspect;

import org.junit.Test;

/**
 * Test for {@link PerformanceLoggingAspect}
 */
public class PerformanceLoggingAspectTest {

    @Test
    public void testLog() throws Exception {

        PerformanceLoggingAspect.PerformanceLoggingContext context = PerformanceLoggingAspect.PerformanceLoggingContext.current();

        context.enter("Test.a()");
        Thread.sleep(50);

        context.enter("Test.b()");
        Thread.sleep(100);

        for (int i = 0; i < 3; i++) {
            context.enter("Test.c()");
            Thread.sleep(10);
            context.exit();
        }

        context.enter("Test.d()");
        context.exit(new ArithmeticException());

        context.exit(new IllegalArgumentException());

        context.enter("Test.e()");
        Thread.sleep(25);
        context.exit(null);

        context.exit();

        context.enter("Other.x()");
        Thread.sleep(2);
        context.enter("Other.y()");
        Thread.sleep(3);
        context.enter("Other.z()");
        Thread.sleep(1);
        context.exit();
        context.exit();
        context.exit();

    }
}
