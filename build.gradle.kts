plugins {
    java
    id("org.springframework.boot") version "4.0.1"
    id("io.spring.dependency-management") version "1.1.7"
}

group = "com.milo"
version = "0.0.1-SNAPSHOT"
description = "AtiperaRecruitmentTask"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-restclient")
    implementation("org.springframework.boot:spring-boot-starter-webmvc")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.wiremock.integrations:wiremock-spring-boot:3.0.1")
}

tasks.withType<Test> {
    useJUnitPlatform()
}
