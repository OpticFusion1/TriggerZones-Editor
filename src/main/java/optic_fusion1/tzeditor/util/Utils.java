package optic_fusion1.tzeditor.util;

import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.util.BoundingBox;

public final class Utils {

    private Utils() {
    }

    public static World getWorld(UUID worldId) {
        return worldId == null ? null : Bukkit.getWorld(worldId);
    }

    public static Location boundsToMinCorner(World w, BoundingBox b) {
        return new Location(w, (int) Math.floor(b.getMinX()), (int) Math.floor(b.getMinY()), (int) Math.floor(b.getMinZ()));
    }

    public static Location boundsToMaxCornerInclusive(World w, BoundingBox b) {
        return new Location(w, ((int) Math.floor(b.getMaxX())) - 1, ((int) Math.floor(b.getMaxY())) - 1, ((int) Math.floor(b.getMaxZ())) - 1
        );
    }

    public static boolean sameWorld(UUID worldId, World w) {
        return w != null && worldId != null && worldId.equals(w.getUID());
    }

    public static String format(org.bukkit.Location location) {
        if (location == null) {
            return "not set";
        }
        return location.getBlockX() + ", " + location.getBlockY() + ", " + location.getBlockZ() + " in " + location.getWorld().getName();
    }
}
