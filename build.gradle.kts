plugins {
    id("java")
}

group = "net.linktofur"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    compileOnly("org.projectlombok:lombok:1.18.42")
    annotationProcessor("org.projectlombok:lombok:1.18.42")

    testCompileOnly("org.projectlombok:lombok:1.18.42")
    testAnnotationProcessor("org.projectlombok:lombok:1.18.42")

    implementation("com.sun.mail:jakarta.mail:2.0.2")

    implementation("io.javalin:javalin:7.0.0")

    implementation("ch.qos.logback:logback-classic:1.5.32")

    implementation("com.fasterxml.jackson.core:jackson-databind:2.21.1")

    implementation("org.mindrot:jbcrypt:0.4")
}

tasks.jar {
    val commitHash = runCatching {
        providers.exec {
            commandLine("git", "rev-parse", "--short", "HEAD")
        }.standardOutput.asText.get().trim()
    }.getOrDefault("local")
    
    if (System.getenv("GITHUB_ACTIONS") == "true") {
        archiveFileName.set("LinkToFur-Server-${commitHash}-fat.jar")
    } else {
        archiveFileName.set("main.jar")
    }

    manifest {
        attributes("Main-Class" to "net.linktofur.Main")
    }

    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    from(sourceSets.main.get().output)
    dependsOn(configurations.runtimeClasspath)
    
    // Package all dependencies into the jar
    from({
        configurations.runtimeClasspath.get().filter { it.name.endsWith("jar") }.map { zipTree(it) }
    })
}

tasks.test {
    enabled = false
}