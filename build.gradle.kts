import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

fun properties(key: String) = providers.gradleProperty(key)

plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "1.8.22"
    id("org.jetbrains.intellij") version "1.14.1"
}

group = properties("plugin.group").get()
version = properties("plugin.version").get()

repositories {
    mavenCentral()
}

dependencies {
    compileOnly("org.projectlombok:lombok:1.18.26")
    annotationProcessor("org.projectlombok:lombok:1.18.26")
    implementation("cn.hutool:hutool-http:5.8.22")
    implementation("cn.hutool:hutool-json:5.8.22")
    implementation("org.apache.velocity:velocity-engine-core:2.3") {
        exclude("org.slf4j")
    }
}

// Configure Gradle IntelliJ Plugin
// Read more: https://plugins.jetbrains.com/docs/intellij/tools-gradle-intellij-plugin.html
intellij {
    version = properties("platform.version").get()
    type = properties("platform.type").get()
    plugins = properties("platform.plugins").map { it.split(',').map(String::trim).filter(String::isNotEmpty) }
}

tasks {
    wrapper {
        gradleVersion = properties("gradle.version").get()
    }

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
        untilBuild.set("233.*")
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
