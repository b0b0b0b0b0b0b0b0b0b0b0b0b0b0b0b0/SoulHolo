package bm.b0b0b0.soulHolo.hologram;

import bm.b0b0b0.soulHolo.model.HologramDisplaySettings;
import bm.b0b0b0.soulHolo.model.PrivateHologram;
import org.bukkit.Color;
import org.bukkit.entity.TextDisplay;
import org.bukkit.util.Transformation;
import org.joml.Vector3f;

final class PaperDisplayApplier {

    private PaperDisplayApplier() {
    }

    static void apply(TextDisplay display, PrivateHologram hologram) {
        HologramDisplaySettings settings = hologram.displaySettings();
        display.setSeeThrough(settings.seeThrough());
        display.setShadowed(settings.textShadow());
        display.setBillboard(settings.billboard().bukkit());
        display.setAlignment(settings.textAlignment());
        display.setBackgroundColor(Color.fromARGB(
                settings.backgroundAlpha(),
                settings.backgroundRed(),
                settings.backgroundGreen(),
                settings.backgroundBlue()
        ));
        display.setShadowRadius(settings.shadowRadius());
        display.setShadowStrength(settings.shadowStrength());
        float scale = settings.scale();
        Transformation current = display.getTransformation();
        display.setTransformation(new Transformation(
                current.getTranslation(),
                current.getLeftRotation(),
                new Vector3f(scale, scale, scale),
                current.getRightRotation()
        ));
    }
}
