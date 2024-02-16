package com.ishland.c2me.client.uncapvd.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.network.packet.c2s.common.SyncedClientOptions;
import net.minecraft.util.math.MathHelper;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(SyncedClientOptions.class)
public class MixinSyncedClientOptions {

    @WrapOperation(method = "write", at = @At(value = "FIELD", target = "Lnet/minecraft/network/packet/c2s/common/SyncedClientOptions;viewDistance:I", opcode = Opcodes.GETFIELD))
    private int wrapRenderDistance(SyncedClientOptions instance, Operation<Integer> original) {
        return MathHelper.clamp(instance.viewDistance(), 2, Byte.MAX_VALUE);
    }

}
