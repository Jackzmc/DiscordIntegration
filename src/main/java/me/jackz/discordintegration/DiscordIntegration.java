package me.jackz.discordintegration;

import discord4j.core.GatewayDiscordClient;
import me.jackz.discordintegration.discord.Bot;
import me.jackz.discordintegration.events.PlayerJoin;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;

public final class DiscordIntegration extends JavaPlugin {
    private DiscordIntegration plugin;
    private YamlConfiguration config;
    private final File configFile = new File(getDataFolder(), "config.yml");

    private Bot bot;
    private UserReg userReg;

    @Override
    public void onEnable() {
        // Plugin startup logic
        if(!getDataFolder().exists()) {
            getDataFolder().mkdir();
            if(!configFile.exists())
                saveResource("config.yml", false);
        }
        config = YamlConfiguration.loadConfiguration(configFile);
        plugin = this;

        userReg = new UserReg(plugin);
        bot = new Bot(plugin);

        this.getServer().getPluginManager().registerEvents(new PlayerJoin(this), this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        config = null;
        plugin = null;
        if(bot != null) {
            try {
                bot.getClient().logout();
                bot = null;
            } catch (Exception ex) {
                plugin.getLogger().warning("Exception during logout: " + ex.getMessage());
            }
        }
        if(userReg != null) {
            try {
                userReg.save();
            } catch (IOException e) {
                e.printStackTrace();
            }
            userReg = null;
        }
    }

    @Override
    public YamlConfiguration getConfig() {
        return config;
    }

    public Plugin getInstance() {
        return plugin;
    }

    public UserReg getUserReg() {
        return userReg;
    }
    public Bot getDiscordBot() {
        return bot;
    }
}
