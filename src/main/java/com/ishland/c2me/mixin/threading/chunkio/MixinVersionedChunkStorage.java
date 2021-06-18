package com.ishland.c2me.mixin.threading.chunkio;

import com.ibm.asyncutil.locks.AsyncLock;
import com.mojang.datafixers.DataFixer;
import net.minecraft.SharedConstants;
import net.minecraft.datafixer.DataFixTypes;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.FeatureUpdater;
import net.minecraft.world.PersistentStateManager;
import net.minecraft.world.World;
import net.minecraft.world.storage.VersionedChunkStorage;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Supplier;

@Mixin(VersionedChunkStorage.class)
public abstract class MixinVersionedChunkStorage {

    @Shadow @Final protected DataFixer dataFixer;

    @Shadow @Nullable private FeatureUpdater featureUpdater;

    private AsyncLock featureUpdaterLock = AsyncLock.createFair();

    @Inject(method = "<init>", at = @At("RETURN"))
    private void onInit(CallbackInfo info) {
        this.featureUpdaterLock = AsyncLock.createFair();
    }

    /**
     * @author ishland
     * @reason async loading
     */
    @Overwrite
    public NbtCompound updateChunkNbt(RegistryKey<World> registryKey, Supplier<PersistentStateManager> persistentStateManagerFactory, NbtCompound tag) {
        // TODO [VanillaCopy] - check when updating minecraft version
        int i = VersionedChunkStorage.getDataVersion(tag);
        if (i < 1493) {
            try (final AsyncLock.LockToken ignored = featureUpdaterLock.acquireLock().toCompletableFuture().join()) { // C2ME - async chunk loading
                tag = NbtHelper.update(this.dataFixer, DataFixTypes.CHUNK, tag, i, 1493);
                if (tag.getCompound("Level").getBoolean("hasLegacyStructureData")) {
                    if (this.featureUpdater == null) {
                        this.featureUpdater = FeatureUpdater.create(registryKey, (PersistentStateManager)persistentStateManagerFactory.get());
                    }

                    tag = this.featureUpdater.getUpdatedReferences(tag);
                }
            } // C2ME - async chunk loading
        }

        tag = NbtHelper.update(this.dataFixer, DataFixTypes.CHUNK, tag, Math.max(1493, i));
        if (i < SharedConstants.getGameVersion().getWorldVersion()) {
            tag.putInt("DataVersion", SharedConstants.getGameVersion().getWorldVersion());
        }

        return tag;
    }

    @Redirect(method = "setNbt", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/FeatureUpdater;markResolved(J)V"))
    private void onSetTagAtFeatureUpdaterMarkResolved(FeatureUpdater featureUpdater, long l) {
        try (final AsyncLock.LockToken ignored = featureUpdaterLock.acquireLock().toCompletableFuture().join()) {
            featureUpdater.markResolved(l);
        }
    }

}
