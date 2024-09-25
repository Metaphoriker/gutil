# Feel free to add any utility classes you want to share with others!

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
    <groupId>de.godcipher</groupId>
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
    implementation 'de.godcipher:gutil:VERSION'
}
```