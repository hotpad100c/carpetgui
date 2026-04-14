pluginManagement {
    repositories {
        mavenLocal()
        mavenCentral()
        gradlePluginPortal()
        maven("https://maven.fabricmc.net/")
        maven("https://maven.kikugie.dev/snapshots") { name = "KikuGie Snapshots" }
        maven("https://maven.terraformersmc.com/")
    }
}

plugins {
    id("dev.kikugie.stonecutter") version "0.7.10"
}

stonecutter {
    create(rootProject) {
        // See https://stonecutter.kikugie.dev/wiki/start/#choosing-minecraft-versions
        versions(/*
            "1.17.1",
            "1.18.2",*///Sorry..
            "1.19.4",
            "1.20.1",
            "1.20.3",
            "1.20.5",
            "1.21.1",
            "1.21.4",
            "1.21.6",
            "1.21.9",
            "1.21.11"
        ).buildscript("build.gradle.kts")
        versions("26.1").buildscript("unobfuscated.gradle.kts")
        vcsVersion = "1.21.1"
    }
}

rootProject.name = "CarpetGUI"