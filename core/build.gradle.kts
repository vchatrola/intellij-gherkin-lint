plugins {
    id("java")
}

group = rootProject.group
version = rootProject.version

dependencies {
    implementation("com.fasterxml.jackson.core:jackson-databind:2.17.0")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.17.+")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}
