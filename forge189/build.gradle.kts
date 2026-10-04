import org.gradle.kotlin.dsl.support.serviceOf
import java.net.URI
import java.security.MessageDigest

// Ionium for Forge 1.8.9. There is no separate source set: the Ornithe 1.8.9 build (Calamus intermediary
// names) is remapped to the SRG names Forge uses at runtime and repackaged as a Forge mod. It needs Java 21 and
// LWJGL3, i.e. RetroFuturaBootstrap (8to25) plus a launcher-provided LWJGL3 and ION Client's LWJGL2 layer.

plugins {
    java
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

// The remapper (src/remapper) runs as a separate process, so its tiny-remapper never meets the one Loom brings.
val remapper: SourceSet = sourceSets.create("remapper")

evaluationDependsOn(":ornithe:1.8.9")

repositories {
    exclusiveContent {
        forRepository { maven("https://maven.ornithemc.net/releases") }
        filter { includeGroup("net.ornithemc") }
    }
    exclusiveContent {
        forRepository {
            maven("https://maven.minecraftforge.net/") {
                // the MCP srg zip is published without a pom
                metadataSources { mavenPom(); artifact() }
            }
        }
        filter { includeGroup("de.oceanlabs.mcp") }
    }
    exclusiveContent {
        forRepository { maven("https://maven.fabricmc.net/") }
        filter { includeGroup("net.fabricmc") }
    }
    mavenCentral()
}

val calamus: Configuration by configurations.creating { isTransitive = false }
val srg: Configuration by configurations.creating { isTransitive = false }
// MC 1.8.9 ships no fastutil; Fabric/Ornithe provided it, Forge does not.
val bundled: Configuration by configurations.creating { isTransitive = false }

dependencies {
    calamus("net.ornithemc:calamus-intermediary:1.8.9")
    srg("de.oceanlabs.mcp:mcp:1.8.9:srg@zip")
    bundled("it.unimi.dsi:fastutil:8.5.15")
    "remapperImplementation"("net.fabricmc:mapping-io:0.7.1")
    "remapperImplementation"("net.fabricmc:tiny-remapper:0.10.2")
}

val modVersion = rootProject.version.toString()
val ornitheJar = project(":ornithe:1.8.9").tasks.named<Jar>("shadowRemapJar")

val minecraftClientJar = layout.buildDirectory.file("minecraft/client-1.8.9.jar")
val downloadMinecraft = tasks.register("downloadMinecraft") {
    val url = "https://launcher.mojang.com/v1/objects/3870888a6c3d349d3771a3e9d16c9bf5e076b908/client.jar"
    val sha1 = "3870888a6c3d349d3771a3e9d16c9bf5e076b908"
    val target = minecraftClientJar
    outputs.file(target)
    doLast {
        val file = target.get().asFile
        fun hash() = MessageDigest.getInstance("SHA-1").digest(file.readBytes()).joinToString("") { "%02x".format(it) }
        if (file.exists() && hash() == sha1) return@doLast
        file.parentFile.mkdirs()
        URI(url).toURL().openStream().use { input -> file.outputStream().use { input.copyTo(it) } }
        check(hash() == sha1) { "Minecraft 1.8.9 client jar checksum mismatch" }
    }
}

val remappedJar = layout.buildDirectory.file("remapped/ionium-srg.jar")
val remapToSrg = tasks.register<JavaExec>("remapToSrg") {
    description = "Remaps the Ornithe 1.8.9 jar from Calamus intermediary to Forge SRG names."
    dependsOn(downloadMinecraft)
    classpath = remapper.runtimeClasspath
    mainClass.set("org.taumc.ionium.remapper.RemapToSrg")
    val input = ornitheJar.flatMap { it.archiveFile }
    inputs.file(input)
    inputs.files(calamus, srg)
    inputs.file(minecraftClientJar)
    outputs.file(remappedJar)
    // locals, so the configuration cache does not have to serialize the build script itself
    val calamusFiles: FileCollection = calamus
    val srgFiles: FileCollection = srg
    val mcJar = minecraftClientJar
    val outJar = remappedJar
    argumentProviders.add(CommandLineArgumentProvider {
        listOf(input.get().asFile.path, calamusFiles.singleFile.path, srgFiles.singleFile.path,
            mcJar.get().asFile.path, outJar.get().asFile.path)
    })
}

val forgeJar = tasks.register<Jar>("forgeJar") {
    dependsOn(remapToSrg)
    val archives = serviceOf<ArchiveOperations>()
    val remapped = remappedJar
    val fastutil: FileCollection = bundled
    archiveBaseName.set("ionium")
    archiveVersion.set(modVersion)
    archiveClassifier.set("forge-1.8.9")
    destinationDirectory.set(layout.buildDirectory.dir("libs"))
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE

    from(remapped.map { archives.zipTree(it) }) {
        // Fabric-only metadata; the Forge jar declares its mixins in the manifest instead
        exclude("fabric.mod.json", "META-INF/MANIFEST.MF")
    }
    from(fastutil.elements.map { files -> files.map { archives.zipTree(it) } }) {
        // fastutil's jar carries no license file; META-INF/licenses/fastutil-LICENSE-2.0.txt comes from resources
        include("it/**")
    }
    val version = modVersion
    from("src/main/resources") {
        filesMatching("mcmod.info") { expand("version" to version) }
    }
    inputs.property("version", modVersion)
    manifest {
        attributes(
            "TweakClass" to "org.spongepowered.asm.launch.MixinTweaker",
            "TweakOrder" to "0",
            "MixinConfigs" to "mixins.celeritas.json",
            "ForceLoadAsMod" to "true",
            "ModSide" to "CLIENT",
            "Implementation-Title" to "Ionium",
            "Implementation-Version" to modVersion,
        )
    }
}

tasks.register<Copy>("packageJar") {
    from(forgeJar.flatMap { it.archiveFile })
    into(rootProject.layout.buildDirectory.dir("libs/$modVersion"))
}

tasks.named<Jar>("jar") {
    enabled = false
}

tasks.named("assemble") {
    dependsOn(forgeJar)
}
