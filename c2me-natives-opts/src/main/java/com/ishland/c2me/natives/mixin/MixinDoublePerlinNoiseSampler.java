package com.ishland.c2me.natives.mixin;

import com.ishland.c2me.natives.common.NativesInterface;
import com.ishland.c2me.natives.common.NativesStruct;
import net.minecraft.util.math.noise.DoublePerlinNoiseSampler;
import net.minecraft.util.math.noise.OctavePerlinNoiseSampler;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(DoublePerlinNoiseSampler.class)
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
        return NativesInterface.sampleDouble(
                ((NativesStruct) this.firstSampler).getNativePointer(),
                ((NativesStruct) this.secondSampler).getNativePointer(),
                x, y, z, this.amplitude
        );
    }

}
