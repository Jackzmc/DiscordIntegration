package me.jackz.discordintegration;

import discord4j.common.util.Snowflake;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Role;
import me.jackz.discordintegration.discord.Bot;
import org.bukkit.configuration.file.YamlConfiguration;

import java.util.*;

public class UserReg {
    private DiscordIntegration plugin;
    private YamlConfiguration config;
    private Bot bot;

    private Map<UUID, Snowflake> registeredUsers = new HashMap<>();
    private List<Snowflake> roleList = new ArrayList<>();

    public UserReg(DiscordIntegration plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfig();
        this.bot = plugin.getDiscordBot();
        reload();
    }

    public void reload() {
        for (String roleID : config.getStringList("whitelist.roles")) {
            roleList.add(Snowflake.of(roleID));
        }
    }

    public void addUser(UUID uuid, Snowflake discordID) {
        registeredUsers.put(uuid, discordID);
    }
    public void addUser(UUID uuid, String discordID) {
        Snowflake snowflake = Snowflake.of(discordID);
        registeredUsers.put(uuid, snowflake);
    }
    public boolean hasUser(UUID uuid) {
        return registeredUsers.containsKey(uuid);
    }
    public boolean hasDiscordId(String id) {
        Snowflake snowflake = Snowflake.of(id);
        return registeredUsers.containsValue(id);
    }
    public boolean hasDiscordId(Snowflake id) {
        return registeredUsers.containsValue(id);
    }
    public void removeUser(UUID uuid) {
        registeredUsers.remove(uuid);
    }

    public boolean isUserAuthorized(UUID uuid) {
        Snowflake user = registeredUsers.get(uuid);
        Member member = bot.getGuild().getMemberById(user).block();
        if(user != null && member != null) {
            for (Snowflake memberRoleID : member.getRoleIds()) {
                for (Snowflake allowedRoleID : roleList) {
                    if (memberRoleID.equals(allowedRoleID)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
