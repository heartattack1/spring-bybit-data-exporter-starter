plugins {
    id("org.springframework.boot") version "3.3.5" apply false
    id("io.spring.dependency-management") version "1.1.6" apply false
    java
}

group = "com.example"
version = "0.1.0"

subprojects {
    repositories {
        mavenCentral()
    }
}
