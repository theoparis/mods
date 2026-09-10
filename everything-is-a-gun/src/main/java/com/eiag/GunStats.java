package com.eiag;

import java.util.Map;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

/** Authoritative weapon profiles for genuine vanilla item stacks. Damage is in health points. */
public record GunStats(
        String weaponClass, float damage, double range, int cooldownTicks, double ratePerSecond,
        double accuracyDegrees, int control, int magazineSize, double reloadSeconds) {
    public static final GunStats DEFAULT = new GunStats("GUN", 4.0F, 48.0, 7, 20.0 / 7.0, 0.0, 0, 0, 0.0);
    public static final GunStats DIAMOND = new GunStats("RIFLE", 14.0F, 25.0, 17, 1.2, 0.1, 10, 2, 4.5);
    // Every displayed stat is deliberately at the panel maximum for this legendary material.
    public static final GunStats NETHER_STAR = new GunStats("STAR CANNON", 15.0F, 100.0, 2, 10.0, 0.0, 20, 50, 0.5);

    private static final Map<Identifier, GunStats> OVERRIDES = Map.of(
            Identifier.withDefaultNamespace("diamond"), DIAMOND,
            Identifier.withDefaultNamespace("nether_star"), NETHER_STAR
    );

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
        return OVERRIDES.getOrDefault(BuiltInRegistries.ITEM.getKey(stack.getItem()), DEFAULT);
    }
}
