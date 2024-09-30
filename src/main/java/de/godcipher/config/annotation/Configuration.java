package de.godcipher.config.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Configuration {
  /**
   * Specifies the fully qualified name of the configuration file.
   *
   * <p>This represents the file name, including the extension.
   *
   * <p>Example usage:
   *
   * <pre>
   *     {@code fileName = "config.yml"}
   * </pre>
   */
  String fileName();

  /**
   * Specifies the prefix to be used for comments in the configuration file.
   *
   * <p>This prefix is applied to comments in the configuration upfront.
   *
   * <p>Example usage:
   *
   * <pre>
   *     {@code commentPrefix = "#"}
   * </pre>
   */
  String commentPrefix() default "#"; // Default to YAML-style comments
}
