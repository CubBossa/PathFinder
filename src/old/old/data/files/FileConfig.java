package de.bossascrew.pathfinder.old.data.files;

import org.bukkit.Particle;

public class FileConfig extends FileManager {

    //edit view
    Particle type;
    int amount;
    double distance;
    int frequence;
    double particleViewDistance;

    public FileConfig(String path, String fileName, String fileResource) {
        super(path, fileName, fileResource);

        type = Particle.FLAME;
        amount = 1;
        distance = 0.3;
        frequence = 10;
        particleViewDistance = 50;
        load();
    }

    private void load() {
        if (cfg.isSet("editmode.particles.type")) {
            type = Particle.valueOf(cfg.getString("editmode.particles.type"));
        } else {
            cfg.set("editmode.particles.type", type.toString());
        }

        if (cfg.isSet("editmode.particles.amount")) {
            amount = cfg.getInt("editmode.particles.amount");
        } else {
            cfg.set("editmode.particles.amount", amount);
        }

        if (cfg.isSet("editmode.particles.distance")) {
            distance = cfg.getDouble("editmode.particles.distance");
        } else {
            cfg.set("editmode.particles.distance", distance);
        }

        if (cfg.isSet("editmode.particles.frequence")) {
            frequence = cfg.getInt("editmode.particles.frequence");
        } else {
            cfg.set("editmode.particles.frequence", frequence);
        }

        if (cfg.isSet("editmode.particles.viewdistance")) {
            particleViewDistance = cfg.getDouble("editmode.particles.viewdistance");
        } else {
            cfg.set("editmode.particles.viewdistance", particleViewDistance);
        }
    }

    public void saveToFile() {
        cfg.set("editmode.particles.type", type.toString());
        cfg.set("editmode.particles.amount", amount);
        cfg.set("editmode.particles.distance", distance);
        cfg.set("editmode.particles.frequence", frequence);
        cfg.set("editmode.particles.viewdistance", particleViewDistance);
        save();
    }

    public Particle getType() {
        return type;
    }

    public int getAmount() {
        return amount;
    }

    public double getDistance() {
        return distance;
    }

    public int getFrequence() {
        return frequence;
    }

    public double getParticleViewDistance() {
        return particleViewDistance;
    }
}
