import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import xyz.jpenilla.runpaper.task.RunServer
import java.net.URI

plugins {
    id("org.mvplugins.multiverse-plugin") version "1.2.2"
    id("xyz.jpenilla.run-paper") version "2.3.1"
}

group = "org.mvplugins.multiverse.core"
description = "Multiverse-Core"
version = "5.3.4-SEED"

repositories {
    maven {
        name = "aikar repo"
        url = URI("https://repo.aikar.co/content/groups/aikar/")
    }

    maven {
        name = "glaremasters repo"
        url = URI("https://repo.glaremasters.me/repository/towny/")
    }

    maven {
        name = "helpchatRepoReleases"
        url = URI("https://repo.helpch.at/releases/")
    }
}

apiDependencies {
    serverApiVersion = "1.21.4-R0.1-SNAPSHOT"
    mockBukkitServerApiVersion = "1.21"
    mockBukkitVersion = "4.34.0"
}

dependencies {
    // Economy
    externalPlugin("com.github.MilkBowl:VaultAPI:1.7.1") {
        exclude(group = "org.bukkit", module = "bukkit")
    }

    // PlaceholderAPI
    externalPlugin("me.clip:placeholderapi:2.11.6")

    // Command Framework
    shadowed("co.aikar:acf-paper:0.5.1-SNAPSHOT")

    // Config
    shadowed("io.github.townyadvanced.commentedconfiguration:CommentedConfiguration:1.0.1") {
        exclude(group = "org.spigotmc", module = "spigot-api")
    }

    // Utils
    shadowed("io.vavr:vavr:0.10.7")
    shadowed("org.glassfish.hk2:hk2-locator:3.1.1")
    shadowed("org.glassfish.hk2:hk2-inhabitant-generator:3.1.1") {
        exclude(group = "org.apache.maven", module = "maven-core")
    }
    shadowed("com.dumptruckman.minecraft:Logging:1.1.1") {
        exclude(group = "junit", module = "junit")
    }
    shadowed("de.themoep.idconverter:mappings:1.2-SNAPSHOT")
    shadowed("org.bstats:bstats-bukkit:3.1.0") {
        exclude(group = "org.bukkit", module = "bukkit")
    }
    shadowed("net.minidev:json-smart:2.5.2")
    shadowed("org.jetbrains:annotations:26.0.2")
    shadowed("io.papermc:paperlib:1.0.8")

    // Tests
    testImplementation("com.googlecode.json-simple:json-simple:1.1.1") {
        exclude(group = "junit", module = "junit")
    }
    testImplementation("org.hamcrest:hamcrest:3.0")
}

// Main Plugin List (core server needs only)
val prodPlugins = runPaper.downloadPluginsSpec {
    modrinth("multiverse-inventories", "5.2.0")
    modrinth("multiverse-portals", "5.1.1")
    modrinth("multiverse-netherportals", "5.0.3")
}

// Plugin List for automated testing
val testPlugins = runPaper.downloadPluginsSpec {
    from(prodPlugins) // Copy everything from prod
    github("Ifiht", "AutoStop", "v1.2.0", "AutoStop-1.2.0.jar")
}

tasks {
    runServer {
        // Keep runServer task to inherit project plugin
        downloadPlugins.from(prodPlugins)
        minecraftVersion("1.21.4")
    }
}

// Test PaperMC run & immediately shut down, for github actions
tasks.register<RunServer>("runServerTest") {
    dependsOn(tasks.shadowJar)
    minecraftVersion("1.21.4")
    downloadPlugins.from(testPlugins)
    pluginJars.from(tasks.shadowJar)
}
// Start a local PaperMC test server for login & manual testing
tasks.register<RunServer>("runServerInteractive") {
    dependsOn(tasks.shadowJar)
    minecraftVersion("1.21.4")
    downloadPlugins.from(prodPlugins)
    pluginJars.from(tasks.shadowJar)
}
// check for error logs
tasks.register("checkServerLogs") {
    doLast {
        // Path to the latest.log file
        val logFile = File("run/logs/latest.log")

        // Check if the log file exists
        if (!logFile.exists()) {
            throw GradleException("Log file not found: " + logFile.absolutePath)
        }

        // Read the log file line by line
        val logContent = logFile.readLines()

        // Find lines that contain the " ERROR]:" substring
        val errorLines = logContent.filter { it.contains("ERROR]:") }

        if (!errorLines.isEmpty()) {
            println("Errors were found:")
            errorLines.forEach(::println)
            throw GradleException("Errors found in log file.")
        } else {
            println("No errors found in log file.")
        }
    }
}

val shadowJar = tasks.named<ShadowJar>("shadowJar")

shadowJar.configure {
    relocate("co.aikar", "org.mvplugins.multiverse.external.acf")
    relocate("com.dumptruckman.minecraft.util.Logging", "org.mvplugins.multiverse.core.utils.CoreLogging")
    relocate("com.dumptruckman.minecraft.util.DebugLog", "org.mvplugins.multiverse.core.utils.DebugFileLogger")
    relocate("de.themoep.idconverter", "org.mvplugins.multiverse.external.idconverter")
    relocate("io.github.townyadvanced.commentedconfiguration", "org.mvplugins.multiverse.external.commentedconfiguration")
    relocate("org.bstats", "org.mvplugins.multiverse.external.bstats")
    relocate("com.sun", "org.mvplugins.multiverse.external.sun")
    relocate("net.minidev", "org.mvplugins.multiverse.external.minidev")
    relocate("org.objectweb", "org.mvplugins.multiverse.external.objectweb")
    relocate("io.vavr", "org.mvplugins.multiverse.external.vavr")
    relocate("jakarta", "org.mvplugins.multiverse.external.jakarta")
    relocate("javassist", "org.mvplugins.multiverse.external.javassist")
    relocate("org.aopalliance", "org.mvplugins.multiverse.external.aopalliance")
    relocate("org.glassfish", "org.mvplugins.multiverse.external.glassfish")
    relocate("org.jvnet", "org.mvplugins.multiverse.external.jvnet")
    relocate("org.intellij", "org.mvplugins.multiverse.external.intellij")
    relocate("org.jetbrains", "org.mvplugins.multiverse.external.jetbrains")
    relocate("io.papermc.lib", "org.mvplugins.multiverse.external.paperlib")
}
