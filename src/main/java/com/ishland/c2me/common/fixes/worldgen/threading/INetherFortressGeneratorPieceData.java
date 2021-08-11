package com.ishland.c2me.common.fixes.worldgen.threading;

import java.util.concurrent.atomic.AtomicInteger;

public interface INetherFortressGeneratorPieceData {

    AtomicInteger getGeneratedCountAtomic();

}
