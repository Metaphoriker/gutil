package de.godcipher.config;


/**
 * ConfigurationOption is a class that represents a configuration option with a value and a comment.
 *
 * @param <T> The type of the value.
 */
public class ConfigurationOption<T> {

  /** An empty configuration option. */
  public static ConfigurationOption<Object> EMPTY_OPTION = new ConfigurationOption<>("", "");

  private final T value;
  private final String comment;

  /**
   * Constructs a new ConfigurationOption with the specified value and comment.
   *
   * @param value The value of the option (cannot be null).
   * @param comment The comment for the option.
   * @throws IllegalArgumentException if the value is null.
   */
  public ConfigurationOption(T value, String comment) {
    if (value == null) {
      throw new IllegalArgumentException("Configuration option value cannot be null");
    }
    this.value = value;
    this.comment = comment;
  }

  /**
   * Retrieves the value of the configuration option.
   *
   * @return The value of the configuration option.
   */
  public T getValue() {
    return value;
  }

  /**
   * Retrieves the comment of the configuration option.
   *
   * @return The comment of the configuration option.
   */
  public String getComment() {
    return comment;
  }

  /**
   * Creates a new ConfigurationOption with the specified new value.
   *
   * @param newValue The new value to set.
   * @param clazz The class type of the new value.
   * @return A new ConfigurationOption with the new value.
   * @throws IllegalArgumentException if the new value is not of the expected type.
   */
  public ConfigurationOption<T> withNewValue(Object newValue, Class<T> clazz) {
    if (newValue == null) {
      throw new IllegalArgumentException("New value cannot be null");
    } else if (clazz.isInstance(newValue)) {
      return new ConfigurationOption<>(clazz.cast(newValue), this.comment);
    } else {
      throw new IllegalArgumentException(
          "Invalid type: expected "
              + clazz.getName()
              + " but got "
              + newValue.getClass().getName());
    }
  }
}
