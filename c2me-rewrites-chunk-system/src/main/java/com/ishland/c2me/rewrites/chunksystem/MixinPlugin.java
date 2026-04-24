package com.ishland.c2me.rewrites.chunksystem;

import com.ishland.c2me.base.common.ModuleMixinPlugin;
import com.ishland.c2me.rewrites.chunksystem.common.Config;

/**
 * Mixin gating. VMP MixinSquared hooks ({@code MixinChunkTicketManagerVmp}) are off on NeoForge and on Fabric when VMP is absent.
 */
public class MixinPlugin extends ModuleMixinPlugin {

    private static final String VMP_MIXIN_CLASS = "com.ishland.c2me.rewrites.chunksystem.mixin.vmp.MixinChunkTicketManagerVmp";
    private static final String NEO_FORGE_FML_LOADER = "net.neoforged.fml.loading.FMLLoader";
    private static final String VMP_TICKET_MIXIN = "com.ishland.vmp.mixins.ticketsystem.ticketpropagator.MixinChunkTicketManager";

    private static final boolean C2ME_NEO_FORGE = classPresent(NEO_FORGE_FML_LOADER);
    private static final boolean C2ME_VMP_TICKET_TARGET = classPresent(VMP_TICKET_MIXIN);

    private static boolean classPresent(String binaryName) {
        try {
            Class.forName(binaryName, false, MixinPlugin.class.getClassLoader());
            return true;
        } catch (ClassNotFoundException | LinkageError e) {
            return false;
        }
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        if (!super.shouldApplyMixin(targetClassName, mixinClassName)) {
            return false;
        }

        if (VMP_MIXIN_CLASS.equals(mixinClassName)) {
            if (C2ME_NEO_FORGE) {
                LOGGER.debug("Skipping {} (VMP MixinSquared hooks not used on NeoForge)", mixinClassName);
                return false;
            }
            if (!C2ME_VMP_TICKET_TARGET) {
                LOGGER.debug("Skipping {} (VMP ticket mixin not on classpath)", mixinClassName);
                return false;
            }
        }

        if (mixinClassName.startsWith("com.ishland.c2me.rewrites.chunksystem.mixin.fluid_postprocessing")) {
            return Config.fluidPostProcessingToScheduledTick;
        }
        if (mixinClassName.startsWith("com.ishland.c2me.rewrites.chunksystem.mixin.async_chunkio.")) {
            return Config.asyncSerialization;
        }

        return true;
    }
}
