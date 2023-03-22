import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.7.20" apply false
    id("com.github.johnrengelman.shadow") version "7.1.0" apply false
}

allprojects {
    group = "com.virusbear.metrix.minecraft"
    version = "1.0-SNAPSHOT"

    repositories {
        mavenCentral()
    }

    tasks.withType<KotlinCompile> {
        kotlinOptions.jvmTarget = "16"
    }
}