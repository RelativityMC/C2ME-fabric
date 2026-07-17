package com.ishland.c2me.opts.dfc.mixin.lithostitched.equality;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Objects;

@Pseudo
@Mixin(targets = "dev.worldgen.lithostitched.api.worldgen.densityfunction.fastnoise.FastNoiseConfig", remap = false)
public class MixinFastNoiseConfig {

    @Shadow @Final protected Object fnl;

    @Shadow @Final private float frequency;

    @Shadow @Final private int salt;

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        MixinFastNoiseConfig that = (MixinFastNoiseConfig) object;
        return Objects.equals(fnl, that.fnl) && Float.compare(frequency, that.frequency) == 0 && Integer.compare(salt, that.salt) == 0;
    }

    @Override
    public int hashCode() {
        int result = 1;
        result = 31 * result + Objects.hashCode(fnl);
        result = 31 * result + Float.hashCode(frequency);
        result = 31 * result + Integer.hashCode(salt);
        return result;
    }
}
