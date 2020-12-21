package me.polymarsdev.teamsnake.commands;

import me.polymarsdev.teamsnake.Bot;
import me.polymarsdev.teamsnake.event.CommandEvent;
import me.polymarsdev.teamsnake.objects.Command;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;

public class InfoCommand extends Command {

    public InfoCommand() {
        super("info", 0.75, "help");
    }

    @Override
    public void execute(CommandEvent event) {
        Bot.debug("Received info command (or bot mention)");
        Guild guild = event.getGuild();
        Member selfMember = guild.getSelfMember();
        if (!selfMember.hasPermission(event.getTextChannel(), Permission.MESSAGE_EMBED_LINKS)) {
            event.reply(event.getAuthor().getAsMention()
                                + " » Sorry, but the bot needs the `Embed Links` permission to print the bot "
                                + "information and help page!\nPlease contact an admin if you think this is an error.");
            return;
        }
        EmbedBuilder info = new EmbedBuilder();
        final String prefix = Bot.getPrefix(guild);
        info.setTitle("Team Snake");
        info.setThumbnail(guild.getJDA().getSelfUser().getAvatarUrl());
        info.setDescription(
                "Team Snake is a bot that lets you play Snake with your friends. Compete with other servers to get "
                        + "the longest snake possible!");
        info.setColor(0xaa8ed6);
        info.addField("How to Play",
                      "Eat apples to grow as long as possible, but avoid hitting your tail and the walls!", false);
        info.addField(
                "Features",
                "**Play with friends**\n:white_small_square: Games are server-wide: The most popular option is "
                        + "picked!\n**Leaderboards**\n:white_small_square: Compete with other servers for high scores!",
                false);
        info.addField(
                "Commands", "``" + prefix + "play`` starts a game.\n``" + prefix
                        + "info`` provides details about the bot and basic rules.\n``" + prefix
                        + "modes`` explains the gameplay for each special snake mode.\n``" + prefix
                        + "leaderboard`` shows the top server scores. (Your server name will be listed here!)\n``"
                        + prefix + "prefix [character]`` changes the prefix the bot responds to.\n``" + prefix
                        + "server`` can be used by admins to manage the game.", false);
        info.addField("Add to your server",
                      "https://top.gg/bot/335051227324743682\nTeam Snake is currently in " + Bot.getShardManager()
                                                                                                .getGuilds().size()
                              + " servers.", false);
        info.addField("Support Server",
                      "Do you have questions? Do you need help?\nFeel free to join our community-friendly support "
                              + "server: " + "https://discord.gg/sGecnxuwxu", false);
        info.addField("Source code", "https://github.com/PolyMarsDev/Team-Snake", false);
        info.setFooter(
                "created by PolyMars and Affluent Avo",
                "https://cdn.discordapp.com/attachments/739252082422317062/781566541912473610/unknown.png");
        event.reply(info.build());
    }
}
