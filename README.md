# 🔧 gutil

Feel free to add any utility classes/functions you want to share with others!

## Installation

### Maven

```xml

<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>

<dependency>
    <groupId>com.github.Metaphoriker</groupId>
    <artifactId>gutil</artifactId>
    <version>VERSION</version>
</dependency>
```

### Gradle

```groovy
allprojects {
    repositories {
        maven { url 'https://jitpack.io' }
    }
}

dependencies {
    implementation 'com.github.Metaphoriker:gutil:VERSION'
}
```

## Usage

### List Paginator

```java

import de.godcipher.gutil.pagination.ListPaginator;

import java.util.List;

public static void main(String[] args) {
    List<String> list = List.of("1", "2", "3", "4", "5");
    ListPaginator<String> paginator = new ListPaginator<>(list, 2);

    System.out.println(paginator.getPage(0)); // [1, 2]
    System.out.println(paginator.getPage(1)); // [3, 4]
    System.out.println(paginator.getPage(2)); // [5]
}
```

### Configuration

Here is a simple test configuration. The values are automatically loaded from the configuration file if it exists,
otherwise the default values are used for first generation.

```java
import de.godcipher.gutil.config.BaseConfiguration;
import de.godcipher.gutil.config.annotation.ConfigValue;
import de.godcipher.gutil.config.annotation.Configuration;

import java.util.List;

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
```

After that you can initialize the configuration and use it like this:

```java
public static void main(String[] args) {
    TestConfiguration config = new TestConfiguration();
    config.initialize(); // loads the configuration from file or creates a new one

    System.out.println(config.getTestString());
    System.out.println(config.getTestInt());
    System.out.println(config.getTestDouble());
    System.out.println(config.getTestLong());
    System.out.println(config.getTestFloat());
    System.out.println(config.getTestList());
    System.out.println(config.isTestBoolean());
}
```
