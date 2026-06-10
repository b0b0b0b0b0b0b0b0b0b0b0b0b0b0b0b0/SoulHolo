package bm.b0b0b0.soulHolo.command.handler;

import bm.b0b0b0.soulHolo.command.DholoCommandContext;
import bm.b0b0b0.soulHolo.command.DholoSubcommand;

import java.util.Collection;
import java.util.List;

public abstract class AbstractPlayerSubcommand implements DholoSubcommand {

    protected final boolean admin;

    protected AbstractPlayerSubcommand(boolean admin) {
        this.admin = admin;
    }

    protected AbstractPlayerSubcommand() {
        this(false);
    }

    @Override
    public final boolean execute(DholoCommandContext context) {
        if (denyUnlessPlayer(context)) {
            return true;
        }
        if (requiresUsePermission() && denyUnlessUse(context)) {
            return true;
        }
        return run(context, context.player().orElseThrow());
    }

    protected boolean requiresUsePermission() {
        return !admin;
    }

    protected abstract boolean run(DholoCommandContext context, org.bukkit.entity.Player player);

    @Override
    public List<String> tabComplete(DholoCommandContext context, int completingArgIndex) {
        return tabCompletePlayer(context, completingArgIndex);
    }

    protected List<String> tabCompletePlayer(DholoCommandContext context, int completingArgIndex) {
        return List.of();
    }
}
