package de.bossascrew.pathfinder.data.findable;

import de.bossascrew.pathfinder.data.FindableGroup;
import de.bossascrew.pathfinder.data.RoadMap;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.util.Vector;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

@Getter
public abstract class Findable {

    public static final int NO_GROUP_ID = -1;

    protected final int databaseId;
    protected final int roadMapId;
    protected final RoadMap roadMap;
    protected final List<Integer> edges;
    protected int nodeGroupId = NO_GROUP_ID;

    protected Double bezierTangentLength = null;
    private String permission = null;

    public Findable(int databaseId, RoadMap roadMap) {
        this.databaseId = databaseId;
        this.roadMap = roadMap;
        this.roadMapId = roadMap.getDatabaseId();

        edges = new ArrayList<>();
    }

    public void setGroup(int nodeGroupId) {
        this.nodeGroupId = nodeGroupId;
        updateData();
    }

    public void setGroup(FindableGroup nodeGroup) {
        setGroup(nodeGroup.getDatabaseId());
    }

    public void setGroupId(int groupId) {
        this.nodeGroupId = groupId;
        roadMap.updateArmorStandDisplay(this);
        updateData();
    }

    public void removeFindableGroup() {
        nodeGroupId = NO_GROUP_ID;
        updateData();
    }

    public @Nullable
    FindableGroup getGroup() {
        return roadMap.getFindableGroup(nodeGroupId);
    }

    public @Nullable
    Double getBezierTangentLength() {
        return bezierTangentLength;
    }

    /**
     * @return Gibt die Bezierwichtung zurück, und falls diese nicht gesetzt ist den vorgegebenen Defaultwert der Roadmap.
     */
    public double getBezierTangentLengthOrDefault() {
        if (bezierTangentLength == null) {
            return roadMap.getDefaultBezierTangentLength();
        }
        return bezierTangentLength;
    }

    public void setBezierTangentLength(@Nullable Double bezierTangentLength) {
        this.bezierTangentLength = bezierTangentLength;
        updateData();
    }

    public void setPermission(@Nullable String permission) {
        this.permission = permission;
        updateData();
    }

    /**
     * @return Gibt den Namen des Objektes an, die hauptsächliche Verwendung findet dieser im /find Befehl.
     */
    public abstract String getName();

    /**
     * @return Gibt die Position des Objektes als Vektor an. Dieser lässt sich mit der Welt der Roadmap zu einer Location konvertieren.
     */
    public abstract Vector getVector();

    /**
     * @return Gibt die Location des Objektes mit Welt an.
     */
    public abstract Location getLocation();

    abstract void updateData();
}
