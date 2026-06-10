package bm.b0b0b0.soulHolo.model;

import org.bukkit.entity.Display;
import org.bukkit.entity.TextDisplay;

import java.util.Locale;

public final class HologramDisplaySettings {

    private boolean enabled;
    private boolean seeThrough;
    private boolean textShadow;
    private DisplayBillboard billboard;
    private String backgroundPreset;
    private int backgroundRed;
    private int backgroundGreen;
    private int backgroundBlue;
    private int backgroundAlpha;
    private float scale;
    private TextDisplay.TextAlignment textAlignment;
    private float shadowRadius;
    private float shadowStrength;

    public HologramDisplaySettings() {
        this.enabled = true;
        this.seeThrough = false;
        this.textShadow = false;
        this.billboard = DisplayBillboard.CENTER;
        this.backgroundPreset = "transparent";
        this.backgroundRed = 0;
        this.backgroundGreen = 0;
        this.backgroundBlue = 0;
        this.backgroundAlpha = 0;
        this.scale = 1.0f;
        this.textAlignment = TextDisplay.TextAlignment.CENTER;
        this.shadowRadius = 0.0f;
        this.shadowStrength = 0.0f;
    }

    public HologramDisplaySettings copy() {
        HologramDisplaySettings copy = new HologramDisplaySettings();
        copy.enabled = enabled;
        copy.seeThrough = seeThrough;
        copy.textShadow = textShadow;
        copy.billboard = billboard;
        copy.backgroundPreset = backgroundPreset;
        copy.backgroundRed = backgroundRed;
        copy.backgroundGreen = backgroundGreen;
        copy.backgroundBlue = backgroundBlue;
        copy.backgroundAlpha = backgroundAlpha;
        copy.scale = scale;
        copy.textAlignment = textAlignment;
        copy.shadowRadius = shadowRadius;
        copy.shadowStrength = shadowStrength;
        return copy;
    }

    public boolean enabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean seeThrough() {
        return seeThrough;
    }

    public void setSeeThrough(boolean seeThrough) {
        this.seeThrough = seeThrough;
    }

    public boolean textShadow() {
        return textShadow;
    }

    public void setTextShadow(boolean textShadow) {
        this.textShadow = textShadow;
    }

    public DisplayBillboard billboard() {
        return billboard;
    }

    public void setBillboard(DisplayBillboard billboard) {
        this.billboard = billboard == null ? DisplayBillboard.CENTER : billboard;
    }

    public String backgroundPreset() {
        return backgroundPreset;
    }

    public void setBackgroundPreset(String backgroundPreset) {
        this.backgroundPreset = backgroundPreset == null ? "transparent" : backgroundPreset;
    }

    public int backgroundRed() {
        return backgroundRed;
    }

    public int backgroundGreen() {
        return backgroundGreen;
    }

    public int backgroundBlue() {
        return backgroundBlue;
    }

    public int backgroundAlpha() {
        return backgroundAlpha;
    }

    public void setBackgroundColor(int red, int green, int blue, int alpha) {
        this.backgroundRed = red;
        this.backgroundGreen = green;
        this.backgroundBlue = blue;
        this.backgroundAlpha = alpha;
    }

    public float scale() {
        return scale;
    }

    public void setScale(float scale) {
        this.scale = scale;
    }

    public TextDisplay.TextAlignment textAlignment() {
        return textAlignment;
    }

    public void setTextAlignment(TextDisplay.TextAlignment textAlignment) {
        this.textAlignment = textAlignment == null ? TextDisplay.TextAlignment.CENTER : textAlignment;
    }

    public float shadowRadius() {
        return shadowRadius;
    }

    public void setShadowRadius(float shadowRadius) {
        this.shadowRadius = shadowRadius;
    }

    public float shadowStrength() {
        return shadowStrength;
    }

    public void setShadowStrength(float shadowStrength) {
        this.shadowStrength = shadowStrength;
    }

    public enum DisplayBillboard {
        FIXED(Display.Billboard.FIXED),
        CENTER(Display.Billboard.CENTER),
        HORIZONTAL(Display.Billboard.HORIZONTAL),
        VERTICAL(Display.Billboard.VERTICAL);

        private final Display.Billboard bukkit;

        DisplayBillboard(Display.Billboard bukkit) {
            this.bukkit = bukkit;
        }

        public Display.Billboard bukkit() {
            return bukkit;
        }

        public DisplayBillboard next() {
            DisplayBillboard[] values = values();
            return values[(ordinal() + 1) % values.length];
        }

        public static DisplayBillboard fromConfig(String raw) {
            if (raw == null) {
                return CENTER;
            }
            try {
                return valueOf(raw.trim().toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException exception) {
                return CENTER;
            }
        }
    }
}
