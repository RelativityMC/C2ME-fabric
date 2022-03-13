package com.ishland.c2me.fixes.worldgen.threading_issues.common;

import java.util.concurrent.atomic.AtomicInteger;

public interface INetherFortressGeneratorPieceData {

    AtomicInteger getGeneratedCountAtomic();

}
