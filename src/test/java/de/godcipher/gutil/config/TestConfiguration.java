package de.godcipher.gutil.config;

import de.godcipher.gutil.config.BaseConfiguration;
import de.godcipher.gutil.config.annotation.ConfigValue;
import de.godcipher.gutil.config.annotation.Configuration;
import java.util.List;

// A concrete class for testing the abstract BaseConfiguration class
@Configuration(fileName = "test-config.yml")
class TestConfiguration extends BaseConfiguration {

  @ConfigValue(name = "test-string", description = "Test string configuration")
  private String testString = "defaultValue";

  @ConfigValue(name = "test-int", description = "Test integer configuration")
  private int testInt = 123;

  @ConfigValue(name = "test-double", description = "Test double configuration")
  private double testDouble = 123.456;

  @ConfigValue(name = "test-long", description = "Test long configuration")
  private long testLong = 1234567890L;

  @ConfigValue(name = "test-float", description = "Test float configuration")
  private float testFloat = 123.456f;

  @ConfigValue(name = "test-list", description = "Test list configuration")
  private List<String> testList = List.of("item1", "item2", "item3");

  @ConfigValue(name = "test-boolean", description = "Test boolean configuration")
  private boolean testBoolean = true;
}
