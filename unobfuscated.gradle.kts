plugins {
    id("net.fabricmc.fabric-loom")
    id("me.modmuss50.mod-publish-plugin") version "0.3.5"
}


version = "${property("mod.version")}+${stonecutter.current.version}"
base.archivesName = property("mod.id") as String

val requiredJava = JavaVersion.VERSION_25

repositories {
    /**
     * Restricts dependency search of the given [groups] to the [maven URL][url],
     * improving the setup speed.
     */
    fun strictMaven(url: String, alias: String, vararg groups: String) = exclusiveContent {
        forRepository { maven(url) { name = alias } }
        filter { groups.forEach(::includeGroup) }
    }
    strictMaven("https://www.cursemaven.com", "CurseForge", "curse.maven")
    strictMaven("https://api.modrinth.com/maven", "Modrinth", "maven.modrinth")
    strictMaven("https://maven.wispforest.io/releases", "Wispforest", "io.wispforest", "io.wispforest.endec" )
    strictMaven("https://jitpack.io", "Jitpack", "io.jitpack", "com.github.kdl-org", "com.github.glisco03")
    strictMaven("https://masa.dy.fi/maven", "Carpet", "carpet")
}

dependencies {

    minecraft("com.mojang:minecraft:${stonecutter.current.version}")
    implementation("net.fabricmc:fabric-loader:${property("deps.fabric_loader")}")
    implementation("net.fabricmc.fabric-api:fabric-api:${property("deps.fabric_api")}")

    implementation("io.wispforest:owo-lib:${property("deps.owo_version")}")
    //include("io.wispforest:owo-sentinel:${property("deps.owo_version")}")

    implementation("carpet:fabric-carpet:${property("deps.carpet_version")}")
}

loom {
    fabricModJsonPath = rootProject.file("src/main/resources/fabric.mod.json") // Useful for interface injection
    //accessWidenerPath = rootProject.file("src/main/resources/carpetgui.accesswidener")

    decompilerOptions.named("vineflower") {
        options.put("mark-corresponding-synthetics", "1") // Adds names to lambdas - useful for mixins
    }

    runConfigs.all {
        ideConfigGenerated(true)
        vmArgs("-Dmixin.debug.export=true") // Exports transformed classes for debugging
        runDir = "../../run" // Shares the run directory between versions
    }
}

java {
    withSourcesJar()
    targetCompatibility = requiredJava
    sourceCompatibility = requiredJava
}

tasks {
    processResources {
        inputs.property("id", project.property("mod.id"))
        inputs.property("name", project.property("mod.name"))
        inputs.property("version", project.property("mod.version"))
        inputs.property("minecraft", project.property("mod.mc_dep"))

        val props = mapOf(
            "id" to project.property("mod.id"),
            "name" to project.property("mod.name"),
            "version" to project.property("mod.version"),
            "minecraft" to project.property("mod.mc_dep")
        )

        filesMatching("fabric.mod.json") { expand(props) }

        val mixinJava = "JAVA_${requiredJava.majorVersion}"
        filesMatching("*.mixins.json") { expand("java" to mixinJava) }
    }

    // Builds the version into a shared folder in `build/libs/${mod version}/`
    register<Copy>("buildAndCollect") {
        group = "build"
        from(jar.map { it.archiveFile })
        into(rootProject.layout.buildDirectory.file("libs/${project.property("mod.version")}"))
        dependsOn("build")
    }
}

publishMods {
    file = tasks.jar.map { it.archiveFile.get() }
    displayName = "${property("mod.name")} ${property("mod.version")} for ${property("mod.mc_title")}"
    version = property("mod.version") as String
    changelog = rootProject.file("CHANGELOG.md").readText()
    type = STABLE
    modLoaders.add("fabric")
    dryRun = false

    modrinth {
        projectId = property("publish.modrinth") as String
        accessToken = "Modrinth upload token!"
        minecraftVersions.addAll(property("mod.mc_targets").toString().split(' '))
        requires {
            id = "ccKDOlHs"
        }
        requires {
            id = "TQTTVgYE"
        }
    }

}

// Publishes builds to a maven repository under `com.example:template:0.1.0+mc`
/*
publishing {
    repositories {
        maven("https://mvnrepository.com/artifact/io.github.hotpad100c/ryansrenderingkit/releases") {
            name = "RyansRenderingKit"
            // To authenticate, create `myMavenUsername` and `myMavenPassword` properties in your Gradle home properties.
            // See https://stonecutter.kikugie.dev/wiki/tips/properties#defining-properties
            credentials(PasswordCredentials::class.java)
            authentication {
                create<BasicAuthentication>("basic")
            }
        }
    }

    publications {
        create<MavenPublication>("mavenJava") {
            groupId = "${property("mod.group")}.${property("mod.id")}"
            artifactId = property("mod.id") as String
            version = project.version.toString()

            from(components["java"])

            pom {
                name = "Ryan's Rendering Kit"
                description = "A powerful rendering utility library for Fabric"
                url = "https://github.com/hotpad100c/ryansrenderingkit"
                licenses {
                    license {
                        name = "MIT"
                        url = "https://opensource.org/licenses/MIT"
                    }
                }
                developers {
                    developer {
                        id = "hotpad100c"
                        name = "Ryan100C"
                    }
                }
                scm {
                    connection = "scm:git:github.com:hotpad100c/ryansrenderingkit.git"
                    developerConnection = "scm:git:github.com:hotpad100c/ryansrenderingkit.git"
                    url = "https://github.com/hotpad100c/ryansrenderingkit"
                }
            }
        }
    }
}*//*
mavenPublishing {
    publishToMavenCentral()

    signAllPublications()
    coordinates(
        "io.github.hotpad100c",
        "ryansrenderingkit",
        project.version.toString()
    )
    pom {
        name.set("Ryans Rendering Kit")
        description.set("A Fabric rendering utility library for Minecraft mods.")
        url.set("https://github.com/hotpad100c/ryansrenderingkit")

        licenses {
            license {
                name.set("MIT License")
                url.set("https://opensource.org/licenses/MIT")
            }
        }

        scm {
            url.set("https://github.com/hotpad100c/ryansrenderingkit")
            connection.set("scm:git:https://github.com/hotpad100c/ryansrenderingkit.git")
            developerConnection.set("scm:git:ssh://git@github.com:hotpad100c/ryansrenderingkit.git")
        }

        developers {
            developer {
                id.set("hotpad100c")
                name.set("Ryan100C")
                email.set("hotpad100c@gmail.com")
            }
        }
    }
}

signing {
    useInMemoryPgpKeys(
        findProperty("signing.keyId") as String,
        file("C:\\Users\\Ryan\\.gnupg\\private.key").readText(),
        findProperty("signing.password") as String
    )
    sign(publishing.publications)
}*/