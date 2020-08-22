package me.jackz.discordintegration.events;

import discord4j.core.GatewayDiscordClient;
import me.jackz.discordintegration.DiscordIntegration;
import me.jackz.discordintegration.UserReg;
import me.jackz.discordintegration.discord.Bot;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.UUID;

public class PlayerJoin implements Listener {
    private DiscordIntegration plugin;
    private UserReg userReg;

    public PlayerJoin(DiscordIntegration plugin) {
        this.plugin = plugin;
        this.userReg = plugin.getUserReg();
    }

    @EventHandler
    public void onConnect(AsyncPlayerPreLoginEvent event) {
        UUID player = event.getUniqueId();
        //If player is not whitelisted: Check if they are registered, then check their role status.
        if(event.getLoginResult().equals(AsyncPlayerPreLoginEvent.Result.KICK_WHITELIST)) {
            if(userReg.hasUser(player)) {
                if(userReg.isUserAuthorized(player)) {
                    OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(player);
                    offlinePlayer.setWhitelisted(true);

                    event.allow();
                }
            }
        }
    }

}
