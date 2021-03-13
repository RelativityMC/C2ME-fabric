package org.yatopiamc.c2me.mixin.threading.chunkio;

import net.minecraft.nbt.ListTag;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.ChunkTickScheduler;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.yatopiamc.c2me.common.util.DeepCloneable;

import java.util.function.Predicate;

@Mixin(ChunkTickScheduler.class)
public abstract class MixinChunkTickScheduler<T> implements DeepCloneable {

    @Shadow
    public abstract ListTag toNbt();

    @Shadow
    @Final
    private ChunkPos pos;
    @Shadow @Final protected Predicate<T> shouldExclude;

    public ChunkTickScheduler<T> deepClone() {
        return new ChunkTickScheduler<>(shouldExclude, pos, toNbt());
    }
}
