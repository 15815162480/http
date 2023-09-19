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
        changeNotes.set("""
<b>1.0.2</b><br>
<ol>
    <li>
        fix bugs: Package and class are at the same level, class node is missing
        <br>
        bug 修复: 包和类处于同一级别，类节点丢失
    </li>
    <li>
        fix bugs: choose a new request, the older request do not clear the response editor
        <br>
        bug 修复: 选择一个新节点, 旧节点的请求结果没有清除
    </li>
</ol>
<b>1.0.1</b><br>
<ol>
    <li>Change plugin name —— ApiTool<br>更改插件名为 ApiTool</li>
</ol>
<b>1.0.0</b><br>
<ol>
    <li>
        Read all annotation request interface methods in Spring MVC
        <br>
        读取 Spring MVC 中所有注解请求接口方法
    </li>
    <li>
        Multi Environment configuration, by default, reads the context-path and port of each module and
        generates a local environment for each module (refresh will still take effect after deletion)
        <br/>
        多环境配置, 默认读取每个模块的 context-path 和 port 并为每个模块生成本地环境(删除后刷新仍会生效)
    </li>
    <li>
        If the project refers to Swagger2 and Swagger3 annotations, hover the mouse over the corresponding
        node will display the corresponding description
        <br/>
        如果项目引用了 Swagger2 和 Swagger3 注解, 将鼠标悬浮在对应的结点上会显示对应的说明
    </li>
    <li>
        Double-click the method node to jump to the specified method
        <br/>
        双击方法结点可以跳转到指定方法处
    </li>
</ol>
""".trimIndent())
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
