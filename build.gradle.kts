plugins {
    kotlin("jvm") version "1.9.23"
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("org.jlleitschuh.gradle.ktlint") version "12.1.1"
}

group = "me.devcexx"
version = "1.0-SNAPSHOT"

val testServerPath = projectDir.resolve("server")
val testServerPluginsPath = testServerPath.resolve("plugins")

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.20.4-R0.1-SNAPSHOT")
    compileOnly(files("server/versions/1.20.4/paper-1.20.4.jar"))
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(21)
}

tasks {
    val copyPluginJarToTestServerTask =
        task<Copy>("copyPluginJarToTestServer") {
            dependsOn(shadowJar)
            from(shadowJar.get().archiveFile.get().asFile)
            into(testServerPluginsPath)
        }

    task<JavaExec>("runTestServer") {
        dependsOn(copyPluginJarToTestServerTask)

        workingDir = testServerPath
        classpath = files(testServerPath.resolve("paper.jar"))
        jvmArgs("-Dbasiclockette.debug=1")
        args = listOf("nogui")
        standardInput = System.`in`
    }

    task("release") {
        dependsOn(shadowJar, ktlintCheck)
    }
}
