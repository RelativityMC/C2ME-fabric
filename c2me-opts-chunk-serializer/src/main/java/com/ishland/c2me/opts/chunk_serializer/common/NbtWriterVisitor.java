package com.ishland.c2me.opts.chunk_serializer.common;

import net.minecraft.nbt.*;
import net.minecraft.nbt.visitor.NbtElementVisitor;

public class NbtWriterVisitor implements NbtElementVisitor {
    private final NbtWriter writer;

    public NbtWriterVisitor(NbtWriter writer) {
        this.writer = writer;
    }

    @Override
    public void visitString(NbtString element) {
        this.writer.putStringEntry(NbtWriter.getStringBytes(element.asString()));
    }

    @Override
    public void visitByte(NbtByte element) {
        this.writer.putByteEntry(element.byteValue());
    }

    @Override
    public void visitShort(NbtShort element) {
        this.writer.putShortEntry(element.shortValue());
    }

    @Override
    public void visitInt(NbtInt element) {
        this.writer.putIntEntry(element.intValue());
    }

    @Override
    public void visitLong(NbtLong element) {
        this.writer.putLongEntry(element.longValue());
    }

    @Override
    public void visitFloat(NbtFloat element) {
        this.writer.putFloatEntry(element.floatValue());
    }

    @Override
    public void visitDouble(NbtDouble element) {
        this.writer.putDoubleEntry(element.doubleValue());
    }

    @Override
    public void visitByteArray(NbtByteArray element) {
        this.writer.putByteArrayEntry(element.getByteArray());
    }

    @Override
    public void visitIntArray(NbtIntArray element) {
        this.writer.putIntArrayEntry(element.getIntArray());
    }

    @Override
    public void visitLongArray(NbtLongArray element) {
        this.writer.putLongArrayEntry(element.getLongArray());
    }

    @Override
    public void visitList(NbtList element) {
        this.writer.startFixedListEntry(element.size(), element.getHeldType());
        for (NbtElement elementBase : element) {
            elementBase.accept(this);
        }
    }

    @Override
    public void visitCompound(NbtCompound compound) {
        for (String name : compound.getKeys()) {
            var element = compound.get(name);
            this.visit(name, element);
        }
        this.writer.finishCompound();
    }

    public void visitString(byte[] name, NbtString element) {
        this.writer.putString(name, element.asString());
    }

    public void visitString(String name, NbtString element) {
        this.visitString(NbtWriter.getStringBytes(name), element);
    }

    public void visitByte(byte[] name, NbtByte element) {
        this.writer.putByte(name, element.byteValue());
    }

    public void visitByte(String name, NbtByte element) {
        this.visitByte(NbtWriter.getStringBytes(name), element);
    }

    public void visitShort(byte[] name, NbtShort element) {
        this.writer.putShort(name, element.shortValue());
    }

    public void visitShort(String name, NbtShort element) {
        this.visitShort(NbtWriter.getStringBytes(name), element);
    }

    public void visitInt(byte[] name, NbtInt element) {
        this.writer.putInt(name, element.intValue());
    }

    public void visitInt(String name, NbtInt element) {
        this.visitInt(NbtWriter.getStringBytes(name), element);
    }

    public void visitLong(byte[] name, NbtLong element) {
        this.writer.putLong(name, element.longValue());
    }

    public void visitLong(String name, NbtLong element) {
        this.visitLong(NbtWriter.getStringBytes(name), element);
    }

    public void visitFloat(byte[] name, NbtFloat element) {
        this.writer.putFloat(name, element.floatValue());
    }

    public void visitFloat(String name, NbtFloat element) {
        this.visitFloat(NbtWriter.getStringBytes(name), element);
    }

    public void visitDouble(byte[] name, NbtDouble element) {
        this.writer.putDouble(name, element.doubleValue());
    }

    public void visitDouble(String name, NbtDouble element) {
        this.visitDouble(NbtWriter.getStringBytes(name), element);
    }

    public void visitByteArray(byte[] name, NbtByteArray element) {
        this.writer.putByteArray(name, element.getByteArray());
    }

    public void visitByteArray(String name, NbtByteArray element) {
        this.visitByteArray(NbtWriter.getStringBytes(name), element);
    }

    public void visitIntArray(byte[] name, NbtIntArray element) {
        this.writer.putIntArray(name, element.getIntArray());
    }

    public void visitIntArray(String name, NbtIntArray element) {
        this.visitIntArray(NbtWriter.getStringBytes(name), element);
    }

    public void visitLongArray(byte[] name, NbtLongArray element) {
        this.writer.putLongArray(name, element.getLongArray());
    }

    public void visitLongArray(String name, NbtLongArray element) {
        this.visitLongArray(NbtWriter.getStringBytes(name), element);
    }

    public void visitList(byte[] name, NbtList element) {
        this.writer.startFixedList(name, element.size(), element.getHeldType());
        for (NbtElement elementBase : element) {
            elementBase.accept(this);
        }
    }

    public void visitList(String name, NbtList element) {
        this.visitList(NbtWriter.getStringBytes(name), element);
    }

    public void visitCompound(byte[] name, NbtCompound compound) {
        this.writer.startCompound(name);
        for (String nameBase : compound.getKeys()) {
            var element = compound.get(nameBase);
            this.visit(nameBase, element);
        }
        this.writer.finishCompound();
    }

    public void visitCompound(String name, NbtCompound compound) {
        this.visitCompound(NbtWriter.getStringBytes(name), compound);
    }

    public void visit(String nameBase, NbtElement element) {
        switch (element.getType()) {
            case NbtElement.STRING_TYPE -> this.visitString(nameBase, (NbtString) element);
            case NbtElement.BYTE_TYPE -> this.visitByte(nameBase, (NbtByte) element);
            case NbtElement.SHORT_TYPE -> this.visitShort(nameBase, (NbtShort) element);
            case NbtElement.INT_TYPE -> this.visitInt(nameBase, (NbtInt) element);
            case NbtElement.LONG_TYPE -> this.visitLong(nameBase, (NbtLong) element);
            case NbtElement.FLOAT_TYPE -> this.visitFloat(nameBase, (NbtFloat) element);
            case NbtElement.DOUBLE_TYPE -> this.visitDouble(nameBase, (NbtDouble) element);
            case NbtElement.BYTE_ARRAY_TYPE -> this.visitByteArray(nameBase, (NbtByteArray) element);
            case NbtElement.INT_ARRAY_TYPE -> this.visitIntArray(nameBase, (NbtIntArray) element);
            case NbtElement.LONG_ARRAY_TYPE -> this.visitLongArray(nameBase, (NbtLongArray) element);
            case NbtElement.LIST_TYPE -> this.visitList(nameBase, (NbtList) element);
            case NbtElement.COMPOUND_TYPE -> this.visitCompound(nameBase, (NbtCompound) element);
            default -> throw new IllegalArgumentException("Unknown NbtElement type: " + element.getType());
        }
    }

    public void visit(byte[] nameBase, NbtElement element) {
        switch (element.getType()) {
            case NbtElement.STRING_TYPE -> this.visitString(nameBase, (NbtString) element);
            case NbtElement.BYTE_TYPE -> this.visitByte(nameBase, (NbtByte) element);
            case NbtElement.SHORT_TYPE -> this.visitShort(nameBase, (NbtShort) element);
            case NbtElement.INT_TYPE -> this.visitInt(nameBase, (NbtInt) element);
            case NbtElement.LONG_TYPE -> this.visitLong(nameBase, (NbtLong) element);
            case NbtElement.FLOAT_TYPE -> this.visitFloat(nameBase, (NbtFloat) element);
            case NbtElement.DOUBLE_TYPE -> this.visitDouble(nameBase, (NbtDouble) element);
            case NbtElement.BYTE_ARRAY_TYPE -> this.visitByteArray(nameBase, (NbtByteArray) element);
            case NbtElement.INT_ARRAY_TYPE -> this.visitIntArray(nameBase, (NbtIntArray) element);
            case NbtElement.LONG_ARRAY_TYPE -> this.visitLongArray(nameBase, (NbtLongArray) element);
            case NbtElement.LIST_TYPE -> this.visitList(nameBase, (NbtList) element);
            case NbtElement.COMPOUND_TYPE -> this.visitCompound(nameBase, (NbtCompound) element);
            default -> throw new IllegalArgumentException("Unknown NbtElement type: " + element.getType());
        }
    }

    @Override
    public void visitEnd(NbtEnd element) {
        throw new IllegalArgumentException("Cannot visit null element");
    }
}
