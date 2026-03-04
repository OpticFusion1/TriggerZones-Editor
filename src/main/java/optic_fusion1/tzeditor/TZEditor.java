package optic_fusion1.tzeditor;

import optic_fusion1.triggerzones.TriggerZones;
import optic_fusion1.triggerzones.trigger.TriggerZoneManager;
import optic_fusion1.tzeditor.command.EditorCommand;
import optic_fusion1.tzeditor.editor.TriggerZoneEditor;
import optic_fusion1.tzeditor.editor.listener.WandSelectionListener;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class TZEditor extends JavaPlugin {

    private PluginManager pluginManager = Bukkit.getPluginManager();
    private TriggerZoneEditor editor;

    @Override
    public void onEnable() {
        TriggerZones plugin = (TriggerZones) pluginManager.getPlugin("TriggerZones");
        if (plugin == null) {
            return;
        }
        TriggerZoneManager zoneManager = plugin.getTriggerZoneManager();
        editor = new TriggerZoneEditor(plugin, zoneManager);
        registerListeners();
        registerCommands();
    }

    private void registerCommands() {
        PluginCommand command = getCommand("triggerzones");
        if (command == null) {
            return;
        }
        EditorCommand editorCommand = new EditorCommand(editor);
        command.setExecutor(editorCommand);
        command.setTabCompleter(editorCommand);
    }

    private void registerListeners() {
        pluginManager.registerEvents(new WandSelectionListener(editor), this);
    }

}
