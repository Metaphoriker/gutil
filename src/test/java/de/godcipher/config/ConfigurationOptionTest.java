package de.godcipher.config;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class ConfigurationOptionTest {

  @Test
  public void testEmptyOption() {
    ConfigurationOption<Object> emptyOption = ConfigurationOption.EMPTY_OPTION;
    assertEquals(emptyOption.getValue(), "");
    assertEquals(emptyOption.getComment(), "");
  }

  @Test
  public void testGetValueWithString() {
    ConfigurationOption<String> option = new ConfigurationOption<>("Test Value", "Test Comment");
    assertEquals("Test Value", option.getValue());
    assertEquals("Test Comment", option.getComment());
  }

  @Test
  public void testGetValueWithInteger() {
    ConfigurationOption<Integer> option = new ConfigurationOption<>(42, "Integer Comment");
    assertEquals(42, option.getValue());
    assertEquals("Integer Comment", option.getComment());
  }

  @Test
  void testNullValueThrowsException() {
    assertThrows(
        IllegalArgumentException.class,
        () -> new ConfigurationOption<>(null, "This should throw an exception"));

    ConfigurationOption<String> option = new ConfigurationOption<>("value", "Valid string option");
    assertThrows(IllegalArgumentException.class, () -> option.withNewValue(null, String.class));
  }

  @Test
  public void testWithNewValueValidString() {
    ConfigurationOption<String> option = new ConfigurationOption<>("Old Value", "Old Comment");
    ConfigurationOption<String> newOption = option.withNewValue("New Value", String.class);
    assertEquals("New Value", newOption.getValue());
    assertEquals("Old Comment", newOption.getComment());
  }

  @Test
  public void testWithNewValueValidInteger() {
    ConfigurationOption<Integer> option = new ConfigurationOption<>(100, "Initial Comment");
    ConfigurationOption<Integer> newOption = option.withNewValue(200, Integer.class);
    assertEquals(200, newOption.getValue());
    assertEquals("Initial Comment", newOption.getComment());
  }

  @Test
  public void testWithNewValueInvalidType() {
    ConfigurationOption<String> option = new ConfigurationOption<>("Old Value", "Old Comment");
    assertThrows(IllegalArgumentException.class, () -> option.withNewValue(123, String.class));
  }

  @Test
  public void testWithNewValueValidTypeButNullValue() {
    ConfigurationOption<String> option =
        new ConfigurationOption<>("Non-null Value", "Non-null Comment");
    assertThrows(IllegalArgumentException.class, () -> option.withNewValue(null, String.class));
  }
}
