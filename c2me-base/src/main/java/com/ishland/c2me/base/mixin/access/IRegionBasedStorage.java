package com.ishland.c2me.base.mixin.access;

import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.storage.RegionBasedStorage;
import net.minecraft.world.storage.RegionFile;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.io.IOException;

@Mixin(RegionBasedStorage.class)
public interface IRegionBasedStorage {

    @Invoker
    RegionFile invokeGetRegionFile(ChunkPos pos) throws IOException;

}
