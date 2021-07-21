package com.ishland.c2me.mixin.threading.worldgen.fixes.threading_issues;

import com.ishland.c2me.common.threading.worldgen.fixes.threading_fixes.IStrongholdGenerator;
import net.minecraft.structure.StrongholdGenerator;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(StrongholdGenerator.SpiralStaircase.class)
public class MixinStrongholdGeneratorSpiralStaircase {

    @Redirect(method = "fillOpenings", at = @At(value = "FIELD", target = "Lnet/minecraft/structure/StrongholdGenerator;activePieceType:Ljava/lang/Class;", opcode = Opcodes.PUTSTATIC))
    private void redirectGetActivePieceType(Class<? extends StrongholdGenerator.Piece> value) {
        IStrongholdGenerator.Holder.INSTANCE.getActivePieceTypeThreadLocal().set(value);
    }

}
