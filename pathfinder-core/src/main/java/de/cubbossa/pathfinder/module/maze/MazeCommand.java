package de.cubbossa.pathfinder.module.maze;

import de.cubbossa.pathfinder.core.commands.CustomArgs;
import de.cubbossa.pathfinder.core.roadmap.RoadMap;
import dev.jorel.commandapi.CommandTree;

public class MazeCommand extends CommandTree {

  public MazeCommand() {
    super("maze");
    then(CustomArgs.literal("generate")
        .then(CustomArgs.integer("x")
            .then(CustomArgs.integer("y")
                .then(CustomArgs.roadMapArgument("roadmap")
                    .executesPlayer((player, objects) -> {
                      Maze maze = new Maze(objects.<Integer>getUnchecked(0), objects.<Integer>getUnchecked(1));
                      new MazeConverter(maze)
                          .convertMaze(new SimpleMazePattern(), player.getLocation())
                          .convertMaze(new RoadMapMazePattern(objects.<RoadMap>getUnchecked(2)),
                              player.getLocation());
                    })))));
  }
}
