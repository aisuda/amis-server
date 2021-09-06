import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.5.30"
    application
    jacoco
}

group = "com.baidu"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.fasterxml.jackson.core:jackson-databind:2.12.5")
    implementation("commons-validator:commons-validator:1.7")
    implementation("org.apache.commons:commons-lang3:3.12.0")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5:1.5.21")
}

jacoco {
    toolVersion = "0.8.7"
}

tasks.test {
    useJUnitPlatform()
    finalizedBy(tasks.jacocoTestReport)
}

tasks.jacocoTestReport {
    dependsOn(tasks.test) // tests are required to run before generating the report
    reports {
        xml.required.set(false)
        csv.required.set(false)
        html.outputLocation.set(layout.buildDirectory.dir("jacocoHtml"))
    }
}

tasks.withType<KotlinCompile>() {
    kotlinOptions.jvmTarget = "1.8"
}

tasks.withType<JavaCompile>() {
    sourceCompatibility = "1.8"
    targetCompatibility = "1.8"
}

application {

}