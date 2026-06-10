package bm.b0b0b0.soulHolo.command.handler;

import bm.b0b0b0.soulHolo.command.DholoCommandContext;
import bm.b0b0b0.soulHolo.command.DholoSubcommand;

import java.util.Collection;
import java.util.List;

public final class DholoReloadSubcommand implements DholoSubcommand {

    @Override
    public Collection<String> aliases() {
        return List.of("reload");
    }

    @Override
    public boolean execute(DholoCommandContext context) {
        if (denyUnlessAdmin(context)) {
            return true;
        }
        context.plugin().reloadAll();
        context.messages().send(context.sender(), "reload-success");
        return true;
    }

    @Override
    public List<String> tabComplete(DholoCommandContext context, int completingArgIndex) {
        return List.of();
    }
}
