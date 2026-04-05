plugins {
    java
    id("io.github.nonoas.worktools-plugin") version "1.0.0"
}

worktools {
    id = "test-plugin"
    name = "Test Plugin"
    version = "1.0.0"
    mainClass = "com.test.TestPluginService"
    description = "A simple test plugin"
    targetPlatform("1.0.0-SNAPSHOT")
}

repositories {
    mavenLocal()
    mavenCentral()
}
