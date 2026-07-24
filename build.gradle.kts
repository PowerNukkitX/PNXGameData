plugins {
    `java`
    `maven-publish`
}

group = "org.powernukkitx"

val branchName: String = try {
    val proc = "git rev-parse --abbrev-ref HEAD".runCommand(rootDir)
    proc.trim().replace("/", "-")
} catch (e: Exception) {
    "dev"
}
version = "$branchName-SNAPSHOT"

java {
    sourceSets["main"].java.setSrcDirs(emptyList<String>())
    sourceSets["main"].resources.srcDir("src/main/resources")
}

tasks.processResources {
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
}

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
        mavenLocal()
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
