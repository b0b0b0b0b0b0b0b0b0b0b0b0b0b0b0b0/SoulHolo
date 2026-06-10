package bm.b0b0b0.soulHolo.repository;

import org.bukkit.Location;

import java.util.UUID;

public record PlayerContext(UUID playerId, Location location, double radius) {
}
