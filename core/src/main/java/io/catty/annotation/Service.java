package io.catty.annotation;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Indicate that an interface is an RPC-service. @Service contains some useful config.
 */
@Target({TYPE})
@Retention(RUNTIME)
@Documented
@Inherited
public @interface Service {

  /**
   * Specify the service's name, if equals "", default name ({@link Class#getName()}) will be used
   * as this service's name.
   */
  String name() default "";

  /**
   * The version of this service.
   * todo : more detail about version rule.
   */
  String version() default "1.0.0";

  /**
   * The group of this service.
   */
  String group() default "";

}
