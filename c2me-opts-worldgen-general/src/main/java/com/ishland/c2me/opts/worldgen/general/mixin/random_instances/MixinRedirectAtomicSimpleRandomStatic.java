package com.ishland.c2me.opts.worldgen.general.mixin.random_instances;

import com.ishland.c2me.opts.worldgen.general.common.random_instances.SimplifiedAtomicSimpleRandom;
import net.minecraft.world.gen.chunk.placement.StructurePlacement;
import net.minecraft.world.gen.random.AtomicSimpleRandom;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = {
        StructurePlacement.class,
})
public class MixinRedirectAtomicSimpleRandomStatic {

    @Redirect(method = "*", at = @At(value = "NEW", target = "net/minecraft/world/gen/random/AtomicSimpleRandom"))
    private static AtomicSimpleRandom redirectAtomicSimpleRandom(long l) {
        return new SimplifiedAtomicSimpleRandom(l);
    }

}
