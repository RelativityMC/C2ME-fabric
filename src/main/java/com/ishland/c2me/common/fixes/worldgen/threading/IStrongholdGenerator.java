package com.ishland.c2me.common.fixes.worldgen.threading;

import net.minecraft.structure.StrongholdGenerator;

public interface IStrongholdGenerator {

    ThreadLocal<Class<? extends StrongholdGenerator.Piece>> getActivePieceTypeThreadLocal();

    class Holder {
        @SuppressWarnings({"InstantiationOfUtilityClass", "ConstantConditions"})
        public static final IStrongholdGenerator INSTANCE = (IStrongholdGenerator) new StrongholdGenerator();
    }

}
