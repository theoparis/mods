package com.eiag;

import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TridentItem;
import net.minecraft.world.level.block.AbstractChestBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.FenceGateBlock;

/** Shared item classification for authoritative stats and held-item silhouettes. */
public enum GunType {
    PISTOL, RIFLE, SMG, SNIPER;

    public static GunType forStack(ItemStack stack) {
        // Legendary profiles retain the full-sized rifle body, including the star cannon.
        if (stack.is(Items.DIAMOND) || stack.is(Items.NETHER_STAR)) return RIFLE;
        Item item = stack.getItem();
        if (item instanceof BowItem || item instanceof CrossbowItem || item instanceof TridentItem) return SNIPER;
        if (stack.is(ItemTags.WOODEN_FENCES) || stack.is(ItemTags.FENCE_GATES)) return SMG;
        if (item instanceof BlockItem blockItem) {
            Block block = blockItem.getBlock();
            if (block instanceof FenceBlock || block instanceof FenceGateBlock) return SMG;
            if (block instanceof AbstractChestBlock<?>) return RIFLE;
        }
        if (stack.is(ItemTags.LOGS)) return RIFLE;
        // Small materials (including sticks), dirt, and all other items use a pistol.
        return PISTOL;
    }
}
