package com.ishland.c2me.opts.allocs.mixin;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.HashMap;
import java.util.Map;

@Mixin(targets = "net.minecraft.nbt.NbtCompound$1")
public abstract class MixinNbtCompound1 implements NbtType<NbtCompound> {

    @SuppressWarnings("UnnecessaryQualifiedMemberReference")
    @ModifyVariable(method = "Lnet/minecraft/nbt/NbtCompound$1;readCompound(Ljava/io/DataInput;Lnet/minecraft/nbt/NbtSizeTracker;)Lnet/minecraft/nbt/NbtCompound;", at = @At(value = "INVOKE_ASSIGN", target = "Lcom/google/common/collect/Maps;newHashMap()Ljava/util/HashMap;", remap = false))
    private static Map<String, NbtElement> modifyMap(Map<String, NbtElement> map) {
        return new Object2ObjectOpenHashMap<>();
    }

    @SuppressWarnings("UnnecessaryQualifiedMemberReference")
    @Redirect(method = "Lnet/minecraft/nbt/NbtCompound$1;readCompound(Ljava/io/DataInput;Lnet/minecraft/nbt/NbtSizeTracker;)Lnet/minecraft/nbt/NbtCompound;", at = @At(value = "INVOKE", target = "Lcom/google/common/collect/Maps;newHashMap()Ljava/util/HashMap;", remap = false))
    private static  <K, V> HashMap<K, V> redirectNewHashMap() {
        return null; // avoid double map creation
    }

}
