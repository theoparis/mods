package com.eiag.mixin.client;

import com.eiag.FirePayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Routes both the initial click and vanilla's held-use loop through one fire request. */
@Mixin(Minecraft.class)
public abstract class GunUseInputMixin {
    @Shadow public LocalPlayer player;
    @Shadow public MultiPlayerGameMode gameMode;
    @Shadow private int rightClickDelay;

    @Inject(method = "startUseItem", at = @At("HEAD"), cancellable = true)
    private void eiag$requestShot(CallbackInfo ci) {
        if (player == null || gameMode == null || player.isSpectator()) return;
        InteractionHand hand = player.getMainHandItem().isEmpty()
                ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
        ItemStack stack = player.getItemInHand(hand);
        if (stack.isEmpty()) return;

        // Always consume the gun interaction, even while cooling down: no vanilla
        // placement, food, container/entity interaction, or offhand fallback.
        ci.cancel();
        // A nonzero delay prevents a second held-use call on the initial click's tick.
        // Thereafter vanilla polls held input once per tick; the server owns fire rate.
        rightClickDelay = 1;
        if (!player.isAlive() || player.isHandsBusy() || gameMode.isDestroying()
                || !stack.isItemEnabled(player.level().enabledFeatures())) return;

        ((GunCarriedItemInvoker) gameMode).eiag$ensureHasSentCarriedItem();
        ClientPlayNetworking.send(new FirePayload(hand, player.getYRot(), player.getXRot()));
    }
}
