package com.ishland.c2me.opts.allocs.mixin;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Mixin(NbtList.class)
public abstract class MixinNbtList extends AbstractList<NbtElement> {

    @Shadow @Final private List<NbtElement> value;

    /**
     * @author ishland
     * @reason copy using fastutil list
     */
    @Overwrite
    public NbtList copy() {
        List<NbtElement> list = new ObjectArrayList<>(this.value.size());

        for (NbtElement nbtElement : this.value) {
            list.add(nbtElement.copy());
        }

        return new NbtList(list);
    }

    @ModifyArg(method = "<init>()V", at = @At(value = "INVOKE", target = "Lnet/minecraft/nbt/NbtList;<init>(Ljava/util/List;)V"), index = 0)
    private static List<NbtElement> modifyList(List<NbtElement> list) {
        return new ObjectArrayList<>();
    }

//    @Redirect(method = "<init>()V", at = @At(value = "INVOKE", target = "Lcom/google/common/collect/Lists;newArrayList()Ljava/util/ArrayList;", remap = false))
//    private static <E> ArrayList<E> redirectNewArrayList() {
//        return null; // avoid double list creation
//    }

}
