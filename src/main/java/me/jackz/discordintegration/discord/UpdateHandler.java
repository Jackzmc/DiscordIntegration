package me.jackz.discordintegration.discord;

import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.guild.MemberUpdateEvent;
import discord4j.core.object.entity.Member;
import discord4j.discordjson.json.gateway.GuildMemberUpdate;
import me.jackz.discordintegration.DiscordIntegration;
import me.jackz.discordintegration.UserReg;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.YamlConfiguration;

public class UpdateHandler {
    private final DiscordIntegration plugin;
    private final GatewayDiscordClient client;
    private YamlConfiguration config;
    private Bot bot;

    private UserReg userReg;


    public UpdateHandler(DiscordIntegration plugin, Bot bot) {
        this.plugin = plugin;
        this.client = bot.getClient();
        this.config = plugin.getConfig();
        this.bot = bot;
        this.userReg = plugin.getUserReg();
    }

    public void process(MemberUpdateEvent event) {
        Member newMember = event.getMember().block();
        Member oldMember = event.getOld().orElse(newMember);

        //if user is registered
        if(newMember != null && userReg.hasDiscordId(newMember.getId())) {
            //if the roles differ:
            if(newMember.getRoleIds() != oldMember.getRoleIds()) {
                boolean previouslyAllowed = userReg.isUserWhitelisted(newMember.getId());
                boolean isNowAllowed = userReg.isUserAuthorized(newMember);
                OfflinePlayer player = Bukkit.getOfflinePlayer(userReg.getMCFromDiscord(newMember.getId()));

                if(previouslyAllowed && !isNowAllowed) {
                    player.setWhitelisted(false);
                }else if(!previouslyAllowed && isNowAllowed) {
                    player.setWhitelisted(true);
                }
            }
        }

    }
}
