package com.ishland.c2me.opts.math.mixin;

import net.minecraft.util.collection.PackedIntegerArray;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = PackedIntegerArray.class, priority = 900)
public abstract class MixinPackedIntegerArray {

    @Shadow
    @Final
    private long[] data;

    @Shadow
    @Final
    private int elementBits;

    @Shadow
    @Final
    private long maxValue;

    @Shadow
    @Final
    private int elementsPerLong;

    @Shadow
    private int getStorageIndex(int index) {
        throw new AbstractMethodError();
    }

    /**
     * @author ishland
     * @reason avoid repeated checked reads in the chunk palette hot path
     */
    @Overwrite
    public int get(int index) {
        int storageIndex = this.getStorageIndex(index);
        long value = this.data[storageIndex];
        int bitOffset = (index - storageIndex * this.elementsPerLong) * this.elementBits;
        return (int) (value >>> bitOffset & this.maxValue);
    }
}
