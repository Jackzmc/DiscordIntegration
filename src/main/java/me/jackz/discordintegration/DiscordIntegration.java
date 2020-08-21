package me.jackz.discordintegration;

import me.jackz.discordintegration.discord.Bot;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public final class DiscordIntegration extends JavaPlugin {
    private DiscordIntegration plugin;
    private YamlConfiguration config;
    private final File configFile = new File(getDataFolder(), "config.yml");

    private Bot bot;

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

        bot = new Bot(plugin);
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
    }

    @Override
    public YamlConfiguration getConfig() {
        return config;
    }

    public Plugin getInstance() {
        return plugin;
    }
}
