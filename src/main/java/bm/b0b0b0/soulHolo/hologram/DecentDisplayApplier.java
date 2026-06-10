package bm.b0b0b0.soulHolo.hologram;

import bm.b0b0b0.soulHolo.model.HologramDisplaySettings;
import bm.b0b0b0.soulHolo.model.PrivateHologram;
import eu.decentsoftware.holograms.api.holograms.DisableCause;
import eu.decentsoftware.holograms.api.holograms.Hologram;

final class DecentDisplayApplier {

    private DecentDisplayApplier() {
    }

    static void apply(Hologram hologram, PrivateHologram model) {
        HologramDisplaySettings settings = model.displaySettings();
        if (!settings.enabled()) {
            hologram.disable(DisableCause.API);
            return;
        }
        hologram.enable();
        boolean facePlayer = settings.billboard() != HologramDisplaySettings.DisplayBillboard.FIXED;
        hologram.setAlwaysFacePlayer(facePlayer);
    }
}
