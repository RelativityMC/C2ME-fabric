package com.ishland.c2me.fixes.general.threading_issues.mixin;

import net.minecraft.server.world.ChunkTicketManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Set;
import java.util.function.Consumer;

@Mixin(ChunkTicketManager.class)
public class MixinChunkTicketManager {

    @SuppressWarnings("unchecked")
    @Redirect(method = "update", at = @At(value = "INVOKE", target = "Ljava/util/Set;forEach(Ljava/util/function/Consumer;)V"))
    private <T> void replaceIterationForHolderTicking(Set<T> instance, Consumer<T> consumer) {
        if (instance.isEmpty()) return;
        for (Object object : instance.toArray()) {
            consumer.accept((T) object);
        }
    }

}
