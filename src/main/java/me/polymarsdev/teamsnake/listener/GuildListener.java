package me.polymarsdev.teamsnake.listener;

import me.polymarsdev.teamsnake.Bot;
import me.polymarsdev.teamsnake.config.GuildConfig;
import me.polymarsdev.teamsnake.handler.GameHandler;
import me.polymarsdev.teamsnake.manager.GameManager;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.channel.text.TextChannelDeleteEvent;
import net.dv8tion.jda.api.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageDeleteEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

public class GuildListener extends ListenerAdapter {
    @Override
    public void onGuildLeave(@NotNull GuildLeaveEvent event) {
        String guildId = event.getGuild().getId();
        GameManager.removeGame(guildId);
        Bot.removePrefix(guildId);
    }

    @Override
    public void onTextChannelDelete(@NotNull TextChannelDeleteEvent event) {
        Guild guild = event.getGuild();
        String guildId = guild.getId();
        TextChannel textChannel = event.getChannel();
        String channelId = textChannel.getId();
        if (GuildConfig.isCached(guildId)) {
            GuildConfig guildConfig = new GuildConfig(guildId);
            guildConfig.removeChannel(channelId);
            return;
        }
        Bot.getDatabase()
           .update("DELETE FROM guildsettings WHERE guildId=? AND setting=? AND value=?;", guildId, "gc-channelId",
                   channelId);
    }

    @Override
    public void onGuildMessageDelete(@NotNull GuildMessageDeleteEvent event) {
        String guildId = event.getGuild().getId();
        if (GameManager.hasGame(guildId)) {
            GameHandler gameHandler = GameManager.getGame(guildId);
            Message message = gameHandler.message;
            if (message != null && message.getId().equals(event.getMessageId())) {
                GameManager.removeGame(guildId);
            }
        }
    }

    @Override
    public void onReady(@NotNull ReadyEvent event) {
        if (Bot.avatarUrl == null) Bot.avatarUrl = event.getJDA().getSelfUser().getAvatarUrl();
    }
}