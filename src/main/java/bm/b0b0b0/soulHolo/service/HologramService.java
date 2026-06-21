package bm.b0b0b0.soulHolo.service;

import bm.b0b0b0.soulHolo.config.PluginConfig;
import bm.b0b0b0.soulHolo.hologram.HologramEntityKeys;
import bm.b0b0b0.soulHolo.hologram.HologramBackend;
import bm.b0b0b0.soulHolo.integration.PlaceholderBridge;
import bm.b0b0b0.soulHolo.integration.RegionGuard;
import bm.b0b0b0.soulHolo.model.HologramDisplaySettings;
import bm.b0b0b0.soulHolo.model.PrivateHologram;
import bm.b0b0b0.soulHolo.model.RegionWorldKey;
import bm.b0b0b0.soulHolo.model.RelativeMoveDirection;
import bm.b0b0b0.soulHolo.repository.HologramRepository;
import bm.b0b0b0.soulHolo.session.PlayerSessionService;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import bm.b0b0b0.soulHolo.permission.SoulHoloPermissions;
import org.bukkit.entity.Entity;
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
    private List<RegionWorldKey> regionPurgeRotation = List.of();
    private int regionPurgeCursor;

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
            if (migrateLegacyPlaceholder(hologram)) {
                repository.save(hologram);
            }
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
        if (!admin) {
            PluginConfig.PlayerLimits tier = limitService.resolve(owner);
            if (tier.maxHologramsPerRegion() <= 0) {
                return HologramFailure.HOLOGRAM_LIMIT_DENIED;
            }
        }
        if (!regionGuard.available()) {
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
        Optional<String> region = regionGuard.ownerRegionAt(owner, location);
        if (region.isEmpty()) {
            return HologramFailure.NOT_OWNER_REGION;
        }
        if (!admin) {
            PluginConfig.PlayerLimits tier = limitService.resolve(owner);
            int count = repository.countForLimit(region.get(), owner.getUniqueId(), limitService.countScope());
            if (count >= tier.maxHologramsPerRegion()) {
                return HologramFailure.REGION_LIMIT;
            }
        }
        UUID id = UUID.randomUUID();
        List<String> initialLines = new ArrayList<>();
        HologramDisplaySettings displaySettings = new HologramDisplaySettings();
        PrivateHologram hologram = new PrivateHologram(
                id,
                name,
                owner.getUniqueId(),
                owner.getName(),
                region.get(),
                location,
                initialLines,
                null,
                displaySettings
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
        return addLine(actor, optional.get(), text, admin);
    }

    public HologramFailure removeLine(Player actor, int lineNumber, boolean admin, String hologramName) {
        Optional<PrivateHologram> optional = resolveHologram(actor, admin, hologramName);
        if (optional.isEmpty()) {
            return resolveMissing(actor, admin, hologramName);
        }
        PrivateHologram hologram = optional.get();
        return removeLine(actor, hologram, lineNumber, admin);
    }

    public HologramFailure editLine(Player actor, int lineNumber, String text, boolean admin, String hologramName) {
        Optional<PrivateHologram> optional = resolveHologram(actor, admin, hologramName);
        if (optional.isEmpty()) {
            return resolveMissing(actor, admin, hologramName);
        }
        PrivateHologram hologram = optional.get();
        return editLine(actor, hologram, lineNumber, text, admin);
    }

    public HologramFailure addLine(Player actor, PrivateHologram hologram, String text, boolean admin) {
        if (!admin && !hologram.ownerId().equals(actor.getUniqueId())) {
            return HologramFailure.NOT_OWNED;
        }
        int target = hologram.lines().isEmpty() ? 1 : hologram.lines().size() + 1;
        return insertLine(actor, hologram, target, text, admin);
    }

    public HologramFailure insertLine(Player actor, PrivateHologram hologram, int lineNumber, String text, boolean admin) {
        if (!admin && !hologram.ownerId().equals(actor.getUniqueId())) {
            return HologramFailure.NOT_OWNED;
        }
        int maxLines = resolveMaxLines(admin, actor);
        if (!admin && lineNumber > maxLines) {
            return HologramFailure.LINE_LIMIT;
        }
        if (!isValidLineSlot(lineNumber, maxLines)) {
            return HologramFailure.INVALID_LINE;
        }
        Player owner = Bukkit.getPlayer(hologram.ownerId());
        if (!admin) {
            PluginConfig.PlayerLimits tier = limitService.resolve(actor);
            HologramFailure textFailure = validateText(actor, text, tier);
            if (textFailure != HologramFailure.NONE) {
                return textFailure;
            }
        } else if (blacklistService.isBlocked(text)) {
            return HologramFailure.BLACKLISTED;
        }
        String value = text == null ? "" : text;
        hologram.ensureLineCapacity(lineNumber);
        hologram.lines().set(lineNumber - 1, value);
        sync(hologram, owner == null ? actor : owner);
        repository.save(hologram);
        actionLogService.log("ADD", actor.getName() + " holo=" + hologram.name() + " line=" + lineNumber);
        return HologramFailure.NONE;
    }

    public HologramFailure removeLine(Player actor, PrivateHologram hologram, int lineNumber, boolean admin) {
        if (!admin && !hologram.ownerId().equals(actor.getUniqueId())) {
            return HologramFailure.NOT_OWNED;
        }
        int maxLines = resolveMaxLines(admin, actor);
        if (!isValidLineSlot(lineNumber, maxLines)) {
            return HologramFailure.INVALID_LINE;
        }
        if (!hologram.hasLineContent(lineNumber)) {
            return HologramFailure.INVALID_LINE;
        }
        hologram.ensureLineCapacity(lineNumber);
        hologram.lines().set(lineNumber - 1, "");
        hologram.setLineHidden(lineNumber, false);
        Player owner = Bukkit.getPlayer(hologram.ownerId());
        sync(hologram, owner == null ? actor : owner);
        repository.save(hologram);
        actionLogService.log("REMOVE", actor.getName() + " holo=" + hologram.name() + " line=" + lineNumber);
        return HologramFailure.NONE;
    }

    public HologramFailure toggleLineHidden(Player actor, PrivateHologram hologram, int lineNumber, boolean admin) {
        if (!admin && !hologram.ownerId().equals(actor.getUniqueId())) {
            return HologramFailure.NOT_OWNED;
        }
        int maxLines = resolveMaxLines(admin, actor);
        if (!isValidLineSlot(lineNumber, maxLines)) {
            return HologramFailure.INVALID_LINE;
        }
        if (!hologram.hasLineContent(lineNumber)) {
            return HologramFailure.INVALID_LINE;
        }
        boolean hidden = hologram.isLineHidden(lineNumber);
        hologram.setLineHidden(lineNumber, !hidden);
        Player owner = Bukkit.getPlayer(hologram.ownerId());
        sync(hologram, owner == null ? actor : owner);
        repository.save(hologram);
        actionLogService.log("SETTING", actor.getName() + " holo=" + hologram.name() + " line=" + lineNumber
                + " hidden=" + !hidden);
        return HologramFailure.NONE;
    }

    public HologramFailure editLine(Player actor, PrivateHologram hologram, int lineNumber, String text, boolean admin) {
        if (!admin && !hologram.ownerId().equals(actor.getUniqueId())) {
            return HologramFailure.NOT_OWNED;
        }
        int maxLines = resolveMaxLines(admin, actor);
        if (!isValidLineSlot(lineNumber, maxLines)) {
            return HologramFailure.INVALID_LINE;
        }
        if (!admin) {
            PluginConfig.PlayerLimits tier = limitService.resolve(actor);
            HologramFailure textFailure = validateText(actor, text, tier);
            if (textFailure != HologramFailure.NONE) {
                return textFailure;
            }
        } else if (blacklistService.isBlocked(text)) {
            return HologramFailure.BLACKLISTED;
        }
        hologram.ensureLineCapacity(lineNumber);
        hologram.lines().set(lineNumber - 1, text == null ? "" : text);
        hologram.setLineHidden(lineNumber, false);
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
        int maxLines = resolveMaxLines(admin, actor);
        int targetLine = lineNumber + direction;
        if (!isValidLineSlot(lineNumber, maxLines) || !isValidLineSlot(targetLine, maxLines)) {
            return HologramFailure.INVALID_LINE;
        }
        hologram.ensureLineCapacity(lineNumber);
        hologram.ensureLineCapacity(targetLine);
        int indexA = lineNumber - 1;
        int indexB = targetLine - 1;
        String value = hologram.lines().get(indexA);
        hologram.lines().set(indexA, hologram.lines().get(indexB));
        hologram.lines().set(indexB, value);
        boolean hiddenA = hologram.isLineHidden(lineNumber);
        boolean hiddenB = hologram.isLineHidden(targetLine);
        hologram.setLineHidden(lineNumber, hiddenB);
        hologram.setLineHidden(targetLine, hiddenA);
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

    public void purgeMissingRegions() {
        if (!config.regionGuardPurgeEnabled() || !regionGuard.available()) {
            return;
        }
        resetRegionPurgeRotation();
        purgeMissingRegionsBatch(Integer.MAX_VALUE);
    }

    public void purgeMissingRegionsBatch(int batchSize) {
        if (!config.regionGuardPurgeEnabled() || !regionGuard.available() || batchSize <= 0) {
            return;
        }
        if (regionPurgeRotation.isEmpty() || regionPurgeCursor >= regionPurgeRotation.size()) {
            resetRegionPurgeRotation();
        }
        if (regionPurgeRotation.isEmpty()) {
            return;
        }
        String fallbackRegionId = config.adminFallbackRegionId();
        int processed = 0;
        while (regionPurgeCursor < regionPurgeRotation.size() && processed < batchSize) {
            RegionWorldKey key = regionPurgeRotation.get(regionPurgeCursor++);
            processed++;
            if (isAdminFallbackRegion(key.regionId(), fallbackRegionId)) {
                continue;
            }
            if (regionGuard.regionExistsInWorld(key.regionId(), key.worldName())) {
                continue;
            }
            deleteHologramsInRegion(key);
        }
    }

    private void resetRegionPurgeRotation() {
        regionPurgeRotation = new ArrayList<>(repository.regionKeys());
        regionPurgeCursor = 0;
    }

    private void deleteHologramsInRegion(RegionWorldKey key) {
        for (PrivateHologram hologram : List.copyOf(repository.hologramsInRegion(key))) {
            deleteHologram(hologram);
        }
    }

    public void deleteHologram(PrivateHologram hologram) {
        deleteHologram(hologram, "region-removed holo=" + hologram.name() + " region=" + hologram.regionId());
    }

    public HologramFailure deleteOwnedHologram(Player actor, PrivateHologram hologram, boolean admin) {
        if (!canManage(actor, hologram)) {
            return HologramFailure.NOT_OWNED;
        }
        deleteHologram(hologram, actor.getName() + " deleted holo=" + hologram.name() + " region=" + hologram.regionId());
        return HologramFailure.NONE;
    }

    private void deleteHologram(PrivateHologram hologram, String logMessage) {
        backend.remove(hologram);
        repository.delete(hologram.id());
        sessionService.clearActiveHologram(hologram.id());
        actionLogService.log("DELETE", logMessage);
    }

    private static boolean isAdminFallbackRegion(String regionId, String fallbackRegionId) {
        return fallbackRegionId != null
                && !fallbackRegionId.isBlank()
                && fallbackRegionId.equalsIgnoreCase(regionId);
    }

    public void select(Player player, PrivateHologram hologram) {
        sessionService.setActive(player.getUniqueId(), hologram.id());
    }

    public boolean canManage(Player player, PrivateHologram hologram) {
        if (SoulHoloPermissions.hasAdmin(player)) {
            return true;
        }
        return hologram.ownerId().equals(player.getUniqueId());
    }

    public Optional<PrivateHologram> findByEntity(Entity entity) {
        return HologramEntityKeys.read(entity).flatMap(repository::findById);
    }

    public boolean hasHologramSlot(Player player) {
        if (SoulHoloPermissions.bypassesLimits(player)) {
            return true;
        }
        return limitService.hasHologramSlot(player);
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

    public PluginConfig.PlayerLimits limits(Player player) {
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
        return repository.findById(activeId).map(PrivateHologram::countFilledLines).orElse(0);
    }

    public String regionForFailure(Player player) {
        Optional<String> region = regionGuard.ownerRegionAt(player, player.getLocation());
        return region.orElse("-");
    }

    private Optional<PrivateHologram> resolveHologram(Player actor, boolean admin, String hologramName) {
        if (admin && hologramName != null) {
            return repository.findByName(hologramName);
        }
        if (!admin && hologramName != null && !hologramName.isBlank()) {
            return repository.findByName(hologramName)
                    .filter(hologram -> hologram.ownerId().equals(actor.getUniqueId()));
        }
        UUID activeId = sessionService.activeHologram(actor.getUniqueId());
        if (activeId == null) {
            return Optional.empty();
        }
        return repository.findById(activeId);
    }

    private HologramFailure resolveMissing(Player actor, boolean admin, String hologramName) {
        if (admin) {
            return HologramFailure.NOT_FOUND;
        }
        return HologramFailure.NO_ACTIVE;
    }

    private HologramFailure validateText(Player actor, String text, PluginConfig.PlayerLimits tier) {
        String value = text == null ? "" : text;
        if (value.length() > tier.maxLineLength()) {
            return HologramFailure.LINE_TOO_LONG;
        }
        if (blacklistService.isBlocked(value)) {
            return HologramFailure.BLACKLISTED;
        }
        return HologramFailure.NONE;
    }

    private int resolveMaxLines(boolean admin, Player actor) {
        if (admin) {
            return Integer.MAX_VALUE;
        }
        return limitService.resolve(actor).maxLines();
    }

    private boolean isValidLineSlot(int lineNumber, int maxLines) {
        return lineNumber >= 1 && lineNumber <= maxLines;
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

    public HologramFailure toggleHintLine(Player actor, PrivateHologram hologram, boolean admin) {
        if (!admin && !hologram.ownerId().equals(actor.getUniqueId())) {
            return HologramFailure.NOT_OWNED;
        }
        HologramDisplaySettings settings = hologram.displaySettings();
        settings.setShowHintLine(!settings.showHintLine());
        Player owner = Bukkit.getPlayer(hologram.ownerId());
        sync(hologram, owner == null ? actor : owner);
        repository.save(hologram);
        actionLogService.log("SETTING", actor.getName() + " holo=" + hologram.name() + " hint-line="
                + settings.showHintLine());
        return HologramFailure.NONE;
    }

    public HologramFailure toggleOwnerLine(Player actor, PrivateHologram hologram, boolean admin) {
        if (!admin && !hologram.ownerId().equals(actor.getUniqueId())) {
            return HologramFailure.NOT_OWNED;
        }
        HologramDisplaySettings settings = hologram.displaySettings();
        settings.setShowOwnerLine(!settings.showOwnerLine());
        Player owner = Bukkit.getPlayer(hologram.ownerId());
        sync(hologram, owner == null ? actor : owner);
        repository.save(hologram);
        actionLogService.log("SETTING", actor.getName() + " holo=" + hologram.name() + " owner-line="
                + settings.showOwnerLine());
        return HologramFailure.NONE;
    }

    public String previewHintLine(Player viewer, PrivateHologram hologram) {
        return previewLine(formatHintLine(viewer, hologram));
    }

    public String previewOwnerLine(Player viewer, PrivateHologram hologram) {
        return previewLine(formatOwnerLine(viewer, hologram));
    }

    private void sync(PrivateHologram hologram, Player viewer) {
        List<String> rendered = renderLines(hologram, viewer);
        backend.update(hologram, rendered);
    }

    private boolean migrateLegacyPlaceholder(PrivateHologram hologram) {
        if (!onlyDefaultPlaceholderLines(hologram)) {
            return false;
        }
        hologram.lines().clear();
        hologram.displaySettings().setShowHintLine(true);
        hologram.displaySettings().setShowOwnerLine(true);
        return true;
    }

    private boolean onlyDefaultPlaceholderLines(PrivateHologram hologram) {
        List<String> lines = hologram.lines();
        if (lines.isEmpty()) {
            return false;
        }
        String placeholder = config.defaultCreateLine();
        for (String line : lines) {
            if (!isDefaultPlaceholderLine(line, placeholder)) {
                return false;
            }
        }
        return true;
    }

    private boolean isDefaultPlaceholderLine(String line, String placeholder) {
        return normalizeLineText(line).equals(normalizeLineText(placeholder));
    }

    private String normalizeLineText(String raw) {
        if (raw == null) {
            return "";
        }
        StringBuilder plain = new StringBuilder();
        for (int index = 0; index < raw.length(); index++) {
            char current = raw.charAt(index);
            if (current == '&' || current == '\u00A7') {
                if (index + 1 < raw.length()) {
                    index++;
                }
                continue;
            }
            plain.append(current);
        }
        return plain.toString().trim();
    }

    private String previewLine(String raw) {
        if (raw == null || raw.isBlank()) {
            return "";
        }
        String trimmed = raw.replace('\n', ' ');
        if (trimmed.length() <= 48) {
            return trimmed;
        }
        return trimmed.substring(0, 45) + "...";
    }

    private List<String> renderLines(PrivateHologram hologram, Player viewer) {
        List<String> rendered = new ArrayList<>();
        if (hologram.displaySettings().showHintLine()) {
            rendered.add(formatHintLine(viewer, hologram));
        }
        for (int index = 0; index < hologram.lines().size(); index++) {
            int lineNumber = index + 1;
            if (hologram.isLineHidden(lineNumber)) {
                continue;
            }
            String line = hologram.lines().get(index);
            String formatted = backend.formatLine(viewer, line);
            if (viewer != null) {
                formatted = placeholders.apply(viewer, formatted);
            }
            rendered.add(formatted);
        }
        if (hologram.displaySettings().showOwnerLine()) {
            rendered.add(formatOwnerLine(viewer, hologram));
        }
        return rendered;
    }

    private String formatHintLine(Player viewer, PrivateHologram hologram) {
        String line = config.defaultCreateLine();
        String formatted = backend.formatLine(viewer, line);
        if (viewer != null) {
            return placeholders.apply(viewer, formatted);
        }
        return formatted;
    }

    private String formatOwnerLine(Player viewer, PrivateHologram hologram) {
        String line = config.ownerLine().replace("%player%", hologram.ownerName());
        if (viewer != null) {
            return placeholders.apply(viewer, line);
        }
        return placeholders.applyOffline(hologram.ownerName(), line);
    }
}
