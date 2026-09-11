package com.eiag.client;

import com.eiag.GunType;
import net.minecraft.world.item.ItemDisplayContext;

/** Marks the resolved native item model for repetition only while held. */
public interface GunRenderState {
    void eiag$setGunContext(ItemDisplayContext context, GunType type);
}
