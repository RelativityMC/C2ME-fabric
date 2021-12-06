package com.ishland.c2me.client.mixin.memcheck;

import com.ishland.c2me.common.config.C2MEConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.NoticeScreen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TitleScreen.class)
public class MixinTitleScreen {
    @Inject(at = @At("RETURN"), method = "init()V")
    private void init(CallbackInfo info) {
        MinecraftClient.getInstance().setScreen(new NoticeScreen(() -> MinecraftClient.getInstance().scheduleStop(), new LiteralText("Not enough memory to run C2ME with " + C2MEConfig.globalExecutorParallelism + " threads. Please increase the JVM heap size to at least " + C2MEConfig.globalExecutorParallelism * 1024 + ".").formatted(Formatting.RED), new LiteralText("C2ME has detected that you do not have enough memory to run C2ME properly. Without enough memory you will experience stutters and errors. Please increase the JVM heap size and restart the game.")));
    }
}
