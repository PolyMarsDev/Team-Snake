package me.polymarsdev.teamsnake.commands;

import me.polymarsdev.teamsnake.Bot;
import me.polymarsdev.teamsnake.event.CommandEvent;
import me.polymarsdev.teamsnake.objects.Command;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;

public class PrefixCommand extends Command {

    public PrefixCommand() {
        super("prefix");
    }

    @Override
    public void execute(CommandEvent event) {
        User user = event.getAuthor();
        Member member = event.getMember();
        String[] args = event.getArgs();
        Guild guild = event.getGuild();
        Bot.debug("Received prefix command: " + event.getMessage().getContentRaw());
        if (!member.hasPermission(Permission.ADMINISTRATOR)) {
            Bot.debug("Failed to change prefix of " + guild.getName() + " (" + guild.getId()
                              + "): Insufficient permissions");
            event.reply(user.getAsMention() + ", you do not have permission to use this command.");
            return;
        }
        if (args.length > 0) {
            String newPrefix = args[0].toLowerCase();
            if (newPrefix.length() > 3) {
                Bot.debug("Failed to change prefix of " + guild.getName() + " (" + guild.getId() + "): length");
                event.reply(user.getAsMention() + ", the prefix can't be longer than 3 characters!");
                return;
            }
            Bot.setPrefix(guild, newPrefix);
            Bot.debug("Successfully changed server prefix of " + guild.getName() + " (" + guild.getId() + ") to: "
                              + newPrefix);
            event.reply("Prefix successfully changed to ``" + newPrefix + "``.");
            return;
        }
        event.reply(user.getAsMention() + ", please use `" + Bot.getPrefix(guild)
                            + "prefix <new prefix>` to set a server-prefix.");
    }
}
