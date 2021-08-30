package com.ishland.c2me.mixin.optimization.chunkio.nbtcompound;

import com.google.common.collect.Maps;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.HashMap;
import java.util.Map;

@Mixin(NbtCompound.class)
public class MixinNbtCompound {

    @Shadow @Final private Map<String, NbtElement> entries;

    /**
     * @author ishland
     * @reason copy using fastutil map
     */
    @Overwrite
    public NbtCompound copy() {
        Map<String, NbtElement> map = new Object2ObjectOpenHashMap<>(Maps.transformValues(this.entries, NbtElement::copy));
        return new NbtCompound(map);
    }

    /**
     * copy using fastutil map
     */
    @ModifyArg(method = "<init>()V", at = @At(value = "INVOKE", target = "Lnet/minecraft/nbt/NbtCompound;<init>(Ljava/util/Map;)V"), index = 0)
    private static Map<String, NbtElement> modifyMap(Map<String, NbtElement> map) {
        return new Object2ObjectOpenHashMap<>();
    }

    @Redirect(method = "<init>()V", at = @At(value = "INVOKE", target = "Lcom/google/common/collect/Maps;newHashMap()Ljava/util/HashMap;"))
    private static <K, V> HashMap<K, V> redirectNewHashMap() {
        return null; // avoid double map creation
    }

}
