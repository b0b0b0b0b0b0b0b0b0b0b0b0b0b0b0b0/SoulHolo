package bm.b0b0b0.soulHolo.message;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

public final class HologramLineParser {

    private static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.builder()
            .character(LegacyComponentSerializer.AMPERSAND_CHAR)
            .hexColors()
            .build();

    private static final MiniMessage MINI = MiniMessage.miniMessage();

    private HologramLineParser() {
    }

    public static Component parse(String raw) {
        if (raw == null || raw.isEmpty()) {
            return Component.empty();
        }
        if (looksLikeMiniMessage(raw)) {
            return MINI.deserialize(raw);
        }
        return LEGACY.deserialize(raw);
    }

    private static boolean looksLikeMiniMessage(String raw) {
        int open = raw.indexOf('<');
        if (open < 0) {
            return false;
        }
        int close = raw.indexOf('>', open);
        return close > open;
    }
}
