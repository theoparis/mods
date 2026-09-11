package com.eiag.client;

import com.eiag.GunType;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.world.item.ItemDisplayContext;

/** Volumetric gun silhouettes: muzzle at negative Z, grip centered on the hand. */
public final class GunModel {
    public static final float CELL_SIZE = 0.065F;
    public static final float MINIATURE_SCALE = CELL_SIZE * 0.94F;

    // Top to bottom; each digit is the number of miniatures across that slice.
    // Small seams keep neighboring block faces individually readable.
    private static final float[] PISTOL_CENTERS = buildCenters(new String[] {
        "00000000000000",
        "01000000001000", // short slide with front/rear sights
        "33333333333330",
        "33333333333330",
        "00000033333330",
        "00000030033300", // trigger opening and grip
        "00000033333300",
        "00000000033300",
        "00000000033300",
        "00000000003330"
    }, 10);
    private static final float[] RIFLE_CENTERS = buildCenters(new String[] {
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
    }, 18);
    private static final float[] SMG_CENTERS = buildCenters(new String[] {
        "000000000000000000",
        "000001000000001000", // sights over a compact receiver
        "000033333333333330",
        "333355555555555550", // stub barrel and broad body
        "000055555555555550",
        "000033333333333330",
        "000000033300033300", // long magazine ahead of the grip
        "000000033300033300",
        "000000033300033300",
        "000000333000033300",
        "000000333000000000",
        "000000333000000000"
    }, 14);
    private static final float[] SNIPER_CENTERS = buildCenters(new String[] {
        "0000000000000000003333330000000000", // raised scope
        "0000000000000000003333330000000000",
        "0000000000000000000100100000000000", // scope mounts
        "1111111111111111111133333333300000", // long, thin barrel
        "1111111111111111113333333333333333",
        "0000000000003333333355555555333333",
        "0000000000000000000000330033333333",
        "0000000000000000000000330033003333",
        "0000000000000000000000330033000000",
        "0000000000000000000000000033000000",
        "0000000000000000000000000033000000"
    }, 26);

    private GunModel() {}

    public static boolean isHeld(ItemDisplayContext context) {
        return context.firstPerson() || context == ItemDisplayContext.THIRD_PERSON_LEFT_HAND
                || context == ItemDisplayContext.THIRD_PERSON_RIGHT_HAND;
    }

    public static int miniatureCount(GunType type) { return centers(type).length / 3; }

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

    public static void applyMiniatureTransform(PoseStack poses, GunType type, int index) {
        float[] centers = centers(type);
        int offset = index * 3;
        poses.translate(centers[offset], centers[offset + 1], centers[offset + 2]);
        poses.scale(MINIATURE_SCALE, MINIATURE_SCALE, MINIATURE_SCALE);
    }

    private static float[] centers(GunType type) {
        return switch (type) {
            case PISTOL -> PISTOL_CENTERS;
            case RIFLE -> RIFLE_CENTERS;
            case SMG -> SMG_CENTERS;
            case SNIPER -> SNIPER_CENTERS;
        };
    }

    private static float[] buildCenters(String[] profile, int gripColumn) {
        int count = 0;
        for (String row : profile) {
            for (int z = 0; z < row.length(); z++) count += row.charAt(z) - '0';
        }
        float[] centers = new float[count * 3];
        int index = 0;
        for (int row = 0; row < profile.length; row++) {
            for (int z = 0; z < profile[row].length(); z++) {
                int width = profile[row].charAt(z) - '0';
                for (int x = 0; x < width; x++) {
                    centers[index++] = (x - (width - 1) * 0.5F) * CELL_SIZE;
                    centers[index++] = (6 - row) * CELL_SIZE;
                    centers[index++] = (z - gripColumn) * CELL_SIZE;
                }
            }
        }
        return centers;
    }
}
