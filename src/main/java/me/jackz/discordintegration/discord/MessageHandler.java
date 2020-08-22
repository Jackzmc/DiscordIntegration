package me.jackz.discordintegration.discord;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;
import jdk.nashorn.internal.runtime.regexp.RegExp;
import me.jackz.discordintegration.DiscordIntegration;
import me.jackz.discordintegration.UserReg;
import me.jackz.discordintegration.Util;
import org.bukkit.configuration.file.YamlConfiguration;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import sun.security.util.IOUtils;

import javax.xml.ws.Response;
import javax.xml.ws.http.HTTPException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class MessageHandler {
    private final DiscordIntegration plugin;
    private final GatewayDiscordClient client;
    private YamlConfiguration config;
    private UserReg userReg;
    private Bot bot;


    public MessageHandler(DiscordIntegration plugin, Bot bot) {
        this.plugin = plugin;
        this.client = bot.getClient();
        this.config = plugin.getConfig();
        this.bot = bot;
        this.userReg = plugin.getUserReg();
    }
    public void handle(Message message) {
        String content = message.getContent();
        String channelID = message.getChannelId().asString();
        if(bot.getRegistrationChannels().contains(channelID)) {
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
        switch(command.toLowerCase()) {
            case "register": {
                if(args.length > 0) {
                    //todo: get UUID from username.
                    try {
                        UUID uuid = Util.getUUID(args[0].trim());
                        if(uuid != null) {
                            userReg.addUser(uuid, message.getAuthor().get().getId());
                            message.getChannel().block().createMessage("Registered " + args[0] + " for your discord account.").subscribe();
                        }else{
                            message.getChannel().block().createMessage("Could not find any users with that username.").subscribe();
                        }
                    }catch(Exception ex) {
                        plugin.getLogger().warning("Fetching UUID for " + args[0] + " failed: " + ex.getMessage());
                        message.getChannel().block().createMessage("An error occurred while trying to fetch your UUID.").subscribe();
                    }
                }else{
                    message.getChannel().block().createMessage("Please enter in your minecraft username.").subscribe();
                }
                break;
            }
        }
    }



}
