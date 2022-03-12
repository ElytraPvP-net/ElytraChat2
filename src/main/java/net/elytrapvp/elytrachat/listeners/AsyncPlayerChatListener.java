package net.elytrapvp.elytrachat.listeners;

import net.elytrapvp.elytrachat.ElytraChat;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class AsyncPlayerChatListener implements Listener {
    private final ElytraChat plugin;

    public AsyncPlayerChatListener(ElytraChat plugin) {
        this.plugin = plugin;
    }


    @EventHandler(priority =  EventPriority.HIGHEST)
    public void onChat(AsyncPlayerChatEvent event) {
        // Makes sure the event wasn't cancelled by another plugin.
        if(event.isCancelled()) {
            return;
        }

        Player player = event.getPlayer();

        // Logs the message to MySQL.
        String name = player.getName();
        String uuid = player.getUniqueId().toString();
        String server = plugin.getSettingsManager().getConfig().getString("server");
        String channel = "global";

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                PreparedStatement statement = plugin.getMySQL().getConnection().prepareStatement("INSERT INTO chat_logs (server,channel,uuid,username,message) VALUES (?,?,?,?,?)");
                statement.setString(1, server);
                statement.setString(2, channel);
                statement.setString(3, uuid);
                statement.setString(4, name);
                statement.setString(5, event.getMessage());
                statement.executeUpdate();
            }
            catch (SQLException exception) {
                exception.printStackTrace();
            }
        });
    }

}
