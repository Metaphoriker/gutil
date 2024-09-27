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
<groupId>com.github.godcipher</groupId>
<artifactId>gutil</artifactId>
<version>1.0</version>
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
    implementation 'com.github.godcipher:gutil:VERSION'
}
```

## Usage

### List Paginator

```java

import de.godcipher.pagination.ListPaginator;

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
import de.godcipher.config.BaseConfiguration;
import de.godcipher.config.annotation.ConfigValue;
import de.godcipher.config.annotation.Configuration;
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
