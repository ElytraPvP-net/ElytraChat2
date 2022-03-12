package net.elytrapvp.elytrachat.listeners;

import net.elytrapvp.elytrachat.ElytraChat;
import net.elytrapvp.elytrachat.utils.chat.ChatUtils;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
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

        // Cancels the event so that the message does not get sent to general chat.
        event.setCancelled(true);

        // Sets the default format to "default".
        String format = "default";

        // Grabs the list of available formats from config.yml
        ConfigurationSection formats = plugin.getSettingsManager().getFormats().getConfigurationSection("formats");

        // Loops through all the available formats.
        for(String str : formats.getKeys(false)) {
            // Checks if the player has permission for that format.
            if(player.hasPermission("format." + str)) {
                // If so, sets the format to that.
                format = str;

                // Breaks out of the loop to prevent it from reassigning a format.
                break;
            }
        }

        // Creates the message to be sent.
        String newMessage = "";

        // Gets the selected format from the list of formats.
        ConfigurationSection formatText = plugin.getSettingsManager().getFormats().getConfigurationSection("formats." + format);

        // Loop through the format to add the text together.
        for(String str : formatText.getKeys(false)) {
            // Add the result to the new message.
            newMessage += formatText.getString(str);
        }

        // Replace the placeholders with their intended text.
        newMessage = newMessage
                .replace("%player_name%", player.getName())
                .replace("%message%", event.getMessage());

        for(Player viewer : Bukkit.getOnlinePlayers()) {
            ChatUtils.chat(viewer, newMessage);
        }

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