package com.ishland.c2me.mixin.metrics;

import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(MinecraftServer.class)
public class MixinMinecraftServer {


//    @SuppressWarnings("ConstantConditions")
//    @Inject(method = "runServer", at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/server/MinecraftServer;setupServer()Z"))
//    private void onInit(CallbackInfo info) {
//        final Metrics metrics = new Metrics(10514, (MinecraftServer) (Object) this);
//        metrics.addCustomChart(new Metrics.SimplePie("useThreadedWorldGeneration", () -> String.valueOf(C2MEConfig.threadedWorldGenConfig.enabled)));
//        if (C2MEConfig.threadedWorldGenConfig.enabled) {
//            metrics.addCustomChart(new Metrics.SimplePie("useThreadedWorldFeatureGeneration", () -> String.valueOf(C2MEConfig.threadedWorldGenConfig.allowThreadedFeatures)));
//            metrics.addCustomChart(new Metrics.SimplePie("useReducedLockRadius", () -> String.valueOf(C2MEConfig.threadedWorldGenConfig.reduceLockRadius)));
//            metrics.addCustomChart(new Metrics.SimplePie("useGlobalBiomeCache", () -> String.valueOf(C2MEConfig.threadedWorldGenConfig.useGlobalBiomeCache)));
//        }
//        metrics.addCustomChart(new Metrics.DrilldownPie("detailedMinecraftVersion", () -> ImmutableMap.of(MinecraftVersion.GAME_VERSION.getReleaseTarget(), ImmutableMap.of(MinecraftVersion.GAME_VERSION.getName(), 1))));
//    }

}
