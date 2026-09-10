package com.eiag.client;

import java.util.ArrayList;
import java.util.List;

import com.eiag.LaserPayload;
import com.eiag.RecoilPayload;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.world.phys.Vec3;

/** Renders each received shot as a camera-facing translucent gold ribbon. */
public final class LaserRenderer implements ClientModInitializer {
    private static final long LIFETIME_NANOS = 350_000_000L;
    private static final List<Beam> BEAMS = new ArrayList<>();

    private record Beam(Vec3 start, Vec3 end, long expiresAt) {}

    @Override
    public void onInitializeClient() {
        ClientPlayNetworking.registerGlobalReceiver(LaserPayload.TYPE, (payload, context) ->
                context.client().execute(() -> BEAMS.add(new Beam(
                        payload.start(), payload.end(), System.nanoTime() + LIFETIME_NANOS))));
        ClientPlayNetworking.registerGlobalReceiver(RecoilPayload.TYPE, (payload, context) ->
                context.client().execute(() -> {
                    var player = context.client().player;
                    if (player == null) return;
                    player.setYRot(player.getYRot() + payload.yawDegrees());
                    player.setYHeadRot(player.getYRot());
                    player.setXRot(Math.clamp(player.getXRot() - payload.pitchDegrees(), -90.0F, 90.0F));
                }));
        // Submit before feature buffers are prepared, not after they execute.
        LevelRenderEvents.COLLECT_SUBMITS.register(LaserRenderer::render);
    }

    private static void render(net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext context) {
        long now = System.nanoTime();
        BEAMS.removeIf(beam -> beam.expiresAt <= now);
        if (BEAMS.isEmpty()) return;

        Vec3 camera = context.levelState().cameraRenderState.pos;
        for (Beam beam : BEAMS) {
            float alpha = (float) (beam.expiresAt - now) / LIFETIME_NANOS;
            submitRibbon(context, beam.start, beam.end, camera, alpha);
        }
    }

    private static void submitRibbon(
            net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext context,
            Vec3 worldStart, Vec3 worldEnd, Vec3 camera, float alpha) {
        if (worldEnd.distanceToSqr(worldStart) < 1.0e-8) return;
        Vec3 direction = worldEnd.subtract(worldStart).normalize();
        Vec3 center = worldStart.add(worldEnd).scale(0.5);
        Vec3 calculatedSide = direction.cross(camera.subtract(center)).normalize();
        // Even when viewed exactly end-on, keep the width perpendicular to
        // the ray (a fixed world-up fallback fails for vertical shots).
        Vec3 reference = Math.abs(direction.y) < 0.9
                ? new Vec3(0, 1, 0) : new Vec3(1, 0, 0);
        final Vec3 side = calculatedSide.lengthSqr() < 0.0001
                ? direction.cross(reference).normalize() : calculatedSide;

        // The render pose is camera-relative; ribbon widens toward the impact,
        // matching the translucent cone in the reference.
        Vec3 start = worldStart.subtract(camera);
        Vec3 end = worldEnd.subtract(camera);
        Vec3 near = side.scale(0.055);
        Vec3 far = side.scale(0.24);
        int outerAlpha = Math.max(0, Math.min(150, (int) (150 * alpha)));
        int coreAlpha = Math.max(0, Math.min(220, (int) (220 * alpha)));

        context.submitNodeCollector().submitCustomGeometry(
                context.poseStack(), RenderTypes.lightning(),
                (pose, vertices) -> {
                    quad(vertices, pose, start.add(near), start.subtract(near),
                            end.subtract(far), end.add(far), 255, 188, 54, outerAlpha);
                    // A narrow brighter core gives the ribbon a laser-like centre.
                    Vec3 coreNear = side.scale(0.018);
                    Vec3 coreFar = side.scale(0.075);
                    quad(vertices, pose, start.add(coreNear), start.subtract(coreNear),
                            end.subtract(coreFar), end.add(coreFar), 255, 245, 190, coreAlpha);
                });
    }

    private static void quad(VertexConsumer vertices, com.mojang.blaze3d.vertex.PoseStack.Pose pose,
                             Vec3 a, Vec3 b, Vec3 c, Vec3 d, int red, int green, int blue, int alpha) {
        vertex(vertices, pose, a, red, green, blue, alpha);
        vertex(vertices, pose, b, red, green, blue, alpha);
        vertex(vertices, pose, c, red, green, blue, alpha);
        vertex(vertices, pose, d, red, green, blue, alpha);
        // Lightning's pipeline uses face culling. Emit the reverse winding
        // too: only the camera-facing side survives, without double blending.
        vertex(vertices, pose, d, red, green, blue, alpha);
        vertex(vertices, pose, c, red, green, blue, alpha);
        vertex(vertices, pose, b, red, green, blue, alpha);
        vertex(vertices, pose, a, red, green, blue, alpha);
    }

    private static void vertex(VertexConsumer vertices, com.mojang.blaze3d.vertex.PoseStack.Pose pose,
                               Vec3 point, int red, int green, int blue, int alpha) {
        vertices.addVertex(pose, (float) point.x, (float) point.y, (float) point.z)
                .setColor(red, green, blue, alpha);
    }
}
