package com.ishland.c2me.compatibility.mixin.charm;

import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import svenhjol.charm.structure.CharmDataBlockProcessor;

@Mixin(CharmDataBlockProcessor.class)
public class MixinCharmDataBlockProcessor {

    @Redirect(method = "process", at = @At(value = "FIELD", target = "Lsvenhjol/charm/structure/CharmDataBlockProcessor;resolver:Lsvenhjol/charm/structure/CharmDataBlockProcessor$DataBlockResolver;", opcode = Opcodes.GETFIELD))
    private CharmDataBlockProcessor.DataBlockResolver redirectResolver(CharmDataBlockProcessor charmDataBlockProcessor) {
        return new CharmDataBlockProcessor.DataBlockResolver();
    }

}
