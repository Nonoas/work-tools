import org.gradle.kotlin.dsl.version
import org.openjfx.gradle.JavaFXOptions
import org.jetbrains.dokka.gradle.DokkaTask
import org.gradle.internal.os.OperatingSystem

plugins {
    kotlin("jvm")
    id("java-library")
    id("org.openjfx.javafxplugin")
    id("maven-publish")
    id("signing")
    id("org.jetbrains.dokka") version "1.9.20"
    signing
}

val platformVersion: String by project
val kotlinVersion: String by project
val javafxClassifier = when {
    OperatingSystem.current().isWindows -> "win"
    OperatingSystem.current().isLinux -> "linux"
    OperatingSystem.current().isMacOsX -> if (System.getProperty("os.arch") == "aarch64") "mac-aarch64" else "mac"
    else -> error("Unsupported operating system for JavaFX: ${OperatingSystem.current().name}")
}

group = "io.github.nonoas"
version = platformVersion

repositories {
    maven("https://mirrors.huaweicloud.com/repository/maven/")
    maven("https://maven.aliyun.com/repository/central/")
    mavenCentral()
}

java {
    withSourcesJar()
}

tasks.withType<Javadoc> {
    (options as StandardJavadocDocletOptions).addStringOption("Xdoclint:none", "-quiet")
    options.encoding = "UTF-8"
}

val dokkaJavadocJar by tasks.registering(Jar::class) {
    archiveClassifier.set("javadoc")
    dependsOn(tasks.named("dokkaJavadoc"))
    from(tasks.named<DokkaTask>("dokkaJavadoc").flatMap { it.outputDirectory })
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            artifact(dokkaJavadocJar)

            artifactId = "work-tools-platform"

            pom {
                name.set(artifactId)
                description.set("A javafx plugin pretty platform")
                url.set("https://github.com/Nonoas/work-tools")

                licenses {
                    license {
                        name.set("The Apache License, Version 2.0")
                        url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }

                developers {
                    developer {
                        name.set("Nonoas")
                        email.set("nonoaswy@163.com")
                    }
                }

                scm {
                    connection.set("scm:git:git://github.com/Nonoas/work-tools.git")
                    developerConnection.set("scm:git:ssh://github.com:Nonoas/work-tools.git")
                    url.set("https://github.com/Nonoas/work-tools")
                }
            }
        }
    }
}

val gpgKeyName = (findProperty("signing.gnupg.keyName") ?: findProperty("signing.keyId")) as String?
val gpgPassphrase = (findProperty("signing.gnupg.passphrase") ?: findProperty("signing.password")) as String?
val gpgExecutable = (findProperty("signing.gnupg.executable") as String?) ?: "gpg"

if (gpgKeyName != null && findProperty("signing.gnupg.keyName") == null) {
    extra["signing.gnupg.keyName"] = gpgKeyName
}
if (gpgPassphrase != null && findProperty("signing.gnupg.passphrase") == null) {
    extra["signing.gnupg.passphrase"] = gpgPassphrase
}
if (findProperty("signing.gnupg.executable") == null) {
    extra["signing.gnupg.executable"] = gpgExecutable
}

signing {
    useGpgCmd()
    sign(publishing.publications["mavenJava"])
}

tasks.register("closeSonatypeStagingRepository") {
    group = "publishing"
    description = "Closes the Sonatype staging repository via the root nexus-publish task."
    dependsOn(rootProject.tasks.named("closeSonatypeStagingRepository"))
}

tasks.register("closeAndReleaseSonatypeStagingRepository") {
    group = "publishing"
    description = "Closes and releases the Sonatype staging repository via the root nexus-publish task."
    dependsOn(rootProject.tasks.named("closeAndReleaseSonatypeStagingRepository"))
}

tasks.register("publishPlatformReleaseToSonatype") {
    group = "publishing"
    description = "Publishes the platform module, then closes and releases the Sonatype staging repository."
    dependsOn("publishMavenJavaPublicationToSonatypeRepository")
    dependsOn("closeAndReleaseSonatypeStagingRepository")
}


val jfxVersion: String by project
val javafxModules = listOf(
    "base",
    "graphics",
    "controls",
    "swing",
)
the<JavaFXOptions>().apply {
    version = jfxVersion
    modules = listOf("javafx.controls", "javafx.swing", "javafx.graphics")
}

dependencies {
    javafxModules.forEach { module ->
        api(
            mapOf(
                "group" to "org.openjfx",
                "name" to "javafx-$module",
                "version" to jfxVersion,
                "classifier" to javafxClassifier,
                "ext" to "jar",
            )
        )
    }

    api("io.github.nonoas:jfx-flat-ui:2.0.0-SNAPSHOT") {
        exclude(group = "org.openjfx")
    }
    api("org.apache.logging.log4j:log4j-core:2.20.0")
    api("net.java.dev.jna:jna:5.12.1")
    api("net.java.dev.jna:jna-platform:5.12.1")

    implementation("com.melloware:jintellitype:1.4.0")
    implementation("org.jetbrains.kotlin:kotlin-stdlib:$kotlinVersion")
    implementation("org.quartz-scheduler:quartz:2.3.2")
    implementation("com.h2database:h2:2.2.220")
    implementation("com.alibaba.fastjson2:fastjson2:2.0.47")
    implementation("org.freemarker:freemarker:2.3.30")
    implementation("org.flywaydb:flyway-core:10.12.0")
    implementation("cn.hutool:hutool-db:5.8.25")
    implementation("com.googlecode.juniversalchardet:juniversalchardet:1.0.3")
    implementation("io.github.mkpaz:atlantafx-base:2.0.1") {
        exclude(group = "org.openjfx")
    }
    implementation("com.google.code.gson:gson:2.8.9")
    implementation("org.slf4j:slf4j-api:2.0.9")
    implementation("org.yaml:snakeyaml:2.2")
    implementation("dev.langchain4j:langchain4j-open-ai:1.0.0-beta3")
    implementation("dev.langchain4j:langchain4j:1.0.0-beta3")
}
