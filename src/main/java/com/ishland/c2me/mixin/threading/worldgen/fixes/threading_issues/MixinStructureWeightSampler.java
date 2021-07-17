package com.ishland.c2me.mixin.threading.worldgen.fixes.threading_issues;

import it.unimi.dsi.fastutil.objects.ObjectList;
import it.unimi.dsi.fastutil.objects.ObjectListIterator;
import net.minecraft.structure.JigsawJunction;
import net.minecraft.structure.StructurePiece;
import net.minecraft.world.gen.StructureWeightSampler;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(StructureWeightSampler.class)
public class MixinStructureWeightSampler {

    @Shadow @Final private ObjectList<StructurePiece> pieces;
    @Shadow @Final private ObjectList<JigsawJunction> junctions;
    private final ThreadLocal<ObjectListIterator<StructurePiece>> pieceIteratorThreadLocal = ThreadLocal.withInitial(() -> this.pieces.iterator());
    private final ThreadLocal<ObjectListIterator<JigsawJunction>> junctionIteratorThreadLocal = ThreadLocal.withInitial(() -> this.junctions.iterator());

    @Redirect(method = "getWeight", at = @At(value = "FIELD", target = "Lnet/minecraft/world/gen/StructureWeightSampler;pieceIterator:Lit/unimi/dsi/fastutil/objects/ObjectListIterator;", opcode = Opcodes.GETFIELD))
    private ObjectListIterator<StructurePiece> redirectPieceIterator(StructureWeightSampler structureWeightSampler) {
        return pieceIteratorThreadLocal.get();
    }

    @Redirect(method = "getWeight", at = @At(value = "FIELD", target = "Lnet/minecraft/world/gen/StructureWeightSampler;junctionIterator:Lit/unimi/dsi/fastutil/objects/ObjectListIterator;", opcode = Opcodes.GETFIELD))
    private ObjectListIterator<JigsawJunction> redirectJunctionIterator(StructureWeightSampler structureWeightSampler) {
        return junctionIteratorThreadLocal.get();
    }

}
