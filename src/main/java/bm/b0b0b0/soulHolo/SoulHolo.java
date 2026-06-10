package bm.b0b0b0.soulHolo;

import bm.b0b0b0.soulHolo.command.DholoCommand;
import bm.b0b0b0.soulHolo.command.DholoCommandRegistry;
import bm.b0b0b0.soulHolo.command.DholoPlayerActions;
import bm.b0b0b0.soulHolo.config.ConfigurationLoader;
import bm.b0b0b0.soulHolo.config.PluginConfig;
import bm.b0b0b0.soulHolo.config.PluginConfigHolder;
import bm.b0b0b0.soulHolo.core.PluginExecutor;
import bm.b0b0b0.soulHolo.core.ServiceReferences;
import bm.b0b0b0.soulHolo.gui.item.GuiItemFactory;
import bm.b0b0b0.soulHolo.hologram.HologramBackend;
import bm.b0b0b0.soulHolo.hologram.HologramBackendFactory;
import bm.b0b0b0.soulHolo.integration.PlaceholderBridge;
import bm.b0b0b0.soulHolo.integration.RegionGuard;
import bm.b0b0b0.soulHolo.integration.WorldGuardRegionGuard;
import bm.b0b0b0.soulHolo.listener.GuiListener;
import bm.b0b0b0.soulHolo.listener.LineEditChatListener;
import bm.b0b0b0.soulHolo.message.MessageService;
import bm.b0b0b0.soulHolo.repository.HologramRepository;
import bm.b0b0b0.soulHolo.repository.YamlHologramRepository;
import bm.b0b0b0.soulHolo.service.ActionLogService;
import bm.b0b0b0.soulHolo.service.BlacklistService;
import bm.b0b0b0.soulHolo.service.DisplaySettingAccess;
import bm.b0b0b0.soulHolo.service.GuiNavigationService;
import bm.b0b0b0.soulHolo.service.HologramDisplayService;
import bm.b0b0b0.soulHolo.service.HologramLineGuiService;
import bm.b0b0b0.soulHolo.service.HologramPositionGuiService;
import bm.b0b0b0.soulHolo.service.HologramService;
import bm.b0b0b0.soulHolo.service.LimitService;
import bm.b0b0b0.soulHolo.session.LineEditSessionService;
import bm.b0b0b0.soulHolo.session.PlayerSessionService;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Level;

public final class SoulHolo extends JavaPlugin implements PluginConfigHolder {

    private PluginConfig pluginConfig;
    private ConfigurationLoader configurationLoader;
    private MessageService messageService;
    private HologramRepository hologramRepository;
    private HologramBackend hologramBackend;
    private HologramService hologramService;
    private GuiNavigationService guiNavigationService;
    private HologramDisplayService hologramDisplayService;
    private GuiItemFactory guiItemFactory;
    private DisplaySettingAccess displaySettingAccess;
    private ActionLogService actionLogService;
    private ServiceReferences serviceReferences;
    private PluginExecutor pluginExecutor;
    private LimitService limitService;
    private BlacklistService blacklistService;
    private DholoPlayerActions dholoPlayerActions;
    private DholoCommandRegistry dholoCommandRegistry;
    private DholoCommand dholoCommand;

    @Override
    public void onEnable() {
        configurationLoader = new ConfigurationLoader(this);
        pluginConfig = configurationLoader.load();

        pluginExecutor = new PluginExecutor(this, pluginConfig.ioThreads());
        serviceReferences = new ServiceReferences();

        messageService = new MessageService(this);
        messageService.load();

        hologramBackend = HologramBackendFactory.resolve(pluginConfig);
        if (!hologramBackend.available()) {
            getLogger().severe("Configured hologram backend unavailable — set hologramBackend to auto or paper, or install DecentHolograms/FancyHolograms");
        }

        RegionGuard regionGuard = new WorldGuardRegionGuard();

        PlaceholderBridge placeholderBridge = new PlaceholderBridge();
        hologramRepository = new YamlHologramRepository(this, pluginExecutor);
        PlayerSessionService sessionService = new PlayerSessionService();
        limitService = new LimitService(pluginConfig);
        blacklistService = new BlacklistService(pluginConfig);
        actionLogService = new ActionLogService(
                this,
                pluginExecutor,
                pluginConfig.loggingEnabled(),
                pluginConfig.loggingFile()
        );

        hologramService = new HologramService(
                this,
                pluginConfig,
                hologramRepository,
                hologramBackend,
                regionGuard,
                placeholderBridge,
                limitService,
                blacklistService,
                sessionService,
                actionLogService
        );

        displaySettingAccess = new DisplaySettingAccess(pluginConfig, hologramBackend, messageService);
        hologramDisplayService = new HologramDisplayService(
                pluginConfig,
                hologramService,
                displaySettingAccess,
                messageService,
                actionLogService
        );
        guiItemFactory = new GuiItemFactory(messageService, pluginConfig.guiLayout());
        LineEditSessionService lineEditSessionService = new LineEditSessionService();
        HologramLineGuiService hologramLineGuiService = new HologramLineGuiService(
                hologramService,
                messageService,
                displaySettingAccess,
                lineEditSessionService
        );
        HologramPositionGuiService hologramPositionGuiService = new HologramPositionGuiService(
                hologramService,
                messageService,
                displaySettingAccess
        );
        guiNavigationService = new GuiNavigationService(
                messageService,
                guiItemFactory,
                pluginConfig.guiLayout(),
                displaySettingAccess,
                hologramService,
                hologramDisplayService,
                hologramLineGuiService,
                hologramPositionGuiService
        );
        serviceReferences.setGuiNavigation(guiNavigationService);
        dholoPlayerActions = new DholoPlayerActions(
                hologramService,
                hologramDisplayService,
                hologramLineGuiService,
                hologramPositionGuiService,
                displaySettingAccess,
                guiNavigationService
        );
        dholoCommandRegistry = DholoCommandRegistry.create(
                this,
                this,
                messageService,
                hologramService,
                dholoPlayerActions
        );

        getServer().getPluginManager().registerEvents(new GuiListener(), this);
        getServer().getPluginManager().registerEvents(new LineEditChatListener(
                this,
                hologramLineGuiService,
                guiNavigationService,
                lineEditSessionService,
                sessionService
        ), this);

        int batchSize = pluginConfig.restoreBatchSize();
        hologramRepository.loadAll().whenComplete((unused, error) -> {
            if (error != null) {
                getLogger().log(Level.SEVERE, "Failed to load holograms", error);
                return;
            }
            getServer().getScheduler().runTask(this, () -> {
                if (hologramBackend.available()) {
                    hologramService.restoreAll(batchSize);
                    getLogger().info("Loaded " + hologramRepository.all().size() + " holograms using " + hologramBackend.id());
                }
            });
        });

        registerCommands();
        getLogger().info("SoulHolo enabled — hologram backend: " + hologramBackend.id());
    }

    @Override
    public void onDisable() {
        if (hologramService != null) {
            hologramService.shutdown();
        }
        if (hologramRepository != null) {
            hologramRepository.awaitPendingSaves();
        }
        if (pluginExecutor != null) {
            pluginExecutor.shutdown();
        }
    }

    public void reloadAll() {
        pluginConfig = configurationLoader.load();
        messageService.reload();

        actionLogService.reload(pluginConfig.loggingEnabled(), pluginConfig.loggingFile());

        HologramBackend resolvedBackend = HologramBackendFactory.resolve(pluginConfig);
        if (!resolvedBackend.available()) {
            getLogger().warning("Configured hologram backend unavailable — use auto, paper, or install DecentHolograms/FancyHolograms");
        }

        hologramService.reloadConfig(pluginConfig, limitService, blacklistService, actionLogService);
        hologramService.switchBackendIfNeeded(resolvedBackend, pluginConfig.restoreBatchSize());
        hologramBackend = hologramService.backend();

        displaySettingAccess.reload(pluginConfig, hologramBackend);
        hologramDisplayService.reload(pluginConfig, actionLogService);
        guiItemFactory.reload(pluginConfig.guiLayout());
        guiNavigationService.reload(pluginConfig.guiLayout());
    }

    @Override
    public PluginConfig pluginConfig() {
        return pluginConfig;
    }

    @Override
    public void setPluginConfig(PluginConfig pluginConfig) {
        this.pluginConfig = pluginConfig;
    }

    private void registerCommands() {
        PluginCommand command = getCommand("dholo");
        if (command == null) {
            getLogger().severe("Command dholo missing from plugin.yml");
            return;
        }
        dholoCommand = new DholoCommand(dholoCommandRegistry);
        command.setExecutor(dholoCommand);
        command.setTabCompleter(dholoCommand);
    }
}
