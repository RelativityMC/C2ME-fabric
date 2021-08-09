<img width="200" src="https://github.com/ishlandbukkit/C2ME-fabric/raw/ver/1.17/src/main/resources/assets/c2me/icon.png" alt="C2ME icon" align="right">
<div align="left">
<h1>C^2M-Engine</h1>

[![Github-CI](https://github.com/ishlandbukkit/C2ME-fabric/workflows/C2ME%20Build%20Script/badge.svg)](https://github.com/YatopiaMC/C2ME-fabric/actions?query=workflow%3ACI)
[![Build Status](https://ci.codemc.io/job/ishlandbukkit/job/C2ME-fabric/job/ver%252F1.17/badge/icon)](https://ci.codemc.io/job/ishlandbukkit/job/C2ME-fabric/job/ver%252F1.17/)
[![Discord](https://img.shields.io/discord/756715786747248641?logo=discord&logoColor=white)](https://discord.io/ishlandbukkit)
<h3>A Fabric mod designed to improve the chunk performance of Minecraft.</h3>
</div>

## So what is C2ME?
C^2M-Engine, or C2ME for short, is a Fabric mod designed to improve the performance of chunk generation, I/O, and loading. This is done by taking advantage of multiple CPU cores in parallel. For the best performance it is recommended to use C2ME with [Lithium](https://github.com/CaffeineMC/lithium-fabric) and [Starlight](https://github.com/Spottedleaf/Starlight).

## What does C2ME stand for?
Concurrent chunk management engine, it's about making the game better threaded and more scalable in regard to world gen and chunk io performance.

## So what is C2ME not?
C2ME is not production ready and still pretty experimental. So backup your worlds and practice good game modding skills.

## Branch development status
| Branch | Status |
| ------ | ------ |
| fabric/ver/1.16.5 | Inactive |
| fabric/ver/1.17 | Active |
| forge/ver/1.16.5 | Partial<sup>[[1]](#forgePartial116)</sup>, Inactive |
| forge/ver/1.17 | Waiting for forge |

## Downloads
You can find semi-stable releases here: https://github.com/ishlandbukkit/C2ME-fabric/releases  
You can find development builds here: https://ci.codemc.io/job/ishlandbukkit/job/C2ME-fabric

## Mod compatibility
<!-- Update this accordingly when updating ModpackConfig.groovy -->

## Building and setting up
JDK 16+ is required to build and use C2ME  
Run the following commands in the root directory:

```shell
./gradlew clean build
```

## License
License information can be found [here](/LICENSE).

## Statistics
[![](https://bstats.org/signatures/bukkit/C2ME-fabric.svg)](https://bstats.org/plugin/bukkit/C2ME-fabric/10514)

## Footnotes
<a name="forgePartial116">[1]</a>: Forge doesn't appear to support Java 16 on 1.16.5, so these versions uses Java 11 and may not contain some features which is present in the fabric versions.  
