package com.ishland.c2me.fixes.worldgen.threading_issues.mixin.threading;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.registry.Registry;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.HeightLimitView;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.UpgradeData;
import net.minecraft.world.gen.chunk.BlendingData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Chunk.class)
public abstract class MixinChunk implements HeightLimitView {

//    @Mutable
//    @Shadow
//    @Final
//    private Map<StructureFeature<?>, StructureStart<?>> structureStarts;
//
//    @Inject(method = "<init>", at = @At("RETURN"))
//    private void onInit(CallbackInfo info) {
//        this.structureStarts = new CMETrackingMap<>(this.structureStarts);
//    }

    @Unique
    private int c2me$bottomY;

    @Unique
    private int c2me$height;

    @Unique
    private int c2me$topY;

    @Unique
    private int c2me$bottomSectionCoord;

    @Unique
    private int c2me$topSectionCoord;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void c2me$cacheHeightLimits(ChunkPos pos, UpgradeData upgradeData, HeightLimitView heightLimitView, Registry<Biome> biomeRegistry, long inhabitedTime, ChunkSection[] sectionArrayInitializer, BlendingData blendingData, CallbackInfo ci) {
        this.c2me$bottomY = heightLimitView.getBottomY();
        this.c2me$height = heightLimitView.getHeight();
        this.c2me$topY = this.c2me$bottomY + this.c2me$height;
        this.c2me$bottomSectionCoord = this.c2me$bottomY >> 4;
        this.c2me$topSectionCoord = ((this.c2me$topY - 1) >> 4) + 1;
    }

    // height limits are immutable for the lifetime of the chunk, so the delegation to the
    // world's HeightLimitView is replaced with a constructor-cached field read
    @WrapMethod(method = "getBottomY")
    private int useCachedBottomY(Operation<Integer> original) {
        return this.c2me$bottomY;
    }

    @WrapMethod(method = "getHeight")
    private int useCachedHeight(Operation<Integer> original) {
        return this.c2me$height;
    }

    @Override
    public int getTopY() {
        return this.c2me$topY;
    }

    @Override
    public int countVerticalSections() {
        return this.c2me$topSectionCoord - this.c2me$bottomSectionCoord;
    }

    @Override
    public int getBottomSectionCoord() {
        return this.c2me$bottomSectionCoord;
    }

    @Override
    public int getTopSectionCoord() {
        return this.c2me$topSectionCoord;
    }

    @Override
    public int getSectionIndex(int y) {
        return (y >> 4) - this.c2me$bottomSectionCoord;
    }

    @Override
    public int sectionCoordToIndex(int coord) {
        return coord - this.c2me$bottomSectionCoord;
    }

    @Override
    public int sectionIndexToCoord(int index) {
        return index + this.c2me$bottomSectionCoord;
    }

}
