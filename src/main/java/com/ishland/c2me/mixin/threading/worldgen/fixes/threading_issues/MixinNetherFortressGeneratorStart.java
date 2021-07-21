package com.ishland.c2me.mixin.threading.worldgen.fixes.threading_issues;

import com.ishland.c2me.common.threading.worldgen.fixes.threading_fixes.INetherFortressGeneratorPieceData;
import net.minecraft.structure.NetherFortressGenerator;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Collections;
import java.util.List;

@Mixin(NetherFortressGenerator.Start.class)
public class MixinNetherFortressGeneratorStart {

    @Shadow public List<NetherFortressGenerator.PieceData> bridgePieces;
    @Shadow public List<NetherFortressGenerator.PieceData> corridorPieces;

    @Redirect(method = "<init>(Ljava/util/Random;II)V", at = @At(value = "FIELD", target = "Lnet/minecraft/structure/NetherFortressGenerator$PieceData;generatedCount:I", opcode = Opcodes.PUTFIELD))
    private void redirectSetPieceDataGeneratedCount(NetherFortressGenerator.PieceData pieceData, int value) {
        ((INetherFortressGeneratorPieceData) pieceData).getGeneratedCountAtomic().set(value);
    }

    @Inject(method = "<init>*", at = @At("RETURN"))
    private void onInit(CallbackInfo info) {
        this.bridgePieces = Collections.synchronizedList(this.bridgePieces);
        this.corridorPieces = Collections.synchronizedList(this.corridorPieces);
    }
}
