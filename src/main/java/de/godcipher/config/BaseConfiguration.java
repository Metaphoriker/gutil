package de.godcipher.config;

import de.godcipher.config.annotation.ConfigValue;
import de.godcipher.config.annotation.Configuration;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.stream.Collectors;

/** BaseConfiguration is a class that manages configuration options and saves them to a file. */
public abstract class BaseConfiguration {

  private final Map<String, ConfigurationOption<?>> configOptions = new LinkedHashMap<>();
  private final Properties properties = new Properties();

  private File file;

  /** Constructor for BaseConfiguration, uses the file name from the @Configuration annotation. */
  public BaseConfiguration() {
    Configuration configAnnotation = retrieveConfigurationAnnotation();
    this.file = new File(configAnnotation.fileName());
    createDirectoryIfNotExists(file.getParentFile());
  }

  /**
   * Loads the configuration file and updates internal options. Creates the configuration file if it
   * does not exist.
   */
  public void initialize() {
    reloadConfig();
    saveConfiguration();
  }

  /**
   * Set the directory where the configuration file should be saved.
   *
   * @param directory The directory path as a String or File
   */
  public void setDirectory(File directory) {
    if (directory == null) {
      throw new IllegalArgumentException("The directory must not be null");
    }

    Configuration configAnnotation = retrieveConfigurationAnnotation();
    this.file = new File(directory, configAnnotation.fileName());
    createDirectoryIfNotExists(directory);
  }

  /**
   * Retrieves the @Configuration annotation from the class.
   *
   * @return The Configuration annotation.
   */
  private Configuration retrieveConfigurationAnnotation() {
    Class<? extends BaseConfiguration> clazz = this.getClass();
    Configuration configAnnotation = clazz.getAnnotation(Configuration.class);
    if (Modifier.isAbstract(clazz.getModifiers())) {
      throw new IllegalStateException("Abstract classes cannot have @Configuration annotations.");
    }
    if (configAnnotation == null || configAnnotation.fileName().isEmpty()) {
      throw new IllegalStateException("Missing or empty @Configuration annotation with fileName.");
    }
    return configAnnotation;
  }

  /**
   * Adds a configuration option to the internal map.
   *
   * @param key The key to identify the configuration option.
   * @param option The configuration option to store.
   */
  private void setConfigOption(String key, ConfigurationOption<?> option) {
    if (key == null || option == null) {
      throw new IllegalArgumentException("Key and option must not be null");
    }
    configOptions.put(key, option);
  }

  /** Reloads the configuration from the file and updates internal options. */
  public void reloadConfig() {
    loadFileIfExists();
    loadConfigValues();
  }

  /** Saves the current configuration options to the file with comments. */
  public void saveConfiguration() {
    try (PrintWriter writer = new PrintWriter(new FileOutputStream(file))) {
      writeConfigHeader(writer);
      for (Map.Entry<String, ConfigurationOption<?>> entry : configOptions.entrySet()) {
        String key = entry.getKey();
        ConfigurationOption<?> option = entry.getValue();
        writeComment(writer, option);
        writeValue(writer, key, option);
        writer.println();
      }
    } catch (IOException e) {
      throw new IllegalStateException("Could not save configuration file: " + file.getName(), e);
    }
  }

  /**
   * Loads configuration values from the properties file and updates internal options. Scans the
   * class for fields annotated with @ConfigValue and updates their values.
   */
  private void loadConfigValues() {
    List<Class<?>> classHierarchy = getClassHierarchy();
    for (Class<?> clazz : classHierarchy) {
      processClassFields(clazz);
    }
  }

  /**
   * Retrieves the class hierarchy for the current class.
   *
   * @return A list of classes in the hierarchy.
   */
  private List<Class<?>> getClassHierarchy() {
    List<Class<?>> classHierarchy = new ArrayList<>();
    Class<?> clazz = this.getClass();
    while (clazz != null && clazz != Object.class) {
      classHierarchy.add(clazz);
      clazz = clazz.getSuperclass();
    }
    Collections.reverse(classHierarchy); // super classes first
    return classHierarchy;
  }

  /** Processes all fields in a class that are annotated with @ConfigValue. */
  private void processClassFields(Class<?> clazz) {
    for (Field field : clazz.getDeclaredFields()) {
      ConfigValue configValueAnnotation = field.getAnnotation(ConfigValue.class);
      if (configValueAnnotation != null) {
        processField(field, configValueAnnotation);
      }
    }
  }

  /**
   * Process an individual field that is annotated with @ConfigValue.
   *
   * @param field The field to process.
   * @param configValueAnnotation The annotation instance for this field.
   */
  private void processField(Field field, ConfigValue configValueAnnotation) {
    String key = configValueAnnotation.name();
    field.setAccessible(true);
    try {
      if (properties.containsKey(key)) {
        processExistingProperty(field, key, configValueAnnotation);
      } else {
        processDefaultValue(field, key, configValueAnnotation);
      }
    } catch (IllegalAccessException e) {
      throw new IllegalStateException("Unable to access field: " + field.getName(), e);
    }
  }

  /**
   * Processes a field that has a corresponding key in the properties file. Assigns the property
   * value to the field and creates a ConfigurationOption.
   */
  private void processExistingProperty(Field field, String key, ConfigValue configValueAnnotation)
      throws IllegalAccessException {
    String newValue = properties.getProperty(key);
    assignNewValue(field, newValue);
    ConfigurationOption<?> option =
        new ConfigurationOption<>(field.get(this), configValueAnnotation.description());
    setConfigOption(key, option);
  }

  /**
   * Processes a field that does not have a corresponding key in the properties file. Uses the
   * current field value or a default value to create a ConfigurationOption.
   */
  private void processDefaultValue(Field field, String key, ConfigValue configValueAnnotation)
      throws IllegalAccessException {
    Object fieldValue = field.get(this);
    ConfigurationOption<?> option;
    if (fieldValue != null) {
      option = new ConfigurationOption<>(fieldValue, configValueAnnotation.description());
    } else {
      option = new ConfigurationOption<>("", configValueAnnotation.description());
    }
    setConfigOption(key, option);
  }

  /** Loads the configuration from the file if it exists. */
  private void loadFileIfExists() {
    if (file.exists()) {
      try (FileInputStream fis = new FileInputStream(file)) {
        properties.load(fis);
      } catch (IOException e) {
        throw new IllegalStateException("Could not load configuration file: " + file.getName(), e);
      }
    } else {
      saveConfiguration();
    }
  }

  /**
   * Writes the header of the configuration file.
   *
   * @param writer The PrintWriter to write to the file.
   */
  private void writeConfigHeader(PrintWriter writer) {
    writer.println("# Configuration File");
    writer.println("# Generated by gutil");
    writer.println();
  }

  /**
   * Writes the comment for a given configuration option.
   *
   * @param writer The PrintWriter to write the comment to the file.
   * @param key The configuration option.
   */
  private void writeValue(PrintWriter writer, String key, ConfigurationOption<?> option) {
    Object value = option.getValue();
    if (value instanceof List) {
      @SuppressWarnings("unchecked")
      List<Object> list = (List<Object>) value;
      String formattedList =
          list.stream().map(Object::toString).collect(Collectors.joining(", ", "[", "]"));
      writer.printf("%s: %s%n", key, formattedList);
    } else {
      writer.printf("%s: %s%n", key, value.toString());
    }
  }

  /**
   * Writes the comment for a given configuration option.
   *
   * @param writer The PrintWriter to write the comment to the file.
   * @param option The configuration option.
   */
  private void writeComment(PrintWriter writer, ConfigurationOption<?> option) {
    if (!option.getComment().isEmpty()) {
      writer.println("# " + option.getComment());
    }
  }

  /**
   * Assigns a new value to a configuration option based on the properties file.
   *
   * @param field The field representing the configuration option.
   * @param newValue The value from the properties file.
   */
  private <T> void assignNewValue(Field field, String newValue) throws IllegalAccessException {
    Class<?> type = field.getType();
    if (type == String.class) {
      field.set(this, newValue);
    } else if (type == int.class || type == Integer.class) {
      field.set(this, Integer.parseInt(newValue));
    } else if (type == long.class || type == Long.class) {
      field.set(this, Long.parseLong(newValue));
    } else if (type == double.class || type == Double.class) {
      field.set(this, Double.parseDouble(newValue));
    } else if (type == float.class || type == Float.class) {
      field.set(this, Float.parseFloat(newValue));
    } else if (type == boolean.class || type == Boolean.class) {
      field.set(this, Boolean.parseBoolean(newValue));
    } else if (List.class.isAssignableFrom(type)) {
      newValue = newValue.replace("[", "").replace("]", "");
      List<String> listValues = Arrays.asList(newValue.split(",\\s*"));
      field.set(this, listValues);
    } else {
      throw new IllegalArgumentException("Unsupported field type: " + type.getName());
    }
  }

  /**
   * Ensures the parent directory exists; creates it if necessary.
   *
   * @param directory The directory to check or create.
   */
  private void createDirectoryIfNotExists(File directory) {
    if (directory != null && !directory.exists()) {
      directory.mkdirs();
    }
  }
}
