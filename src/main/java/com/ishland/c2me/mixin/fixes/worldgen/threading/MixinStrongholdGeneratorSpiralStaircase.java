package com.ishland.c2me.mixin.fixes.worldgen.threading;

import net.minecraft.structure.StrongholdGenerator;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(StrongholdGenerator.SpiralStaircase.class)
public class MixinStrongholdGeneratorSpiralStaircase {

    // TODO
//    @Redirect(method = "fillOpenings", at = @At(value = "FIELD", target = "Lnet/minecraft/structure/StrongholdGenerator;activePieceType:Ljava/lang/Class;", opcode = Opcodes.PUTSTATIC))
//    private void redirectGetActivePieceType(Class<? extends StrongholdGenerator.Piece> value) {
//        IStrongholdGenerator.getActivePieceTypeThreadLocal.set(value);
//    }

}
