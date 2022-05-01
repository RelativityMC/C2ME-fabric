package com.ishland.c2me.natives.mixin;

import com.ishland.c2me.natives.common.NativesInterface;
import com.ishland.c2me.natives.common.NativeStruct;
import net.minecraft.util.math.noise.SimplexNoiseSampler;
import net.minecraft.world.biome.source.TheEndBiomeSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(TheEndBiomeSource.class)
public class MixinTheEndBiomeSource {

    /**
     * @author ishland
     * @reason use native method
     */
    @Overwrite
    public static float getNoiseAt(SimplexNoiseSampler simplexNoiseSampler, int i, int j) {
        return NativesInterface.theEndSample(((NativeStruct) simplexNoiseSampler).getNativePointer(), i, j);
    }

}
