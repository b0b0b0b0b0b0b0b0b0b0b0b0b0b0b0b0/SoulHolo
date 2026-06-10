package bm.b0b0b0.soulHolo.service;

import bm.b0b0b0.soulHolo.config.PluginConfig;

import java.util.Locale;
import java.util.regex.Pattern;

public final class BlacklistService {

    private PluginConfig config;

    public BlacklistService(PluginConfig config) {
        this.config = config;
    }

    public void reload(PluginConfig config) {
        this.config = config;
    }

    public boolean isBlocked(String text) {
        if (text == null || text.isBlank()) {
            return false;
        }
        String lower = text.toLowerCase(Locale.ROOT);
        for (String literal : config.blacklistLiterals()) {
            if (literal != null && !literal.isBlank() && lower.contains(literal.toLowerCase(Locale.ROOT))) {
                return true;
            }
        }
        for (Pattern pattern : config.blacklistRegex()) {
            if (pattern.matcher(text).find()) {
                return true;
            }
        }
        return false;
    }
}
