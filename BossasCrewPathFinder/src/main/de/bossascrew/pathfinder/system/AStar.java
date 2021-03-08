package main.de.bossascrew.pathfinder.system;

import java.util.PriorityQueue;
import java.util.HashSet;
import java.util.Set;
import java.util.List;
import java.util.Comparator;
import java.util.ArrayList;
import java.util.Collections;

import org.bukkit.Location;

import main.de.bossascrew.pathfinder.PathSystem;
import main.de.bossascrew.pathfinder.RoadMap;

public class AStar {

	public List<Node> AStaraufruf(RoadMap rm, Location pLoc, Node zielnode, Node...startnodes) {

		List<Node> nodes = new ArrayList<Node>(rm.getFile().getWaypoints(pLoc));
		if(startnodes != null && startnodes.length > 0) {
			nodes = new ArrayList<Node>();
			for(Node n : startnodes) {
				nodes.add(n);
			}
		};
		
		Node nearestNode = nodes.get(0);
		double distance = pLoc.toVector().distance(nearestNode.loc);
		for (Node n : nodes) {
			double distTemp = pLoc.toVector().distance(n.loc);
			if (distTemp < distance) {
				distance = distTemp;
				nearestNode = n;
			}
		}
		Node playerNode = new Node(0, PathSystem.PLAYER_NODE, 0, pLoc.toVector());
		playerNode.adjacencies = new Edge[] { new Edge(nearestNode, distance) };

		AstarSearch(playerNode, zielnode);
		return printPath(zielnode);
	}

	public List<Node> printPath(Node target) {
		List<Node> path = new ArrayList<Node>();

		for (Node node = target; node != null; node = node.parent) {
			path.add(node);
		}

		Collections.reverse(path);
		return path;
	}

	public void AstarSearch(Node source, Node goal) {

		Set<Node> explored = new HashSet<Node>();

		PriorityQueue<Node> queue = new PriorityQueue<Node>(20, new Comparator<Node>() {
			@Override
			public int compare(Node i, Node j) {
				if (i.f_scores > j.f_scores) {
					return 1;
				}

				else if (i.f_scores < j.f_scores) {
					return -1;
				}

				else {
					return 0;
				}
			}
		});

		// cost from start
		source.g_scores = 0;

		queue.add(source);

		boolean found = false;

		while ((!queue.isEmpty()) && (!found)) {

			// the node in having the lowest f_score value
			Node current = queue.poll();

			explored.add(current);

			// goal found
			if (current.id == goal.id) {
				found = true;
			}

			// check every child of current node
			for (Edge e : current.adjacencies) {
				Node child = e.target;
				double cost = e.cost;
				double temp_g_scores = current.g_scores + cost;
				double temp_f_scores = temp_g_scores + child.h_scores;

				/*
				 * if child node has been evaluated and the newer f_score is higher, skip
				 */

				if ((explored.contains(child)) && (temp_f_scores >= child.f_scores)) {
					continue;
				}

				/*
				 * else if child node is not in queue or newer f_score is lower
				 */

				else if ((!queue.contains(child)) || (temp_f_scores < child.f_scores)) {

					child.parent = current;
					child.g_scores = temp_g_scores;
					child.f_scores = temp_f_scores;

					if (queue.contains(child)) {
						queue.remove(child);
					}

					queue.add(child);

				}
			}
		}
	}
}
