package com.ishland.c2me.tests.testmod.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

public class TestMixinPlugin implements IMixinConfigPlugin {
    private static final boolean doPreGen = !Boolean.getBoolean("com.ishland.c2me.tests.testmod.disablePreGen");

    @Override
    public void onLoad(String mixinPackage) {

    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        if (mixinClassName.startsWith("com.ishland.c2me.tests.testmod.mixin.pregen."))
            return doPreGen;
        if (mixinClassName.startsWith("com.ishland.c2me.tests.testmod.mixin.fix.client."))
            return FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT;
        if (mixinClassName.startsWith("com.ishland.c2me.tests.testmod.mixin.fix.remapper_being_broken."))
            return FabricLoader.getInstance().isDevelopmentEnvironment();
        return true;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {

    }

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {

    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {

    }
}
