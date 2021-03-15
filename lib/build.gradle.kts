group = "com.github.MozarellaMan"
version = "0.0.12"

val ktorVersion = "1.5.0"
val fuelVersion  = "2.3.1"
val lsp4jVersion  = "0.11.0-SNAPSHOT"
plugins {
    // Apply the org.jetbrains.kotlin.jvm Plugin to add support for Kotlin.
    id("org.jetbrains.kotlin.jvm") version "1.4.20"
    kotlin("plugin.serialization") version "1.4.10"
    id("maven")
    // Apply the java-library plugin for API and implementation separation.
    `java-library`
}

repositories {
    // Use JCenter for resolving dependencies.
    google()
    jcenter()
    maven(url = "https://oss.sonatype.org/content/repositories/snapshots")
}

tasks.jar {
    archiveBaseName.set("lsp_proxy_tools")
    manifest {
        attributes(mapOf("Implementation-Title" to project.name,
                "Implementation-Version" to project.version))
    }
}

java {
    withSourcesJar()
}

dependencies {
    implementation(platform("org.jetbrains.kotlin:kotlin-bom"))
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("com.google.guava:guava:29.0-jre")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:1.0.1")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.0.1")
    implementation ("org.eclipse.lsp4j:org.eclipse.lsp4j:0.11.0-SNAPSHOT")
    implementation ("org.eclipse.lsp4j:org.eclipse.lsp4j.jsonrpc:$lsp4jVersion")
    implementation ("org.eclipse.lsp4j:org.eclipse.lsp4j.websocket:$lsp4jVersion")
    implementation("com.github.kittinunf.fuel:fuel:$fuelVersion")
    implementation("com.github.kittinunf.fuel:fuel-kotlinx-serialization:$fuelVersion")
    implementation("com.github.kittinunf.fuel:fuel-coroutines:$fuelVersion")
    implementation ("io.ktor:ktor-client-websockets:$ktorVersion")
    implementation ("io.ktor:ktor-client-cio:$ktorVersion")
    implementation ("com.google.code.gson:gson:2.8.6")
    testImplementation("org.jetbrains.kotlin:kotlin-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit")
    testImplementation("org.skyscreamer:jsonassert:1.5.0")
    api("org.apache.commons:commons-math3:3.6.1")
}
