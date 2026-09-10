package com.eiag;

import java.util.List;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.SwingAnimation;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/** Shared gun behaviour for genuine vanilla item stacks. */
public final class Gunfire {
    private static final float DAMAGE = 4.0F;
    private static final double RANGE = 48.0;
    private static final int COOLDOWN_TICKS = 7;

    private Gunfire() {}

    /** Fires the stack currently held by {@code player}; called on both sides by Fabric's callbacks. */
    public static void use(Player player, Level level, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.getCooldowns().isOnCooldown(stack)) {
            return;
        }
        player.getCooldowns().addCooldown(stack, COOLDOWN_TICKS);
        player.swing(hand, SwingAnimation.DEFAULT, true);

        // Damage and authoritative particles/sound belong on the logical server.
        if (level instanceof ServerLevel serverLevel) {
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.DISPENSER_FAIL, SoundSource.PLAYERS, 1.0F, 1.0F);
            fire(serverLevel, player, hand);
        }
    }

    private static void fire(ServerLevel level, Player shooter, InteractionHand hand) {
        Vec3 eye = shooter.getEyePosition(1.0F);
        Vec3 look = shooter.getLookAngle();
        Vec3 end = eye.add(look.scale(RANGE));
        BlockHitResult block = level.clip(new ClipContext(
                eye, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, shooter));
        Vec3 traceEnd = block.getType() == HitResult.Type.MISS ? end : block.getLocation();
        double closest = eye.distanceToSqr(traceEnd);

        Entity target = null;
        Vec3 targetPos = null;
        AABB search = shooter.getBoundingBox().expandTowards(look.scale(RANGE)).inflate(1.0);
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

        if (target instanceof LivingEntity living) {
            var source = level.damageSources().playerAttack(shooter);
            living.hurtServer(level, source, DAMAGE);
            living.knockback(0.4, look.x, look.z, source, 0.0F);
        }
        Vec3 impact = targetPos != null ? targetPos : traceEnd;
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
