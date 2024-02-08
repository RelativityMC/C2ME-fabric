<img width="200" src="https://github.com/RelativityMC/C2ME-fabric/raw/ver/1.17/src/main/resources/assets/c2me/icon.png" alt="C2ME icon" align="right">
<div align="left">
<h1>C^2M-Engine</h1>

[![Github-CI](https://github.com/RelativityMC/C2ME-fabric/workflows/C2ME%20Build%20Script/badge.svg)](https://github.com/RelativityMC/C2ME-fabric/actions?query=workflow%3ACI)
[![Build Status](https://ci.codemc.io/job/RelativityMC/job/C2ME-fabric/job/ver%252F1.18/badge/icon)](https://ci.codemc.io/job/RelativityMC/job/C2ME-fabric/job/ver%252F1.18/)
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
| fabric/ver/1.20.1 | Active, Mainline                     |
| fabric/ver/1.19.4 | Bugfixes only                        |
| fabric/ver/1.18   | Bugfixes only                        |


## Downloads
You can find semi-stable releases here: https://github.com/RelativityMC/C2ME-fabric/releases  

### Development builds
**Note: Development builds may modify the config files in a way unreadable by previous versions. You may encounter crashes or config reset when rolling back to previous versions. Please always backup your config.**

You can find development builds here: https://ci.codemc.io/job/RelativityMC/job/C2ME-fabric  
Note that these builds may contain more bugfixes and performance improvements but are less tested.  
If you have encountered any problems in release builds, give development builds a try before reporting.

## Support
Our issue tracker: [link](https://github.com/RelativityMC/C2ME-fabric/issues)  
Our discord server: [link](https://discord.io/ishlandbukkit)

## Building and setting up
JDK 17+ is required to build and use C2ME  
Run the following commands in the root directory:

```shell
git submodule update --init --recursive
./gradlew clean build
```

## License
License information can be found [here](/LICENSE).

