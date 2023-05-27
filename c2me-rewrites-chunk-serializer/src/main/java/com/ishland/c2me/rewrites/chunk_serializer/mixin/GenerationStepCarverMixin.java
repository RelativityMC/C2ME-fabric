package com.ishland.c2me.rewrites.chunk_serializer.mixin;

import com.ishland.c2me.rewrites.chunk_serializer.common.GenerationStepCarverAccessor;
import com.ishland.c2me.rewrites.chunk_serializer.common.NbtWriter;
import net.minecraft.world.gen.GenerationStep;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GenerationStep.Carver.class)
public class GenerationStepCarverMixin implements GenerationStepCarverAccessor {
    private byte[] nameBytes;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void postInit(String enum$name, int enum$ordinal, String name, CallbackInfo ci) {
        this.nameBytes = NbtWriter.getStringBytes(enum$name);
    }

    @Override
    public byte[] getNameBytes() {
        return this.nameBytes;
    }

}

