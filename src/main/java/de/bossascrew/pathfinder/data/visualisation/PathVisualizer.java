package de.bossascrew.pathfinder.data.visualisation;

import de.bossascrew.core.util.ComponentUtils;
import de.bossascrew.core.util.PluginUtils;
import de.bossascrew.pathfinder.data.DatabaseModel;
import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;

import javax.annotation.Nullable;

/**
 * Visualisiert einen Pfad aus Locations mit Partikeln
 */
@Getter
@Setter
public class PathVisualizer extends Visualizer<PathVisualizer> {

    private Integer particleSteps = null;

	/**
	 * Ob der Visualizer als Style für eine Roadmap eingesetzt werden kann
	 */
	private boolean pickable = false;
	private @Nullable
	String pickPermission = null;
	private @Nullable
	String displayName = null;
	private @Nullable
	Material iconType = Material.NAME_TAG;

    public PathVisualizer(int databaseId, String name, @Nullable Integer parentId) {
        super(databaseId, name, parentId);
    }

    public void createPickable(@Nullable String permission, @Nullable String miniDisplayName, @Nullable Material iconType) {
		DatabaseModel.getInstance().newVisualizerStyle(this, permission, iconType, miniDisplayName);
		setupPickable(permission, miniDisplayName, iconType, false);
	}

	public void setupPickable(@Nullable String permission, @Nullable String displayName, @Nullable Material iconType, boolean updateDatabase) {
		this.pickable = true;
		this.pickPermission = permission;
		this.displayName = displayName;
		this.iconType = iconType;
		if (updateDatabase) {
			DatabaseModel.getInstance().updateVisualizerStyle(this);
		}
	}

	public @Nullable
	String getStringDisplayName() {
		return displayName;
	}

	public Component getDisplayName() {
		return displayName != null ?
				Component.empty().decoration(TextDecoration.ITALIC, false)
						.append(ComponentUtils.parseMiniMessage(displayName)) :
				Component.text(getName(), NamedTextColor.GREEN).decoration(TextDecoration.ITALIC, false);
	}

	public void removePickable() {
		this.pickable = false;
		DatabaseModel.getInstance().deleteStyleVisualizer(this.getDatabaseId());
	}

	public Integer getParticleSteps() {
		if (particleSteps == null) {
			if (parent == null) {
				try {
                    throw new VisualizerParentException();
                } catch (VisualizerParentException e) {
                    e.printStackTrace();
                }
            }
            return parent.getParticleSteps();
        }
        return particleSteps;
    }

    public @Nullable
    Integer getUnsafeParticleSteps() {
        return particleSteps;
    }

    public void setAndSaveParticleSteps(int particleSteps) {
        if (particleSteps < 1) {
            particleSteps = 1;
        }
        if (particleSteps > 100) {
            particleSteps = 100;
        }
        this.particleSteps = particleSteps;
        saveData();
        callParticleStepsSubscribers(this);
    }

    private void callParticleStepsSubscribers(PathVisualizer vis) {
        vis.updateParticle.perform(null);
        for (PathVisualizer child : children) {
            if (child.getUnsafeParticle() != null) {
                continue;
            }
            child.updateParticle.perform(null);
            vis.callParticleStepsSubscribers(child);
        }
    }

    public void saveData() {
        PluginUtils.getInstance().runAsync(() -> DatabaseModel.getInstance().updatePathVisualizer(this));
    }
}
