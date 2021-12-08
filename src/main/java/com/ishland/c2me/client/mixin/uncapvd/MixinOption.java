package com.ishland.c2me.client.mixin.uncapvd;

import com.ishland.c2me.common.config.C2MEConfig;
import net.minecraft.client.option.DoubleOption;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.Option;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

@Mixin(Option.class)
public class MixinOption {

    @ModifyArg(method = "<clinit>", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/option/DoubleOption;<init>(Ljava/lang/String;DDFLjava/util/function/Function;Ljava/util/function/BiConsumer;Ljava/util/function/BiFunction;)V"), index = 2)
    private static double modifyMaxViewDistance(String key, double min, double max, float step, Function<GameOptions, Double> getter, BiConsumer<GameOptions, Double> setter, BiFunction<GameOptions, DoubleOption, Text> displayStringGetter) {
        if (key == "options.renderDistance") {
            return C2MEConfig.clientSideConfig.modifyMaxVDConfig.maxViewDistance;
        }
        return max;
    }

}
