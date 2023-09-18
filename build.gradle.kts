import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "1.8.22"
    id("org.jetbrains.intellij") version "1.14.1"
}

group = "com.zys"

repositories {
    mavenCentral()
}

dependencies {
    compileOnly("org.projectlombok:lombok:1.18.24")
    annotationProcessor("org.projectlombok:lombok:1.18.24")
    implementation("cn.hutool:hutool-all:5.8.11")
}

// Configure Gradle IntelliJ Plugin
// Read more: https://plugins.jetbrains.com/docs/intellij/tools-gradle-intellij-plugin.html
intellij {
    version.set("2022.2.5")
    type.set("IC") // Target IDE Platform

    plugins.set(listOf("java", "properties", "yaml", "Kotlin", "gradle"))
}

tasks {
    // Set the JVM compatibility versions
    withType<JavaCompile> {
        options.encoding = Charsets.UTF_8.name()
        sourceCompatibility = JavaVersion.VERSION_17.toString()
        targetCompatibility = JavaVersion.VERSION_17.toString()
    }
    withType<KotlinCompile> {
        kotlinOptions.jvmTarget = JavaVersion.VERSION_17.toString()
    }

    withType<JavaExec> {
        systemProperty("file.encoding", Charsets.UTF_8.name())
    }

    patchPluginXml {
        sinceBuild.set("222")
        untilBuild.set("232.*")
    }



    signPlugin {
        certificateChain.set(System.getenv("CERTIFICATE_CHAIN"))
        privateKey.set(System.getenv("PRIVATE_KEY"))
        password.set(System.getenv("PRIVATE_KEY_PASSWORD"))
    }

    publishPlugin {
        token.set(System.getenv("PUBLISH_TOKEN"))
    }
}
