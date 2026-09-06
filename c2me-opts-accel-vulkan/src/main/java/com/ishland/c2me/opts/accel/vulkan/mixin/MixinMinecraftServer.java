package com.ishland.c2me.opts.accel.vulkan.mixin;

import com.ishland.c2me.opts.accel.vulkan.common.Config;
import com.ishland.c2me.opts.accel.vulkan.common.ducks.MinecraftServerExtension;
import com.ishland.c2me.opts.accel.vulkan.common.gen.VkServerGlobalContext;
import com.ishland.c2me.opts.accel.vulkan.common.progress.GlobalProgressStash;
import net.minecraft.server.MinecraftServer;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(MinecraftServer.class)
public class MixinMinecraftServer implements MinecraftServerExtension {

    @Shadow @Final private static Logger LOGGER;

    @Unique
    private VkServerGlobalContext c2me$vkContext;

    @Inject(method = "runServer", at = @At("HEAD"))
    private void preRunServer(CallbackInfo ci) {
        try {
            if (this.c2me$vkContext != null) {
                throw new IllegalStateException("Context already exists?");
            }
            this.c2me$vkContext = new VkServerGlobalContext();
            boolean openedAnyDevice = false;
            for (org.lwjgl.vulkan.VkPhysicalDevice physicalDevice : this.c2me$vkContext.getPhysicalDevices()) {
                if (this.c2me$vkContext.openDevice(physicalDevice)) openedAnyDevice = true;
            }
            if (!openedAnyDevice) {
                LOGGER.warn("No suitable Vulkan devices found");
                if (!Config.allowIncompatibilityFallback) {
                    throw new IllegalStateException("No suitable Vulkan devices found");
                }
                this.c2me$vkContext.close();
                this.c2me$vkContext = null;
            }
        } catch (Throwable t) {
            LOGGER.error("Failed to initialize Vulkan context", t);
            this.c2me$vkContext = null;
            if (!Config.allowIncompatibilityFallback) {
                GlobalProgressStash.PROGRESS_TEXT =
                        String.format("Failed to initialize Vulkan context, see logs for details: %s", t);
                throw t;
            }
        }
    }

    @Inject(method = "shutdown", at = @At("RETURN"))
    private void postStopServer(CallbackInfo ci) {
        try {
            if (this.c2me$vkContext != null) {
                this.c2me$vkContext.close();
                this.c2me$vkContext = null;
            }
        } catch (Throwable t) {
            LOGGER.error("Failed to release Vulkan context", t);
        }
    }

    @Override
    public VkServerGlobalContext c2me$getCLContext() {
        return this.c2me$vkContext;
    }

}
