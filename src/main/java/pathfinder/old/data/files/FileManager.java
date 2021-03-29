package pathfinder.old.data.files;

import java.io.File;
import java.io.IOException;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import main.de.bossascrew.pathfinder.PathSystem;

public class FileManager {

	String path;
	String fileResource;
	File file;
	FileConfiguration cfg;

	public FileManager(String path, String fileName) {
		this.path = path;
		this.file = new File(path, fileName);
		loadCfg();
		if(file != null)
			setup();
	}
	
	public void loadCfg() {
		this.cfg = new YamlConfiguration();
		try {
			cfg.load(file);
		} catch (IOException | InvalidConfigurationException e) {
			e.printStackTrace();
		}
	}
	
	public FileManager(String path, String fileName, String fileResource) {
		this.path = path;
		this.fileResource = fileResource;
		this.file = new File(path, fileName);
		this.cfg = new YamlConfiguration();
		try {
			cfg.load(file);
		} catch (IOException | InvalidConfigurationException e) {
			e.printStackTrace();
		}

		if(file != null)
			setup();
		else
			System.out.println("File == null");
	}
	
	void setup() {
		if(!file.exists()) {
			PathSystem.getInstance().printToConsole("Kein File gefunden: Starte Setup!");
			File file = new File(path);
			file.mkdirs();
			if(fileResource != null) {
				PathSystem.getInstance().printToConsole("File wird aufgebaut");
				PathSystem.getInstance().saveResource(fileResource, true);
						        
				PathSystem.getInstance().printToConsole("File gefunden: �c" + file.exists());
			} else {
				try {
					this.file.createNewFile();
				} catch (IOException e) {
					PathSystem.getInstance().printToConsole("�cFilemanager konnte File nicht erstellen!");
					e.printStackTrace();
				}
			}
			
		} else {
			PathSystem.getInstance().printToConsole("FileManager Setup nicht gestartet, File existiert bereits: " + file.getName());
		}
	}
	
	void save() {
		try {
			cfg.save(file);
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	public FileConfiguration getCfg() {
		return this.cfg;
	}
}
