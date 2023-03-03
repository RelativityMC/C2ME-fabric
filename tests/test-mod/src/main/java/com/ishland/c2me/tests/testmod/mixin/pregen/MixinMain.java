package com.ishland.c2me.tests.testmod.mixin.pregen;

import net.minecraft.resource.DataConfiguration;
import net.minecraft.resource.DataPackSettings;
import net.minecraft.resource.ResourcePackManager;
import net.minecraft.resource.featuretoggle.FeatureFlags;
import net.minecraft.resource.featuretoggle.FeatureSet;
import net.minecraft.server.Main;
import net.minecraft.server.dedicated.EulaReader;
import net.minecraft.server.dedicated.ServerPropertiesHandler;
import net.minecraft.world.level.storage.LevelStorage;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.ArrayList;

@Mixin(Main.class)
public class MixinMain {

    @Shadow @Final private static Logger LOGGER;

    @Redirect(method = "main", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/dedicated/EulaReader;isEulaAgreedTo()Z"))
    private static boolean redirectEULA(EulaReader eulaReader) {
        LOGGER.info("Automatically agreed to EULA. If you don't, please stop using this test suite.");
        return true;
    }

    @Redirect(method = "createServerConfig", at = @At(value = "NEW", target = "net/minecraft/resource/DataConfiguration"))
    private static DataConfiguration enableAllFeatures(DataPackSettings dataPackSettings, FeatureSet featureSet, ServerPropertiesHandler serverPropertiesHandler, LevelStorage.Session session, boolean safeMode, ResourcePackManager dataPackManager) {
        dataPackManager.scanPacks();
        return new DataConfiguration(new DataPackSettings(new ArrayList<>(dataPackManager.getNames()), new ArrayList<>()), FeatureFlags.FEATURE_MANAGER.getFeatureSet());
    }

}
