package de.godcipher.config;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import de.godcipher.config.annotation.ConfigHeader;
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

/** BaseConfiguration is a class that manages configuration options and saves them to a file. */
public abstract class BaseConfiguration {

  private static final Gson GSON = new Gson();

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
      syncFieldsWithConfigOptions();
      for (Map.Entry<String, ConfigurationOption<?>> entry : configOptions.entrySet()) {
        String key = entry.getKey();
        ConfigurationOption<?> option = entry.getValue();
        writeComment(writer, option);
        writeValue(writer, key, option);
        writer.println();
      }
    } catch (IOException | IllegalAccessException e) {
      throw new IllegalStateException("Could not save configuration file: " + file.getName(), e);
    }
  }

  /**
   * Synchronizes the current field values with the configuration options.
   *
   * <p>This method iterates over the fields of the current class and its superclasses. For each
   * field annotated with {@link ConfigValue}, the current field value is retrieved using reflection
   * and the corresponding entry in the {@code configOptions} map is updated with this value.
   *
   * @throws IllegalAccessException if the field values cannot be accessed via reflection.
   */
  private void syncFieldsWithConfigOptions() throws IllegalAccessException {
    List<Class<?>> classHierarchy = getClassHierarchy();
    for (Class<?> clazz : classHierarchy) {
      for (Field field : clazz.getDeclaredFields()) {
        ConfigValue configValueAnnotation = field.getAnnotation(ConfigValue.class);
        if (configValueAnnotation != null) {
          field.setAccessible(true);
          Object fieldValue = field.get(this);
          ConfigurationOption<?> option =
              new ConfigurationOption<>(fieldValue, configValueAnnotation.description());
          configOptions.put(configValueAnnotation.name(), option);
        }
      }
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
   * Writes the header for the configuration file from the @ConfigHeader annotation if present.
   * Otherwise, writes a default header.
   *
   * @param writer The PrintWriter to write the header to the file.
   */
  private void writeConfigHeader(PrintWriter writer) {
    ConfigHeader headerAnnotation = this.getClass().getAnnotation(ConfigHeader.class);
    if (headerAnnotation != null) {
      String[] headerLines = headerAnnotation.value();
      for (String line : headerLines) {
        writeComment(writer, line);
      }
    } else {
      writeDefaultHeader(writer);
    }
    writer.println();
  }

  /**
   * Writes the default header to the configuration file.
   *
   * @param writer The PrintWriter to write the header to the file.
   */
  private void writeDefaultHeader(PrintWriter writer) {
    writeComment(writer, "Configuration File");
    writeComment(writer, "Generated by gutil");
  }

  /**
   * Writes the comment for a given configuration option.
   *
   * @param writer The PrintWriter to write the comment to the file.
   * @param key The configuration option.
   */
  private void writeValue(PrintWriter writer, String key, ConfigurationOption<?> option) {
    Object value = option.getValue();
    String serializedValue = GSON.toJson(value);
    writer.printf("%s: %s%n", key, serializedValue);
  }

  /**
   * Writes the comment for a given configuration option.
   *
   * @param writer The PrintWriter to write the comment to the file.
   * @param option The configuration option.
   */
  private void writeComment(PrintWriter writer, ConfigurationOption<?> option) {
    Configuration configAnnotation = retrieveConfigurationAnnotation();
    if (!option.getComment().isEmpty()) {
      writer.println(configAnnotation.commentPrefix() + " " + option.getComment());
    }
  }

  /**
   * Writes a comment to the configuration file.
   *
   * @param writer The PrintWriter to write the comment to the file.
   * @param comment The comment to write.
   */
  private void writeComment(PrintWriter writer, String comment) {
    Configuration configAnnotation = retrieveConfigurationAnnotation();
    writer.println(configAnnotation.commentPrefix() + " " + comment);
  }

  /**
   * Assigns a new value to a configuration option based on the properties file.
   *
   * @param field The field representing the configuration option.
   * @param newValue The value from the properties file.
   */
  private <T> void assignNewValue(Field field, String newValue) throws IllegalAccessException {
    Class<?> type = field.getType();
    try {
      Object value = GSON.fromJson(newValue, type);
      field.set(this, value);
    } catch (JsonSyntaxException e) {
      throw new IllegalArgumentException(
          "Unable to parse the configuration value for field: " + field.getName(), e);
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
