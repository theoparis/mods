package com.eiag.mixin.client;

import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/** Keeps a same-tick hotbar change ahead of the custom fire request, just like vanilla use. */
@Mixin(MultiPlayerGameMode.class)
public interface GunCarriedItemInvoker {
    @Invoker("ensureHasSentCarriedItem")
    void eiag$ensureHasSentCarriedItem();
}
