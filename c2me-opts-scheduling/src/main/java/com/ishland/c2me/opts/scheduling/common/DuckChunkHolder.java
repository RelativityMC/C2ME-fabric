package com.ishland.c2me.opts.scheduling.common;

import net.minecraft.world.LightType;

public interface DuckChunkHolder {

    void c2me$queueLightSectionDirty(LightType lightType, int sectionY);

    boolean c2me$shouldScheduleUndirty();

    void c2me$undirtyLight();

}
