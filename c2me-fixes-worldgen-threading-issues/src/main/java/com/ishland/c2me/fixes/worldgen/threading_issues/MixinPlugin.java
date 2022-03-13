package com.ishland.c2me.fixes.worldgen.threading_issues;

import com.ishland.c2me.base.common.ModuleMixinPlugin;
import com.ishland.c2me.fixes.worldgen.threading_issues.asm.ASMTransformerMakeVolatile;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

public class MixinPlugin extends ModuleMixinPlugin {

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
        ASMTransformerMakeVolatile.transform(targetClass);
    }
}
