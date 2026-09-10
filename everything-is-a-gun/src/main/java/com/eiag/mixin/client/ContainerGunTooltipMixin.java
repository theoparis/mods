package com.eiag.mixin.client;

import com.eiag.GunStats;
import com.eiag.client.GunTooltip;
import java.util.Optional;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/** Inventory/chest hover path: it passes ItemStack#getTooltipImage directly. */
@Mixin(AbstractContainerScreen.class)
public abstract class ContainerGunTooltipMixin {
    @Redirect(method = "extractTooltip(Lnet/minecraft/client/gui/GuiGraphicsExtractor;II)V", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/world/item/ItemStack;getTooltipImage()Ljava/util/Optional;"))
    private Optional<TooltipComponent> eiag$inventoryGunPanel(ItemStack stack) {
        return Optional.of(new GunTooltip.Data(GunStats.forStack(stack), stack.getTooltipImage()));
    }
}
