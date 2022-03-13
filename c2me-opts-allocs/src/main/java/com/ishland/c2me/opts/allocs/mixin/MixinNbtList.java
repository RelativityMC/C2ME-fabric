package com.ishland.c2me.opts.allocs.mixin;

import com.google.common.collect.Iterables;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.nbt.AbstractNbtList;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtTypes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.ArrayList;
import java.util.List;

@Mixin(NbtList.class)
public abstract class MixinNbtList extends AbstractNbtList<NbtElement> {

    @Shadow private byte type;

    @Shadow @Final private List<NbtElement> value;

    @Shadow protected abstract boolean canAdd(NbtElement element);

    /**
     * @author ishland
     * @reason copy using fastutil list
     */
    @Overwrite
    public NbtList copy() {
        Iterable<NbtElement> iterable = NbtTypes.byId(this.type).isImmutable() ? this.value : Iterables.transform(this.value, NbtElement::copy);
        List<NbtElement> list = new ObjectArrayList<>(this.value.size());
        iterable.forEach(list::add);
        return new NbtList(list, this.type);
    }

    @ModifyArg(method = "<init>()V", at = @At(value = "INVOKE", target = "Lnet/minecraft/nbt/NbtList;<init>(Ljava/util/List;B)V"), index = 0)
    private static List<NbtElement> modifyList(List<NbtElement> list) {
        return new ObjectArrayList<>();
    }

    @Redirect(method = "<init>()V", at = @At(value = "INVOKE", target = "Lcom/google/common/collect/Lists;newArrayList()Ljava/util/ArrayList;"))
    private static <E> ArrayList<E> redirectNewArrayList() {
        return null; // avoid double list creation
    }

    @Override
    public boolean add(NbtElement element) {
        if (this.canAdd(element)) {
            this.value.add(element);
            return true;
        } else {
            return false;
        }
    }
}
