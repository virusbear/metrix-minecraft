import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    kotlin("jvm")
    kotlin("plugin.serialization") version "1.7.20"
    id("com.github.johnrengelman.shadow")
}

dependencies {
    api(kotlin("stdlib"))

    "shadow"("org.apache.logging.log4j:log4j-api:2.11.2")

    api("io.ktor:ktor-server-netty:1.6.1")
    api("io.ktor:ktor-server-core:1.6.1")
    api("io.ktor:ktor-metrics-micrometer:1.6.8")
    api("io.micrometer:micrometer-registry-prometheus:1.10.5")

    api("com.virusbear.metrix:metrix-micrometer:0.0.2")
    api("com.virusbear.metrix:metrix-api:0.0.2")

    api("org.jetbrains.kotlinx:kotlinx-serialization-properties:1.5.0")
}

val api by configurations.getting

tasks.withType<ShadowJar> {
    configurations = listOf(api)
    archiveBaseName.set("metrix-core")
}