package de.bossascrew.pathfinder.module.maze;

import de.bossascrew.pathfinder.core.commands.argument.CustomArgs;
import de.bossascrew.pathfinder.core.roadmap.RoadMap;
import dev.jorel.commandapi.CommandTree;
import dev.jorel.commandapi.arguments.IntegerArgument;
import dev.jorel.commandapi.arguments.LiteralArgument;

public class MazeCommand extends CommandTree {

	public MazeCommand() {
		super("maze");
		then(new LiteralArgument("generate")
				.then(new IntegerArgument("x")
						.then(new IntegerArgument("y")
								.then(CustomArgs.roadMapArgument("roadmap")
										.executesPlayer((player, objects) -> {
											Maze maze = new Maze((int) objects[0], (int) objects[1]);
											new MazeConverter(maze)
													.convertMaze(new SimpleMazePattern(), player.getLocation())
													.convertMaze(new RoadMapMazePattern((RoadMap) objects[2]), player.getLocation());
										})))));
	}
}
