package com.eiag.mixin.client;

import com.eiag.GunStats;
import com.eiag.client.GunTooltip;
import java.util.Optional;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(GuiGraphicsExtractor.class)
public abstract class GunTooltipMixin {
    @Redirect(method = "setTooltipForNextFrame(Lnet/minecraft/client/gui/Font;Lnet/minecraft/world/item/ItemStack;II)V",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;getTooltipImage()Ljava/util/Optional;"))
    private Optional<TooltipComponent> eiag$gunPanel(ItemStack stack) {
        Optional<TooltipComponent> original = stack.getTooltipImage();
        return stack.isEmpty() ? original : Optional.of(new GunTooltip.Data(GunStats.forStack(stack), original));
    }
}
