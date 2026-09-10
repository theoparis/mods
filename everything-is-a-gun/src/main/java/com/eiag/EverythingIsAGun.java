package com.eiag;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.world.InteractionResult;

/** Makes the actual vanilla items shoot; no replacement item registry exists. */
public class EverythingIsAGun implements ModInitializer {
    @Override
    public void onInitialize() {
        PayloadTypeRegistry.clientboundPlay().register(LaserPayload.TYPE, LaserPayload.CODEC);
        UseItemCallback.EVENT.register((player, level, hand) -> {
            Gunfire.use(player, level, hand);
            return InteractionResult.SUCCESS;
        });
        // Aiming at a block must shoot too, rather than placing/using the item.
        ServerTickEvents.END_LEVEL_TICK.register(Gunfire::tickProjectiles);
        UseBlockCallback.EVENT.register((player, level, hand, hit) -> {
            Gunfire.use(player, level, hand);
            return InteractionResult.SUCCESS;
        });
        System.out.println("[everything-is-a-gun] loaded - vanilla items fire guns");
    }
}
