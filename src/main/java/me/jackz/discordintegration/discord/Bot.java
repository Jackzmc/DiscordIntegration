package me.jackz.discordintegration.discord;

import discord4j.common.util.Snowflake;
import discord4j.core.DiscordClientBuilder;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.lifecycle.ReadyEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.event.domain.message.MessageEvent;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.Channel;
import discord4j.core.object.entity.channel.GuildChannel;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.object.entity.channel.TextChannel;
import me.jackz.discordintegration.DiscordIntegration;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;

import java.util.ArrayList;
import java.util.List;

public class Bot {
    private DiscordIntegration plugin;
    private YamlConfiguration config;
    private GatewayDiscordClient client;
    private MessageHandler messageHandler;

    public static String PREFIX;
    private List<String> registrationChannels = new ArrayList<>();
    private Guild guild;

    public Bot(DiscordIntegration plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfig();

        String token = config.getString("discord.bot_token");
        if(token != null) {
            reload();
            if(registrationChannels.size() > 0) {
                client = DiscordClientBuilder.create(token).build().login().block();
                messageHandler = new MessageHandler(plugin, this);
                registerEvents();

                Snowflake channelSnowflake = Snowflake.of(registrationChannels.get(0));
                GuildChannel firstChannel = (GuildChannel) client.getChannelById(channelSnowflake).subscribe();
                guild = firstChannel.getGuild().block();
            }else{
                plugin.getLogger().warning("No registration channels were provided.");
            }
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
        registrationChannels = config.getStringList("discord.allow_registration_channels");
    }

    public GatewayDiscordClient getClient() {
        return client;
    }

    public List<String> getRegistrationChannels() {
        return registrationChannels;
    }

    public Guild getGuild() {
        return guild;
    }
}
