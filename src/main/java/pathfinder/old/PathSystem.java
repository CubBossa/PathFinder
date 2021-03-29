package pathfinder.old;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.java.JavaPlugin;

import pathfinder.old.commands.CancelPathCommand;
import pathfinder.old.commands.NavigateCommand;
import pathfinder.old.commands.PathSystemCommand;
import pathfinder.old.commands.PathSystemCommandCombined;
import pathfinder.old.data.files.FileConfig;
import pathfinder.old.listener.ArmorstandInteractListener;
import pathfinder.old.listener.EntityDamageListener;
import pathfinder.old.listener.InventoryClickListener;
import pathfinder.old.listener.PlayerMoveListener;
import pathfinder.old.visualization.VisualizerEditMode;

public class PathSystem extends JavaPlugin {

	public static final String PLAYER_NODE = "player";
	
	private static PathSystem INSTANCE;
	private FileConfig fileConfig;
	
	@Override
	public void onEnable() {
		INSTANCE = this;
		
		fileConfig = new FileConfig(getDataFolder().getPath(), "config.yml", "config.yml");
		
		NavigateCommand cmd = new NavigateCommand();
		getCommand("navigate").setExecutor(cmd);
		
		PathSystemCommand admincmd = new PathSystemCommand();
		getCommand("pathsystem").setExecutor(admincmd);

		CancelPathCommand cancelcmd = new CancelPathCommand();
		getCommand("cancelpath").setExecutor(cancelcmd);
		
		PathSystemCommandCombined admincmdnew = new PathSystemCommandCombined();
		admincmdnew.registerSubCommands();
		getCommand("pathsystemnew").setExecutor(admincmdnew);

		Bukkit.getServer().getPluginManager().registerEvents(new InventoryClickListener(), this);
		Bukkit.getServer().getPluginManager().registerEvents(new PlayerMoveListener(), this);
		Bukkit.getServer().getPluginManager().registerEvents(new ArmorstandInteractListener(), this);
		Bukkit.getServer().getPluginManager().registerEvents(new EntityDamageListener(), this);
		
		RoadMap.loadRoadmaps();
	}
	
	@Override
	public void onDisable() {
		for(RoadMap rm : RoadMap.getRoadMaps()) {
			rm.save();
			for(Entity e : rm.getWorld().getEntities()) {
				if(e.getName().equals(VisualizerEditMode.WAYPOINT_NAME) || e.getName().equals(VisualizerEditMode.EDGE_NAME))
					e.remove();
			}
		}
		
	}
	
	public void printToConsole(String message) {
		Bukkit.getConsoleSender().sendMessage("�7[�4�lPathSystem�7] �f" + message);
	}
	
	public static PathSystem getInstance() {
		return INSTANCE;
	}
	
	public FileConfig getConfigFile() {
		return fileConfig;
	}
}
