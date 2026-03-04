package optic_fusion1.tzeditor.editor.event;

import optic_fusion1.tzeditor.editor.Selection;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class TriggerZoneSelectionUpdateEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();
    private Player player;
    private Selection previousSelection;
    private Selection selection;

    public TriggerZoneSelectionUpdateEvent(Player player, Selection previousSelection, Selection selection) {
        this.player = player;
        this.previousSelection = previousSelection;
        this.selection = selection;
    }

    public Player getPlayer() {
        return player;
    }

    public Selection getPreviousSelection() {
        return previousSelection;
    }

    public Selection getSelection() {
        return selection;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
