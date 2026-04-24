package com.ishland.c2me.rewrites.chunksystem.mixin.vmp;

import com.bawnorton.mixinsquared.TargetHandler;
import net.minecraft.server.world.ChunkHolder;
import net.minecraft.server.world.ChunkTicketManager;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * MixinSquared integration with Volumetrica/VMP's {@code MixinChunkTicketManager#tickTickets}.
 * <p>
 * <b>Not applied on NeoForge</b> (VMP is not on the classpath; MixinSquared would fail to resolve
 * {@link TargetHandler} targets). See {@link com.ishland.c2me.rewrites.chunksystem.MixinPlugin}.
 * <p>
 * <b>Applied on Fabric</b> only when the VMP target mixin is present (see
 * {@link com.ishland.c2me.rewrites.chunksystem.MixinPlugin#shouldApplyMixin}).
 */
@Mixin(value = ChunkTicketManager.class, priority = 1050)
public abstract class MixinChunkTicketManagerVmp {

    @Dynamic
    @TargetHandler(
            mixin = "com.ishland.vmp.mixins.ticketsystem.ticketpropagator.MixinChunkTicketManager",
            name = "tickTickets"
    )
    @Redirect(
            method = "@MixinSquared:Handler",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ChunkHolder;getLevel()I"),
            require = 0
    )
    private int fakeLevelForVmp(ChunkHolder instance) {
        return Integer.MAX_VALUE;
    }

    @Dynamic
    @TargetHandler(
            mixin = "com.ishland.vmp.mixins.ticketsystem.ticketpropagator.MixinChunkTicketManager",
            name = "tickTickets"
    )
    @Redirect(
            method = "@MixinSquared:Handler",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/world/ChunkTicketManager;getChunkHolder(J)Lnet/minecraft/server/world/ChunkHolder;"
            ),
            require = 0
    )
    private ChunkHolder fakeLevelForVmp(ChunkTicketManager instance, long pos) {
        return null;
    }

    @Dynamic
    @TargetHandler(
            mixin = "com.ishland.vmp.mixins.ticketsystem.ticketpropagator.MixinChunkTicketManager",
            name = "tickTickets"
    )
    @Redirect(
            method = "@MixinSquared:Handler",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/server/world/ChunkLevels;INACCESSIBLE:I",
                    opcode = Opcodes.GETSTATIC,
                    ordinal = 0
            ),
            require = 0
    )
    private int fakeInaccessibleForVmp() {
        return Integer.MAX_VALUE - 1;
    }
}
