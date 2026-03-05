package optic_fusion1.tzeditor.editor.visual;

import java.util.Optional;
import optic_fusion1.tzeditor.editor.Selection;
import optic_fusion1.tzeditor.editor.TriggerZoneEditor;
import org.bukkit.Location;

import org.bukkit.entity.Player;

public class EditorSelectionProvider implements SelectionProvider {

    private final TriggerZoneEditor editor;

    public EditorSelectionProvider(TriggerZoneEditor editor) {
        this.editor = editor;
    }

    @Override
    public Optional<SelectionCorners> getSelection(Player player) {
        Optional<Selection> maybeSelection = editor.getSelection(player);
        if (maybeSelection.isEmpty()) {
            return Optional.empty();
        }
        Selection sel = maybeSelection.get();
        if (sel == null) {
            return Optional.empty();
        }
        Location p1 = sel.firstPosition();
        Location p2 = sel.secondPosition();
        if (p1 == null || p2 == null) {
            return Optional.empty();
        }
        return Optional.of(new SelectionCorners(p1, p2));
    }
}
