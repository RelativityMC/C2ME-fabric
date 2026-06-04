/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2021-2026 ishland
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

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
        this.c2me$ticketLevelUpdates = new Long2IntLinkedOpenHashMap();
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
