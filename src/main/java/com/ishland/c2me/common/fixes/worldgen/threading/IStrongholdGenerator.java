package com.ishland.c2me.common.fixes.worldgen.threading;

import net.minecraft.structure.StrongholdGenerator;

public class IStrongholdGenerator {

    public static final ThreadLocal<Class<? extends StrongholdGenerator.Piece>> getActivePieceTypeThreadLocal = new ThreadLocal<>();

}
