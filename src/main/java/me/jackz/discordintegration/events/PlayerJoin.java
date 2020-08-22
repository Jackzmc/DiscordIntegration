package me.jackz.discordintegration.events;

import discord4j.core.GatewayDiscordClient;
import me.jackz.discordintegration.DiscordIntegration;
import me.jackz.discordintegration.UserReg;
import me.jackz.discordintegration.discord.Bot;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.Set;
import java.util.UUID;

public class PlayerJoin implements Listener {
    private DiscordIntegration plugin;
    private UserReg userReg;

    private Server server;
    private boolean enabled;
    private String formatMessage;

    public PlayerJoin(DiscordIntegration plugin) {
        this.plugin = plugin;
        this.userReg = plugin.getUserReg();
        server = plugin.getServer();
        reload();
    }

    public void reload() {
        enabled = plugin.getConfig().getBoolean("whitelist.enabled");
        if(enabled) {
            plugin.getServer().setWhitelist(false);
        }
        formatMessage = plugin.getConfig().getString("whitelist.format");
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPreConnect(AsyncPlayerPreLoginEvent event) {
        if(enabled) {
            UUID player = event.getUniqueId();
            //If player is not whitelisted: Check if they are registered, then check their role status.
            plugin.getLogger().info("UUID: " + player + " is connecting. Result: " + event.getLoginResult());
            if (!server.getWhitelistedPlayers().contains(Bukkit.getOfflinePlayer(player))) {
                plugin.getLogger().info("UUID: " + player + " not whitelisted.");
                if (userReg.hasUser(player)) {
                    plugin.getLogger().info("UUID: " + player + " is registered");
                    if (userReg.isUserAuthorized(player)) {
                        plugin.getLogger().info("UUID: " + player + " is authorized");
                        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(player);
                        offlinePlayer.setWhitelisted(true);

                        event.allow();
                        return;
                    }
                }
                event.setLoginResult(AsyncPlayerPreLoginEvent.Result.KICK_WHITELIST);
                String message = formatMessage.replaceAll("/%player%/", event.getName());
                event.setKickMessage(message);
            }
        }
    }

}
