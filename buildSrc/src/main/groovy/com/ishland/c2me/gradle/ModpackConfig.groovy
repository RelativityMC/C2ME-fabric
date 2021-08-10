package com.ishland.c2me.gradle

class ModpackConfig {

    static def applyModpack(handler) {
        // Don't forget to bring this changes to README.md
        // Curseforge projects: https://www.cursemaven.com/
        // Modrinth projects: "maven.modrinth:<modid>:<version>"

        // mod dependencies
        // https://fabricmc.net/versions.html
        handler.modImplementation("net.fabricmc.fabric-api:fabric-api:0.37.1+1.17")

        // mods

        ////////////////////////////////////////
        // Optimization mods

        // https://modrinth.com/mod/lithium
        handler.modImplementation("maven.modrinth:lithium:mc1.17.1-0.7.3") {
            transitive false
        }

        ////////////////////////////////////////
        // TelepathicGrunt content mods

        // https://github.com/TelepathicGrunt/RepurposedStructures-Fabric
        handler.modImplementation("com.telepathicgrunt:RepurposedStructures-Fabric:2.2.0+1.17.1") {
            exclude(group: "net.fabricmc.fabric-api")
            exclude(group: "maven.modrinth", module: "modmenu")
        }

        // https://github.com/TelepathicGrunt/UltraAmplifiedDimension-Fabric
        // TODO removed due to bad memleak when paired with starlight
//        handler.modImplementation("com.telepathicgrunt:UltraAmplifiedModFabric:10.0.0+1.17.1") {
//            exclude(group: "net.fabricmc.fabric-api")
//            exclude(group: "maven.modrinth", module: "modmenu")
//        }

        // https://github.com/TelepathicGrunt/Bumblezone-Fabric
        handler.modImplementation("com.telepathicgrunt:Bumblezone-Fabric:3.0.9+1.17.1") {
            exclude(group: "net.fabricmc.fabric-api")
            exclude(group: "maven.modrinth", module: "modmenu")
        }

        ////////////////////////////////////////
        // paulevsGitch content mods

        // https://www.curseforge.com/minecraft/mc-mods/betterend
        // https://github.com/ishland/BetterEnd/
        handler.modImplementation("com.github.ishland:BetterEnd:3c0120db2e") {
            exclude(group: "net.fabricmc.fabric-api")
        }

        ////////////////////////////////////////
        // PolyDev content mods

        // TODO https://modrinth.com/mod/terra
        // handler.modImplementation("maven.modrinth:terra:5.4.1-BETA+40e95073")

        ////////////////////////////////////////
        // SuperCoder79 content mods

        // https://www.curseforge.com/minecraft/mc-mods/ecotones
        // Ecotones 0.8.1 for 1.17.1 TODO need workaround for dev environment detection
        // handler.modImplementation("curse.maven:ecotones-356678:3402996")

        // https://www.curseforge.com/minecraft/mc-mods/vanilla-biomes
        // Vanilla+ Biomes 0.3.2 for 1.17
        handler.modImplementation("curse.maven:vanilla-biomes-367944:3355670")
    }

}
