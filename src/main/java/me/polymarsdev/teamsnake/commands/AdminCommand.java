package me.polymarsdev.teamsnake.commands;

import me.polymarsdev.teamsnake.Bot;
import me.polymarsdev.teamsnake.event.CommandEvent;
import me.polymarsdev.teamsnake.manager.GameManager;
import me.polymarsdev.teamsnake.objects.Command;
import net.dv8tion.jda.api.entities.User;

import java.util.Arrays;
import java.util.List;

public class AdminCommand extends Command {

    // ID's of users who are permitted to use admin commands (default: [PolyMars, Affluent Avo])
    private static final List<String> adminIds = Arrays.asList("583028945113448471", "335051227324743682");

    public AdminCommand() {
        super("admin");
    }

    @Override
    public void execute(CommandEvent e) {
        User u = e.getAuthor();
        String uid = u.getId();
        if (!adminIds.contains(uid)) return; // Not permitted to use admin commands
        String[] args = e.getArgs();
        String prefix = Bot.getPrefix(e.getGuild());
        if (args.length < 1) {
            e.reply(u.getAsMention() + " » Please use `" + prefix + "admin <end/restart/shutdown> ...`");
            return;
        }
        String arg = args[0].toLowerCase();
        if (arg.equals("end")) {
            if (args.length < 2) {
                e.reply(u.getAsMention() + " » Please use `" + prefix + "admin end <guildID/this>`");
                return;
            }
            String endGid;
            String gid = args[1];
            if (gid.equalsIgnoreCase("this")) {
                endGid = e.getGuild().getId();
            } else endGid = gid;
            if (GameManager.hasGame(endGid)) {
                GameManager.getGame(endGid).shutdownGame("Shutdown by an admin.");
                e.reply(u.getAsMention() + " » Successfully shutdown game from guild ID " + endGid);
                return;
            }
            e.reply(u.getAsMention() + " » This guild has no running game.");
            return;
        }
        if (arg.equals("shutdown") || arg.equals("restart")) {
            e.reply(u.getAsMention() + " » Executing console command `stop`.", s -> Bot.processCommand("stop"));
            return;
        }
        e.reply(u.getAsMention() + " » Invalid argument.");
    }
}