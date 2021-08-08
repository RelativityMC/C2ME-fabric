package com.ishland.c2me.gradle

class ModpackConfig {

    static def applyModpack(handler) {
        // mod dependencies
        handler.modImplementation("net.fabricmc.fabric-api:fabric-api:0.37.1+1.17") // https://fabricmc.net/versions.html

        // mods
        handler.modImplementation("com.telepathicgrunt:RepurposedStructures-Fabric:2.1.11+1.17.1") {
            exclude(group: "net.fabricmc.fabric-api")
            exclude(group: "maven.modrinth", module: "modmenu")
        } // https://github.com/TelepathicGrunt/RepurposedStructures-Fabric
    }

}
