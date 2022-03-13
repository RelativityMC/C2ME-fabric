package com.ishland.c2me.opts.allocs.mixin;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.nbt.NbtElement;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.ArrayList;
import java.util.List;

@Mixin(targets = "net.minecraft.nbt.NbtList$1")
public class MixinNbtList1 {

    @ModifyVariable(method = "Lnet/minecraft/nbt/NbtList$1;read(Ljava/io/DataInput;ILnet/minecraft/nbt/NbtTagSizeTracker;)Lnet/minecraft/nbt/NbtList;", at = @At(value = "INVOKE_ASSIGN", target = "Lcom/google/common/collect/Lists;newArrayListWithCapacity(I)Ljava/util/ArrayList;"))
    private List<NbtElement> modifyList(List<NbtElement> list) {
        return new ObjectArrayList<>();
    }

    @Redirect(method = "Lnet/minecraft/nbt/NbtList$1;read(Ljava/io/DataInput;ILnet/minecraft/nbt/NbtTagSizeTracker;)Lnet/minecraft/nbt/NbtList;", at = @At(value = "INVOKE", target = "Lcom/google/common/collect/Lists;newArrayListWithCapacity(I)Ljava/util/ArrayList;"))
    private <E> ArrayList<E> redirectNewArrayList(int initialArraySize) {
        return null;
    }

}
