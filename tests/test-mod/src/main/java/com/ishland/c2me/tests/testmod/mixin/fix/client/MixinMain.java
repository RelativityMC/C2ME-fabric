package com.ishland.c2me.tests.testmod.mixin.fix.client;

import net.minecraft.client.main.Main;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Mixin(Main.class)
public class MixinMain {

    @Redirect(method = "main", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;getOfflinePlayerUuid(Ljava/lang/String;)Ljava/util/UUID;"))
    private static UUID redirectGetOfflineUUID(String nickname) {
        return UUID.nameUUIDFromBytes(("OfflinePlayer:" + nickname).getBytes(StandardCharsets.UTF_8));
    }

}
