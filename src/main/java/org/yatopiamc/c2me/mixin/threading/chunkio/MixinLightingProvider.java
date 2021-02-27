package org.yatopiamc.c2me.mixin.threading.chunkio;

import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.world.LightType;
import net.minecraft.world.chunk.ChunkNibbleArray;
import net.minecraft.world.chunk.light.ChunkLightingView;
import net.minecraft.world.chunk.light.LightingProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.yatopiamc.c2me.common.threading.chunkio.ICachedLightingProvider;

import java.util.concurrent.ConcurrentHashMap;

@Mixin(LightingProvider.class)
public abstract class MixinLightingProvider implements ICachedLightingProvider {

    @Shadow public abstract ChunkLightingView get(LightType lightType);

    @Shadow public abstract int method_31929();

    @Shadow public abstract int method_31930();

    private ConcurrentHashMap<ChunkSectionPos, LightData> cachedData = new ConcurrentHashMap<>();

    @Inject(method = "<init>", at = @At("RETURN"))
    private void onInit(CallbackInfo info) {
        cachedData = new ConcurrentHashMap<>();
    }

    @Override
    public void prepareLightData(ChunkPos pos) {
        for (int i = method_31929(); i < method_31930(); i ++) {
            final ChunkSectionPos sectionPos = ChunkSectionPos.from(pos, i);
            ChunkNibbleArray blockLight = this.get(LightType.BLOCK).getLightSection(sectionPos);
            ChunkNibbleArray skyLight = this.get(LightType.SKY).getLightSection(sectionPos);
            byte[] blockLightBytes = null;
            byte[] skyLightBytes = null;
            if (blockLight != null && !blockLight.isUninitialized()) {
                blockLightBytes = blockLight.asByteArray();
            }
            if (skyLight != null && !skyLight.isUninitialized()) {
                skyLightBytes = skyLight.asByteArray();
            }
            cachedData.put(sectionPos, new LightData(blockLightBytes, skyLightBytes));
        }
    }

    @Override
    public LightData takeLightData(ChunkSectionPos pos) {
        return cachedData.remove(pos);
    }
}
