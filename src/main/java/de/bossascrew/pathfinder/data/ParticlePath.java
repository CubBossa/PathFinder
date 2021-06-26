package de.bossascrew.pathfinder.data;

import de.bossascrew.core.bukkit.util.VectorUtils;
import de.bossascrew.core.util.PluginUtils;
import de.bossascrew.core.util.Tuple3;
import de.bossascrew.pathfinder.PathPlugin;
import de.bossascrew.pathfinder.data.findable.Findable;
import de.bossascrew.pathfinder.data.visualisation.PathVisualizer;
import de.bossascrew.pathfinder.util.BezierUtils;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.util.Vector;
import xyz.xenondevs.particle.ParticleBuilder;
import xyz.xenondevs.particle.ParticleEffect;
import xyz.xenondevs.particle.task.TaskManager;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Enthält alle wichtigen Informationen zum Anzeigen des Pfades gecached und läuft die Repeating Tasks
 */
@Getter
public class ParticlePath extends ArrayList<Findable> {

    private final RoadMap roadMap;
    private final UUID playerUuid;
    @Setter
    private boolean active;
    @Setter
    private PathVisualizer visualizer;

    private final List<Integer> taskIds;

    private double cachedDistance = -1;
    private final List<Vector> calculatedPoints;

    public ParticlePath(RoadMap roadMap, UUID playerUuid) {
        this.roadMap = roadMap;
        this.playerUuid = playerUuid;
        this.active = false;
        this.taskIds = new ArrayList<>();
        this.visualizer = roadMap.getPathVisualizer();
        this.calculatedPoints = new ArrayList<>();
    }

    public void calculate() {
        calculatedPoints.clear();
        if (roadMap.getDefaultBezierTangentLength() == 0) {
            //Setzt man die Tangentlength auf 0, wird smoothing komplett deaktiviert
            calculateLinear();
        } else {
            calculateSmooth();
        }
        cachedDistance = visualizer.getParticleDistance();

        Findable target = this.get(this.size() - 1);
        calculatedPoints.addAll(VectorUtils.getCircle(target.getVector(), visualizer.getParticleDistance(), roadMap.getNodeFindDistance()));
    }

    private void calculateLinear() {
        for (int i = 0; i < this.size(); i++) {
            if (i == this.size() - 1) {
                continue;
            }
            calculatedPoints.addAll(BezierUtils.getBezierCurveDistanced(visualizer.getParticleDistance(), get(i).getVector(), get(i + 1).getVector()));
        }
    }

    private void calculateSmooth() {
        List<Tuple3<Vector, Vector, Vector>> tangentPoints = getTangentPoints();
        for (int i = 0; i < tangentPoints.size() - 1; i++) {
            final int fi = i;
            final Vector fv = tangentPoints.get(i + 1).getMiddle();
            calculatedPoints.addAll(BezierUtils.getBezierCurveDistanced(visualizer.getParticleDistance(), tangentPoints.get(i).getMiddle(), tangentPoints.get(i + 1).getMiddle(),
                    tangentPoints.get(i).getRight(), tangentPoints.get(i + 1).getLeft())
                    .stream()
                    .filter(vector -> (fi != tangentPoints.size() - 2) || vector.distance(fv) > roadMap.getNodeFindDistance())
                    .collect(Collectors.toList()));
        }
        List<Vector> evenSpacing = BezierUtils.getEvenSpacing(calculatedPoints, visualizer.getParticleDistance());
        calculatedPoints.clear();
        calculatedPoints.addAll(evenSpacing);
    }

    /**
     * @return Gibt eine Liste aus Vektoren-Tripeln zurück. Der erste Wert ist der linke Kontrollpunkt, der zweite der eigentliche Punkt und der dritte
     * der rechte Kontrollpunkt. Ist ein Wert null, gibt es keinen Kontrollpunkt weil es kein benachbartes Node gab.
     */
    private List<Tuple3<Vector, Vector, Vector>> getTangentPoints() {
        //TODO wenn zu nahe aneinander überschneidung: automatisch Tangenten kürzen

        if (size() < 1) {
            return new ArrayList<>();
        }
        int count = 0;
        List<Tuple3<Vector, Vector, Vector>> tangentPoints = new ArrayList<>();
        if (this.size() == 1) {
            tangentPoints.add(new Tuple3<>(null, this.get(0).getVector(), null));
            return tangentPoints;
        }
        for (Findable findable : this) {
            if (count == 0) {
                //Gerade in Richtung nächsten Punktes
                tangentPoints.add(new Tuple3<>(null, findable.getVector(), findable.getVector().clone().add(this.get(1).getVector().clone()
                        .subtract(findable.getVector()).normalize().multiply(findable.getBezierTangentLengthOrDefault()))));
            } else if (count == this.size() - 1) {
                //Gerade bis zum letzten Punkt
                tangentPoints.add(new Tuple3<>(findable.getVector().clone().add(this.get(count - 1).getVector().clone().subtract(findable.getVector()).normalize()
                        .multiply(findable.getBezierTangentLengthOrDefault())), findable.getVector(), null));
            } else {
                //Alle Fälle mit 2 Nachbarpunkten. Benennung: a = linker nachbar, b = punkt, c = rechter nachbar
                Vector a = get(count - 1).getVector();
                Vector b = findable.getVector();
                Vector c = get(count + 1).getVector();

                Vector ba = a.clone().subtract(b).normalize();
                Vector bc = c.clone().subtract(b).normalize();

                //Vector, der genau in der Mitte zwischen ba und bc
                Vector middle = ba.clone().add(bc).normalize();
                //Senkrechtvektor:
                Vector up = ba.clone().crossProduct(bc);
                //Kontrollpunktrichtung:
                Vector dir = middle.clone().crossProduct(up).normalize().multiply(findable.getBezierTangentLengthOrDefault());

                tangentPoints.add(new Tuple3<>(b.clone().add(dir), findable.getVector(), b.clone().add(dir.clone().multiply(-1))));
            }
            count++;
        }
        return tangentPoints;
    }

    public void run() {
        run(playerUuid);
    }

    public void run(UUID uuid) {
        PluginUtils.getInstance().runSync(() -> {
            System.out.println("Erst cancellen.");
            cancelSync();
            this.active = true;

            //PluginUtils.getInstance().runAsync(() -> {
            int steps = visualizer.getParticleSteps();
            ParticleEffect effect = ParticleEffect.valueOf(visualizer.getParticle().name());
            int period = visualizer.getSchedulerPeriod();
            World world = roadMap.getWorld();

            if (visualizer.getParticleDistance() != cachedDistance) {
                calculate();
            }

            //PluginUtils.getInstance().runSync(() -> {
            for (int i = 0; i < steps; i++) {
                final List<Object> packets = new ArrayList<>();
                ParticleBuilder particle = new ParticleBuilder(effect);

                int moduloCount = 0;
                for (Vector vector : calculatedPoints) {
                    if (moduloCount % steps == i) {
                        packets.add(particle.setLocation(vector.toLocation(world)).toPacket());
                    }
                    moduloCount++;
                }
                System.out.println("Starte Scheduler:");
                Bukkit.getScheduler().runTaskLater(PathPlugin.getInstance(), () -> {
                    int id = TaskManager.startSingularTask(packets, period * steps, uuid);
                    System.out.println("Add ID: " + id);
                    this.taskIds.add(id);
                    System.out.println(taskIds.size());
                }, (long) i * period);
            }
            //});
            //});
        });
    }

    public void cancel() {
        PluginUtils.getInstance().runSync(this::cancelSync);
    }

    /**
     * Nur im Mainthread aufrufen
     */
    public void cancelSync() {
        System.out.println(taskIds.size());
        List<Integer> tasksToRemove = new ArrayList<>();
        for(int i = 0; i < taskIds.size(); i++) {
            int taskId = taskIds.get(i);
            System.out.println("Cancelling Task: " + taskId);
            Bukkit.getScheduler().cancelTask(taskId);
            tasksToRemove.add(taskId);
        }
        taskIds.removeAll(tasksToRemove);
        this.active = false;
    }
}
