package net.elytrapvp.elytrachat.listeners;

import me.clip.placeholderapi.PlaceholderAPI;
import net.elytrapvp.elytrachat.ElytraChat;
import net.elytrapvp.elytrachat.utils.chat.ChatUtils;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * This listens to the AsyncPlayerChatEvent event, which is called every time a player send a chat message.
 * We use this to grab the chat message being sent and format it properly depending on permissions.
 */
public class AsyncPlayerChatListener implements Listener {
    private final ElytraChat plugin;

    /**
     * To be able to access the configuration files, we need to pass an instance of the plugin to our listener.
     * This is known as Dependency Injection.
     * @param plugin Instance of the plugin.
     */
    public AsyncPlayerChatListener(ElytraChat plugin) {
        this.plugin = plugin;
    }


    /**
     * Runs when the event is called.
     * @param event AsyncPlayerChatEvent.
     */
    @EventHandler(priority =  EventPriority.HIGHEST)
    public void onChat(AsyncPlayerChatEvent event) {
        // Makes sure the event wasn't cancelled by another plugin.
        if(event.isCancelled()) {
            return;
        }

        Player player = event.getPlayer();

        // Cancels the event so that the message does not get sent to general chat.
        event.setCancelled(true);

        String chatFormat = getFormat(player);
        String chatMessage = event.getMessage();

        // Remove color codes if the player doesn't have permission to use them.
        if(!player.hasPermission("elytrachat.color")) {
            chatMessage = ChatColor.stripColor(ChatUtils.translate(chatMessage));
        }

        String finalMessage = PlaceholderAPI.setPlaceholders(player, chatFormat.replace("%message%", chatMessage));

        String toFilter = chatMessage.toLowerCase()
                .replace(" ", "")
                .replace("-", "")
                .replace("_", "")
                .replace("~", "")
                .replace("/", "")
                .replace("\\", "")
                .replace("=", "")
                .replace("+", "")
                .replace(".", "")
                .replace("`", "")
                .replace("&", "")
                .replace("$", "")
                .replace("%", "")
                .replace("*", "")
                .replace("(", "")
                .replace(")", "");

        // Check filter
        for(String filter : plugin.getSettingsManager().getConfig().getStringList("filter")) {
            if(toFilter.matches(filter)) {
                System.out.println("(filter) " + finalMessage);
                logMessage(player, "filter: " + chatMessage);

                // Send normal message to player.
                ChatUtils.chat(player, finalMessage);

                // Loop through staff.
                for(Player staff : Bukkit.getOnlinePlayers()) {
                    if(!staff.hasPermission("staff.filter")) {
                        continue;
                    }

                    // Send filtered message to staff.
                    ChatUtils.chat(staff, "&c(filter) " + finalMessage);
                }
                return;
            }
        }

        // Log message to the console.
        System.out.println(ChatColor.stripColor(finalMessage));

        // Send message to all online players.
        for(Player viewer : Bukkit.getOnlinePlayers()) {
            ChatUtils.chat(viewer, finalMessage);
        }

        // Log the message to MySQL.
        logMessage(player, chatMessage);
    }

    /**
     * Get the message format of a player.
     * @param player Player to get format of.
     * @return Message format with placeholders.
     */
    private String getFormat(Player player) {
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
        String chatFormat = "";

        // Gets the selected format from the list of formats.
        ConfigurationSection formatText = plugin.getSettingsManager().getFormats().getConfigurationSection("formats." + format);

        // Loop through the format to add the text together.
        for(String str : formatText.getKeys(false)) {
            // Add the result to the new message.
            chatFormat += formatText.getString(str);
        }

        return chatFormat;
    }

    /**
     * Log the chat message to MySQL
     * @param player Player who sent the message.
     * @param message Message they sent.
     */
    private void logMessage(Player player, String message) {
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
                statement.setString(5, message);
                statement.executeUpdate();
            }
            catch (SQLException exception) {
                exception.printStackTrace();
            }
        });
    }
}