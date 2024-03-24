package com.ishland.c2me.fixes.worldgen.threading_issues.mixin.threading_detections.random_instances.carpet;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.mojang.authlib.GameProfile;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

@Pseudo
@Mixin(targets = "carpet.patches.EntityPlayerMPFake")
public class MixinEntityPlayerMPFake {

    @ModifyExpressionValue(method = "createFake", at = @At(value = "INVOKE", target = "Lcarpet/patches/EntityPlayerMPFake;fetchGameProfile(Ljava/lang/String;)Ljava/util/concurrent/CompletableFuture;"), require = 0)
    private static CompletableFuture<Optional<GameProfile>> modifyGameProfileFuture(CompletableFuture<Optional<GameProfile>> original, String username, MinecraftServer server) {
        return original.thenApplyAsync(Function.identity(), server);
    }

}
