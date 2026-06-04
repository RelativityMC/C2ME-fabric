/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2021-2026 ishland
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.ishland.c2me.fixes.worldgen.threading_issues.mixin.threading;

import com.ishland.c2me.fixes.worldgen.threading_issues.common.IStrongholdGenerator;
import com.ishland.c2me.fixes.worldgen.threading_issues.common.XPieceDataExtension;
import net.minecraft.structure.StrongholdGenerator;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.ArrayList;
import java.util.List;

@Mixin(StrongholdGenerator.class)
public class MixinStrongholdGenerator implements IStrongholdGenerator {

    @Unique
    private static final ThreadLocal<List<StrongholdGenerator.PieceData>> possiblePiecesThreadLocal = ThreadLocal.withInitial(() -> new ArrayList<>());
    @Unique
    private static final ThreadLocal<Integer> totalWeightThreadLocal = ThreadLocal.withInitial(() -> 0);
    @Unique
    private static final ThreadLocal<Class<? extends StrongholdGenerator.Piece>> activePieceTypeThreadLocal = new ThreadLocal<>();

    @Redirect(method = "init", at = @At(value = "FIELD", target = "Lnet/minecraft/structure/StrongholdGenerator;possiblePieces:Ljava/util/List;", opcode = Opcodes.PUTSTATIC), require = 1)
    private static void redirectSetPossiblePieces(List<StrongholdGenerator.PieceData> value) {
        possiblePiecesThreadLocal.set(value);
    }

    @Redirect(method = {"init", "checkRemainingPieces", "pickPiece"}, at = @At(value = "FIELD", target = "Lnet/minecraft/structure/StrongholdGenerator;possiblePieces:Ljava/util/List;", opcode = Opcodes.GETSTATIC), require = 4)
    private static List<StrongholdGenerator.PieceData> redirectGetPossiblePieces() {
        return possiblePiecesThreadLocal.get();
    }

    @Redirect(method = {"checkRemainingPieces", "pickPiece"}, at = @At(value = "FIELD", target = "Lnet/minecraft/structure/StrongholdGenerator$PieceData;generatedCount:I", opcode = Opcodes.GETFIELD), require = 2)
    private static int redirectGetGeneratedCount(StrongholdGenerator.PieceData instance) {
        return ((XPieceDataExtension) instance).c2me$getGeneratedCountThreadLocal().get();
    }

    @Redirect(method = {"init", "pickPiece"}, at = @At(value = "FIELD", target = "Lnet/minecraft/structure/StrongholdGenerator$PieceData;generatedCount:I", opcode = Opcodes.PUTFIELD), require = 2)
    private static void redirectSetGeneratedCount(StrongholdGenerator.PieceData pieceData, int value) {
        if (value == 0) {
            ((XPieceDataExtension) pieceData).c2me$getGeneratedCountThreadLocal().remove();
        } else {
            ((XPieceDataExtension) pieceData).c2me$getGeneratedCountThreadLocal().set(value);
        }
    }

    @Redirect(method = "checkRemainingPieces", at = @At(value = "FIELD", target = "Lnet/minecraft/structure/StrongholdGenerator;totalWeight:I", opcode = Opcodes.PUTSTATIC))
    private static void redirectSetTotalWeight(int value) {
        totalWeightThreadLocal.set(value);
    }

    @Redirect(method = {"pickPiece", "checkRemainingPieces"}, at = @At(value = "FIELD", target = "Lnet/minecraft/structure/StrongholdGenerator;totalWeight:I", opcode = Opcodes.GETSTATIC))
    private static int redirectGetTotalWeight() {
        return totalWeightThreadLocal.get();
    }

    @Redirect(method = "pickPiece", at = @At(value = "FIELD", target = "Lnet/minecraft/structure/StrongholdGenerator;activePieceType:Ljava/lang/Class;", opcode = Opcodes.PUTSTATIC))
    private static void redirectSetActivePieceType(Class<? extends StrongholdGenerator.Piece> value) {
        activePieceTypeThreadLocal.set(value);
    }

    @Redirect(method = "pickPiece", at = @At(value = "FIELD", target = "Lnet/minecraft/structure/StrongholdGenerator;activePieceType:Ljava/lang/Class;", opcode = Opcodes.GETSTATIC))
    private static Class<? extends StrongholdGenerator.Piece> redirectGetActivePieceType() {
        return activePieceTypeThreadLocal.get();
    }

    @Override
    public ThreadLocal<Class<? extends StrongholdGenerator.Piece>> getActivePieceTypeThreadLocal() {
        return activePieceTypeThreadLocal;
    }
}
