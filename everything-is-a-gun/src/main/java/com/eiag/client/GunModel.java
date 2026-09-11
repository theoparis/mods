package com.eiag.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.world.item.ItemDisplayContext;

/** A shared, volumetric rifle: muzzle at negative Z, stock at positive Z. */
public final class GunModel {
    public static final float CELL_SIZE = 0.065F;
    public static final float MINIATURE_SCALE = CELL_SIZE * 0.94F;

    // Top to bottom; each digit is the number of miniatures across that slice.
    // Small seams keep neighboring block faces individually readable.
    private static final String[] PROFILE = {
        "00000000000000000000000000",
        "00100000000000001000000000", // front/rear sights
        "00100000000000001000000000",
        "33333333333333333333000000", // barrel and upper receiver
        "33333333335555555555333333",
        "00000033335555555555333333", // fore-end, receiver, stock
        "00000000000033300330333333",
        "00000000000033300330003333", // magazine, trigger opening, grip
        "00000000000033300330000000",
        "00000000000333000330000000",
        "00000000000333000330000000"
    };
    private static final float[] CENTERS = buildCenters();

    private GunModel() {}

    public static boolean isHeld(ItemDisplayContext context) {
        return context.firstPerson() || context == ItemDisplayContext.THIRD_PERSON_LEFT_HAND
                || context == ItemDisplayContext.THIRD_PERSON_RIGHT_HAND;
    }

    public static int miniatureCount() { return CENTERS.length / 3; }

    public static void applyHandTransform(PoseStack poses, ItemDisplayContext context) {
        if (context.firstPerson()) {
            poses.translate(context.leftHand() ? -0.04F : 0.04F, -0.02F, -0.12F);
        } else {
            poses.scale(0.7F, 0.7F, 0.7F);
            // Living-entity hand poses rotate the local frame down by 90 degrees.
            poses.rotateDegrees(com.mojang.math.Axis.XP, 90.0F);
            poses.translate(0.0F, 0.08F, -0.12F);
        }
    }

    public static void applyMiniatureTransform(PoseStack poses, int index) {
        int offset = index * 3;
        poses.translate(CENTERS[offset], CENTERS[offset + 1], CENTERS[offset + 2]);
        poses.scale(MINIATURE_SCALE, MINIATURE_SCALE, MINIATURE_SCALE);
    }

    private static float[] buildCenters() {
        int count = 0;
        for (String row : PROFILE) {
            for (int z = 0; z < row.length(); z++) count += row.charAt(z) - '0';
        }
        float[] centers = new float[count * 3];
        int index = 0;
        for (int row = 0; row < PROFILE.length; row++) {
            for (int z = 0; z < PROFILE[row].length(); z++) {
                int width = PROFILE[row].charAt(z) - '0';
                for (int x = 0; x < width; x++) {
                    centers[index++] = (x - (width - 1) * 0.5F) * CELL_SIZE;
                    centers[index++] = (6 - row) * CELL_SIZE;
                    centers[index++] = (z - 18) * CELL_SIZE;
                }
            }
        }
        return centers;
    }
}
