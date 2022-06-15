package de.bossascrew.pathfinder.node;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class Edge {

	private Node start;
	private Node end;
}
