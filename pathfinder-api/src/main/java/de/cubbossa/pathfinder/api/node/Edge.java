package de.cubbossa.pathfinder.api.node;

import java.util.UUID;

public interface Edge {

  UUID getStart();
  UUID getEnd();
  float getWeight();
}
