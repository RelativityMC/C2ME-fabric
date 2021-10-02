package com.ishland.c2me.gradle

class ModpackConfig {

    static def applyModpack(handler) {
        // Don't forget to bring this changes to README.md
        // Curseforge projects: https://www.cursemaven.com/
        // Modrinth projects: "maven.modrinth:<modid>:<version>"

        // mod dependencies
        // https://fabricmc.net/versions.html
        handler.modImplementation("net.fabricmc.fabric-api:fabric-api:0.39.2+1.17")

        // mods

        ////////////////////////////////////////
        // Optimization mods

        // https://modrinth.com/mod/lithium
        handler.modImplementation("com.github.CaffeineMC.lithium-fabric:lithium:mc1.17.1-0.7.4") {
            transitive false
        }

        ////////////////////////////////////////
        // TelepathicGrunt content mods

        // https://github.com/TelepathicGrunt/RepurposedStructures-Fabric
        handler.modImplementation("com.telepathicgrunt:RepurposedStructures-Fabric:2.2.0+1.17.1") {
            exclude(group: "net.fabricmc.fabric-api")
            exclude(group: "net.fabricmc", module: "fabric-loader")
            exclude(group: "maven.modrinth", module: "modmenu")
        }

        // https://github.com/TelepathicGrunt/UltraAmplifiedDimension-Fabric
        // TODO removed due to bad memleak when paired with starlight
//        handler.modImplementation("com.telepathicgrunt:UltraAmplifiedModFabric:10.0.0+1.17.1") {
//            exclude(group: "net.fabricmc.fabric-api")
//            exclude(group: "net.fabricmc", module: "fabric-loader")
//            exclude(group: "maven.modrinth", module: "modmenu")
//        }

        // https://github.com/TelepathicGrunt/Bumblezone-Fabric
        handler.modImplementation("com.telepathicgrunt:Bumblezone-Fabric:3.0.9+1.17.1") {
            exclude(group: "net.fabricmc.fabric-api")
            exclude(group: "net.fabricmc", module: "fabric-loader")
            exclude(group: "maven.modrinth", module: "modmenu")
            exclude(group: "com.telepathicgrunt", module: "Blame-Fabric")
        }

        ////////////////////////////////////////
        // paulevsGitch content mods

        // https://www.curseforge.com/minecraft/mc-mods/betterend
        // https://github.com/ishland/BetterEnd/
        handler.modImplementation("com.github.ishland:BetterEnd:2cce1d2") {
            exclude(group: "net.fabricmc.fabric-api")
            exclude(group: "net.fabricmc", module: "fabric-loader")
        }

        // https://github.com/paulevsGitch/BetterNether/
        // better-nether-5.1.3-1.17/1.17.1
        handler.modImplementation("curse.maven:betternether-311377:3379682")

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

        // https://www.curseforge.com/minecraft/mc-mods/river-redux
        // River Redux 0.2.0 for 1.17
        handler.modImplementation("curse.maven:river-redux-380548:3344516")

        // https://www.curseforge.com/minecraft/mc-mods/cave-biomes
        // Cave Biomes 0.6.3 for 1.17
        handler.modImplementation("curse.maven:cave-biomes-371307:3344491")

        ////////////////////////////////////////
        // TerraformersMC content mods

        // https://github.com/TerraformersMC/Traverse/
        // Traverse v4.0.0-beta.2 for 1.17.1
        handler.modImplementation("com.terraformersmc:traverse:4.0.0-beta.2") {
            exclude(group: "net.fabricmc.fabric-api")
            exclude(group: "net.fabricmc", module: "fabric-loader")
        }

        ////////////////////////////////////////
        // yungnickyoung content mods

        // https://www.curseforge.com/minecraft/mc-mods/yungs-api-fabric
        // [1.17 - 1.17.1] YUNG's API vFabric-16
        handler.modImplementation("com.yungnickyoung.minecraft.yungsapi:YungsApi:1.17-Fabric-16") {
            transitive false
        }

        // https://www.curseforge.com/minecraft/mc-mods/yungs-better-mineshafts-fabric
        // [1.17] YUNG's Better Mineshafts (Fabric) v1.0.1
        handler.modImplementation("curse.maven:yungs-better-mineshafts-fabric-373591:3414789")

        // https://www.curseforge.com/minecraft/mc-mods/yungs-better-strongholds-fabric
        // [1.17 - 1.17.1] YUNG's Better Strongholds (Fabric) v1.1.1
        handler.modImplementation("curse.maven:yungs-better-strongholds-fabric-480684:3412649")

        // https://www.curseforge.com/minecraft/mc-mods/yungs-better-dungeons-fabric
        // [1.17] YUNG's Better Dungeons (Fabric) v1.0.2
        handler.modImplementation("curse.maven:yungs-better-dungeons-fabric-525586:3476131")

        ////////////////////////////////////////
        // Others

        // https://www.curseforge.com/minecraft/mc-mods/charm
        // charm-fabric-1.17-3.3.2.jar
        handler.modImplementation("curse.maven:charm-318872:3393290")
        handler.implementation("com.moandjiezana.toml:toml4j:0.7.2")

//        // https://www.curseforge.com/minecraft/mc-mods/miners-horizon
//        // https://github.com/Draylar/miners-horizon/issues/20
//        // miners-horizon-1.7.0-1.17.1.jar
//        handler.modImplementation("curse.maven:miners-horizon-319826:3455972")

        // https://www.curseforge.com/minecraft/mc-mods/the-graveyard-fabric
        // The Graveyard 1.4 (FABRIC) for 1.17-1.17.1
        handler.modImplementation("curse.maven:the-graveyard-fabric-514468:3457664")

    }

}
