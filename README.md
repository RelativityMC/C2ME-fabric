<img width="200" src="https://github.com/RelativityMC/C2ME-fabric/raw/ver/1.17/src/main/resources/assets/c2me/icon.png" alt="C2ME icon" align="right">
<div align="left">
<h1>C^2M-Engine</h1>

[![Github-CI](https://github.com/RelativityMC/C2ME-fabric/workflows/C2ME%20Build%20Script/badge.svg)](https://github.com/RelativityMC/C2ME-fabric/actions?query=workflow%3ACI)
[![Build Status](https://ci.codemc.io/job/RelativityMC/job/C2ME-fabric/job/ver%252F1.18/badge/icon)](https://ci.codemc.io/job/RelativityMC/job/C2ME-fabric/job/ver%252F1.17/)
[![Discord](https://img.shields.io/discord/756715786747248641?logo=discord&logoColor=white)](https://discord.io/ishlandbukkit)
<h3>A Fabric mod designed to improve the chunk performance of Minecraft.</h3>
</div>

## So what is C2ME?
C^2M-Engine, or C2ME for short, is a Fabric mod designed to improve the performance of chunk generation, I/O, and loading. This is done by taking advantage of multiple CPU cores in parallel. For the best performance it is recommended to use C2ME with [Lithium](https://github.com/CaffeineMC/lithium-fabric) and [Starlight](https://github.com/Spottedleaf/Starlight).

## What does C2ME stand for?
Concurrent chunk management engine, it's about making the game better threaded and more scalable in regard to world gen and chunk io performance.

## So what is C2ME not?
**C2ME is currently in alpha stage and pretty experimental.**  
Although it is usable in most cases and tested during build time, it doesn't mean that it is fully stable for a production server.  
So backup your worlds and practice good game modding skills.

## Branch development status
| Branch            | Status                               |
|-------------------|--------------------------------------|
| fabric/ver/1.18   | Active, Mainline                     |
| fabric/ver/1.17   | Active                               |
| fabric/ver/1.16.5 | Inactive                             |
| forge/ver/1.16.5  | Partial[^forge_partial116], Inactive |

[^forge_partial116]: Forge doesn't appear to support Java 16 on 1.16.5, so these versions uses Java 11 and may not contain some features which is present in the fabric versions.

## Downloads
You can find semi-stable releases here: https://github.com/RelativityMC/C2ME-fabric/releases  
You can find development builds here: https://ci.codemc.io/job/RelativityMC/job/C2ME-fabric

## Mod compatibility
<!-- Update this accordingly when updating ModpackConfig.groovy -->

**Fabric known compatible<sup>[^compatible]</sup> content mods:**  
- [Terra 5.4.1-BETA+efd1665](https://modrinth.com/mod/terra/version/i38N6tkR)
- [BetterEnd 0.10.5-pre](https://github.com/paulevsGitch/BetterEnd/releases/tag/0.10.5-pre)
- [BetterNether 5.1.3](https://www.curseforge.com/minecraft/mc-mods/betternether/files/3379682)
- [RepurposedStructures-Fabric 2.2.0+1.17.1](https://modrinth.com/mod/repurposed-structures-fabric/version/Hp3zNCHi)
- [Bumblezone-Fabric 3.0.9+1.17.1](https://modrinth.com/mod/the-bumblezone-fabric/version/VK0znAOW)
- [Vanilla+ Biomes 0.3.2 for 1.17](https://www.curseforge.com/minecraft/mc-mods/vanilla-biomes/files/3355670)
- [River Redux 0.2.0 for 1.17](https://www.curseforge.com/minecraft/mc-mods/river-redux/files/3344516)
- [Cave Biomes 0.6.3 for 1.17](https://www.curseforge.com/minecraft/mc-mods/cave-biomes/files/3344491)
- [Traverse v4.0.0-beta.2 for 1.17.1](https://github.com/TerraformersMC/Traverse/releases/tag/v4.0.0-beta.2)
- [YUNG's Better Mineshafts (Fabric) v1.0.1](https://www.curseforge.com/minecraft/mc-mods/yungs-better-mineshafts-fabric/files/3414789)
- [YUNG's Better Strongholds (Fabric) v1.1.1](https://www.curseforge.com/minecraft/mc-mods/yungs-better-strongholds-fabric/files/3412649)
- [YUNG's Better Dungeons (Fabric) v1.0.2](https://www.curseforge.com/minecraft/mc-mods/yungs-better-dungeons-fabric/files/3476131)
- [Charm 3.3.2](https://www.curseforge.com/minecraft/mc-mods/charm/files/3393290)
- [The Graveyard 1.4 (FABRIC) for 1.17-1.17.1](https://www.curseforge.com/minecraft/mc-mods/the-graveyard-fabric/files/3457664)

[All of Fabric 4 - 0.0.9](https://www.curseforge.com/minecraft/modpacks/all-of-fabric-4/files/3420600) should work without crashes now. 

[^compatible]: Working with `chunkio` and `threadedWorldGen` with `allowThreadedFeatures` `reduceLockRadius` enabled

## Support
Our issue tracker: [link](https://github.com/RelativityMC/C2ME-fabric/issues)  
Our discord server: [link](https://discord.io/ishlandbukkit)


## Building and setting up
JDK 17+ is required to build and use C2ME  
Run the following commands in the root directory:

```shell
./gradlew clean build
```

## License
License information can be found [here](/LICENSE).

