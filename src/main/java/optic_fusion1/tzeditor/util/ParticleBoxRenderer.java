package optic_fusion1.tzeditor.util;

import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.util.BoundingBox;

public final class ParticleBoxRenderer {

    private ParticleBoxRenderer() {
    }

    public static void drawBox(Player player, BoundingBox box, Particle particle, double step) {
        double minX = box.getMinX();
        double minY = box.getMinY();
        double minZ = box.getMinZ();

        double maxX = box.getMaxX();
        double maxY = box.getMaxY();
        double maxZ = box.getMaxZ();

        // Bottom rectangle
        drawLine(player, particle, minX, minY, minZ, maxX, minY, minZ, step);
        drawLine(player, particle, maxX, minY, minZ, maxX, minY, maxZ, step);
        drawLine(player, particle, maxX, minY, maxZ, minX, minY, maxZ, step);
        drawLine(player, particle, minX, minY, maxZ, minX, minY, minZ, step);

        // Top rectangle
        drawLine(player, particle, minX, maxY, minZ, maxX, maxY, minZ, step);
        drawLine(player, particle, maxX, maxY, minZ, maxX, maxY, maxZ, step);
        drawLine(player, particle, maxX, maxY, maxZ, minX, maxY, maxZ, step);
        drawLine(player, particle, minX, maxY, maxZ, minX, maxY, minZ, step);

        // Vertical edges
        drawLine(player, particle, minX, minY, minZ, minX, maxY, minZ, step);
        drawLine(player, particle, maxX, minY, minZ, maxX, maxY, minZ, step);
        drawLine(player, particle, maxX, minY, maxZ, maxX, maxY, maxZ, step);
        drawLine(player, particle, minX, minY, maxZ, minX, maxY, maxZ, step);
    }

    private static void drawLine(
            Player player,
            Particle particle,
            double x1, double y1, double z1,
            double x2, double y2, double z2,
            double step
    ) {

        double dx = x2 - x1;
        double dy = y2 - y1;
        double dz = z2 - z1;

        double length = Math.sqrt(dx * dx + dy * dy + dz * dz);
        if (length <= 0.0001) {
            return;
        }

        double ux = dx / length;
        double uy = dy / length;
        double uz = dz / length;

        for (double d = 0; d <= length; d += step) {
            double x = x1 + ux * d;
            double y = y1 + uy * d;
            double z = z1 + uz * d;

            player.spawnParticle(particle, x, y, z, 1, 0, 0, 0, 0);
        }
    }
}
