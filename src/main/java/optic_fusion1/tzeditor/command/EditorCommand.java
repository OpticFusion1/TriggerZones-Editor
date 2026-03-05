package optic_fusion1.tzeditor.command;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;
import optic_fusion1.triggerzones.trigger.TriggerZone;
import optic_fusion1.triggerzones.trigger.TriggerZoneManager;
import optic_fusion1.tzeditor.TZEditor;
import optic_fusion1.tzeditor.editor.Selection;
import optic_fusion1.tzeditor.editor.TriggerZoneEditor;
import optic_fusion1.tzeditor.editor.listener.WandSelectionListener;
import optic_fusion1.tzeditor.editor.visual.EditorSelectionProvider;
import optic_fusion1.tzeditor.editor.visual.ZoneParticleViewer;
import static optic_fusion1.tzeditor.util.Utils.boundsToMaxCornerInclusive;
import static optic_fusion1.tzeditor.util.Utils.boundsToMinCorner;
import static optic_fusion1.tzeditor.util.Utils.format;
import static org.bukkit.Bukkit.getWorld;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class EditorCommand implements CommandExecutor, TabCompleter {

    private static final List<String> SUB_COMMANDS = List.of("wand", "pos1", "pos2", "create", "delete", "select", "update",
            "clear", "info", "list", "toggleshow");
    private TriggerZoneEditor editor;
    private TriggerZoneManager manager;
    private ZoneParticleViewer viewer;

    public EditorCommand(TZEditor plugin) {
        this.editor = plugin.getEditor();
        manager = editor.getPlugin().getTriggerZoneManager();
        viewer = editor.getParticleViewer();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }
        if (!player.hasPermission("triggerzones.editor")) {
            player.sendMessage("§cYou do not have permission to use the editor.");
            return true;
        }
        if (args.length == 0) {
            player.sendMessage("§e/triggerzones <wand|pos1|pos2|create/delete/select/toggleShow <id>|clear|info>");
            return true;
        }
        String action = args[0].toLowerCase(Locale.ROOT);
        switch (action) {
            case "wand" ->
                giveWand(player);
            case "pos1" -> {
                editor.setFirstPosition(player, player.getLocation());
                player.sendMessage("§aFirst position set to your location.");
            }
            case "pos2" -> {
                editor.setSecondPosition(player, player.getLocation());
                player.sendMessage("§aSecond position set to your location.");
            }
            case "create" ->
                createZone(player, args);
            case "delete" ->
                deleteZone(player, args);
            case "clear" -> {
                editor.clearSelection(player.getUniqueId());
                player.sendMessage("§aSelection cleared.");
            }
            case "info" ->
                sendInfo(player);
            case "list" ->
                listZones(player);
            case "select" ->
                selectZone(player, args[1]);
            case "update" ->
                updateZone(player, args[1]);
            case "toggleshow" ->
                showZone(player, args[1]);
            default ->
                player.sendMessage("§cUnknown subcommand. ");
        }
        return true;
    }

    private void updateZone(Player player, String zoneId) {
        Optional<Selection> maybeSelection = editor.getSelection(player);
        if (maybeSelection.isEmpty()) {
            player.sendMessage("You haven't made a selection yet.");
            return;
        }
        Selection sel = maybeSelection.get();
        if (sel == null || !sel.isComplete()) {
            player.sendMessage("Select pos1 and pos2 first.");
            return;
        }

        if (sel.firstPosition().getWorld().getUID() != sel.secondPosition().getWorld().getUID()) {
            player.sendMessage("pos1 and pos2 must be in the same world.");
            return;
        }

        TriggerZone zone = manager.getZone(zoneId).get();
        if (zone == null) {
            player.sendMessage("No zone with id " + zoneId);
            return;
        }

        World sw = sel.firstPosition().getWorld();
        if (zone.getWorldId() != sw.getUID()) {
            World zw = getWorld(zone.getWorldId());
            player.sendMessage("Zone is in world " + (zw == null ? "<unloaded>" : zw.getName()) + " but your selection is in " + sw.getName());
            return;
        }

        zone.updateLocation(sel.firstPosition(), sel.secondPosition());
        player.sendMessage("Updated zone " + zoneId + ".");
    }

    private void showZone(Player player, String zoneId) {
        Optional<TriggerZone> maybeTrigger = manager.getZone(zoneId);
        if (maybeTrigger.isEmpty()) {
            player.sendMessage("Zone not found.");
            return;
        }
        viewer.toggleZone(player, maybeTrigger.get());
    }

    private void selectZone(Player player, String zoneId) {
        Optional<TriggerZone> zone = manager.getZone(zoneId);
        if (zone.isEmpty()) {
            player.sendMessage("No zone with id " + zoneId);
            return;
        }

        TriggerZone foundZone = zone.get();

        World zw = getWorld(foundZone.getWorldId());
        if (zw == null) {
            player.sendMessage("Zone world is not loaded.");
            return;
        }

        if (!zw.equals(player.getWorld())) {
            player.sendMessage("Zone is in world " + zw.getName() + " but you are in " + player.getWorld().getName());
            return;
        }

        Location min = boundsToMinCorner(zw, foundZone.getBounds());
        Location max = boundsToMaxCornerInclusive(zw, foundZone.getBounds());

        editor.setFirstPosition(player, min);
        editor.setSecondPosition(player, max);

        player.sendMessage("Selected zone " + zoneId + " (" + format(min) + " -> " + format(max) + ")");
    }

    private void listZones(Player player) {
        Collection<TriggerZone> zoneCollection = manager.getZones();
        String zoneStr = zoneCollection.stream()
                .map(TriggerZone::getId)
                .sorted(String::compareToIgnoreCase)
                .collect(Collectors.joining(", "));
        player.sendMessage("§eZones (" + zoneCollection.size() + "): " + (zoneCollection.isEmpty() ? "none" : zoneStr));
    }

    // TODO: Rewrite this to use PDC
    private void giveWand(Player player) {
        player.getInventory().addItem(new ItemStack(WandSelectionListener.WAND_MATERIAL));
        player.sendMessage("§aGiven editor wand (wooden axe).");
    }

    private void createZone(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage("§cUsage: /triggerzones create <id>");
            return;
        }
        try {
            TriggerZone zone = editor.createZone(player, args[1]);
            player.sendMessage("§aCreated trigger zone §f" + zone.getId() + "§a.");
        } catch (IllegalStateException ex) {
            player.sendMessage("§cSelection is incomplete. Set both positions first.");
        }
    }

    private void deleteZone(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage("§cUsage: /triggerzones delete <id>");
            return;
        }
        if (!manager.getZone(args[1]).isPresent()) {
            player.sendMessage("§c" + args[1] + " is not a valid TriggerZone id");
            return;
        }
        manager.removeZone(args[1]);
        player.sendMessage("§aDeleted trigger zone §f" + args[1] + "§a.");
    }

    private void sendInfo(Player player) {
        Optional<Selection> maybeSelection = editor.getSelection(player);
        if (maybeSelection.isEmpty()) {
            player.sendMessage("§eYou do not currently have a selection.");
            return;
        }
        Selection selection = maybeSelection.get();
        player.sendMessage("§eSelection status:");
        player.sendMessage("§7- Pos1: " + format(selection.firstPosition()));
        player.sendMessage("§7- Pos2: " + format(selection.secondPosition()));
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return SUB_COMMANDS.stream().filter(entry -> entry.startsWith(args[0].toLowerCase(Locale.ROOT))).collect(Collectors.toList());
        }
        return new ArrayList<>();
    }
}
