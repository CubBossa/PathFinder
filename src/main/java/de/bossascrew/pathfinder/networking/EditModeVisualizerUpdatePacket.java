package de.bossascrew.pathfinder.networking;

import de.bossascrew.core.networking.packets.Packet;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.Particle;

@Getter
@RequiredArgsConstructor
public class EditModeVisualizerUpdatePacket extends Packet {

	private final int databaseId;
	private final int parentId;
	private final int[] children;
	private final String name;
	private final Particle particle;
	private final double particleDistance;
	private final int particleLimit;
	private final int particlePeriod;
	private final int edgeHeadId;
	private final int nodeHeadId;
}
