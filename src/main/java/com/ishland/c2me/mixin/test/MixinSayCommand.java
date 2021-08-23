package com.ishland.c2me.mixin.test;

import net.minecraft.network.MessageType;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.command.SayCommand;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.UUID;

@Mixin(SayCommand.class)
public class MixinSayCommand {

    @Dynamic
    @Redirect(method = "method_13563", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/PlayerManager;broadcastChatMessage(Lnet/minecraft/text/Text;Lnet/minecraft/network/MessageType;Ljava/util/UUID;)V"))
    private static void redirectBroadcast(PlayerManager playerManager, Text message, MessageType type, UUID sender) {
        playerManager.broadcastChatMessage(new LiteralText(String.format("[%dgt] ", playerManager.getServer().getTicks())).append(message), type, sender);
    }

}
