package bm.b0b0b0.soulHolo.command;

import bm.b0b0b0.soulHolo.SoulHolo;
import bm.b0b0b0.soulHolo.command.handler.DholoAddSubcommand;
import bm.b0b0b0.soulHolo.command.handler.DholoAdminSubcommand;
import bm.b0b0b0.soulHolo.command.handler.DholoCreateSubcommand;
import bm.b0b0b0.soulHolo.command.handler.DholoEditSubcommand;
import bm.b0b0b0.soulHolo.command.handler.DholoGuiSubcommand;
import bm.b0b0b0.soulHolo.command.handler.DholoLineMoveSubcommand;
import bm.b0b0b0.soulHolo.command.handler.DholoMoveSubcommand;
import bm.b0b0b0.soulHolo.command.handler.DholoReloadSubcommand;
import bm.b0b0b0.soulHolo.command.handler.DholoRemoveSubcommand;
import bm.b0b0b0.soulHolo.command.handler.DholoSettingSubcommand;
import bm.b0b0b0.soulHolo.config.PluginConfigHolder;
import bm.b0b0b0.soulHolo.message.MessageService;
import bm.b0b0b0.soulHolo.service.HologramService;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

public final class DholoCommandRegistry {

    private final Map<String, DholoSubcommand> rootByAlias;
    private final List<DholoSubcommand> rootOrdered;
    private final DholoCommandContext template;

    private DholoCommandRegistry(Map<String, DholoSubcommand> rootByAlias,
                                 List<DholoSubcommand> rootOrdered,
                                 DholoCommandContext template) {
        this.rootByAlias = rootByAlias;
        this.rootOrdered = rootOrdered;
        this.template = template;
    }

    public static DholoCommandRegistry create(PluginConfigHolder configHolder,
                                              SoulHolo plugin,
                                              MessageService messages,
                                              HologramService hologramService,
                                              DholoPlayerActions actions) {
        HologramFailureMessages failures = new HologramFailureMessages(messages, hologramService);
        DholoCommandContext template = new DholoCommandContext(
                null,
                null,
                null,
                new String[0],
                configHolder,
                messages,
                hologramService,
                actions,
                failures,
                plugin
        );
        DholoReloadSubcommand reload = new DholoReloadSubcommand();
        Map<String, DholoSubcommand> adminChildren = new HashMap<>();
        adminChildren.put("create", new DholoCreateSubcommand(true));
        adminChildren.put("add", new DholoAddSubcommand(true));
        adminChildren.put("remove", new DholoRemoveSubcommand(true));
        adminChildren.put("edit", new DholoEditSubcommand(true));
        adminChildren.put("move", new DholoMoveSubcommand(true));
        adminChildren.put("line", new DholoLineMoveSubcommand(true));
        DholoSettingSubcommand adminSetting = new DholoSettingSubcommand(true);
        adminChildren.put("setting", adminSetting);
        adminChildren.put("set", adminSetting);
        DholoAdminSubcommand admin = new DholoAdminSubcommand(Map.copyOf(adminChildren), reload);
        List<DholoSubcommand> ordered = List.of(
                new DholoCreateSubcommand(false),
                new DholoAddSubcommand(false),
                new DholoRemoveSubcommand(false),
                new DholoEditSubcommand(false),
                new DholoMoveSubcommand(false),
                new DholoLineMoveSubcommand(false),
                new DholoSettingSubcommand(false),
                new DholoGuiSubcommand(),
                admin,
                reload
        );
        Map<String, DholoSubcommand> root = new HashMap<>();
        for (DholoSubcommand subcommand : ordered) {
            for (String alias : subcommand.aliases()) {
                root.put(alias.toLowerCase(Locale.ROOT), subcommand);
            }
        }
        return new DholoCommandRegistry(Map.copyOf(root), ordered, template);
    }

    public Optional<DholoSubcommand> find(String raw) {
        if (raw == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(rootByAlias.get(raw.toLowerCase(Locale.ROOT)));
    }

    public DholoCommandContext context(CommandSender sender,
                                       org.bukkit.command.Command command,
                                       String label,
                                       String[] args) {
        return new DholoCommandContext(
                sender,
                command,
                label,
                args,
                template.config(),
                template.messages(),
                template.hologramService(),
                template.actions(),
                template.failures(),
                template.plugin()
        );
    }

    public List<String> tabComplete(org.bukkit.command.CommandSender sender, String[] args) {
        if (args.length == 0) {
            return List.of();
        }
        DholoCommandContext context = context(sender, null, "dholo", args);
        if (!context.hasAccess()) {
            return List.of();
        }
        if (args.length == 1) {
            List<String> suggestions = new ArrayList<>();
            for (DholoSubcommand subcommand : rootOrdered) {
                if (subcommand instanceof DholoAdminSubcommand || subcommand instanceof DholoReloadSubcommand) {
                    if (!context.hasAdminPermission()) {
                        continue;
                    }
                }
                for (String alias : subcommand.aliases()) {
                    if (!shouldSuggest(context, alias)) {
                        continue;
                    }
                    suggestions.add(alias);
                }
            }
            return filterPrefix(suggestions, args[0]);
        }
        Optional<DholoSubcommand> root = find(args[0]);
        if (root.isEmpty()) {
            return List.of();
        }
        return root.get().tabComplete(context, args.length - 1);
    }

    private static boolean shouldSuggest(DholoCommandContext context, String alias) {
        if (!"create".equalsIgnoreCase(alias)) {
            return true;
        }
        return context.player()
                .map(player -> context.hasAdminPermission() || context.hologramService().hasHologramSlot(player))
                .orElse(false);
    }

    private static List<String> filterPrefix(List<String> options, String prefix) {
        if (prefix == null || prefix.isEmpty()) {
            return options;
        }
        String lower = prefix.toLowerCase(Locale.ROOT);
        return options.stream()
                .filter(option -> option.toLowerCase(Locale.ROOT).startsWith(lower))
                .toList();
    }
}
