package ch.frostnova.spring.boot.platform.aspect;


import org.junit.jupiter.api.Test;

import static java.lang.Thread.sleep;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

/**
 * Test for {@link PerformanceLoggingAspect}
 */
public class PerformanceLoggingAspectTest {

    @Test
    public void testExecuteLog() {

        PerformanceLoggingAspect.PerformanceLoggingContext context = PerformanceLoggingAspect.PerformanceLoggingContext.current();

        assertThatThrownBy(() ->
                context.execute("Test.a()", () -> {
                    sleep(50);
                    context.execute("Test.b()", () -> {
                        sleep(100);
                        for (int i = 0; i < 3; i++) {
                            context.execute("Test.c()", () -> {
                                sleep(10);
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
                })).isInstanceOf(IllegalArgumentException.class);

        context.execute("Test.e()", () -> {
            sleep(25);
        });

        context.execute("Other.x()", () -> {
            sleep(3);
            context.execute("Other.y()", () -> {
                sleep(2);
                context.execute("Other.z()", () -> {
                    sleep(1);
                });
            });
        });
    }
}
