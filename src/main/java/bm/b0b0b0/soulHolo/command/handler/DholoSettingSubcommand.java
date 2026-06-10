package bm.b0b0b0.soulHolo.command.handler;

import bm.b0b0b0.soulHolo.command.DholoCommandContext;
import bm.b0b0b0.soulHolo.service.HologramFailure;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.List;

public final class DholoSettingSubcommand extends AbstractPlayerSubcommand {

    private static final List<String> SETTING_KEYS = List.of(
            "enabled", "see-through", "text-shadow", "billboard",
            "background", "scale", "alignment", "shadow"
    );

    public DholoSettingSubcommand(boolean admin) {
        super(admin);
    }

    @Override
    public Collection<String> aliases() {
        return List.of("setting", "set");
    }

    @Override
    protected boolean run(DholoCommandContext context, Player player) {
        String holoName;
        String key;
        String scaleDirection = null;
        if (admin) {
            if (context.args().length < 4) {
                context.messages().send(context.sender(), "usage-admin-setting");
                return true;
            }
            holoName = context.arg(2);
            key = context.arg(3);
            if ("scale".equalsIgnoreCase(key) && context.args().length > 4) {
                scaleDirection = context.arg(4);
            }
        } else {
            if (context.args().length < 2) {
                context.messages().send(player, "usage-setting");
                return true;
            }
            holoName = null;
            key = context.arg(1);
            if ("scale".equalsIgnoreCase(key) && context.args().length > 2) {
                scaleDirection = context.arg(2);
            }
        }
        HologramFailure failure = context.actions().applySettingFailure(player, key, scaleDirection, holoName, admin);
        if (failure == HologramFailure.NONE || failure == HologramFailure.SETTING_DENIED) {
            return true;
        }
        context.failures().send(context.sender(), failure, holoName, 0);
        return true;
    }

    @Override
    protected List<String> tabCompletePlayer(DholoCommandContext context, int completingArgIndex) {
        if (admin) {
            if (completingArgIndex == 2) {
                return filterPrefix(allHologramNames(context), context.arg(completingArgIndex));
            }
            if (completingArgIndex == 3) {
                return filterPrefix(SETTING_KEYS, context.arg(completingArgIndex));
            }
            if (completingArgIndex == 4 && "scale".equalsIgnoreCase(context.arg(3))) {
                return filterPrefix(List.of("+", "-"), context.arg(completingArgIndex));
            }
            return List.of();
        }
        if (completingArgIndex == 1) {
            return filterPrefix(SETTING_KEYS, context.arg(completingArgIndex));
        }
        if (completingArgIndex == 2 && "scale".equalsIgnoreCase(context.arg(1))) {
            return filterPrefix(List.of("+", "-"), context.arg(completingArgIndex));
        }
        return List.of();
    }
}
