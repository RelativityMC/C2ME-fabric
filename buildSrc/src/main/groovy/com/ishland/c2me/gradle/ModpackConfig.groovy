package com.ishland.c2me.gradle

class ModpackConfig {

    static def applyModpack(handler) {
        // mod dependencies
        handler.modImplementation("net.fabricmc.fabric-api:fabric-api:0.37.1+1.17")
        handler.modImplementation("me.shedaniel.cloth:cloth-config-fabric:5.0.34") // https://www.curseforge.com/minecraft/mc-mods/cloth-config
        handler.modImplementation("com.github.Draylar.omega-config:omega-config-base:1.0.8-1.17") // https://github.com/Draylar/omega-config

        // mods
        handler.modImplementation("maven.modrinth:repurposed-structures-fabric:2.1.11+1.17.1") // https://modrinth.com/mod/repurposed-structures-fabric
    }

}
