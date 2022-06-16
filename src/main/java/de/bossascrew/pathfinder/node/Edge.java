package de.bossascrew.pathfinder.node;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

@Getter
@Setter
@AllArgsConstructor
public class Edge implements Comparable<Edge> {

	private Node start;
	private Node end;
	private float weightModifier;

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof Edge edge)) {
			return false;
		}

		if (Float.compare(edge.weightModifier, weightModifier) != 0) {
			return false;
		}
		if (!Objects.equals(start, edge.start)) {
			return false;
		}
		return Objects.equals(end, edge.end);
	}

	@Override
	public int hashCode() {
		int result = start != null ? start.hashCode() : 0;
		result = 31 * result + (end != null ? end.hashCode() : 0);
		result = 31 * result + (weightModifier != +0.0f ? Float.floatToIntBits(weightModifier) : 0);
		return result;
	}

	@Override
	public int compareTo(@NotNull Edge o) {
		int sA = start.getNodeId();
		int eA = end.getNodeId();
		int sB = o.getStart().getNodeId();
		int eB = o.getEnd().getNodeId();
		int compareStart = Integer.compare(sA, sB);
		return compareStart == 0 ? Integer.compare(eA, eB) : compareStart;
	}
}
