package com.ishland.c2me.compatibility.mixin.betterend;

import net.minecraft.block.Block;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import ru.betterend.world.features.DoublePlantFeature;

@Pseudo
@Mixin(DoublePlantFeature.class)
public class MixinDoublePlantFeature {

    private final ThreadLocal<Block> plantThreadLocal = new ThreadLocal<>();

    @Dynamic
    @Redirect(method = "*", at = @At(value = "FIELD", target = "Lru/betterend/world/features/DoublePlantFeature;plant:Lnet/minecraft/block/Block;", opcode = Opcodes.PUTFIELD))
    private void redirectSetPlant(DoublePlantFeature doublePlantFeature, Block value) {
        plantThreadLocal.set(value);
    }

    @Dynamic
    @Redirect(method = "*", at = @At(value = "FIELD", target = "Lru/betterend/world/features/DoublePlantFeature;plant:Lnet/minecraft/block/Block;", opcode = Opcodes.GETFIELD))
    private Block redirectGetPlant(DoublePlantFeature doublePlantFeature) {
        return plantThreadLocal.get();
    }

}
