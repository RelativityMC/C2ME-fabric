package com.ishland.c2me.opts.chunk_access;

import com.ishland.c2me.base.common.ModuleMixinPlugin;
import com.ishland.c2me.opts.chunk_access.asm.ASMTransformerLithiumChunkAccessWorkaround;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

public class MixinPlugin extends ModuleMixinPlugin {

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
        super.postApply(targetClassName, targetClass, mixinClassName, mixinInfo);
        ASMTransformerLithiumChunkAccessWorkaround.transform(targetClass);
    }

}
