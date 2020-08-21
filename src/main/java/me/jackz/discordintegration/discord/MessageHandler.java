package me.jackz.discordintegration.discord;

import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;
import jdk.nashorn.internal.runtime.regexp.RegExp;
import me.jackz.discordintegration.DiscordIntegration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MessageHandler {
    private final DiscordIntegration plugin;
    private final GatewayDiscordClient client;
    private YamlConfiguration config;

    private List<String> registrationChannels = new ArrayList<>();

    public MessageHandler(DiscordIntegration plugin, GatewayDiscordClient client) {
        this.plugin = plugin;
        this.client = client;
        this.config = plugin.getConfig();
        reload();
    }
    public void handle(Message message) {
        String content = message.getContent();
        String channelID = message.getChannelId().asString();
        if(registrationChannels.contains(channelID)) {
            String[] arguments = content.split("\\s");
            if(arguments.length > 0) {
                String command = arguments[0].substring(1);
                String[] args;
                args = arguments.length > 1 ? Arrays.copyOfRange(arguments, 1, arguments.length) : new String[0];
                processCommand(message, command, args);
            }
        }
    }
    private void processCommand(Message message, String command, String[] args) {
        plugin.getLogger().info("command=" + command + "|args=" + Arrays.toString(args) + "|chn=" + message.getChannelId().asString());
        switch(command.toLowerCase()) {
            case "register": {
                if(args.length > 0) {
                    //todo: register user
                    message.getChannel().block().createMessage("Registered " + args[0] + " for your discord account.").subscribe();
                }else{

                    message.getChannel().block().createMessage("Please enter in your minecraft username.").subscribe();
                }
                break;
            }
        }
    }

    public void reload() {
        registrationChannels = config.getStringList("discord.allow_registration_channels");
    }
}
