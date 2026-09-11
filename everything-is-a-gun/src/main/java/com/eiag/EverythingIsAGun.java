package com.eiag;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

/** Makes the actual vanilla items shoot; no replacement item registry exists. */
public class EverythingIsAGun implements ModInitializer {
    @Override
    public void onInitialize() {
        PayloadTypeRegistry.clientboundPlay().register(LaserPayload.TYPE, LaserPayload.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(RecoilPayload.TYPE, RecoilPayload.CODEC);
        PayloadTypeRegistry.serverboundPlay().register(FirePayload.TYPE, FirePayload.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(FirePayload.TYPE, (payload, context) ->
                context.server().execute(() -> Gunfire.use(context.player(), payload.hand(),
                        payload.yawDegrees(), payload.pitchDegrees())));
        ServerTickEvents.END_LEVEL_TICK.register(Gunfire::tickProjectiles);
        System.out.println("[everything-is-a-gun] loaded - vanilla items fire guns");
    }
}
