package bm.b0b0b0.soulHolo.service;

import bm.b0b0b0.soulHolo.model.RelativeMoveDirection;
import org.bukkit.Location;

public final class RelativeMoveCalculator {

    private RelativeMoveCalculator() {
    }

    public static Location shifted(Location current, float yaw, RelativeMoveDirection direction, double step) {
        Location target = current.clone();
        switch (direction) {
            case UP -> target.add(0.0, step, 0.0);
            case DOWN -> target.add(0.0, -step, 0.0);
            case LEFT, RIGHT -> {
                double yawRad = Math.toRadians(yaw);
                double rightX = -Math.cos(yawRad);
                double rightZ = -Math.sin(yawRad);
                double factor = direction == RelativeMoveDirection.RIGHT ? step : -step;
                target.add(rightX * factor, 0.0, rightZ * factor);
            }
        }
        return target;
    }
}
