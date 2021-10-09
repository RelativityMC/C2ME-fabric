package com.ishland.c2me.mixin.optimization.worldgen.random_instances;

import net.minecraft.util.math.MathHelper;
import net.minecraft.world.gen.random.AbstractRandom;
import net.minecraft.world.gen.random.AtomicSimpleRandom;
import net.minecraft.world.gen.random.BlockPosRandomDeriver;
import net.minecraft.world.gen.random.SimpleRandom;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(AtomicSimpleRandom.class_6671.class)
public class MixinAtomicSimpleRandomFactory implements BlockPosRandomDeriver {

    @Shadow @Final private long field_35125;

    /**
     * @author ishland
     * @reason non-atomic
     */
    @Overwrite
    @Override
    public AbstractRandom createRandom(int x, int y, int z) { // TODO [VanillaCopy]
        long l = MathHelper.hashCode(x, y, z);
        long m = l ^ this.field_35125;
        return new SimpleRandom(m);
    }

    /**
     * @author ishland
     * @reason non-atomic
     */
    @Overwrite
    @Override
    public AbstractRandom method_38995(String string) { // TODO [VanillaCopy]
        int i = string.hashCode();
        return new SimpleRandom((long)i ^ this.field_35125);
    }

}
