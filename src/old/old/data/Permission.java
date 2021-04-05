package de.bossascrew.pathfinder.old.data;

public class Permission {

	public static final String SET_WAYPOINT = "pathsystem.setup.waypoint.set";
	public static final String REMOVE_WAYPOINT = "pathsystem.setup.waypoint.remove";
	public static final String LIST_WAYPOINTS = "pathsystem.setup.waypoint.list";
	
	public static final String EDGE_CREATE = "pathsystem.setup.edge.set";
	public static final String EDGE_REMOVE = "pathsystem.setup.edge.remove";
	
	public static final String NAVIGATE_COMMAND = "pathsystem.command.navigate.*";
	public static final String NAVIGATE_COMMAND_SUBSTRING_BASE = "pathsystem.command.navigate.";
	public static final String NAVIGATE_COMMAND_SUBSTRING_POINT = ".target.";
	
	public static final String CANCEL_PATH_GENERAL = "pathsystem.command.cancelpath";
	public static final String CANCEL_PATH_SUBSTRING = "pathsystem.command.cancelpath.roadmap.";
	public static final String CANCEL_PATH_OTHER = "pathsystem.command.cancelpath.other";
	

}
