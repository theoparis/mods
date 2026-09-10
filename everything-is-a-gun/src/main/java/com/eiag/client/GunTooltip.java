package com.eiag.client;

import com.eiag.GunStats;
import java.util.Locale;
import java.util.Optional;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.ClientTooltipComponentCallback;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.world.inventory.tooltip.TooltipComponent;

/** Vanilla positions/clamps the tooltip; all drawing goes through the GUI extractor. */
public final class GunTooltip implements ClientModInitializer {
    public record Data(GunStats stats, Optional<TooltipComponent> original) implements TooltipComponent {}

    @Override public void onInitializeClient() {
        ClientTooltipComponentCallback.EVENT.register(data -> data instanceof Data gun ? new Panel(gun) : null);
    }

    private static final class Panel implements ClientTooltipComponent {
        private static final int WIDTH = 224, HEIGHT = 115;
        private final GunStats stats;
        private final ClientTooltipComponent original;
        Panel(Data data) {
            stats = data.stats();
            original = data.original().map(ClientTooltipComponent::create).orElse(null);
        }
        public int getWidth(Font font) { return Math.max(WIDTH, original == null ? 0 : original.getWidth(font)); }
        public int getHeight(Font font) { return HEIGHT + (original == null ? 0 : original.getHeight(font)); }
        public void extractText(GuiGraphicsExtractor g, Font font, int x, int y) {
            if (original != null) original.extractText(g, font, x, y + HEIGHT);
        }
        public void extractImage(Font font, int x, int y, int w, int h, GuiGraphicsExtractor g) {
            g.text(font, "GUN / " + stats.weaponClass(), x + 3, y + 4, 0xffffd477);
            g.fill(x, y + 18, x + WIDTH, y + 19, 0xffa08349);
            row(g, font, x, y + 24, "Damage", format(stats.damage()), stats.damage() / 15.0);
            row(g, font, x, y + 36, "Rate", format(stats.ratePerSecond()) + "/s", stats.ratePerSecond() / 10.0);
            row(g, font, x, y + 48, "Accuracy", format(stats.accuracyDegrees()) + "°", 1.0 - stats.accuracyDegrees() / 5.0);
            row(g, font, x, y + 60, "Control", Integer.toString(stats.control()), stats.control() / 20.0);
            row(g, font, x, y + 72, "Range", format(stats.range()) + "m", stats.range() / 100.0);
            row(g, font, x, y + 84, "Mag size", Integer.toString(stats.magazineSize()), stats.magazineSize() / 50.0);
            row(g, font, x, y + 96, "Reload", format(stats.reloadSeconds()) + "s", 1.0 - stats.reloadSeconds() / 5.0);
            if (original != null) original.extractImage(font, x, y + HEIGHT, w, h - HEIGHT, g);
        }
        private static String format(double value) {
            return String.format(Locale.ROOT, "%.2f", value).replaceAll("0+$", "").replaceAll("\\.$", "");
        }
        private static void row(GuiGraphicsExtractor g, Font font, int x, int y, String label, String value, double fraction) {
            g.text(font, label, x + 3, y, 0xffdddddd);
            int left = x + 66, right = x + 160;
            g.fill(left, y, right, y + 9, 0xff777777);
            g.fill(left + 1, y + 1, right - 1, y + 8, 0xff282828);
            if (fraction >= 0) {
                double amount = Math.clamp(fraction, 0, 1);
                int color = amount < .25 ? 0xffdc8054 : amount < .6 ? 0xffd6bf62 : 0xff8bd65c;
                g.fillGradient(left + 1, y + 1, left + 1 + (int)(92 * amount), y + 8, 0xffe6e6c0, color);
            }
            for (int i = 1; i < 4; i++) g.fill(left + i * 23, y + 1, left + i * 23 + 1, y + 8, 0x55777777);
            g.text(font, value, x + WIDTH - 3 - font.width(value), y, fraction < 0 ? 0xff888888 : 0xffffffff);
        }
    }
}
