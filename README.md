<img width="200" src="https://yatopiamc.org/static/img/barrium.png" alt="Yatopia" align="right">
<div align="left">
<h1>C^2M-Engine</h1>

[![Github-CI](https://github.com/YatopiaMC/C2ME-fabric/workflows/C2ME%20Build%20Script/badge.svg)](https://github.com/YatopiaMC/C2ME-fabric/actions?query=workflow%3ACI)
[![CodeMC](https://ci.codemc.io/buildStatus/icon?job=YatopiaMC%2FC2ME-fabric%2Fver%252F1.16.5)](https://ci.codemc.io/job/YatopiaMC/job/C2ME-fabric/job/ver%252F1.16.5/)
[![Discord](https://img.shields.io/discord/342814924310970398?color=%237289DA&label=Discord&logo=discord&logoColor=white)](https://discord.io/YatopiaMC)
<h3>A Fabric mod designed to improve the chunk performance of Minecraft.</h3>
</div>

## So what is C2ME?
C^2M-Engine or C2ME is a for short is a  Fabric mod designed to improve the performance of chunk generation, I/O, and loading. This is done by taking advantage of multiple CPU cores in parallel. For the best performance it is recommended to use C2ME with [Lithium](https://github.com/CaffeineMC/lithium-fabric) and [Starlight](https://github.com/Spottedleaf/Starlight).
What does C2ME stand for?
Concurrent chunk management engine, its about making the game better threaded and more scallable in regards to world gen and chunk io performance.
## So what is C2ME not?

C2ME is not production ready and still pretty experimental. 
So backup your worlds and practice good game modding skills.
 
## Building and setting up

Run the following commands in the root directory:

```shell
./gradlew clean
./gradlew build
```

## License

License information can be found [here](/LICENSE).
