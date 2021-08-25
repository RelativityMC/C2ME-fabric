package com.ishland.c2me.mixin.optimization.chunkio.nbtcompound;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.nbt.NbtElement;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.HashMap;
import java.util.Map;

@Mixin(targets = "net.minecraft.nbt.NbtCompound$1")
public class MixinNbtCompound1 {

    @ModifyVariable(method = "read", at = @At(value = "STORE", ordinal = 0)) // TODO check ordinal when updating minecraft version
    private Map<String, NbtElement> modifyMap(Map<String, NbtElement> map) {
        return new Object2ObjectOpenHashMap<>();
    }

    @Redirect(method = "read", at = @At(value = "INVOKE", target = "Lcom/google/common/collect/Maps;newHashMap()Ljava/util/HashMap;"))
    private <K, V> HashMap<K, V> redirectNewHashMap() {
        return null; // avoid double map creation
    }

}
