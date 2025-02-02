plugins {
    kotlin("plugin.spring")
    id("org.springframework.boot")
    id("io.spring.dependency-management")
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    implementation("org.springframework:spring-tx")

    implementation(project(":domain-layer:performance-context"))
    implementation(project(":domain-layer:seat-context"))
    implementation(project(":domain-layer:user-context"))
    implementation(project(":domain-layer:reservation-context"))
    implementation(project(":domain-layer:discount-context"))
    implementation(project(":domain-layer:payment-context"))
}

tasks {
    bootJar {
        enabled = false
    }
}
