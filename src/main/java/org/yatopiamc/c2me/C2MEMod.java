package org.yatopiamc.c2me;

import net.fabricmc.api.ModInitializer;
import org.yatopiamc.c2me.metrics.Metrics;

public class C2MEMod implements ModInitializer {
    @Override
    public void onInitialize() {
        final Metrics metrics = new Metrics(10514);
    }
}
