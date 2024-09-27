package de.godcipher.config;

import de.godcipher.config.annotation.ConfigValue;
import de.godcipher.config.annotation.Configuration;
import java.util.List;

// A concrete class for testing the abstract BaseConfiguration class
@Configuration(fileName = "test-config.yml")
class TestConfiguration extends BaseConfiguration {

  @ConfigValue(name = "test-string", description = "Test string configuration")
  private String testString = "defaultValue";

  @ConfigValue(name = "test-int", description = "Test integer configuration")
  private int testInt = 123;

  @ConfigValue(name = "test-list", description = "Test list configuration")
  private List<String> testList = List.of("item1", "item2", "item3");

  @ConfigValue(name = "test-boolean", description = "Test boolean configuration")
  private boolean testBoolean = true;
}
