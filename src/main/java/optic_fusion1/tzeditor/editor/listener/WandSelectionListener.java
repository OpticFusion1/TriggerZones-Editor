package optic_fusion1.tzeditor.editor.listener;

import optic_fusion1.tzeditor.editor.TriggerZoneEditor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

public class WandSelectionListener implements Listener {

    public static final Material WAND_MATERIAL = Material.WOODEN_AXE; // TODO: Make this configurable
    private TriggerZoneEditor editor;

    public WandSelectionListener(TriggerZoneEditor editor) {
        this.editor = editor;
    }

    @EventHandler
    public void onWandClick(PlayerInteractEvent event) {
        if (event.getClickedBlock() == null || event.getItem() == null || event.getItem().getType() != WAND_MATERIAL) {
            return;
        }
        Player player = event.getPlayer();
        if (!player.hasPermission("triggerzones.editor")) {
            return;
        }
        if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
            editor.setFirstPosition(player, event.getClickedBlock().getLocation());
            player.sendMessage("§aFirst position set.");
            event.setCancelled(true);
            return;
        }
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            editor.setSecondPosition(player, event.getClickedBlock().getLocation());
            player.sendMessage("§aSecond position set.");
            event.setCancelled(true);
        }
    }
}
