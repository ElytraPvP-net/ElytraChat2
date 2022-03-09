package net.elytrapvp.elytrachat;

import org.bukkit.plugin.java.JavaPlugin;

public final class ElytraChat extends JavaPlugin {
    private SettingsManager settingsManager;

    @Override
    public void onEnable() {
        // Plugin startup logic
        settingsManager = new SettingsManager(this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public SettingsManager getSettingsManager() {
        return settingsManager;
    }
}
