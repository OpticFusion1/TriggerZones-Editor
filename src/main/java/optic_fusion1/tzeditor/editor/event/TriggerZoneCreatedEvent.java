package optic_fusion1.tzeditor.editor.event;

import optic_fusion1.triggerzones.trigger.TriggerZone;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class TriggerZoneCreatedEvent extends Event implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();
    private Player player;
    private TriggerZone zone;
    private boolean cancelled;

    public TriggerZoneCreatedEvent(Player player, TriggerZone zone) {
        this.player = player;
        this.zone = zone;
    }

    public Player getPlayer() {
        return player;
    }

    public TriggerZone getZone() {
        return zone;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
