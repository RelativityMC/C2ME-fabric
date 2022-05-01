package com.ishland.c2me.natives.mixin;

import com.ishland.c2me.natives.common.NativeInterface;
import com.ishland.c2me.natives.common.NativeStruct;
import net.minecraft.util.math.noise.SimplexNoiseSampler;
import net.minecraft.world.biome.source.TheEndBiomeSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(value = TheEndBiomeSource.class, priority = 1200)
public class MixinTheEndBiomeSource {

    /**
     * @author ishland
     * @reason use native method
     */
    @Overwrite
    public static float getNoiseAt(SimplexNoiseSampler simplexNoiseSampler, int i, int j) {
        return NativeInterface.theEndSample(((NativeStruct) simplexNoiseSampler).getNativePointer(), i, j);
    }

}
