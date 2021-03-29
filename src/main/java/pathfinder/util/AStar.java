package pathfinder.util;

import java.util.*;

public class AStar {

    public List<AStarNode> printPath(AStarNode target) {
        List<AStarNode> path = new ArrayList<AStarNode>();

        for (AStarNode node = target; node != null; node = node.parent) {
            path.add(node);
        }

        Collections.reverse(path);
        return path;
    }

    public void aStarSearch(AStarNode source, AStarNode goal) {

        Set<AStarNode> explored = new HashSet<AStarNode>();

        PriorityQueue<AStarNode> queue = new PriorityQueue<AStarNode>(20, new Comparator<AStarNode>() {
            @Override
            public int compare(AStarNode i, AStarNode j) {
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
            AStarNode current = queue.poll();

            explored.add(current);

            // goal found
            if (current.nodeId == goal.nodeId) {
                found = true;
            }

            // check every child of current node
            for (AStarEdge e : current.adjacencies) {
                AStarNode child = e.target;
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
