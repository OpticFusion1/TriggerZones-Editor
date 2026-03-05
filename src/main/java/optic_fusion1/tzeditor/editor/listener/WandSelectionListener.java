package optic_fusion1.tzeditor.editor.listener;

import java.util.Optional;
import optic_fusion1.tzeditor.editor.Selection;
import optic_fusion1.tzeditor.editor.TriggerZoneEditor;
import optic_fusion1.tzeditor.editor.visual.ZoneParticleViewer;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;

import org.bukkit.event.player.PlayerInteractEvent;

public class WandSelectionListener implements Listener {

    public static final Material WAND_MATERIAL = Material.WOODEN_AXE; // TODO: Make this configurable

    private final TriggerZoneEditor editor;
    private final ZoneParticleViewer particleViewer;

    public WandSelectionListener(TriggerZoneEditor editor) {
        this.editor = editor;
        this.particleViewer = editor.getParticleViewer();
    }

    @EventHandler
    public void onWandClick(PlayerInteractEvent event) {
        if (event.getClickedBlock() == null) {
            return;
        }
        if (event.getItem() == null || event.getItem().getType() != WAND_MATERIAL) {
            return;
        }

        Player player = event.getPlayer();
        if (!player.hasPermission("triggerzones.editor")) {
            return;
        }

        Action action = event.getAction();

        if (action == Action.LEFT_CLICK_BLOCK) {
            editor.setFirstPosition(player, event.getClickedBlock().getLocation());
            player.sendMessage("§aFirst position set.");
            event.setCancelled(true);

            enableSelectionPreviewIfUseful(player);
            return;
        }

        if (action == Action.RIGHT_CLICK_BLOCK) {
            editor.setSecondPosition(player, event.getClickedBlock().getLocation());
            player.sendMessage("§aSecond position set.");
            event.setCancelled(true);

            enableSelectionPreviewIfUseful(player);
        }
    }

    private void enableSelectionPreviewIfUseful(Player player) {
        // If they have no selection at all yet, don't spam toggles.
        // If they have a selection (even incomplete), turning preview on is still useful,
        // because ZoneParticleViewer only renders once it is complete.
        Optional<Selection> sel = editor.getSelection(player);
        if (sel.isEmpty()) {
            return;
        }

        // Ensure selection preview is ON. ZoneParticleViewer only exposes "toggle",
        // so we avoid accidentally disabling it by checking state first.
        if (!particleViewer.isSelectionPreviewEnabled(player)) {
            particleViewer.toggleSelectionPreview(player);
        }
    }
}
