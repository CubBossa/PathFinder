package de.bossascrew.pathfinder.data;

import com.google.common.collect.Lists;
import de.bossascrew.core.bukkit.util.BezierUtils;
import de.bossascrew.core.bukkit.util.VectorUtils;
import de.bossascrew.core.util.PluginUtils;
import de.bossascrew.core.util.Tuple3;
import de.bossascrew.pathfinder.PathPlugin;
import de.bossascrew.pathfinder.data.findable.Findable;
import de.bossascrew.pathfinder.data.visualisation.PathVisualizer;
import de.bossascrew.pathfinder.handler.PathPlayerHandler;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import xyz.xenondevs.particle.ParticleBuilder;
import xyz.xenondevs.particle.ParticleEffect;
import xyz.xenondevs.particle.task.TaskManager;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Enthält alle wichtigen Informationen zum Anzeigen des Pfades gecached und läuft die Repeating Tasks
 */
@Getter
public class ParticlePath extends ArrayList<Findable> {

    @RequiredArgsConstructor
    @Getter @Setter
    private static class SchedulerHandler {

        private final long id;
        private final List<Integer> schedulerIds;
        private boolean cancelled = false;

        public SchedulerHandler(long id) {
            this(id, new ArrayList<>());
        }
    }

    private final RoadMap roadMap;
    private final UUID playerUuid;
    @Setter
    private boolean active;
    @Setter
    private PathVisualizer visualizer;

    private final List<SchedulerHandler> schedulerHandlers;

    private double cachedDistance = -1;
    private List<Vector> calculatedPoints;

    public ParticlePath(RoadMap roadMap, UUID playerUuid, PathVisualizer visualizer) {
        this.roadMap = roadMap;
        this.playerUuid = playerUuid;
        this.active = false;
        this.schedulerHandlers = new ArrayList<>();
        this.visualizer = visualizer;
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
            final int finalIndex = i;
            final Vector actualCenter = tangentPoints.get(i).getMiddle();
            final Vector actualRight = tangentPoints.get(i).getRight();
            final Vector nextLeft = tangentPoints.get(i + 1).getLeft();
            final Vector nextCenter = tangentPoints.get(i + 1).getMiddle();
            List<Vector> bezier = BezierUtils.getBezierCurveDistanced(visualizer.getParticleDistance(),
                    actualCenter, nextCenter, actualRight, nextLeft);

            calculatedPoints.addAll(bezier.stream()
                    .filter(vector -> (finalIndex != tangentPoints.size() - 2) || vector.distance(nextCenter) > roadMap.getNodeFindDistance())
                    .collect(Collectors.toList()));
        }
        List<Vector> evenSpacing = BezierUtils.getEvenlySpacedPoints(calculatedPoints, visualizer.getParticleDistance());
        calculatedPoints = new ArrayList<>(evenSpacing);
    }

    /**
     * @return Gibt eine Liste aus Vektoren-Tripeln zurück. Der erste Wert ist der linke Kontrollpunkt, der zweite der eigentliche Punkt und der dritte
     * der rechte Kontrollpunkt. Ist ein Wert null, gibt es keinen Kontrollpunkt weil es kein benachbartes Node gab.
     */
    private List<Tuple3<Vector, Vector, Vector>> getTangentPoints() {

        if (size() < 1) {
            return new ArrayList<>();
        }
        int count = 0;
        List<Tuple3<Vector, Vector, Vector>> tangentPoints = new ArrayList<>();
        if (this.size() == 1) {
            tangentPoints.add(new Tuple3<>(null, this.get(0).getVector(), null));
            return tangentPoints;
        }
        Tuple3<Vector, Vector, Vector> lastTangentSet = null;
        for (Findable findable : this) {
            if (count == 0) {
                //Gerade in Richtung nächsten Punktes
                tangentPoints.add(new Tuple3<>(null, findable.getVector(), findable.getVector().clone().add(this.get(1).getVector().clone()
                        .subtract(findable.getVector()).normalize().multiply(findable.getBezierTangentLengthOrDefault()))));
            } else if (count == this.size() - 1) {
                //Gerade bis zum letzten Punkt
                if (lastTangentSet != null) {
                    tangentPoints.add(new Tuple3<>(lastTangentSet.getRight(), findable.getVector(), null));
                } else {
                    tangentPoints.add(new Tuple3<>(findable.getVector().clone().add(this.get(count - 1).getVector().clone().subtract(findable.getVector()).normalize()
                            .multiply(findable.getBezierTangentLengthOrDefault())), findable.getVector(), null));
                }
            } else {
                //Alle Fälle mit 2 Nachbarpunkten. Benennung: a = linker nachbar, b = punkt, c = rechter nachbar
                Vector a = get(count - 1).getVector();
                Vector b = findable.getVector();
                Vector c = get(count + 1).getVector();

                Vector ba = a.clone().subtract(b).normalize();
                Vector bc = c.clone().subtract(b).normalize();

                double effectiveBezierLenght = findable.getBezierTangentLengthOrDefault();
                double leftDist = a.distance(b);
                if (effectiveBezierLenght > leftDist / 2) {
                    effectiveBezierLenght = leftDist / 2;
                }

                //Senkrechtvektor:
                Vector up = ba.clone().crossProduct(bc);
                if (up.getX() == 0 && up.getY() == 0 && up.getZ() == 0) {
                    up = new Vector(0, 1, 0);
                }

                //Vector, der genau in der Mitte zwischen ba und bc
                Vector middle = ba.clone().add(bc).normalize();
                fixVectorNaN(middle);
                if (middle.getX() == 0 && middle.getY() == 0 && middle.getZ() == 0) {
                    middle = ba.clone().crossProduct(up);
                }

                //Kontrollpunktrichtung:
                Vector dir = middle.clone().crossProduct(up).normalize().multiply(effectiveBezierLenght);

                Vector left = b.clone().add(dir);
                Vector right = b.clone().add(dir.clone().multiply(-1));

                Tuple3<Vector, Vector, Vector> last = new Tuple3<>(left, findable.getVector(), right);
                tangentPoints.add(last);
                lastTangentSet = last;
            }
            count++;
        }
        return tangentPoints;
    }

    public void fixVectorNaN(Vector vector) {
        if (Double.isNaN(vector.getX())) {
            vector.setX(0);
        }
        if (Double.isNaN(vector.getY())) {
            vector.setY(0);
        }
        if (Double.isNaN(vector.getZ())) {
            vector.setZ(0);
        }
    }

    public void run() {
        run(playerUuid);
    }

    public void run(UUID uuid) {
        PluginUtils.getInstance().runSync(() -> {
            cancelSync();
            PathPlayer pathPlayer = PathPlayerHandler.getInstance().getPlayer(uuid);
            if (pathPlayer == null) {
                return;
            }
            this.visualizer = pathPlayer.getVisualizer(roadMap);

            this.active = true;

            int steps = visualizer.getParticleSteps();
            ParticleEffect effect = ParticleEffect.valueOf(visualizer.getParticle().name());
            int period = visualizer.getSchedulerPeriod();
            World world = roadMap.getWorld();

            if (visualizer.getParticleDistance() != cachedDistance) {
                calculate();
            }

            final SchedulerHandler schedulerHandler = new SchedulerHandler(new Date().getTime());
            schedulerHandlers.add(schedulerHandler);
            for (int i = 0; i < steps; i++) {
				final List<Object> packets = new ArrayList<>();
				ParticleBuilder particle = new ParticleBuilder(effect)
						.setColor(Color.RED);

				int moduloCount = 0;
				for (Vector vector : calculatedPoints) {
					if (moduloCount % steps == i) {
						packets.add(particle.setLocation(vector.toLocation(world)).toPacket());
					}
					moduloCount++;
				}
				Bukkit.getScheduler().runTaskLater(PathPlugin.getInstance(), () -> {
					if (schedulerHandler.isCancelled()) {
                        return;
                    }
                    int id = TaskManager.startSuppliedTask(packets, period * steps, () -> {
                        Player searching = Bukkit.getPlayer(uuid);
                        if(searching != null && searching.getWorld().equals(world)) {
                            return Lists.newArrayList(searching);
                        }
                        return new ArrayList<>();
                    });
                    schedulerHandler.getSchedulerIds().add(id);
                }, (long) i * period);
            }
        });
    }

    public void cancel() {
        PluginUtils.getInstance().runSync(this::cancelSync);
    }

    /**
     * Nur im Mainthread aufrufen
     */
    public void cancelSync() {
        List<SchedulerHandler> handlers = new ArrayList<>(schedulerHandlers);
        for(SchedulerHandler handler : handlers) {
            handler.setCancelled(true);
            for(int i : handler.getSchedulerIds()) {
                Bukkit.getScheduler().cancelTask(i);
            }
            schedulerHandlers.remove(handler);
        }
        this.active = false;
    }
}
