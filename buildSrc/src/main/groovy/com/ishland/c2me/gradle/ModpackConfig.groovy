package com.ishland.c2me.gradle

class ModpackConfig {

    static def applyModpack(handler) {
        // Don't forget to bring this changes to README.md
        // Curseforge projects: https://www.cursemaven.com/

        // mod dependencies
        handler.modImplementation("net.fabricmc.fabric-api:fabric-api:0.37.1+1.17") // https://fabricmc.net/versions.html

        // mods

        // https://github.com/TelepathicGrunt/RepurposedStructures-Fabric
        handler.modImplementation("com.telepathicgrunt:RepurposedStructures-Fabric:2.2.0+1.17.1") {
            exclude(group: "net.fabricmc.fabric-api")
            exclude(group: "maven.modrinth", module: "modmenu")
        }

        // https://www.curseforge.com/minecraft/mc-mods/betterend
        // https://github.com/ishland/BetterEnd/
        handler.modImplementation("com.github.ishland:BetterEnd:3c0120db2e")

        // TODO https://modrinth.com/mod/terra
        // handler.modImplementation("maven.modrinth:terra:5.4.1-BETA+40e95073")
    }

}
