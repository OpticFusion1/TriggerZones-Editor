package optic_fusion1.tzeditor.editor.visual;

import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import optic_fusion1.triggerzones.trigger.TriggerZone;
import static optic_fusion1.tzeditor.editor.listener.WandSelectionListener.WAND_MATERIAL;
import optic_fusion1.tzeditor.util.ParticleBoxRenderer;
import org.bukkit.Bukkit;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

public final class ZoneParticleViewer {

    private static final double MAX_DISTANCE_BLOCKS = 256.0;
    private static final double MAX_DISTANCE_SQ = MAX_DISTANCE_BLOCKS * MAX_DISTANCE_BLOCKS;

    private static final long RENDER_PERIOD_TICKS = 5L;

    private static final Particle ZONE_PARTICLE = Particle.END_ROD;
    private static final Particle SELECTION_PARTICLE = Particle.FLAME;

    private static final double ZONE_STEP = 1.0;
    private static final double SELECTION_STEP = 1.0;

    private final Plugin plugin;
    private final SelectionProvider selectionProvider;

    // Multiple zone viewing: player -> set of zones being previewed
    private final Map<UUID, Set<TriggerZone>> zoneViewers = new ConcurrentHashMap<>();

    // Selection preview toggle: player -> on/off
    private final Map<UUID, Boolean> selectionPreview = new ConcurrentHashMap<>();

    public ZoneParticleViewer(Plugin plugin, SelectionProvider selectionProvider) {
        this.plugin = plugin;
        this.selectionProvider = selectionProvider;
        startRenderer();
    }

    public void toggleZone(Player player, TriggerZone zone) {
        UUID pid = player.getUniqueId();
        Set<TriggerZone> set = zoneViewers.computeIfAbsent(pid, k -> ConcurrentHashMap.newKeySet());

        if (set.remove(zone)) {
            if (set.isEmpty()) {
                zoneViewers.remove(pid);
            }
            player.sendMessage("Zone preview disabled for " + zone.getId() + ".");
        } else {
            set.add(zone);
            player.sendMessage("Zone preview enabled for " + zone.getId() + ".");
        }
    }

    public void clearZones(Player player) {
        zoneViewers.remove(player.getUniqueId());
        player.sendMessage("All zone previews cleared.");
    }

    public void toggleSelectionPreview(Player player) {
        UUID pid = player.getUniqueId();
        boolean next = !selectionPreview.getOrDefault(pid, false);
        selectionPreview.put(pid, next);

        if (!next) {
            selectionPreview.remove(pid);
            player.sendMessage("Selection preview disabled.");
        } else {
            player.sendMessage("Selection preview enabled.");
        }
    }

    private void startRenderer() {
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {

            // Render zones
            for (Iterator<Map.Entry<UUID, Set<TriggerZone>>> it = zoneViewers.entrySet().iterator(); it.hasNext();) {
                Map.Entry<UUID, Set<TriggerZone>> entry = it.next();

                Player player = Bukkit.getPlayer(entry.getKey());
                if (player == null || !player.isOnline()) {
                    it.remove();
                    continue;
                }

                Set<TriggerZone> zones = entry.getValue();
                if (zones == null || zones.isEmpty()) {
                    it.remove();
                    continue;
                }

                Vector eye = player.getEyeLocation().toVector();
                Vector dir = player.getEyeLocation().getDirection().normalize();

                for (Iterator<TriggerZone> zit = zones.iterator(); zit.hasNext();) {
                    TriggerZone zone = zit.next();

                    // world safety (Region carries worldId)
                    if (!player.getWorld().getUID().equals(zone.getWorldId())) {
                        continue;
                    }

                    BoundingBox box = zone.getBounds();

                    if (!withinDistance(eye, box)) {
                        continue;
                    }

                    // Pixel-perfect “is looking at it”: ray-box intersection
                    if (!rayIntersectsAabb(eye, dir, box, 0.0, MAX_DISTANCE_BLOCKS)) {
                        continue;
                    }

                    if (player.getInventory().getItemInMainHand().getType() != WAND_MATERIAL && player.getInventory().getItemInOffHand().getType() != WAND_MATERIAL) {
                        continue;
                    }

                    ParticleBoxRenderer.drawBox(player, box, ZONE_PARTICLE, ZONE_STEP);
                }
            }

            // Render selection preview
            for (Iterator<Map.Entry<UUID, Boolean>> it = selectionPreview.entrySet().iterator(); it.hasNext();) {
                Map.Entry<UUID, Boolean> entry = it.next();
                if (!Boolean.TRUE.equals(entry.getValue())) {
                    it.remove();
                    continue;
                }

                Player player = Bukkit.getPlayer(entry.getKey());
                if (player == null || !player.isOnline()) {
                    it.remove();
                    continue;
                }

                Optional<SelectionProvider.SelectionCorners> opt = selectionProvider.getSelection(player);
                if (opt.isEmpty()) {
                    continue;
                }

                SelectionProvider.SelectionCorners sel = opt.get();
                if (sel.pos1() == null || sel.pos2() == null) {
                    continue;
                }
                if (sel.pos1().getWorld() == null || sel.pos2().getWorld() == null) {
                    continue;
                }
                if (!sel.pos1().getWorld().equals(sel.pos2().getWorld())) {
                    continue;
                }

                // Only draw selection in the player’s current world
                if (!player.getWorld().equals(sel.pos1().getWorld())) {
                    continue;
                }

                BoundingBox selectionBox = selectionToBoundsMaxExclusive(sel.pos1(), sel.pos2());

                Vector eye = player.getEyeLocation().toVector();
                Vector dir = player.getEyeLocation().getDirection().normalize();

                if (!withinDistance(eye, selectionBox)) {
                    continue;
                }
                if (!rayIntersectsAabb(eye, dir, selectionBox, 0.0, MAX_DISTANCE_BLOCKS)) {
                    continue;
                }

                if (player.getInventory().getItemInMainHand().getType() != WAND_MATERIAL && player.getInventory().getItemInOffHand().getType() != WAND_MATERIAL) {
                    continue;
                }

                ParticleBoxRenderer.drawBox(player, selectionBox, SELECTION_PARTICLE, SELECTION_STEP);
            }

        }, 0L, RENDER_PERIOD_TICKS);
    }

    // Distance limit improvement
    private static boolean withinDistance(Vector eye, BoundingBox box) {
        Vector center = new Vector(
                (box.getMinX() + box.getMaxX()) * 0.5,
                (box.getMinY() + box.getMaxY()) * 0.5,
                (box.getMinZ() + box.getMaxZ()) * 0.5
        );
        return eye.distanceSquared(center) <= MAX_DISTANCE_SQ;
    }

    // Selection preview must match Region.normalizeBounds (max exclusive)
    private static BoundingBox selectionToBoundsMaxExclusive(org.bukkit.Location a, org.bukkit.Location b) {
        int minX = Math.min(a.getBlockX(), b.getBlockX());
        int minY = Math.min(a.getBlockY(), b.getBlockY());
        int minZ = Math.min(a.getBlockZ(), b.getBlockZ());
        int maxX = Math.max(a.getBlockX(), b.getBlockX()) + 1;
        int maxY = Math.max(a.getBlockY(), b.getBlockY()) + 1;
        int maxZ = Math.max(a.getBlockZ(), b.getBlockZ()) + 1;
        return new BoundingBox(minX, minY, minZ, maxX, maxY, maxZ);
    }

    // Ray–AABB intersection (slab method). “Pixel-perfect” in the sense that it checks if the eye ray hits the box.
    // tMin/tMax are along ray (origin + dir*t), using blocks as units.
    private static boolean rayIntersectsAabb(Vector origin, Vector dir, BoundingBox box, double tMin, double tMax) {

        // Avoid divide-by-zero by treating very small components as zero
        double ox = origin.getX(), oy = origin.getY(), oz = origin.getZ();
        double dx = dir.getX(), dy = dir.getY(), dz = dir.getZ();

        double minX = box.getMinX(), minY = box.getMinY(), minZ = box.getMinZ();
        double maxX = box.getMaxX(), maxY = box.getMaxY(), maxZ = box.getMaxZ();

        // X slab
        if (Math.abs(dx) < 1e-12) {
            if (ox < minX || ox > maxX) {
                return false;
            }
        } else {
            double inv = 1.0 / dx;
            double t1 = (minX - ox) * inv;
            double t2 = (maxX - ox) * inv;
            if (t1 > t2) {
                double tmp = t1;
                t1 = t2;
                t2 = tmp;
            }
            tMin = Math.max(tMin, t1);
            tMax = Math.min(tMax, t2);
            if (tMax < tMin) {
                return false;
            }
        }

        // Y slab
        if (Math.abs(dy) < 1e-12) {
            if (oy < minY || oy > maxY) {
                return false;
            }
        } else {
            double inv = 1.0 / dy;
            double t1 = (minY - oy) * inv;
            double t2 = (maxY - oy) * inv;
            if (t1 > t2) {
                double tmp = t1;
                t1 = t2;
                t2 = tmp;
            }
            tMin = Math.max(tMin, t1);
            tMax = Math.min(tMax, t2);
            if (tMax < tMin) {
                return false;
            }
        }

        // Z slab
        if (Math.abs(dz) < 1e-12) {
            return !(oz < minZ || oz > maxZ);
        } else {
            double inv = 1.0 / dz;
            double t1 = (minZ - oz) * inv;
            double t2 = (maxZ - oz) * inv;
            if (t1 > t2) {
                double tmp = t1;
                t1 = t2;
                t2 = tmp;
            }
            tMin = Math.max(tMin, t1);
            tMax = Math.min(tMax, t2);
            return tMax >= tMin;
        }
    }

    public boolean isSelectionPreviewEnabled(Player player) {
        return selectionPreview.getOrDefault(player.getUniqueId(), false);
    }
}
