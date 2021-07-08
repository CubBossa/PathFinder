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

    protected final int databaseId;
    protected final int roadMapId;
    protected final RoadMap roadMap;
    protected final List<Integer> edges;
    protected @Nullable Integer nodeGroupId = null;

    protected @Nullable Double bezierTangentLength = null;
    private @Nullable String permission = null;

    public Findable(int databaseId, RoadMap roadMap) {
        this.databaseId = databaseId;
        this.roadMap = roadMap;
        this.roadMapId = roadMap.getDatabaseId();

        edges = new ArrayList<>();
    }

    public void setGroup(Integer groupId) {
        setGroup(groupId, true);
    }

    public void setGroup(Integer groupId, boolean updateArmorStands) {
        this.nodeGroupId = groupId;
        if(updateArmorStands) {
            roadMap.updateArmorStandDisplay(this);
        }
        updateData();
    }

    public void setGroup(@Nullable FindableGroup nodeGroup) {
        setGroup(nodeGroup == null ? null : nodeGroup.getDatabaseId(), true);
    }

    public void removeFindableGroup() {
        setGroup((Integer) null, true);
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

    public abstract String getScope();

    abstract void updateData();
}
