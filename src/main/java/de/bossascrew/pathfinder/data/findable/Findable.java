package de.bossascrew.pathfinder.data.findable;

import de.bossascrew.pathfinder.data.FindableGroup;
import de.bossascrew.pathfinder.data.RoadMap;
import org.bukkit.Location;
import org.bukkit.util.Vector;

import javax.annotation.Nullable;
import java.util.List;

public interface Findable {

    /**
     * @return Die DatenbankID für Findables, die in einer Datenbank gespeichert werden können.
     */
    int getDatabaseId();

    /**
     * @return Die ID der übergeordneten Straßenkarte
     */
    int getRoadMapId();

    /**
     * @return Gibt die übergeordnete Straßenkarte zurück, in der sich das Objekt befindet.
     */
    RoadMap getRoadMap();

    /**
     * @return Gibt den Namen des Objektes an, die hauptsächliche Verwendung findet dieser im /find Befehl.
     */
    String getName();

    /**
     * @return Gibt die Position des Objektes als Vektor an. Dieser lässt sich mit der Welt der Roadmap zu einer Location konvertieren.
     */
    Vector getVector();

    /**
     * @return Gibt die Location des Objektes mit Welt an.
     */
    Location getLocation();

    /**
     * @return Eine Liste aller Nodes als ID, die eine direkte Anbindung zum Objekt haben.
     */
    List<Integer> getEdges();

    /**
     * @return Die Permissionnode, die notwendig ist um dieses Objekt zu finden. "none" ist der Platzhalter, um eine Permissionabfrage zu umgehen.
     */
    String getPermission();

    /**
     * @return Die ID der Nodegruppe, in der das Objekt sich befindet. Node.NO_GROUP_ID, um Gruppe zu entfernen
     */
    int getNodeGroupId();

    /**
     * @return Die Nodegruppe, der das Objekt angehört.
     */
    @Nullable FindableGroup getFindableGroup();

    /**
     * Löscht die gesetzte Gruppe.
     */
    void removeFindableGroup();

    /**
     * @return Gibt die Bezierwichtung zurück, oder falls nicht gesetzt null.
     */
    @Nullable
    Double getBezierTangentLength();

    /**
     * @return Gibt die Bezierwichtung zurück, und falls diese nicht gesetzt ist den vorgegebenen Defaultwert der Roadmap.
     */
    double getBezierTangentLengthOrDefault();
}
