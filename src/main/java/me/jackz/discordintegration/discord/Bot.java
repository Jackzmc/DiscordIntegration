package me.jackz.discordintegration.discord;

import discord4j.common.util.Snowflake;
import discord4j.core.DiscordClientBuilder;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.guild.MemberUpdateEvent;
import discord4j.core.event.domain.lifecycle.ReadyEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.channel.GuildChannel;
import me.jackz.discordintegration.DiscordIntegration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.util.ArrayList;
import java.util.List;

public class Bot {
    private DiscordIntegration plugin;
    private YamlConfiguration config;
    private GatewayDiscordClient client;
    private MessageHandler messageHandler;
    private UpdateHandler updateHandler;

    public static String PREFIX;
    private List<String> registrationChannels = new ArrayList<>();
    private Guild guild;

    private boolean statusEnabled = true;
    private String statusFormat;
    private String statusType;

    public Bot(DiscordIntegration plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfig();

        String token = config.getString("discord.bot_token");
        if(token != null) {
            reload();
            if(registrationChannels.size() > 0) {
                client = DiscordClientBuilder.create(token).build().login().block();
                messageHandler = new MessageHandler(plugin, this);
                updateHandler = new UpdateHandler(plugin, this);
                registerEvents();

                Snowflake channelSnowflake = Snowflake.of(registrationChannels.get(0));
                GuildChannel firstChannel = (GuildChannel) client.getChannelById(channelSnowflake).block();
                if(firstChannel != null) {
                    guild = firstChannel.getGuild().block();
                }else{
                    plugin.getLogger().warning("No valid registration channels were provided.");
                }
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
        client.on(MemberUpdateEvent.class)
            .filter(event -> event.getGuildId().equals(guild.getId()))
            .subscribe(event -> updateHandler.process(event));
    }

    public void reload() {
        PREFIX = config.getString("discord.prefix", "!");
        registrationChannels = config.getStringList("discord.allow_registration_channels");

        messageHandler = null;
        updateHandler = null;

        statusEnabled = config.getBoolean("botstatus.enabled");
        statusType = config.getString("botstatus.type", "playing");
        statusFormat = config.getString("botstatus.format");
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
