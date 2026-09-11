package com.eiag.mixin.client;

import com.eiag.client.GunModel;
import com.eiag.client.GunRenderState;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.function.Consumer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.world.item.ItemDisplayContext;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemStackRenderState.class)
public abstract class GunItemStackRenderStateMixin implements GunRenderState {
    @Unique private ItemDisplayContext eiag$gunContext = ItemDisplayContext.NONE;
    @Unique private boolean eiag$insideMiniature;

    @Shadow public abstract void submit(PoseStack poses, SubmitNodeCollector collector,
            int light, int overlay, int outlineColor);
    @Shadow public abstract void visitExtents(Consumer<Vector3fc> consumer);

    @Override
    public void eiag$setGunContext(ItemDisplayContext context) {
        eiag$gunContext = context;
    }

    @Inject(method = "clear", at = @At("HEAD"))
    private void eiag$clearGun(CallbackInfo ci) {
        eiag$gunContext = ItemDisplayContext.NONE;
        eiag$insideMiniature = false;
    }

    @Inject(method = "submit", at = @At("HEAD"), cancellable = true)
    private void eiag$submitGun(PoseStack poses, SubmitNodeCollector collector,
            int light, int overlay, int outlineColor, CallbackInfo ci) {
        if (eiag$insideMiniature || !GunModel.isHeld(eiag$gunContext)) return;
        eiag$insideMiniature = true;
        poses.pushPose();
        try {
            GunModel.applyHandTransform(poses, eiag$gunContext);
            for (int i = 0; i < GunModel.miniatureCount(); i++) {
                poses.pushPose();
                try {
                    GunModel.applyMiniatureTransform(poses, i);
                    // Reuse resolved layers, tints, glint, and special renderers. Never
                    // resolve the stack or bake/copy its geometry per miniature.
                    submit(poses, collector, light, overlay, outlineColor);
                } finally {
                    poses.popPose();
                }
            }
        } finally {
            poses.popPose();
            eiag$insideMiniature = false;
        }
        ci.cancel();
    }

    @Inject(method = "visitExtents", at = @At("HEAD"), cancellable = true)
    private void eiag$visitGunExtents(Consumer<Vector3fc> consumer, CallbackInfo ci) {
        if (eiag$insideMiniature || !GunModel.isHeld(eiag$gunContext)) return;
        PoseStack poses = new PoseStack();
        Vector3f point = new Vector3f();
        Consumer<Vector3fc> transformed = value -> consumer.accept(point.set(value).mulPosition(poses.last().pose()));
        eiag$insideMiniature = true;
        try {
            GunModel.applyHandTransform(poses, eiag$gunContext);
            for (int i = 0; i < GunModel.miniatureCount(); i++) {
                poses.pushPose();
                GunModel.applyMiniatureTransform(poses, i);
                visitExtents(transformed);
                poses.popPose();
            }
        } finally {
            eiag$insideMiniature = false;
        }
        ci.cancel();
    }
}
