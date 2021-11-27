package com.ishland.c2me.mixin.optimization.worldgen.random_instances;

import com.ishland.c2me.common.optimization.worldgen.random_instances.SimplifiedAtomicSimpleRandom;
import net.minecraft.structure.pool.StructurePoolBasedGenerator;
import net.minecraft.world.gen.random.AtomicSimpleRandom;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = {
        StructurePoolBasedGenerator.class
})
public class MixinRedirectAtomicSimpleRandomStatic {

    @Redirect(method = "*", at = @At(value = "NEW", target = "net/minecraft/world/gen/random/AtomicSimpleRandom"))
    private static AtomicSimpleRandom redirectAtomicSimpleRandom(long l) {
        return new SimplifiedAtomicSimpleRandom(l);
    }

}
