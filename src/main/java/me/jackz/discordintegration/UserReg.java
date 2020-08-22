package me.jackz.discordintegration;

import discord4j.common.util.Snowflake;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Role;
import me.jackz.discordintegration.discord.Bot;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class UserReg {
    private DiscordIntegration plugin;
    private YamlConfiguration config;
    private Bot bot;

    private Map<UUID, Snowflake> registeredUsers = new HashMap<>();
    private List<Snowflake> roleList = new ArrayList<>();

    private final File USER_FILE;

    public UserReg(DiscordIntegration plugin) {
        USER_FILE = new File(plugin.getDataFolder(), "users.yml");
        this.plugin = plugin;
        this.config = plugin.getConfig();
        this.bot = plugin.getDiscordBot();
        reload();
    }

    public void reload() {
        for (String roleID : config.getStringList("whitelist.roles")) {
            roleList.add(Snowflake.of(roleID));
        }
        YamlConfiguration data = YamlConfiguration.loadConfiguration(USER_FILE);
        for (String key : data.getKeys(false)) {
            UUID uuid = Util.stringToUUID(key);
            Snowflake discordUser = Snowflake.of(data.getString(key));
            registeredUsers.put(uuid, discordUser);
        }
    }

    public void save() throws IOException {
        YamlConfiguration data = YamlConfiguration.loadConfiguration(USER_FILE);
        for (Map.Entry<UUID, Snowflake> entry : registeredUsers.entrySet()) {
            data.set(entry.getKey().toString(), entry.getValue().asString());
        }
        data.save(USER_FILE);
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
        if(member == null) return false;
        return isUserAuthorized(member);
    }
    public boolean isUserAuthorized(Member member) {
        if(member != null) {
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
    public boolean isUserWhitelisted(Snowflake snowflake) {
        if(registeredUsers.containsValue(snowflake)) {
            for (Map.Entry<UUID, Snowflake> entry : registeredUsers.entrySet()) {
                if(entry.getValue().equals(snowflake)) {
                    OfflinePlayer player = Bukkit.getOfflinePlayer(entry.getKey());
                    return plugin.getServer().getWhitelistedPlayers().contains(player);
                }
            }
        }
        return false;
    }

    public UUID getMCFromDiscord(Snowflake snowflake) {
        for (Map.Entry<UUID, Snowflake> entry : registeredUsers.entrySet()) {
            if(entry.getValue().equals(snowflake)) {
                return entry.getKey();
            }
        }
        return null;
    }
}
