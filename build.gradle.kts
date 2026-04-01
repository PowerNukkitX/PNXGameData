plugins {
    `java`
    `maven-publish`
}

group = "org.powernukkitx"

// Dynamically compute version from Git branch
val branchName: String = try {
    val proc = "git rev-parse --abbrev-ref HEAD".runCommand(rootDir)
    proc.trim().replace("/", "-")
} catch (e: Exception) {
    "dev"
}
version = "$branchName-SNAPSHOT"

println("Building version: $version")

// Resource-only project
java {
    sourceSets["main"].java.setSrcDirs(emptyList<String>()) // no Java sources
    sourceSets["main"].resources.srcDir("src/main/resources")
}

tasks.processResources {
    // Ignore duplicate files with the same path
    duplicatesStrategy = DuplicatesStrategy.WARN
}

// Optional: clean target folder
tasks.clean {
    delete(buildDir)
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            groupId = project.group.toString()
            artifactId = "gamedata"
            version = project.version.toString()
        }
    }

    repositories {
        mavenLocal() // publish to local Maven cache

        // PNX Maven repository
        maven {
            name = "pnx"
            url = uri("https://repo.powernukkitx.org/releases")
            credentials {
                username = providers.gradleProperty("pnxUsername")
                    .orElse(providers.environmentVariable("PNX_REPO_USERNAME"))
                    .orNull
                password = providers.gradleProperty("pnxPassword")
                    .orElse(providers.environmentVariable("PNX_REPO_PASSWORD"))
                    .orNull
            }
        }
    }
}

// Helper function to run shell commands
fun String.runCommand(workingDir: File): String {
    val parts = this.split("\\s".toRegex())
    val proc = ProcessBuilder(*parts.toTypedArray())
        .directory(workingDir)
        .redirectOutput(ProcessBuilder.Redirect.PIPE)
        .redirectError(ProcessBuilder.Redirect.PIPE)
        .start()
    proc.waitFor()
    return proc.inputStream.bufferedReader().readText()
}