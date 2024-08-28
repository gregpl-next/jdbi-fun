import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  kotlin("jvm") version "2.0.20"
  id("org.jetbrains.kotlin.plugin.jpa") version "2.0.20"
}

repositories {
  mavenCentral()
}

dependencies {
  implementation(kotlin("stdlib"))
  implementation("com.h2database:h2:2.1.210")
  implementation("org.apache.logging.log4j:log4j-api:2.20.0")
  implementation("org.apache.logging.log4j:log4j-core:2.20.0")
  implementation("org.apache.logging.log4j:log4j-slf4j-impl:2.20.0")
  implementation("org.hibernate:hibernate-core:6.5.2.Final")
  implementation("org.jdbi:jdbi3-core:3.45.4")
  implementation("org.jdbi:jdbi3-kotlin:3.45.4")
  implementation("org.jdbi:jdbi3-sqlobject:3.45.4")
  implementation("org.jdbi:jdbi3-kotlin-sqlobject:3.45.4")
  implementation("org.jetbrains.kotlin:kotlin-reflect")

  testImplementation("com.nhaarman:expect.kt:1.0.1")
  testImplementation("org.apache.logging.log4j:log4j-jul:2.20.0")
  testImplementation("org.junit.jupiter:junit-jupiter-api:5.7.0")
  testImplementation("org.junit.jupiter:junit-jupiter-params:5.7.0")
  testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.7.0")
}

java {
  sourceCompatibility = JavaVersion.VERSION_21
  targetCompatibility = JavaVersion.VERSION_21
}

tasks.withType<KotlinCompile>().configureEach {
  compilerOptions {
    jvmTarget.set(JvmTarget.JVM_21)
  }
}

tasks.test {
  useJUnitPlatform()
  testLogging {
    events("passed", "skipped", "failed")
    showStandardStreams = true // Ensure that the standard output and error streams are shown
  }
}
