package me.stella.plugin;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

public class GhostOptions {

    private final String absolute;

    private File file;
    private FileConfiguration config;

    public GhostOptions(File configFile) {
        this.absolute = configFile.getAbsoluteFile().getAbsolutePath();
        reload();
    }

    public void reload() {
        this.file = new File(this.absolute).getAbsoluteFile();
        this.config = YamlConfiguration.loadConfiguration(this.file);
    }

    public File getConfigFile() {
        return this.file;
    }

    public FileConfiguration getConfig() {
        return this.config;
    }

}
