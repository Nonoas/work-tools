import org.jetbrains.kotlin.gradle.dsl.JvmTarget

group = "io.github.nonoas"
version = "1.0.0"

plugins {
    `kotlin-dsl`
    `maven-publish`
}

repositories {
    mavenCentral()
    gradlePluginPortal()
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    compilerOptions.jvmTarget.set(JvmTarget.JVM_17)
}

gradlePlugin {
    plugins {
        create("worktools-plugin") {
            id = "io.github.nonoas.worktools-plugin"
            implementationClass = "WorktoolsPluginConventionPlugin"
        }
    }
}

publishing {
    repositories {
        maven {
            name = "local"
            url = uri(layout.projectDirectory.dir("repo"))
        }
    }
}
