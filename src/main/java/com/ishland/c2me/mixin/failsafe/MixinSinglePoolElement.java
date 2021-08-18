package com.ishland.c2me.mixin.failsafe;

import com.ishland.c2me.C2MEMod;
import com.mojang.datafixers.util.Either;
import net.minecraft.structure.Structure;
import net.minecraft.structure.pool.SinglePoolElement;
import net.minecraft.structure.processor.StructureProcessorList;
import net.minecraft.structure.processor.StructureProcessorLists;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Objects;
import java.util.function.Supplier;

@Mixin(SinglePoolElement.class)
public class MixinSinglePoolElement {

    @Shadow
    @Final
    protected Supplier<StructureProcessorList> processors;

    @Shadow @Final protected Either<Identifier, Structure> location;

    @Redirect(method = "createPlacementData", at = @At(value = "INVOKE", target = "Ljava/util/function/Supplier;get()Ljava/lang/Object;"))
    private <T> T redirectProcessor(Supplier<T> supplier) {
        final StructureProcessorList structureProcessorList = (StructureProcessorList) supplier.get();
        if (structureProcessorList == null) {
            final String identifier = this.location.map(Objects::toString, structure -> String.format("<Raw structure: %s>", structure));
            C2MEMod.LOGGER.error("An recoverable error is detected by C2ME while preparing to generate structure {}",
                    identifier);
            C2MEMod.LOGGER.error("Reason: null processor list");
            return (T) StructureProcessorLists.EMPTY;
        } else {
            return (T) structureProcessorList;
        }
    }

}
