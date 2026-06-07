package com.ishland.c2me.opts.math.mixin;

import net.minecraft.world.chunk.PalettedContainer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(PalettedContainer.class)
public abstract class MixinPalettedContainer<T> {

    @Shadow
    @Final
    private PalettedContainer.PaletteProvider paletteProvider;

    @Shadow
    protected abstract T get(int index);

    /**
     * @author ishland
     * @reason avoid the PaletteProvider.computeIndex call in the chunk palette read hot path
     */
    @Overwrite
    public T get(int x, int y, int z) {
        PalettedContainer.PaletteProvider provider = this.paletteProvider;
        final int index;
        if (provider == PalettedContainer.PaletteProvider.BLOCK_STATE) {
            index = y << 8 | z << 4 | x;
        } else if (provider == PalettedContainer.PaletteProvider.BIOME) {
            index = y << 4 | z << 2 | x;
        } else {
            index = provider.computeIndex(x, y, z);
        }
        return this.get(index);
    }
}
