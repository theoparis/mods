package com.eiag;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.world.InteractionResult;

/** Makes the actual vanilla items shoot; no replacement item registry exists. */
public class EverythingIsAGun implements ModInitializer {
    @Override
    public void onInitialize() {
        PayloadTypeRegistry.clientboundPlay().register(LaserPayload.TYPE, LaserPayload.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(RecoilPayload.TYPE, RecoilPayload.CODEC);
        PayloadTypeRegistry.serverboundPlay().register(FirePayload.TYPE, FirePayload.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(FirePayload.TYPE, (payload, context) ->
                context.server().execute(() -> Gunfire.use(context.player(), context.player().level(), payload.hand())));
        UseItemCallback.EVENT.register((player, level, hand) ->
                Gunfire.use(player, level, hand) ? InteractionResult.SUCCESS_SERVER : InteractionResult.PASS);
        // Aiming at a block must shoot too, rather than placing/using the item.
        ServerTickEvents.END_LEVEL_TICK.register(Gunfire::tickProjectiles);
        UseBlockCallback.EVENT.register((player, level, hand, hit) ->
                Gunfire.use(player, level, hand) ? InteractionResult.SUCCESS_SERVER : InteractionResult.PASS);
        // Entities otherwise consume right-click before item/block-use callbacks run.
        UseEntityCallback.EVENT.register((player, level, hand, entity, hit) ->
                level.isClientSide() ? InteractionResult.PASS
                        : (Gunfire.use(player, level, hand) ? InteractionResult.SUCCESS_SERVER : InteractionResult.PASS));
        System.out.println("[everything-is-a-gun] loaded - vanilla items fire guns");
    }
}
