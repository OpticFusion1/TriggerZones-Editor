package optic_fusion1.tzeditor.editor.visual;

import java.util.Optional;
import org.bukkit.Location;

public interface SelectionProvider {

    Optional<SelectionCorners> getSelection(org.bukkit.entity.Player player);

    record SelectionCorners(Location pos1, Location pos2) {

    }
}
