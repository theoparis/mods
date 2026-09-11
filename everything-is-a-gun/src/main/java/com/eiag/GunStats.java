package com.eiag;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/** Authoritative weapon profiles for genuine vanilla item stacks. Damage is in health points. */
public record GunStats(
        String weaponClass, float damage, double range, int cooldownTicks, double ratePerSecond,
        double accuracyDegrees, int control, int magazineSize, double reloadSeconds) {
    private static final GunStats PISTOL = new GunStats("PISTOL", 5.0F, 40.0, 8, 20.0 / 8.0, 0.8, 14, 12, 1.5);
    private static final GunStats RIFLE = new GunStats("RIFLE", 7.0F, 72.0, 5, 20.0 / 5.0, 0.35, 12, 30, 2.4);
    private static final GunStats SMG = new GunStats("SMG", 3.0F, 32.0, 2, 20.0 / 2.0, 1.6, 16, 32, 1.8);
    private static final GunStats SNIPER = new GunStats("SNIPER", 12.0F, 100.0, 24, 20.0 / 24.0, 0.05, 4, 5, 3.2);
    public static final GunStats DIAMOND = new GunStats("RIFLE", 14.0F, 25.0, 17, 1.2, 0.1, 10, 2, 4.5);
    // Every displayed stat is deliberately at the panel maximum for this legendary material.
    public static final GunStats NETHER_STAR = new GunStats("STAR CANNON", 15.0F, 100.0, 2, 10.0, 0.0, 20, 50, 0.5);

    public GunStats {
        if (weaponClass.isBlank() || !Float.isFinite(damage) || damage < 0 || !Double.isFinite(range) || range <= 0
                || cooldownTicks <= 0 || !Double.isFinite(ratePerSecond) || ratePerSecond <= 0 || !Double.isFinite(accuracyDegrees) || accuracyDegrees < 0
                || control < 0 || magazineSize < 0 || !Double.isFinite(reloadSeconds) || reloadSeconds < 0) {
            throw new IllegalArgumentException("Invalid gun stats");
        }
    }

    /** Vertical kick in degrees; control is the inverse recoil stat (0..20). */
    public double recoilDegrees() { return 5.0 * (1.0 - Math.clamp(control / 20.0, 0.0, 1.0)); }

    public static GunStats forStack(ItemStack stack) {
        if (stack.is(Items.DIAMOND)) return DIAMOND;
        if (stack.is(Items.NETHER_STAR)) return NETHER_STAR;
        return switch (GunType.forStack(stack)) {
            case PISTOL -> PISTOL;
            case RIFLE -> RIFLE;
            case SMG -> SMG;
            case SNIPER -> SNIPER;
        };
    }
}
