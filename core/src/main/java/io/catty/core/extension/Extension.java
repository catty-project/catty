package io.catty.core.extension;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Indicate that this object is an extension.
 *
 * @see ExtensionFactory
 */
@Target({TYPE})
@Retention(RUNTIME)
@Documented
@Inherited
public @interface Extension {

  /**
   * Extension's name.
   *
   * @see ExtensionType
   */
  String value();

}
