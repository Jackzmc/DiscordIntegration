package me.jackz.discordintegration.discord;

import discord4j.core.DiscordClientBuilder;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.lifecycle.ReadyEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.event.domain.message.MessageEvent;
import discord4j.core.object.entity.Message;
import me.jackz.discordintegration.DiscordIntegration;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;

public class Bot {
    private DiscordIntegration plugin;
    private YamlConfiguration config;
    private GatewayDiscordClient client;
    private MessageHandler messageHandler;

    public static String PREFIX;

    public Bot(DiscordIntegration plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfig();

        String token = config.getString("discord.bot_token");
        if(token != null) {
            client = DiscordClientBuilder.create(token).build().login().block();
            messageHandler = new MessageHandler(plugin, client);
            reload();
            registerEvents();
        }else{
            plugin.getLogger().warning("Config value discord.bot_token missing: plugin disabled.");
            plugin.getServer().getPluginManager().disablePlugin(plugin);
        }

    }

    private void registerEvents() {
        client.on(ReadyEvent.class).subscribe(event -> {
            plugin.getLogger().info("Bot is now ready. Username=" + event.getSelf().getUsername() + " | Prefix:" + PREFIX);
        });
        client.on(MessageCreateEvent.class)
            .map(MessageCreateEvent::getMessage)
            .filter(message -> message.getAuthor().map(user -> !user.isBot()).orElse(false))
            .filter(message -> message.getContent().startsWith(PREFIX))
            .subscribe(event -> messageHandler.handle(event));
    }

    public void reload() {
        PREFIX = config.getString("discord.prefix", "!");
    }

    public GatewayDiscordClient getClient() {
        return client;
    }
}
