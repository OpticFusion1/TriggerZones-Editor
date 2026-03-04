package optic_fusion1.tzeditor.editor;

import org.bukkit.Location;
import org.bukkit.util.BoundingBox;

public record Selection(Location firstPosition, Location secondPosition) {

    public boolean isComplete() {
        return firstPosition != null && secondPosition != null;
    }

    public BoundingBox toBoundingBox() {
        if (!isComplete()) {
            throw new IllegalStateException("Cannot create a bounding box with an incomplete selection");
        }
        return BoundingBox.of(firstPosition, secondPosition);
    }
}
