package com.ishland.c2me.compatibility.common;

import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.AxisCycleDirection;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Position;
import net.minecraft.util.math.Vec3i;

public class ThreadLocalMutableBlockPos extends BlockPos.Mutable {

    private final ThreadLocal<Mutable> delegate = ThreadLocal.withInitial(Mutable::new);

    @Override
    public BlockPos add(double d, double e, double f) {
        return delegate.get().add(d, e, f);
    }

    @Override
    public BlockPos add(int i, int j, int k) {
        return delegate.get().add(i, j, k);
    }

    @Override
    public BlockPos offset(Direction direction, int i) {
        return delegate.get().offset(direction, i);
    }

    @Override
    public BlockPos offset(Direction.Axis axis, int i) {
        return delegate.get().offset(axis, i);
    }

    @Override
    public BlockPos rotate(BlockRotation rotation) {
        return delegate.get().rotate(rotation);
    }

    @Override
    public Mutable set(int x, int y, int z) {
        return delegate.get().set(x, y, z);
    }

    @Override
    public Mutable set(double x, double y, double z) {
        return delegate.get().set(x, y, z);
    }

    @Override
    public Mutable set(Vec3i pos) {
        return delegate.get().set(pos);
    }

    @Override
    public Mutable set(long pos) {
        return delegate.get().set(pos);
    }

    @Override
    public Mutable set(AxisCycleDirection axis, int x, int y, int z) {
        return delegate.get().set(axis, x, y, z);
    }

    @Override
    public Mutable set(Vec3i pos, Direction direction) {
        return delegate.get().set(pos, direction);
    }

    @Override
    public Mutable set(Vec3i pos, int x, int y, int z) {
        return delegate.get().set(pos, x, y, z);
    }

    @Override
    public Mutable move(Direction direction) {
        return delegate.get().move(direction);
    }

    @Override
    public Mutable move(Direction direction, int distance) {
        return delegate.get().move(direction, distance);
    }

    @Override
    public Mutable move(int dx, int dy, int dz) {
        return delegate.get().move(dx, dy, dz);
    }

    @Override
    public Mutable move(Vec3i vec) {
        return delegate.get().move(vec);
    }

    @Override
    public Mutable clamp(Direction.Axis axis, int min, int max) {
        return delegate.get().clamp(axis, min, max);
    }

    @Override
    public BlockPos toImmutable() {
        return delegate.get().toImmutable();
    }

    @Override
    public long asLong() {
        return delegate.get().asLong();
    }

    @Override
    public BlockPos add(Vec3i vec3i) {
        return delegate.get().add(vec3i);
    }

    @Override
    public BlockPos subtract(Vec3i vec3i) {
        return delegate.get().subtract(vec3i);
    }

    @Override
    public BlockPos up() {
        return delegate.get().up();
    }

    @Override
    public BlockPos up(int distance) {
        return delegate.get().up(distance);
    }

    @Override
    public BlockPos down() {
        return delegate.get().down();
    }

    @Override
    public BlockPos down(int i) {
        return delegate.get().down(i);
    }

    @Override
    public BlockPos north() {
        return delegate.get().north();
    }

    @Override
    public BlockPos north(int distance) {
        return delegate.get().north(distance);
    }

    @Override
    public BlockPos south() {
        return delegate.get().south();
    }

    @Override
    public BlockPos south(int distance) {
        return delegate.get().south(distance);
    }

    @Override
    public BlockPos west() {
        return delegate.get().west();
    }

    @Override
    public BlockPos west(int distance) {
        return delegate.get().west(distance);
    }

    @Override
    public BlockPos east() {
        return delegate.get().east();
    }

    @Override
    public BlockPos east(int distance) {
        return delegate.get().east(distance);
    }

    @Override
    public BlockPos offset(Direction direction) {
        return delegate.get().offset(direction);
    }

    @Override
    public BlockPos crossProduct(Vec3i pos) {
        return delegate.get().crossProduct(pos);
    }

    @Override
    public Mutable mutableCopy() {
        return delegate.get().mutableCopy();
    }

    @Override
    public boolean equals(Object o) {
        return delegate.get().equals(o);
    }

    @Override
    public int hashCode() {
        return delegate.get().hashCode();
    }

    @Override
    public int compareTo(Vec3i vec3i) {
        return delegate.get().compareTo(vec3i);
    }

    @Override
    public int getX() {
        return delegate.get().getX();
    }

    @Override
    public int getY() {
        return delegate.get().getY();
    }

    @Override
    public int getZ() {
        return delegate.get().getZ();
    }

    @Override
    public boolean isWithinDistance(Vec3i vec, double distance) {
        return delegate.get().isWithinDistance(vec, distance);
    }

    @Override
    public boolean isWithinDistance(Position pos, double distance) {
        return delegate.get().isWithinDistance(pos, distance);
    }

    @Override
    public double getSquaredDistance(Vec3i vec) {
        return delegate.get().getSquaredDistance(vec);
    }

    @Override
    public double getSquaredDistance(Position pos, boolean treatAsBlockPos) {
        return delegate.get().getSquaredDistance(pos, treatAsBlockPos);
    }

    @Override
    public double getSquaredDistance(double x, double y, double z, boolean treatAsBlockPos) {
        return delegate.get().getSquaredDistance(x, y, z, treatAsBlockPos);
    }

    @Override
    public int getManhattanDistance(Vec3i vec) {
        return delegate.get().getManhattanDistance(vec);
    }

    @Override
    public int getComponentAlongAxis(Direction.Axis axis) {
        return delegate.get().getComponentAlongAxis(axis);
    }

    @Override
    public String toString() {
        return delegate.get().toString();
    }

    @Override
    public String toShortString() {
        return delegate.get().toShortString();
    }

    @Override
    public void setX(int x) {
        delegate.get().setX(x);
    }

    @Override
    public void setY(int y) {
        delegate.get().setY(y);
    }

    @Override
    public void setZ(int z) {
        delegate.get().setZ(z);
    }
}
