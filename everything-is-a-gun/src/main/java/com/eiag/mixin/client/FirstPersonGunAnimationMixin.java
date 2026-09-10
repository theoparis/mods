package com.eiag.mixin.client;

import com.eiag.client.GunFireAnimation;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.FirstPersonHandsAndItemsRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.state.level.FirstPersonHandsAndItemsRenderState;
import net.minecraft.client.renderer.state.level.PlayerRenderState;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Adds a gun-like pivot kick after vanilla has chosen the held-item model. */
@Mixin(FirstPersonHandsAndItemsRenderer.class)
public abstract class FirstPersonGunAnimationMixin {
    @Inject(method = "submitArmWithItem", at = @At(value = "INVOKE",
            target = "Lcom/mojang/blaze3d/vertex/PoseStack;pushPose()V", shift = Shift.AFTER))
    private void eiag$kickHeldGun(PlayerRenderState playerState, FirstPersonHandsAndItemsRenderState handsState,
            float partialTick, float pitch, InteractionHand hand, float swing, ItemStack stack, float equip,
            PoseStack poses, SubmitNodeCollector collector, int light, CallbackInfo ci) {
        float amount = GunFireAnimation.progress();
        if (amount == 0.0F || stack.isEmpty()) return;
        // A restrained recoil kick: retreat toward the camera with a small muzzle rise.
        // Large pitch rotations make flat held-item models look like they flip vertically.
        poses.translate(0.0F, 0.0F, 0.12F * amount);
        poses.rotateDegrees(Axis.XP, -8.0F * amount);
    }
}
