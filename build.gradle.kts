plugins {
    id("java")
    id("net.kyori.blossom") version "2.1.0"
    id("net.minecraftforge.gradle") version "6.0.+"
    id("org.parchmentmc.librarian.forgegradle") version "1.+"
    id("org.spongepowered.mixin") version "0.7.+"
}

val modId: String by project
val modName: String by project
val modGroup: String by project
val modVersion: String by project
val modRepo: String by project

val mcVersion: String by project
val fmlVersion: String by project
val parchmentVersion: String by project
val mixinConfig = "mixins.${modId}.json"

group = "${modGroup}.${modId}"
version = "${mcVersion}-${modVersion}"

minecraft {
    // Since 1.20.6, Forge uses Mojang mappings at runtime in both dev and prod
    reobf = false

    mappings("parchment", "${parchmentVersion}-${mcVersion}")
    accessTransformer("src/main/resources/META-INF/accesstransformer.cfg")

    runs {
        create("client") {
            workingDirectory = project.file("run").toString()

            mixin {
                config(mixinConfig)
            }

            mods {
                create(modId) {
                    source(sourceSets["main"])
                }
            }
        }
    }
}

repositories {
    mavenCentral()
    maven("https://pkgs.dev.azure.com/djtheredstoner/DevAuth/_packaging/public/maven/v1")
}

dependencies {
    "minecraft"("net.minecraftforge:forge:${mcVersion}-${fmlVersion}")
    implementation("me.djtheredstoner:DevAuth-forge-latest:1.2.1")
    annotationProcessor("org.spongepowered:mixin:0.8.5:processor")

    // A workaround required by Forge for some reason
    implementation("net.sf.jopt-simple:jopt-simple:5.0.4") {
        version {
            strictly("5.0.4")
        }
    }
}

sourceSets {
    main {
        blossom {
            javaSources {
                property("modId", modId)
                property("modName", modName)
                property("modGroup", modGroup)
            }
            resources {
                property("modId", modId)
                property("modName", modName)
                property("modVersion", modVersion)
                property("modRepo", modRepo)
                property("mcVersion", mcVersion)
            }
        }
    }

    // A workaround required by Forge for some reason
    all {
        val dir = layout.buildDirectory.dir("sourcesSets/${name}")
        output.setResourcesDir(dir)
        java.destinationDirectory.set(dir)
    }
}

tasks.jar {
    from(rootProject.file("LICENSE")) {
        into("META-INF/")
    }

    from(rootProject.file("NOTICE")) {
        into("META-INF/")
    }

    archiveFileName.set("${modId}-mc${mcVersion}-${modVersion}.jar")

    manifest.attributes(
        mapOf(
            "MixinConfigs" to mixinConfig
        )
    )
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}
