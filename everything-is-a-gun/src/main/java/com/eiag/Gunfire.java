package com.eiag;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/** Shared gun behaviour for genuine vanilla item stacks. */
public final class Gunfire {
    private record GunpowderProjectile(ServerLevel level, Player shooter, LivingEntity target,
            Vec3 start, Vec3 end, Vec3 direction, GunStats stats, int age, int duration) {}
    private static final List<GunpowderProjectile> GUNPOWDER_PROJECTILES = new ArrayList<>();

    private Gunfire() {}

    /** Advances slow gunpowder shots on the authoritative server tick. */
    public static void tickProjectiles(ServerLevel level) {
        for (int i = GUNPOWDER_PROJECTILES.size() - 1; i >= 0; i--) {
            GunpowderProjectile shot = GUNPOWDER_PROJECTILES.get(i);
            if (shot.level != level) continue;
            int age = shot.age + 1;
            double progress = Math.min(1.0, (double) age / shot.duration);
            Vec3 point = ballisticPoint(shot.start, shot.end, progress);
            level.sendParticles(ParticleTypes.FLAME, true, true, point.x, point.y, point.z,
                    5, 0.09, 0.09, 0.09, 0.012);
            level.sendParticles(ParticleTypes.SMOKE, true, true, point.x, point.y, point.z,
                    1, 0.03, 0.03, 0.03, 0.003);
            if (progress < 1.0) {
                GUNPOWDER_PROJECTILES.set(i, new GunpowderProjectile(level, shot.shooter, shot.target,
                        shot.start, shot.end, shot.direction, shot.stats, age, shot.duration));
                continue;
            }
            GUNPOWDER_PROJECTILES.remove(i);
            // Explosion damage is evaluated now, rather than retaining only the original ray target.
            // This catches enemies which walk into the blast while the fireball is in flight.
            double blastRadius = 3.5;
            var source = level.damageSources().playerAttack(shot.shooter);
            for (Entity entity : level.getEntities(shot.shooter,
                    new AABB(point, point).inflate(blastRadius), Entity::isAlive)) {
                if (!(entity instanceof LivingEntity living)) continue;
                double distance = living.getBoundingBox().getCenter().distanceTo(point);
                if (distance > blastRadius) continue;
                float damage = (float) (shot.stats.damage() * (1.0 - distance / blastRadius));
                if (damage <= 0.0F) continue;
                living.hurtServer(level, source, damage);
                Vec3 away = living.position().subtract(point);
                living.knockback(1.1 * (1.0 - distance / blastRadius), away.x, away.z, source, 0.25F);
            }
            level.sendParticles(ParticleTypes.EXPLOSION_EMITTER, true, true, point.x, point.y, point.z,
                    1, 0, 0, 0, 0);
            level.sendParticles(ParticleTypes.FLAME, true, true, point.x, point.y, point.z,
                    30, 0.65, 0.65, 0.65, 0.08);
            level.playSound(null, point.x, point.y, point.z, SoundEvents.GENERIC_EXPLODE.value(),
                    SoundSource.PLAYERS, 1.0F, 1.0F);
        }
    }

    /**
     * A gravity-like arc which begins and ends exactly at the authoritative ray endpoints.
     * This keeps the explosion at the aimed destination while making its flight visibly fall.
     */
    private static Vec3 ballisticPoint(Vec3 start, Vec3 end, double progress) {
        double distance = start.distanceTo(end);
        double arcHeight = Math.min(6.0, Math.max(0.75, distance * 0.12));
        return start.lerp(end, progress).add(0, 4.0 * arcHeight * progress * (1.0 - progress), 0);
    }

    /** Fires the stack currently held by {@code player}; called on both sides by Fabric's callbacks. */
    public static boolean use(Player player, Level level, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.getCooldowns().isOnCooldown(stack)) {
            return false;
        }
        GunStats stats = GunStats.forStack(stack);
        player.getCooldowns().addCooldown(stack, stats.cooldownTicks());

        // Damage and authoritative particles/sound belong on the logical server.
        if (level instanceof ServerLevel serverLevel) {
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.DISPENSER_FAIL, SoundSource.PLAYERS, 1.0F, 1.0F);
            fire(serverLevel, player, hand, stats);
            applyRecoil(player, stats);
        }
        return true;
    }

    /** Applies a small upward kick plus controlled horizontal variance after a shot. */
    private static void applyRecoil(Player player, GunStats stats) {
        float pitchKick = (float) stats.recoilDegrees();
        float yawKick = pitchKick <= 0.0F ? 0.0F
                : (player.getRandom().nextFloat() - 0.5F) * pitchKick * 0.35F;
        if (pitchKick > 0.0F) {
            player.setYRot(player.getYRot() + yawKick);
            player.setYHeadRot(player.getYRot());
            player.setXRot(Math.clamp(player.getXRot() - pitchKick, -90.0F, 90.0F));
        }
        // This packet doubles as a shot-confirmation for the held-model animation,
        // including max-control weapons whose physical recoil is zero.
        if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.connection.send(new ClientboundCustomPayloadPacket(new RecoilPayload(yawKick, pitchKick)));
        }
    }

    private static void fire(ServerLevel level, Player shooter, InteractionHand hand, GunStats stats) {
        Vec3 eye = shooter.getEyePosition(1.0F);
        Vec3 look = shooter.getLookAngle();
        Vec3 end = eye.add(look.scale(stats.range()));
        BlockHitResult block = level.clip(new ClipContext(
                eye, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, shooter));
        Vec3 traceEnd = block.getType() == HitResult.Type.MISS ? end : block.getLocation();
        double closest = eye.distanceToSqr(traceEnd);

        Entity target = null;
        Vec3 targetPos = null;
        AABB search = shooter.getBoundingBox().expandTowards(look.scale(stats.range())).inflate(1.0);
        List<Entity> candidates = level.getEntities(shooter, search,
                entity -> entity.isPickable() && !entity.isSpectator());
        for (Entity candidate : candidates) {
            var hit = candidate.getBoundingBox().inflate(candidate.getPickRadius()).clip(eye, end);
            if (hit.isEmpty()) continue;
            double distance = eye.distanceToSqr(hit.get());
            if (distance < closest) {
                closest = distance;
                target = candidate;
                targetPos = hit.get();
            }
        }

        Vec3 impact = targetPos != null ? targetPos : traceEnd;
        if (shooter.getItemInHand(hand).is(Items.GUNPOWDER)) {
            int travelTicks = Math.clamp((int) Math.ceil(eye.distanceTo(impact) / 1.5), 2, 40);
            LivingEntity living = target instanceof LivingEntity entity ? entity : null;
            GUNPOWDER_PROJECTILES.add(new GunpowderProjectile(level, shooter, living, eye, impact,
                    look, stats, 0, travelTicks));
            level.playSound(null, eye.x, eye.y, eye.z, SoundEvents.TNT_PRIMED, SoundSource.PLAYERS, 0.8F, 1.3F);
            return;
        }
        if (target instanceof LivingEntity living) {
            var source = level.damageSources().playerAttack(shooter);
            living.hurtServer(level, source, stats.damage());
            living.knockback(0.4, look.x, look.z, source, 0.0F);
        }
        // Visual muzzle approximation only; damage still uses the eye ray.
        double yaw = Math.toRadians(shooter.getYRot());
        Vec3 right = new Vec3(-Math.cos(yaw), 0, -Math.sin(yaw));
        boolean rightHand = shooter.getMainArm() == net.minecraft.world.entity.HumanoidArm.RIGHT;
        if (hand == InteractionHand.OFF_HAND) rightHand = !rightHand;
        Vec3 muzzle = eye.add(look.scale(0.45)).add(right.scale(rightHand ? 0.3 : -0.3))
                .add(0, -0.25, 0);
        // Avoid starting the visual through a nearby wall.
        BlockHitResult muzzleClip = level.clip(new ClipContext(
                eye, muzzle, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, shooter));
        if (muzzleClip.getType() != HitResult.Type.MISS) muzzle = eye;
        broadcastLaser(level, muzzle, impact);
        level.sendParticles(ParticleTypes.CRIT, impact.x, impact.y, impact.z,
                6, 0.1, 0.1, 0.1, 0.01);
    }

    /** Sends the authoritative ray to every player tracking this level. */
    private static void broadcastLaser(ServerLevel level, Vec3 start, Vec3 end) {
        ClientboundCustomPayloadPacket packet = new ClientboundCustomPayloadPacket(
                new LaserPayload(start, end));
        for (var player : level.players()) {
            player.connection.send(packet);
        }
    }
}
