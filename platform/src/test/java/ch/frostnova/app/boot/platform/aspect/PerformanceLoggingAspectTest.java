package ch.frostnova.app.boot.platform.aspect;


import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Test for {@link PerformanceLoggingAspect}
 */
public class PerformanceLoggingAspectTest {

    @Test
    public void testExecuteLog() {

        PerformanceLoggingAspect.PerformanceLoggingContext context = PerformanceLoggingAspect.PerformanceLoggingContext.current();

        Assertions.assertThrows(IllegalArgumentException.class, () ->
                context.execute("Test.a()", () -> {
                    Thread.sleep(50);
                    context.execute("Test.b()", () -> {
                        Thread.sleep(100);
                        for (int i = 0; i < 3; i++) {
                            context.execute("Test.c()", () -> {
                                Thread.sleep(10);
                            });
                        }
                        try {
                            context.execute("Test.d()", () -> {
                                throw new ArithmeticException();
                            });
                        } catch (ArithmeticException ex) {
                            throw new IllegalArgumentException(ex);
                        }
                    });
                }));

        context.execute("Test.e()", () -> {
            Thread.sleep(25);
        });

        context.execute("Other.x()", () -> {
            Thread.sleep(3);
            context.execute("Other.y()", () -> {
                Thread.sleep(2);
                context.execute("Other.z()", () -> {
                    Thread.sleep(1);
                });
            });
        });
    }
}
