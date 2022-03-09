package net.elytrapvp.elytrachat;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

/**
 * Manages the configurable settings in the plugin.
 */
public class SettingsManager {
    private FileConfiguration config;
    private FileConfiguration formats;
    private final File configFile;
    private final File formatsFile;

    /**
     * Loads or Creates configuration files.
     * @param plugin Instance of the plugin.
     */
    public SettingsManager(ElytraChat plugin) {
        config = plugin.getConfig();
        config.options().copyDefaults(true);
        configFile = new File(plugin.getDataFolder(), "config.yml");
        plugin.saveConfig();

        formatsFile = new File(plugin.getDataFolder(), "formats.yml");
        if(!formatsFile.exists()) {
            plugin.saveResource("formats.yml", false);
        }
        formats = YamlConfiguration.loadConfiguration(formatsFile);
    }

    /**
     * Get the config.yml FileConfiguration.
     * @return config.yml FileConfiguration.
     */
    public FileConfiguration getConfig() {
        return config;
    }

    /**
     * Get the formats.yml FileConfiguration.
     * @return formats.yml FileConfiguration.
     */
    public FileConfiguration getFormats() {
        return formats;
    }

    /**
     * Update the configuration files.
     */
    public void reload() {
        config = YamlConfiguration.loadConfiguration(configFile);
        formats = YamlConfiguration.loadConfiguration(formatsFile);
    }
}