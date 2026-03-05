package optic_fusion1.tzeditor.editor;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import optic_fusion1.triggerzones.TriggerZones;
import optic_fusion1.triggerzones.trigger.TriggerZone;
import optic_fusion1.triggerzones.trigger.TriggerZoneManager;
import optic_fusion1.tzeditor.editor.event.TriggerZoneCreatedEvent;
import optic_fusion1.tzeditor.editor.event.TriggerZoneSelectionUpdateEvent;
import optic_fusion1.tzeditor.editor.visual.EditorSelectionProvider;
import optic_fusion1.tzeditor.editor.visual.ZoneParticleViewer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class TriggerZoneEditor {

    private TriggerZones plugin;
    private ZoneParticleViewer particleViewer;
    private TriggerZoneManager triggerZoneManager;
    private Map<UUID, Selection> playerSelections = new HashMap<>();

    public TriggerZoneEditor(TriggerZones plugin, TriggerZoneManager triggerZoneManager) {
        this.plugin = plugin;
        this.triggerZoneManager = triggerZoneManager;
        particleViewer = new ZoneParticleViewer(plugin, new EditorSelectionProvider(this));
    }

    public Optional<Selection> getSelection(Player player) {
        return Optional.ofNullable(playerSelections.get(player.getUniqueId()));
    }

    public void setFirstPosition(Player player, Location location) {
        Selection oldSelection = playerSelections.getOrDefault(player.getUniqueId(), new Selection(null, null));
        Selection updatedSelection = new Selection(location, oldSelection.secondPosition());
        playerSelections.put(player.getUniqueId(), updatedSelection);
        Bukkit.getPluginManager().callEvent(new TriggerZoneSelectionUpdateEvent(player, oldSelection, updatedSelection));
    }

    public void setSecondPosition(Player player, Location location) {
        Selection oldSelection = playerSelections.getOrDefault(player.getUniqueId(), new Selection(null, null));
        Selection updatedSelection = new Selection(oldSelection.firstPosition(), location);
        playerSelections.put(player.getUniqueId(), updatedSelection);
        Bukkit.getPluginManager().callEvent(new TriggerZoneSelectionUpdateEvent(player, oldSelection, updatedSelection));
    }

    public void clearSelection(UUID playerId) {
        playerSelections.remove(playerId);
    }

    public TriggerZone createZone(Player player, String zoneId) {
        Selection selection = getSelection(player)
                .filter(Selection::isComplete).orElseThrow(() -> new IllegalStateException("Selection is incomplete"));
        TriggerZone createdZone = new TriggerZone(zoneId, selection.firstPosition(), selection.secondPosition());
        TriggerZoneCreatedEvent event = new TriggerZoneCreatedEvent(player, createdZone);
        Bukkit.getPluginManager().callEvent(event);
        if (!event.isCancelled()) {
            triggerZoneManager.addZone(zoneId, createdZone);
            clearSelection(player.getUniqueId());
            return createdZone;
        }
        return null;
    }

    public TriggerZones getPlugin() {
        return plugin;
    }

    public ZoneParticleViewer getParticleViewer() {
        return particleViewer;
    }

}
