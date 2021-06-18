package com.ishland.c2me.mixin.threading.chunkio;

import net.minecraft.nbt.NbtList;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.ChunkTickScheduler;
import net.minecraft.world.HeightLimitView;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import com.ishland.c2me.common.util.DeepCloneable;

import java.util.function.Predicate;

@Mixin(ChunkTickScheduler.class)
public abstract class MixinChunkTickScheduler<T> implements DeepCloneable {

    @Shadow
    public abstract NbtList toNbt();

    @Shadow
    @Final
    private ChunkPos pos;
    @Shadow @Final protected Predicate<T> shouldExclude;
    @Shadow private HeightLimitView world;

    public ChunkTickScheduler<T> deepClone() {
        return new ChunkTickScheduler<>(shouldExclude, pos, toNbt(), world);
    }
}
