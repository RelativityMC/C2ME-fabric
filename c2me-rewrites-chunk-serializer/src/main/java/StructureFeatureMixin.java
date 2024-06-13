//package com.ishland.c2me.opts.chunk_serializer.mixin;
//
//import com.ishland.c2me.common.optimization.chunkserialization.NbtWriter;
//import com.ishland.c2me.common.optimization.chunkserialization.StructureFeatureAccessor;
//import com.ishland.c2me.rewrites.chunk_serializer.common.chunk_serializer.NbtWriter;
//import com.ishland.c2me.opts.chunk_serializer.common.StructureFeatureAccessor;
//import net.minecraft.world.gen.feature.StructureFeature;
//import org.spongepowered.asm.mixin.Mixin;
//
//@Mixin(StructureFeature.class)
//public class StructureFeatureMixin implements StructureFeatureAccessor {
//    private byte[] nameBytes;
//
//    @Override
//    public byte[] getNameBytes() {
//        if (this.nameBytes == null) {
//            //noinspection SuspiciousMethodCalls
//            final String name = StructureFeature.STRUCTURES.inverse().get(this);
//            return this.nameBytes = NbtWriter.getStringBytes(name);
//        }
//        return this.nameBytes;
//    }
//}
