package com.eiag.client;

import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.world.InteractionResult;

/** Short first-person weapon kick, predicted locally when the fire input is pressed. */
public final class GunFireAnimation {
    private static final long DURATION_NANOS = 260_000_000L;
    private static volatile long startedAt;

    private GunFireAnimation() {}

    public static void trigger() { startedAt = System.nanoTime(); }

    public static void registerInputTrigger() {
        UseItemCallback.EVENT.register((player, level, hand) -> {
            if (level.isClientSide()) trigger();
            return InteractionResult.PASS;
        });
        UseBlockCallback.EVENT.register((player, level, hand, hit) -> {
            if (level.isClientSide()) trigger();
            return InteractionResult.PASS;
        });
    }

    /** 0 at rest, peaking at 1 halfway through the raise-and-return motion. */
    public static float progress() {
        long elapsed = System.nanoTime() - startedAt;
        if (elapsed <= 0 || elapsed >= DURATION_NANOS) return 0.0F;
        return (float) Math.sin(Math.PI * (double) elapsed / DURATION_NANOS);
    }
}
