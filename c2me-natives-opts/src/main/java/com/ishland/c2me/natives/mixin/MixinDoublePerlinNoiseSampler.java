package com.ishland.c2me.natives.mixin;

import com.ishland.c2me.natives.common.NativesInterface;
import com.ishland.c2me.natives.common.NativeStruct;
import net.minecraft.util.math.noise.DoublePerlinNoiseSampler;
import net.minecraft.util.math.noise.OctavePerlinNoiseSampler;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = DoublePerlinNoiseSampler.class, priority = 1200)
public class MixinDoublePerlinNoiseSampler {

    @Shadow
    @Final
    private OctavePerlinNoiseSampler firstSampler;

    @Shadow
    @Final
    private OctavePerlinNoiseSampler secondSampler;

    @Shadow
    @Final
    private double amplitude;

    /**
     * @author ishland
     * @reason use native method
     */
    @Overwrite
    public double sample(double x, double y, double z) {
        return NativesInterface.perlinSampleDouble(
                ((NativeStruct) this.firstSampler).getNativePointer(),
                ((NativeStruct) this.secondSampler).getNativePointer(),
                x, y, z, this.amplitude
        );
    }

}
