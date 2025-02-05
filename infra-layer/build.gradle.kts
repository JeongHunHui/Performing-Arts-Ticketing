plugins {
    kotlin("plugin.spring")
    id("org.springframework.boot")
    id("io.spring.dependency-management")
    kotlin("plugin.jpa")
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    implementation("org.jetbrains.kotlin:kotlin-reflect:1.9.25")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.18.2")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.18.2")

    implementation(project(":domain-layer:performance-context"))
    implementation(project(":domain-layer:seat-area-context"))
    implementation(project(":domain-layer:user-context"))
    implementation(project(":domain-layer:reservation-context"))
    implementation(project(":domain-layer:seat-grade-context"))
    implementation(project(":domain-layer:payment-context"))

    // jpa
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")

    // mysql
    runtimeOnly("com.mysql:mysql-connector-j")
}

tasks {
    bootJar {
        enabled = false
    }
}
