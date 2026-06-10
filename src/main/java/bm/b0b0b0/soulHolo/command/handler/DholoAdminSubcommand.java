package bm.b0b0b0.soulHolo.command.handler;

import bm.b0b0b0.soulHolo.command.DholoCommandContext;
import bm.b0b0b0.soulHolo.command.DholoSubcommand;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class DholoAdminSubcommand implements DholoSubcommand {

    private static final List<String> ADMIN_KEYS = List.of(
            "create", "add", "remove", "edit", "move", "line", "setting", "reload"
    );

    private final Map<String, DholoSubcommand> children;
    private final DholoReloadSubcommand reloadSubcommand;

    public DholoAdminSubcommand(Map<String, DholoSubcommand> children, DholoReloadSubcommand reloadSubcommand) {
        this.children = children;
        this.reloadSubcommand = reloadSubcommand;
    }

    @Override
    public Collection<String> aliases() {
        return List.of("admin");
    }

    @Override
    public boolean execute(DholoCommandContext context) {
        if (denyUnlessAdmin(context)) {
            return true;
        }
        if (context.args().length < 2) {
            context.messages().send(context.sender(), "unknown-subcommand");
            return true;
        }
        String childKey = context.arg(1).toLowerCase(Locale.ROOT);
        if ("reload".equals(childKey)) {
            return reloadSubcommand.execute(context);
        }
        if (denyUnlessPlayer(context)) {
            return true;
        }
        DholoSubcommand child = children.get(childKey);
        if (child == null) {
            context.messages().send(context.sender(), "unknown-subcommand");
            return true;
        }
        return child.execute(context);
    }

    @Override
    public List<String> tabComplete(DholoCommandContext context, int completingArgIndex) {
        if (!context.hasAdminPermission()) {
            return List.of();
        }
        if (completingArgIndex == 1) {
            return filterPrefix(ADMIN_KEYS, context.arg(completingArgIndex));
        }
        String childKey = context.arg(1);
        if (childKey == null) {
            return List.of();
        }
        if ("reload".equalsIgnoreCase(childKey)) {
            return List.of();
        }
        DholoSubcommand child = children.get(childKey.toLowerCase(Locale.ROOT));
        if (child == null) {
            return List.of();
        }
        return child.tabComplete(context, completingArgIndex);
    }
}
