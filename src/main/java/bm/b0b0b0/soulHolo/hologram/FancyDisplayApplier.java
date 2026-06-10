package bm.b0b0b0.soulHolo.hologram;

import bm.b0b0b0.soulHolo.model.HologramDisplaySettings;
import bm.b0b0b0.soulHolo.model.PrivateHologram;
import de.oliver.fancyholograms.api.data.TextHologramData;
import org.bukkit.Color;
import org.joml.Vector3f;

final class FancyDisplayApplier {

    private FancyDisplayApplier() {
    }

    static void apply(TextHologramData data, PrivateHologram hologram) {
        HologramDisplaySettings settings = hologram.displaySettings();
        data.setSeeThrough(settings.seeThrough());
        data.setTextShadow(settings.textShadow());
        data.setBillboard(settings.billboard().bukkit());
        data.setTextAlignment(settings.textAlignment());
        data.setScale(new Vector3f(settings.scale(), settings.scale(), settings.scale()));
        data.setShadowRadius(settings.shadowRadius());
        data.setShadowStrength(settings.shadowStrength());
        data.setBackground(Color.fromARGB(
                settings.backgroundAlpha(),
                settings.backgroundRed(),
                settings.backgroundGreen(),
                settings.backgroundBlue()
        ));
        data.setPersistent(false);
    }
}
