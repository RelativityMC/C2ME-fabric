package com.ishland.c2me.mixin.threading.worldgen.fixes.threading_issues;

import com.ishland.c2me.common.threading.worldgen.fixes.threading_fixes.ThreadLocalSurfaceBuilder;
import net.minecraft.world.gen.surfacebuilder.BadlandsSurfaceBuilder;
import net.minecraft.world.gen.surfacebuilder.BasaltDeltasSurfaceBuilder;
import net.minecraft.world.gen.surfacebuilder.DefaultSurfaceBuilder;
import net.minecraft.world.gen.surfacebuilder.FrozenOceanSurfaceBuilder;
import net.minecraft.world.gen.surfacebuilder.GiantTreeTaigaSurfaceBuilder;
import net.minecraft.world.gen.surfacebuilder.GravellyMountainSurfaceBuilder;
import net.minecraft.world.gen.surfacebuilder.MountainSurfaceBuilder;
import net.minecraft.world.gen.surfacebuilder.NetherForestSurfaceBuilder;
import net.minecraft.world.gen.surfacebuilder.NetherSurfaceBuilder;
import net.minecraft.world.gen.surfacebuilder.NopeSurfaceBuilder;
import net.minecraft.world.gen.surfacebuilder.ShatteredSavannaSurfaceBuilder;
import net.minecraft.world.gen.surfacebuilder.SoulSandValleySurfaceBuilder;
import net.minecraft.world.gen.surfacebuilder.SurfaceBuilder;
import net.minecraft.world.gen.surfacebuilder.SurfaceConfig;
import net.minecraft.world.gen.surfacebuilder.SwampSurfaceBuilder;
import net.minecraft.world.gen.surfacebuilder.TernarySurfaceConfig;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(SurfaceBuilder.class)
public abstract class MixinSurfaceBuilder {

    @Shadow
    private static <C extends SurfaceConfig, F extends SurfaceBuilder<C>> F register(String id, F surfaceBuilder) {
        throw new UnsupportedOperationException();
    }

    @SuppressWarnings("unchecked")
    @Dynamic
    @Redirect(method = "<clinit>", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/gen/surfacebuilder/SurfaceBuilder;register(Ljava/lang/String;Lnet/minecraft/world/gen/surfacebuilder/SurfaceBuilder;)Lnet/minecraft/world/gen/surfacebuilder/SurfaceBuilder;"))
    private static <C extends SurfaceConfig, F extends SurfaceBuilder<C>> F redirectRegister(String id, F surfaceBuilder) {
        if (surfaceBuilder instanceof BadlandsSurfaceBuilder) {
            return (F) register(id, new ThreadLocalSurfaceBuilder<>(() -> new BadlandsSurfaceBuilder(TernarySurfaceConfig.CODEC), TernarySurfaceConfig.CODEC));
        } else if (surfaceBuilder instanceof BasaltDeltasSurfaceBuilder) {
            return (F) register(id, new ThreadLocalSurfaceBuilder<>(() -> new BasaltDeltasSurfaceBuilder(TernarySurfaceConfig.CODEC), TernarySurfaceConfig.CODEC));
        } else if (surfaceBuilder instanceof FrozenOceanSurfaceBuilder) {
            return (F) register(id, new ThreadLocalSurfaceBuilder<>(() -> new FrozenOceanSurfaceBuilder(TernarySurfaceConfig.CODEC), TernarySurfaceConfig.CODEC));
        } else if (surfaceBuilder instanceof NetherForestSurfaceBuilder) {
            return (F) register(id, new ThreadLocalSurfaceBuilder<>(() -> new NetherForestSurfaceBuilder(TernarySurfaceConfig.CODEC), TernarySurfaceConfig.CODEC));
        } else if (surfaceBuilder instanceof NetherSurfaceBuilder) {
            return (F) register(id, new ThreadLocalSurfaceBuilder<>(() -> new NetherSurfaceBuilder(TernarySurfaceConfig.CODEC), TernarySurfaceConfig.CODEC));
        }
        if (!(surfaceBuilder instanceof DefaultSurfaceBuilder ||
                surfaceBuilder instanceof GiantTreeTaigaSurfaceBuilder ||
                surfaceBuilder instanceof GravellyMountainSurfaceBuilder ||
                surfaceBuilder instanceof MountainSurfaceBuilder ||
                surfaceBuilder instanceof NopeSurfaceBuilder ||
                surfaceBuilder instanceof ShatteredSavannaSurfaceBuilder ||
                surfaceBuilder instanceof SoulSandValleySurfaceBuilder ||
                surfaceBuilder instanceof SwampSurfaceBuilder)) {
            //noinspection RedundantStringFormatCall
            System.err.println(String.format("Warning: Unknown surface builder: %s. It may cause issues when using this with C2ME threaded worldgen.", surfaceBuilder.getClass().getName()));
        }
        return register(id, surfaceBuilder);
    }

}
