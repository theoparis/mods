package com.eiag.client;

/** Short first-person weapon kick, triggered only by authoritative shot confirmation. */
public final class GunFireAnimation {
    private static final long DURATION_NANOS = 260_000_000L;
    private static volatile long startedAt;

    private GunFireAnimation() {}

    public static void trigger() { startedAt = System.nanoTime(); }

    /** 0 at rest, peaking at 1 halfway through the raise-and-return motion. */
    public static float progress() {
        long elapsed = System.nanoTime() - startedAt;
        if (elapsed <= 0 || elapsed >= DURATION_NANOS) return 0.0F;
        return (float) Math.sin(Math.PI * (double) elapsed / DURATION_NANOS);
    }
}
