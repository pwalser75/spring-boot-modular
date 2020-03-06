package ch.frostnova.spring.boot.platform.aspect;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to explicitly <b>enable</b> performance logging for specific classes or methods.
 *
 * @author Peter Walser
 * @since 2018-11-02
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface PerformanceLogging {
}
