plugins {
    id("org.springframework.boot")
    id("io.spring.dependency-management")
    java
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

dependencies {
    implementation(project(":spring-bybit-data-exporter-starter"))
    runtimeOnly("org.postgresql:postgresql")
}
