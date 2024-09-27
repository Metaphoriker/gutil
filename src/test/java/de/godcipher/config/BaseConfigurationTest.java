package de.godcipher.config;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.lang.reflect.Field;
import java.util.Arrays;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class BaseConfigurationTest {

  private TestConfiguration config;

  @BeforeEach
  void setUp() {
    config = new TestConfiguration();
  }

  @Test
  void testStringFieldLoadAndSave() throws Exception {
    Field stringField = TestConfiguration.class.getDeclaredField("testString");
    stringField.setAccessible(true);

    assertEquals("defaultValue", stringField.get(config));

    stringField.set(config, "newValue");
    config.saveConfiguration();

    config.reloadConfig();
    assertEquals("newValue", stringField.get(config));
  }

  @Test
  void testIntegerFieldLoadAndSave() throws Exception {
    Field intField = TestConfiguration.class.getDeclaredField("testInt");
    intField.setAccessible(true);

    assertEquals(123, intField.get(config));

    intField.set(config, 100);
    config.saveConfiguration();

    config.reloadConfig();
    assertEquals(100, intField.get(config));
  }

  @Test
  void testBooleanFieldLoadAndSave() throws Exception {
    Field booleanField = TestConfiguration.class.getDeclaredField("testBoolean");
    booleanField.setAccessible(true);

    assertTrue((boolean) booleanField.get(config));

    booleanField.set(config, false);
    config.saveConfiguration();

    config.reloadConfig();
    assertFalse((boolean) booleanField.get(config));
  }

  @Test
  void testListFieldLoadAndSave() throws Exception {
    Field listField = TestConfiguration.class.getDeclaredField("testList");
    listField.setAccessible(true);

    assertEquals(Arrays.asList("item1", "item2", "item3"), listField.get(config));

    listField.set(config, Arrays.asList("newItem1", "newItem2"));
    config.saveConfiguration();

    config.reloadConfig();
    assertEquals(Arrays.asList("newItem1", "newItem2"), listField.get(config));
  }

  @Test
  void testFileCreationAndLoading() {
    File configFile = new File("test-config.yml");
    assertTrue(configFile.exists(), "Config file should be created initially.");

    assertTrue(configFile.delete(), "Config file should be deleted.");

    config.reloadConfig();

    assertTrue(configFile.exists(), "Config file should be recreated after reloadConfig().");
  }
}
