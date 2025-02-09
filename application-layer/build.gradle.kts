plugins {
    kotlin("plugin.spring")
    id("org.springframework.boot")
    id("io.spring.dependency-management")
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    implementation("org.springframework:spring-tx")

    // coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor:1.10.1")

    implementation(project(":domain-layer:performance-context"))
    implementation(project(":domain-layer:seat-area-context"))
    implementation(project(":domain-layer:user-context"))
    implementation(project(":domain-layer:reservation-context"))
    implementation(project(":domain-layer:seat-grade-context"))
    implementation(project(":domain-layer:payment-context"))
    implementation(project(":domain-layer:kopis-context"))
}

tasks {
    bootJar {
        enabled = false
    }
}
