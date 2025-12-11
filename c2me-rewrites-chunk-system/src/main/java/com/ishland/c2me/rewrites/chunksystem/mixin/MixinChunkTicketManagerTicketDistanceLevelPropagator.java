package com.ishland.c2me.rewrites.chunksystem.mixin;

import com.ishland.c2me.rewrites.chunksystem.common.ducks.TicketDistanceLevelPropagatorExtension;
import it.unimi.dsi.fastutil.longs.Long2IntLinkedOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2IntMap;
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import net.minecraft.server.world.TicketDistanceLevelPropagator;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TicketDistanceLevelPropagator.class)
public class MixinChunkTicketManagerTicketDistanceLevelPropagator implements TicketDistanceLevelPropagatorExtension {

    @Shadow @Final private static int UNLOADED;

    @Unique
    private Long2IntMap c2me$levels;

    @Unique
    private Long2IntLinkedOpenHashMap c2me$ticketLevelUpdates;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void postInit(CallbackInfo ci) {
        this.c2me$levels = new Long2IntOpenHashMap();
        this.c2me$levels.defaultReturnValue(UNLOADED + 1);
        this.c2me$ticketLevelUpdates = new Long2IntLinkedOpenHashMap() {
            @Override
            protected void rehash(int newN) {
                if (newN <= n) {
                    return; // prevent shrinking
                }
                super.rehash(newN);
            }
        };
    }

    /**
     * @author ishland
     * @reason use internal levels
     */
    @Overwrite
    public int getLevel(long id) {
        return this.c2me$levels.get(id);
    }

    /**
     * @author ishland
     * @reason use internal levels, plus defer update
     */
    @Overwrite
    protected void setLevel(long id, int level) {
        if (level >= UNLOADED) {
            this.c2me$levels.remove(id);
        } else {
            this.c2me$levels.put(id, level);
        }
        this.c2me$ticketLevelUpdates.putAndMoveToLast(id, level);
    }

    @Override
    public Long2IntLinkedOpenHashMap c2me$getTicketLevelUpdates() {
        return this.c2me$ticketLevelUpdates;
    }

}
