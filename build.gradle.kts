plugins {
    kotlin("jvm") version "1.9.23"
}

group = "ru.pchurzin"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.0")
}