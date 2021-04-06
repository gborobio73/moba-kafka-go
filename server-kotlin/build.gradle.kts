import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

group = "com.reaktor.kafka"

plugins {
  java
  kotlin("jvm") version "1.3.11"
}

java {
  sourceCompatibility = JavaVersion.VERSION_1_8
  targetCompatibility = JavaVersion.VERSION_1_8
}

dependencies {
  implementation(kotlin("stdlib"))
  implementation(kotlin("reflect"))

  implementation("org.apache.kafka:kafka-clients:2.1.0")
  implementation("org.apache.kafka:kafka-streams:2.1.0")
  implementation("org.apache.kafka:connect-runtime:2.1.0")
  implementation("io.confluent:kafka-json-serializer:5.0.1")
  implementation("org.slf4j:slf4j-api:1.7.6")
  implementation("org.slf4j:slf4j-log4j12:1.7.6")
  implementation("com.fasterxml.jackson.core:jackson-databind:[2.8.11.1,)")
  implementation("com.google.code.gson:gson:2.2.4")

  implementation("org.glassfish.jersey.containers:jersey-container-servlet:2.31")
  implementation("org.glassfish.jersey.media:jersey-media-json-jackson:2.31")
}

repositories {
  jcenter()
  maven(url = "http://packages.confluent.io/maven/")
}

val mainClass: String by project
val input: String by project
val output: String by project
val port: String by project

tasks.withType<KotlinCompile> {
  kotlinOptions.jvmTarget = "1.8"
}

task("runApp", JavaExec::class) {
  classpath = sourceSets["main"].runtimeClasspath
  main = mainClass
  args = listOf(input, output, port)
}