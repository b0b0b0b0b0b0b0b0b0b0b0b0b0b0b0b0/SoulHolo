package bm.b0b0b0.soulHolo.service;

import bm.b0b0b0.soulHolo.config.PluginConfig;
import org.bukkit.entity.Player;

public final class LimitService {

    private PluginConfig config;

    public LimitService(PluginConfig config) {
        this.config = config;
    }

    public void reload(PluginConfig config) {
        this.config = config;
    }

    public PluginConfig.LimitTier resolve(Player player) {
        PluginConfig.LimitTier best = config.defaultLimit();
        int bestScore = player.hasPermission(best.permission()) ? score(best) : -1;
        for (PluginConfig.LimitTier tier : config.limitTiers()) {
            if (!player.hasPermission(tier.permission())) {
                continue;
            }
            int tierScore = score(tier);
            if (tierScore > bestScore) {
                best = tier;
                bestScore = tierScore;
            }
        }
        return best;
    }

    public int nameMinLength() {
        return config.nameMinLength();
    }

    public int nameMaxLength() {
        return config.nameMaxLength();
    }

    public String namePatternLabel() {
        return config.namePattern().pattern();
    }

    private static int score(PluginConfig.LimitTier tier) {
        return tier.maxLines() * 100000 + tier.maxHologramsPerRegion() * 1000 + tier.maxLineLength();
    }

    public boolean isNameValid(String name) {
        if (name == null) {
            return false;
        }
        int length = name.length();
        if (length < config.nameMinLength() || length > config.nameMaxLength()) {
            return false;
        }
        return config.namePattern().matcher(name).matches();
    }

    public PluginConfig.LimitCountScope countScope() {
        return config.limitCountScope();
    }
}
