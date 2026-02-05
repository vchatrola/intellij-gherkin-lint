plugins {
    id("java")
}

group = rootProject.group
version = rootProject.version

dependencies {
    implementation("com.fasterxml.jackson.core:jackson-databind:2.17.0")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.17.+")
    testImplementation("org.junit.jupiter:junit-jupiter:6.0.2")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

tasks.test {
    useJUnitPlatform()
}
