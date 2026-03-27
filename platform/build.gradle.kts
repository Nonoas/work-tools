import org.openjfx.gradle.JavaFXOptions

plugins {
    id("org.jetbrains.kotlin.jvm")
    id("org.openjfx.javafxplugin")
    id("maven-publish")
}

group = "indi.nonoas.worktools"
version = "1.0.0"

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"]) // 获取 java 组件
        }
    }
}

val jfxVersion: String by project
the<JavaFXOptions>().apply {
    version = jfxVersion
    modules = listOf("javafx.controls", "javafx.swing")
}

// platform 独有依赖
dependencies {
    api("io.github.nonoas:jfx-flat-ui:1.0.3")
    api("org.apache.logging.log4j:log4j-core:2.20.0")
    api("net.java.dev.jna:jna:5.12.1")
    api("net.java.dev.jna:jna-platform:5.12.1")

    implementation("com.melloware:jintellitype:1.4.0")
    implementation("org.jetbrains.kotlin:kotlin-stdlib:1.9.0")
    implementation("org.quartz-scheduler:quartz:2.3.2")
    implementation("com.h2database:h2:2.2.220")
    implementation("com.alibaba.fastjson2:fastjson2:2.0.47")
    implementation("org.freemarker:freemarker:2.3.30")
    implementation("org.flywaydb:flyway-core:10.12.0")
    implementation("cn.hutool:hutool-db:5.8.25")
    implementation("com.googlecode.juniversalchardet:juniversalchardet:1.0.3")
    implementation("io.github.mkpaz:atlantafx-base:2.0.1")
    implementation("com.google.code.gson:gson:2.8.9")
    implementation("org.slf4j:slf4j-api:2.0.9")
    implementation("org.yaml:snakeyaml:2.2")
    implementation("dev.langchain4j:langchain4j-open-ai:1.0.0-beta3")
    implementation("dev.langchain4j:langchain4j:1.0.0-beta3")
}
