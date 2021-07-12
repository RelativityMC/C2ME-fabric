package com.ishland.c2me.compatibility.mixin;

import net.fabricmc.loader.api.ModContainer;
import net.minecraft.util.crash.CrashReport;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(CrashReport.class)
public class MixinCrashReport {

    @Redirect(method = "asString", at = @At(value = "INVOKE", target = "Ljava/lang/StringBuilder;append(Ljava/lang/String;)Ljava/lang/StringBuilder;"))
    private StringBuilder redirectAppend(StringBuilder stringBuilder, String str) {
        stringBuilder.append(str);
        if (str.equals("---- Minecraft Crash Report ----\n") && !C2MECompatibilityModule.getEnabledMods().isEmpty()) {
            stringBuilder.append("\n");
            stringBuilder.append("-".repeat(16)).append("\n");
            stringBuilder.append("C2ME Compatibility Module Notice: \n");
            stringBuilder.append("Do NOT report to mod authors if you encountered issues with the following mods: \n");
            for (ModContainer mod : C2MECompatibilityModule.getEnabledMods()) {
                stringBuilder.append(String.format("- %s@%s\n", mod.getMetadata().getName(), mod.getMetadata().getVersion().getFriendlyString()));
            }
            stringBuilder.append("You can try disabling compatibility modules for these mods in \"c2me-compat.toml\" and try reproduce again. \n");
            stringBuilder.append("Or try reproduce without C2ME. \n");
            stringBuilder.append("-".repeat(16)).append("\n");
            stringBuilder.append("\n");
        }
        return stringBuilder;
    }

}
