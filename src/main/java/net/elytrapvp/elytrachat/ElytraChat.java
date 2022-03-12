package net.elytrapvp.elytrachat;

import net.elytrapvp.elytrachat.listeners.AsyncPlayerChatListener;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public final class ElytraChat extends JavaPlugin {
    private SettingsManager settingsManager;
    private MySQL mySQL;

    @Override
    public void onEnable() {
        // Plugin startup logic
        settingsManager = new SettingsManager(this);

        // Connects to the mysql database.
        mySQL = new MySQL(this);
        // Connection is opened async.
        Bukkit.getScheduler().runTaskAsynchronously(this, () -> mySQL.openConnection());

        // We need to tell Spigot that our listeners exist for them to work.
        Bukkit.getPluginManager().registerEvents(new AsyncPlayerChatListener(this), this);
    }

    /**
     * Be able to connect to MySQL.
     * @return MySQL.
     */
    public MySQL getMySQL() {
        return mySQL;
    }

    /**
     * Get the Settings Manager, which gives us access to the plugin Configuration.
     * @return Settings Manager.
     */
    public SettingsManager getSettingsManager() {
        return settingsManager;
    }
}
