package bm.b0b0b0.soulHolo.service;

import bm.b0b0b0.soulHolo.config.PluginConfig;
import bm.b0b0b0.soulHolo.hologram.HologramBackend;
import bm.b0b0b0.soulHolo.integration.PlaceholderBridge;
import bm.b0b0b0.soulHolo.integration.RegionGuard;
import bm.b0b0b0.soulHolo.model.HologramDisplaySettings;
import bm.b0b0b0.soulHolo.model.PrivateHologram;
import bm.b0b0b0.soulHolo.model.RelativeMoveDirection;
import bm.b0b0b0.soulHolo.repository.HologramRepository;
import bm.b0b0b0.soulHolo.repository.PlayerContext;
import bm.b0b0b0.soulHolo.session.PlayerSessionService;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

public final class HologramService {

    private final JavaPlugin plugin;
    private PluginConfig config;
    private final HologramRepository repository;
    private HologramBackend backend;
    private final RegionGuard regionGuard;
    private final PlaceholderBridge placeholders;
    private final LimitService limitService;
    private final BlacklistService blacklistService;
    private final PlayerSessionService sessionService;
    private ActionLogService actionLogService;

    public HologramService(JavaPlugin plugin,
                           PluginConfig config,
                           HologramRepository repository,
                           HologramBackend backend,
                           RegionGuard regionGuard,
                           PlaceholderBridge placeholders,
                           LimitService limitService,
                           BlacklistService blacklistService,
                           PlayerSessionService sessionService,
                           ActionLogService actionLogService) {
        this.plugin = plugin;
        this.config = config;
        this.repository = repository;
        this.backend = backend;
        this.regionGuard = regionGuard;
        this.placeholders = placeholders;
        this.limitService = limitService;
        this.blacklistService = blacklistService;
        this.sessionService = sessionService;
        this.actionLogService = actionLogService;
    }

    public void reloadConfig(PluginConfig config,
                             LimitService limitService,
                             BlacklistService blacklistService,
                             ActionLogService actionLogService) {
        this.config = config;
        this.actionLogService = actionLogService;
        limitService.reload(config);
        blacklistService.reload(config);
    }

    public void switchBackendIfNeeded(HologramBackend backend, int batchSize) {
        if (this.backend.id().equals(backend.id())) {
            return;
        }
        for (PrivateHologram hologram : repository.all()) {
            this.backend.remove(hologram);
        }
        this.backend = backend;
        restoreAll(batchSize);
    }

    public void restoreAll(int batchSize) {
        List<PrivateHologram> holograms = new ArrayList<>(repository.all());
        restoreBatch(holograms, 0, batchSize);
    }

    private void restoreBatch(List<PrivateHologram> holograms, int from, int batchSize) {
        int to = Math.min(from + batchSize, holograms.size());
        for (int index = from; index < to; index++) {
            PrivateHologram hologram = holograms.get(index);
            Player owner = Bukkit.getPlayer(hologram.ownerId());
            backend.spawn(hologram, renderLines(hologram, owner));
        }
        if (to < holograms.size()) {
            Bukkit.getScheduler().runTaskLater(plugin, () -> restoreBatch(holograms, to, batchSize), 1L);
        }
    }

    public HologramFailure create(Player actor, String rawName, Player owner, boolean admin) {
        if (!backend.available()) {
            return HologramFailure.BACKEND_MISSING;
        }
        if (!regionGuard.available() && !admin) {
            return HologramFailure.WORLDGUARD_MISSING;
        }
        String name = rawName.toLowerCase(Locale.ROOT);
        if (!limitService.isNameValid(name)) {
            return HologramFailure.INVALID_NAME;
        }
        if (repository.findByName(name).isPresent()) {
            return HologramFailure.NAME_TAKEN;
        }
        Location location = owner.getLocation().clone();
        Optional<String> region;
        if (admin) {
            if (regionGuard.available()) {
                region = regionGuard.ownerRegionAt(owner, location);
                if (region.isEmpty()) {
                    region = regionGuard.anyRegionAt(location);
                }
            } else {
                region = Optional.empty();
            }
            if (region.isEmpty()) {
                region = Optional.of(config.adminFallbackRegionId());
            }
        } else {
            region = regionGuard.ownerRegionAt(owner, location);
            if (region.isEmpty()) {
                return HologramFailure.NOT_OWNER_REGION;
            }
        }
        if (!admin) {
            PluginConfig.LimitTier tier = limitService.resolve(owner);
            int count = repository.countForLimit(region.get(), owner.getUniqueId(), limitService.countScope());
            if (count >= tier.maxHologramsPerRegion()) {
                return HologramFailure.REGION_LIMIT;
            }
        }
        UUID id = UUID.randomUUID();
        PrivateHologram hologram = new PrivateHologram(
                id,
                name,
                owner.getUniqueId(),
                owner.getName(),
                region.get(),
                location,
                new ArrayList<>(),
                null,
                new HologramDisplaySettings()
        );
        List<String> rendered = renderLines(hologram, owner);
        backend.spawn(hologram, rendered);
        repository.save(hologram);
        sessionService.setActive(actor.getUniqueId(), hologram.id());
        actionLogService.log("CREATE", owner.getName() + " name=" + name + " region=" + region.get());
        return HologramFailure.NONE;
    }

    public HologramFailure addLine(Player actor, String text, boolean admin, String hologramName) {
        Optional<PrivateHologram> optional = resolveHologram(actor, admin, hologramName);
        if (optional.isEmpty()) {
            return resolveMissing(actor, admin, hologramName);
        }
        PrivateHologram hologram = optional.get();
        if (!admin && !hologram.ownerId().equals(actor.getUniqueId())) {
            return HologramFailure.NOT_OWNED;
        }
        Player owner = Bukkit.getPlayer(hologram.ownerId());
        if (!admin) {
            PluginConfig.LimitTier tier = limitService.resolve(actor);
            if (hologram.lines().size() >= tier.maxLines()) {
                return HologramFailure.LINE_LIMIT;
            }
            HologramFailure textFailure = validateText(actor, text, tier);
            if (textFailure != HologramFailure.NONE) {
                return textFailure;
            }
        } else if (blacklistService.isBlocked(text)) {
            return HologramFailure.BLACKLISTED;
        }
        hologram.lines().add(text == null ? "" : text);
        sync(hologram, owner == null ? actor : owner);
        repository.save(hologram);
        actionLogService.log("ADD", actor.getName() + " holo=" + hologram.name() + " line=" + hologram.lines().size());
        return HologramFailure.NONE;
    }

    public HologramFailure removeLine(Player actor, int lineNumber, boolean admin, String hologramName) {
        Optional<PrivateHologram> optional = resolveHologram(actor, admin, hologramName);
        if (optional.isEmpty()) {
            return resolveMissing(actor, admin, hologramName);
        }
        PrivateHologram hologram = optional.get();
        if (!admin && !hologram.ownerId().equals(actor.getUniqueId())) {
            return HologramFailure.NOT_OWNED;
        }
        if (!isValidUserLine(hologram, lineNumber)) {
            return HologramFailure.INVALID_LINE;
        }
        hologram.lines().remove(lineNumber - 1);
        Player owner = Bukkit.getPlayer(hologram.ownerId());
        sync(hologram, owner == null ? actor : owner);
        repository.save(hologram);
        actionLogService.log("REMOVE", actor.getName() + " holo=" + hologram.name() + " line=" + lineNumber);
        return HologramFailure.NONE;
    }

    public HologramFailure editLine(Player actor, int lineNumber, String text, boolean admin, String hologramName) {
        Optional<PrivateHologram> optional = resolveHologram(actor, admin, hologramName);
        if (optional.isEmpty()) {
            return resolveMissing(actor, admin, hologramName);
        }
        PrivateHologram hologram = optional.get();
        if (!admin && !hologram.ownerId().equals(actor.getUniqueId())) {
            return HologramFailure.NOT_OWNED;
        }
        if (!isValidUserLine(hologram, lineNumber)) {
            return HologramFailure.INVALID_LINE;
        }
        if (!admin) {
            PluginConfig.LimitTier tier = limitService.resolve(actor);
            HologramFailure textFailure = validateText(actor, text, tier);
            if (textFailure != HologramFailure.NONE) {
                return textFailure;
            }
        } else if (blacklistService.isBlocked(text)) {
            return HologramFailure.BLACKLISTED;
        }
        hologram.lines().set(lineNumber - 1, text == null ? "" : text);
        Player owner = Bukkit.getPlayer(hologram.ownerId());
        sync(hologram, owner == null ? actor : owner);
        repository.save(hologram);
        actionLogService.log("EDIT", actor.getName() + " holo=" + hologram.name() + " line=" + lineNumber);
        return HologramFailure.NONE;
    }

    public HologramFailure addLine(Player actor, PrivateHologram hologram, String text, boolean admin) {
        if (!admin && !hologram.ownerId().equals(actor.getUniqueId())) {
            return HologramFailure.NOT_OWNED;
        }
        Player owner = Bukkit.getPlayer(hologram.ownerId());
        if (!admin) {
            PluginConfig.LimitTier tier = limitService.resolve(actor);
            if (hologram.lines().size() >= tier.maxLines()) {
                return HologramFailure.LINE_LIMIT;
            }
            HologramFailure textFailure = validateText(actor, text, tier);
            if (textFailure != HologramFailure.NONE) {
                return textFailure;
            }
        } else if (blacklistService.isBlocked(text)) {
            return HologramFailure.BLACKLISTED;
        }
        hologram.lines().add(text == null ? "" : text);
        sync(hologram, owner == null ? actor : owner);
        repository.save(hologram);
        actionLogService.log("ADD", actor.getName() + " holo=" + hologram.name() + " line=" + hologram.lines().size());
        return HologramFailure.NONE;
    }

    public HologramFailure removeLine(Player actor, PrivateHologram hologram, int lineNumber, boolean admin) {
        if (!admin && !hologram.ownerId().equals(actor.getUniqueId())) {
            return HologramFailure.NOT_OWNED;
        }
        if (!isValidUserLine(hologram, lineNumber)) {
            return HologramFailure.INVALID_LINE;
        }
        hologram.lines().remove(lineNumber - 1);
        Player owner = Bukkit.getPlayer(hologram.ownerId());
        sync(hologram, owner == null ? actor : owner);
        repository.save(hologram);
        actionLogService.log("REMOVE", actor.getName() + " holo=" + hologram.name() + " line=" + lineNumber);
        return HologramFailure.NONE;
    }

    public HologramFailure editLine(Player actor, PrivateHologram hologram, int lineNumber, String text, boolean admin) {
        if (!admin && !hologram.ownerId().equals(actor.getUniqueId())) {
            return HologramFailure.NOT_OWNED;
        }
        if (!isValidUserLine(hologram, lineNumber)) {
            return HologramFailure.INVALID_LINE;
        }
        if (!admin) {
            PluginConfig.LimitTier tier = limitService.resolve(actor);
            HologramFailure textFailure = validateText(actor, text, tier);
            if (textFailure != HologramFailure.NONE) {
                return textFailure;
            }
        } else if (blacklistService.isBlocked(text)) {
            return HologramFailure.BLACKLISTED;
        }
        hologram.lines().set(lineNumber - 1, text == null ? "" : text);
        Player owner = Bukkit.getPlayer(hologram.ownerId());
        sync(hologram, owner == null ? actor : owner);
        repository.save(hologram);
        actionLogService.log("EDIT", actor.getName() + " holo=" + hologram.name() + " line=" + lineNumber);
        return HologramFailure.NONE;
    }

    public HologramFailure moveLine(Player actor, PrivateHologram hologram, int lineNumber, int direction, boolean admin) {
        if (!admin && !hologram.ownerId().equals(actor.getUniqueId())) {
            return HologramFailure.NOT_OWNED;
        }
        if (!isValidUserLine(hologram, lineNumber)) {
            return HologramFailure.INVALID_LINE;
        }
        int targetLine = lineNumber + direction;
        if (!isValidUserLine(hologram, targetLine)) {
            return HologramFailure.INVALID_LINE;
        }
        int indexA = lineNumber - 1;
        int indexB = targetLine - 1;
        String value = hologram.lines().get(indexA);
        hologram.lines().set(indexA, hologram.lines().get(indexB));
        hologram.lines().set(indexB, value);
        Player owner = Bukkit.getPlayer(hologram.ownerId());
        sync(hologram, owner == null ? actor : owner);
        repository.save(hologram);
        actionLogService.log("MOVE", actor.getName() + " holo=" + hologram.name() + " line=" + lineNumber + " dir=" + direction);
        return HologramFailure.NONE;
    }

    public HologramFailure shiftPosition(Player actor,
                                         PrivateHologram hologram,
                                         float yaw,
                                         RelativeMoveDirection direction,
                                         boolean admin) {
        if (!admin && !hologram.ownerId().equals(actor.getUniqueId())) {
            return HologramFailure.NOT_OWNED;
        }
        Location current = hologram.location();
        if (current == null || current.getWorld() == null) {
            return HologramFailure.NOT_FOUND;
        }
        Location target = RelativeMoveCalculator.shifted(current, yaw, direction, config.positionStep());
        if (!isAllowedPosition(hologram, actor, target, admin)) {
            return HologramFailure.OUTSIDE_REGION;
        }
        hologram.setCoordinates(target.getX(), target.getY(), target.getZ());
        Player owner = Bukkit.getPlayer(hologram.ownerId());
        List<String> rendered = renderLines(hologram, owner == null ? actor : owner);
        backend.relocate(hologram, rendered);
        repository.save(hologram);
        actionLogService.log("MOVE_POS", actor.getName() + " holo=" + hologram.name() + " dir=" + direction.name());
        return HologramFailure.NONE;
    }

    public double positionStep() {
        return config.positionStep();
    }

    public void restoreAll() {
        restoreAll(config.restoreBatchSize());
    }

    public void shutdown() {
        for (PrivateHologram hologram : repository.all()) {
            backend.remove(hologram);
        }
    }

    public Optional<PrivateHologram> findByName(String name) {
        return repository.findByName(name);
    }

    public Optional<PrivateHologram> findById(UUID id) {
        return repository.findById(id);
    }

    public Optional<PrivateHologram> resolveForPlayer(Player player, String hologramName, boolean admin) {
        return resolveHologram(player, admin, hologramName);
    }

    public List<PrivateHologram> ownedHolograms(Player player) {
        return repository.findByOwner(player.getUniqueId());
    }

    public String backendId() {
        return backend.id();
    }

    public HologramBackend backend() {
        return backend;
    }

    public void resync(PrivateHologram hologram, Player viewer) {
        sync(hologram, viewer);
        repository.save(hologram);
    }

    public int maxUserLines(Player player) {
        return limitService.resolve(player).maxLines();
    }

    public PluginConfig.LimitTier tier(Player player) {
        return limitService.resolve(player);
    }

    public int nameMinLength() {
        return limitService.nameMinLength();
    }

    public int nameMaxLength() {
        return limitService.nameMaxLength();
    }

    public String namePatternLabel() {
        return limitService.namePatternLabel();
    }

    public List<String> hologramNames() {
        List<String> names = new ArrayList<>();
        for (PrivateHologram hologram : repository.all()) {
            names.add(hologram.name());
        }
        return names;
    }

    public int activeLineCount(Player player) {
        UUID activeId = sessionService.activeHologram(player.getUniqueId());
        if (activeId == null) {
            return 0;
        }
        return repository.findById(activeId).map(h -> h.lines().size()).orElse(0);
    }

    public String regionForFailure(Player player) {
        Optional<String> region = regionGuard.ownerRegionAt(player, player.getLocation());
        return region.orElse("-");
    }

    private Optional<PrivateHologram> resolveHologram(Player actor, boolean admin, String hologramName) {
        if (admin && hologramName != null) {
            return repository.findByName(hologramName);
        }
        UUID activeId = sessionService.activeHologram(actor.getUniqueId());
        if (activeId != null) {
            Optional<PrivateHologram> active = repository.findById(activeId);
            if (active.isPresent()) {
                return active;
            }
        }
        return repository.findNearestOwned(new PlayerContext(
                actor.getUniqueId(),
                actor.getLocation(),
                config.nearestRadius()
        ));
    }

    private HologramFailure resolveMissing(Player actor, boolean admin, String hologramName) {
        if (admin) {
            return HologramFailure.NOT_FOUND;
        }
        return HologramFailure.NO_ACTIVE;
    }

    private HologramFailure validateText(Player actor, String text, PluginConfig.LimitTier tier) {
        String value = text == null ? "" : text;
        if (value.length() > tier.maxLineLength()) {
            return HologramFailure.LINE_TOO_LONG;
        }
        if (blacklistService.isBlocked(value)) {
            return HologramFailure.BLACKLISTED;
        }
        return HologramFailure.NONE;
    }

    private boolean isValidUserLine(PrivateHologram hologram, int lineNumber) {
        return lineNumber >= 1 && lineNumber <= hologram.lines().size();
    }

    private boolean isAllowedPosition(PrivateHologram hologram, Player actor, Location target, boolean admin) {
        Location current = hologram.location();
        if (current == null || target.getWorld() == null) {
            return false;
        }
        if (!target.getWorld().equals(current.getWorld())) {
            return false;
        }
        if (!regionGuard.available()) {
            return admin;
        }
        String regionId = hologram.regionId();
        if (regionGuard.containsInRegion(regionId, target)) {
            if (admin) {
                return true;
            }
            return regionGuard.ownerRegionAt(actor, target)
                    .filter(id -> id.equals(regionId))
                    .isPresent();
        }
        if (admin && !regionGuard.regionExists(regionId, target)) {
            return true;
        }
        return false;
    }

    private void sync(PrivateHologram hologram, Player viewer) {
        List<String> rendered = renderLines(hologram, viewer);
        backend.update(hologram, rendered);
    }

    private List<String> renderLines(PrivateHologram hologram, Player viewer) {
        List<String> rendered = new ArrayList<>();
        for (String line : hologram.lines()) {
            String formatted = backend.formatLine(viewer, line);
            if (viewer != null) {
                formatted = placeholders.apply(viewer, formatted);
            }
            rendered.add(formatted);
        }
        String ownerLine = config.ownerLine().replace("%player%", hologram.ownerName());
        if (viewer != null) {
            ownerLine = placeholders.apply(viewer, ownerLine);
        } else {
            ownerLine = placeholders.applyOffline(hologram.ownerName(), ownerLine);
        }
        rendered.add(ownerLine);
        return rendered;
    }
}
