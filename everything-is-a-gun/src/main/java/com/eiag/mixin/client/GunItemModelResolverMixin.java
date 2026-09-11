package com.eiag.mixin.client;

import com.eiag.client.GunRenderState;
import com.eiag.client.GunModel;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemModelResolver.class)
public abstract class GunItemModelResolverMixin {
    @Shadow
    public abstract void updateForTopItem(ItemStackRenderState state, ItemStack stack,
            ItemDisplayContext context, Level level, ItemOwner owner, int seed);

    @Inject(method = "updateForTopItem", at = @At("HEAD"), cancellable = true)
    private void eiag$resolveMiniature(ItemStackRenderState state, ItemStack stack,
            ItemDisplayContext context, Level level, ItemOwner owner, int seed, CallbackInfo ci) {
        if (!GunModel.isHeld(context) || stack.isEmpty()) return;
        // NONE resolves the real item without its hand/GUI display transform. It also
        // terminates this interception; nested composite models are resolved only once.
        updateForTopItem(state, stack, ItemDisplayContext.NONE, level, owner, seed);
        ((GunRenderState) state).eiag$setGunContext(context);
        ci.cancel();
    }
}
