package com.ishland.c2me.mixin.failsafe;

import net.minecraft.structure.pool.SinglePoolElement;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(SinglePoolElement.class)
public class MixinSinglePoolElement {

//    @Shadow @Final protected Either<Identifier, Structure> location;
//
//    @Redirect(method = "createPlacementData", at = @At(value = "INVOKE", target = "Ljava/util/function/Supplier;get()Ljava/lang/Object;"))
//    private <T> T redirectProcessor(Supplier<T> supplier) {
//        final StructureProcessorList structureProcessorList = (StructureProcessorList) supplier.get();
//        if (structureProcessorList == null) {
//            final String identifier = this.location.map(Objects::toString, structure -> String.format("<Raw structure: %s>", structure));
//            C2MEMod.LOGGER.error("An recoverable error is detected by C2ME while preparing to generate structure {}",
//                    identifier);
//            C2MEMod.LOGGER.error("Reason: null processor list");
//            return (T) StructureProcessorLists.EMPTY;
//        } else {
//            return (T) structureProcessorList;
//        }
//    }

}
