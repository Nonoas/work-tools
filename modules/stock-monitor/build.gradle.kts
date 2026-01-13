plugins {
    id("org.jetbrains.kotlin.jvm")
    id("org.openjfx.javafxplugin")
}

dependencies {
    implementation("org.apache.logging.log4j:log4j-core:2.20.0")
    implementation ("org.apache.httpcomponents:httpclient:4.5.14")
    implementation ("org.apache.commons:commons-text:1.10.0")
    implementation ("com.google.code.gson:gson:2.8.9")
    implementation ("com.fasterxml.jackson.core:jackson-databind:2.17.2")
    implementation ("dev.langchain4j:langchain4j-open-ai:1.0.0-beta3")
    implementation ("dev.langchain4j:langchain4j:1.0.0-beta3")
    implementation("one.jpro.platform:jpro-mdfx:0.5.7")
}