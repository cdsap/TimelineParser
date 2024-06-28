plugins {
    id("java")
    kotlin("jvm") version "1.9.20"
    id("io.github.cdsap.fatbinary") version "1.0"
    id("org.jlleitschuh.gradle.ktlint") version "11.5.1"
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

fatBinary {
    mainClass = "io.github.cdsap.parsetimeline.Main"
    name = "parsetimeline"
}

dependencies {
    implementation("io.github.cdsap:comparescans:0.1.1")
    implementation("com.github.ajalt.clikt:clikt:3.5.0")
    implementation("com.google.code.gson:gson:2.8.9")
    implementation("io.github.cdsap:geapi-data:0.2.7")
    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    implementation(kotlin("stdlib-jdk8"))
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(17)
}
