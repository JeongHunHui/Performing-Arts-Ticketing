plugins {
    kotlin("jvm") version "1.9.25"
    kotlin("plugin.spring") version "1.9.25" apply false
    kotlin("plugin.jpa") version "1.9.25" apply false
    id("org.springframework.boot") version "3.4.1" apply false
    id("io.spring.dependency-management") version "1.1.7" apply false

    // ktlint
    id("org.jlleitschuh.gradle.ktlint") version "12.1.2"
    id("jacoco")
    id("jacoco-report-aggregation")
}

dependencies {
    jacocoAggregation(project(":domain-layer:performance-context"))
    jacocoAggregation(project(":domain-layer:seat-area-context"))
}

allprojects {
    group = "com.hunhui"
    version = "1.0.0-SNAPSHOT"

    repositories {
        mavenCentral()
    }
}

subprojects {
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "org.jlleitschuh.gradle.ktlint")
    apply(plugin = "jacoco")

    kotlin {
        compilerOptions {
            freeCompilerArgs.addAll("-Xjsr305=strict")
        }
    }

    java {
        toolchain {
            languageVersion = JavaLanguageVersion.of(21)
        }
    }

    tasks.withType<Test> {
        useJUnitPlatform()
        finalizedBy(tasks.jacocoTestReport)
    }

    tasks.jacocoTestReport {
        dependsOn(tasks.test)
        reports {
            html.required.set(true)
            xml.required.set(true)
            csv.required.set(false)
        }
    }

    afterEvaluate {
        if (name != "common") {
            dependencies {
                implementation(project(":common"))
            }
        }
    }

    // 공통 의존성
    dependencies {
        testImplementation("org.jetbrains.kotlin:kotlin-test-junit5:1.9.25")
        testRuntimeOnly("org.junit.platform:junit-platform-launcher:1.11.3")
        testImplementation("io.mockk:mockk:1.13.16")
    }
}
