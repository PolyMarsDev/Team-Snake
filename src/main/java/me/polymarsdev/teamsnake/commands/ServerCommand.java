package me.polymarsdev.teamsnake.commands;

import me.polymarsdev.teamsnake.Bot;
import me.polymarsdev.teamsnake.config.GuildConfig;
import me.polymarsdev.teamsnake.event.CommandEvent;
import me.polymarsdev.teamsnake.manager.GameManager;
import me.polymarsdev.teamsnake.objects.Command;
import me.polymarsdev.teamsnake.util.MessageUtil;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;

import java.util.List;

public class ServerCommand extends Command {

    public ServerCommand() {
        super("server", 1.0);
    }

    @Override
    public void execute(CommandEvent e) {
        Member m = e.getMember();
        if (!m.hasPermission(Permission.MANAGE_SERVER)) {
            e.reply(MessageUtil.err(null, m.getAsMention()
                    + " » You are not permitted to use this command.\nOnly members with the `Manage Server` "
                    + "permission are allowed to use this."));
            return;
        }
        User u = e.getAuthor();
        Guild guild = e.getGuild();
        TextChannel tc = e.getTextChannel();
        String guildId = guild.getId();
        String[] args = e.getArgs();
        String prefix = Bot.getPrefix(guild);
        if (args.length < 1) {
            e.reply(MessageUtil.info(null, u.getAsMention() + " » Please use `" + prefix + "server <stop/channels>`"));
            return;
        }
        String arg = args[0].toLowerCase();
        if (arg.equals("channels")) {
            if (args.length < 2) {
                e.reply(MessageUtil.info(null, u.getAsMention() + " » Please use `" + prefix
                        + "server channels <toggle/list/add/remove/togglenotify> ...`"));
                return;
            }
            GuildConfig guildConfig = new GuildConfig(guildId);
            boolean limitNotify = guildConfig.isLimitNotify();
            boolean channelLimited = guildConfig.isChannelLimited();
            List<String> channels = guildConfig.getChannels();
            String channelArg = args[1].toLowerCase();
            if (channelArg.equals("togglenotify")) {
                limitNotify = !limitNotify;
                guildConfig.setLimitNotify(limitNotify);
                if (limitNotify) e.reply(MessageUtil.info(
                        "Guild Configuration",
                        "Team Snake will now notify users when they are trying to run commands on disallowed channels"
                                + "."));
                else e.reply(MessageUtil.info("Guild Configuration",
                                              "Team Snake will no longer notify users when they are trying to run "
                                                      + "commands on disallowed channels."));
                return;
            }
            if (channelArg.equals("toggle")) {
                channelLimited = !channelLimited;
                guildConfig.setChannelLimited(channelLimited);
                if (channelLimited) e.reply(MessageUtil.info("Guild Configuration",
                                                             "Team Snake is now limited to certain channels.\nUse `"
                                                                     + prefix
                                                                     + "server channels list` to list the allowed "
                                                                     + "channels.\nUse `" + prefix
                                                                     + "server channels <add/remove> <#channel>` to "
                                                                     + "add or remove a channel from the allowed list"
                                                                     + "."));
                else e.reply(MessageUtil.info("Guild Configuration",
                                              "Team Snake is now playable in any channel the bot can see and chat in"
                                                      + "."));
                return;
            }
            if (channelArg.equals("list")) {
                StringBuilder list = new StringBuilder();
                if (channels.size() > 0) {
                    for (String channel : channels) {
                        list.append("\n<#").append(channel).append("> (*").append(channel).append("*)");
                    }
                } else list = new StringBuilder(
                        "\nNo limited channels were added.\nUse `" + prefix + "server channels add <#channel>`.");
                String topLine = channelLimited ?
                        "The channel limit is on. Team Snake is limited to the following " + "channels:" :
                        "The channel limit is off. Team Snake is not limited. Turning on the "
                                + "channel limit will limit Team Snake to the following channels:";
                e.reply(MessageUtil.info("Guild Configuration", topLine + "\n" + list));
                return;
            }
            if (channelArg.equals("add")) {
                if (args.length < 3) {
                    e.reply(MessageUtil.info(null,
                                             "Please use `" + prefix + "server channels add <#channel/channel ID>`"));
                    return;
                }
                Message msg = e.getMessage();
                List<TextChannel> mentioneds = msg.getMentionedChannels();
                if (mentioneds.size() > 0) {
                    TextChannel mentioned = mentioneds.get(0);
                    if (channels.contains(mentioned.getId())) {
                        e.reply(MessageUtil.err(null, "This channel is already added."));
                        return;
                    }
                    guildConfig.addChannel(mentioned.getId());
                    e.reply(MessageUtil.success(null, "Successfully added " + mentioned.getAsMention()
                            + " to the limited channel list!"));
                    return;
                }
                String idArg = args[2];
                if (idArg.length() == 18) {
                    boolean validId;
                    try {
                        Long.parseLong(idArg);
                        validId = true;
                    } catch (NumberFormatException ex) {
                        validId = false;
                    }
                    if (validId) {
                        if (channels.contains(idArg)) {
                            e.reply(MessageUtil.err(null, "This channel is already added."));
                            return;
                        }
                        guildConfig.addChannel(idArg);
                        e.reply(MessageUtil.success(null, "Successfully added <#" + idArg
                                + "> to the limited channel list!"));
                        return;
                    }
                }
                e.reply(MessageUtil.err(null, "Invalid argument.\nPlease use `" + prefix
                        + "server channels add <#channel/channel ID>`"));
                return;
            }
            if (channelArg.equals("remove")) {
                if (args.length < 3) {
                    e.reply(MessageUtil.info(null, "Please use `" + prefix
                            + "server channels remove <#channel/channel ID>`"));
                    return;
                }
                Message msg = e.getMessage();
                List<TextChannel> mentioneds = msg.getMentionedChannels();
                if (mentioneds.size() > 0) {
                    TextChannel mentioned = mentioneds.get(0);
                    if (!channels.contains(mentioned.getId())) {
                        e.reply(MessageUtil.err(null, "This channel is not in the limited channel list."));
                        return;
                    }
                    guildConfig.removeChannel(mentioned.getId());
                    e.reply(MessageUtil.success(null, "Successfully removed " + mentioned.getAsMention()
                            + " from the limited channel list!"));
                    return;
                }
                String idArg = args[2];
                if (idArg.length() == 18) {
                    boolean validId;
                    try {
                        Long.parseLong(idArg);
                        validId = true;
                    } catch (NumberFormatException ex) {
                        validId = false;
                    }
                    if (validId) {
                        if (!channels.contains(idArg)) {
                            e.reply(MessageUtil.err(null, "This channel is not in the limited channel list."));
                            return;
                        }
                        guildConfig.removeChannel(idArg);
                        e.reply(MessageUtil.success(null, "Successfully removed <#" + idArg
                                + "> from the limited channel list!"));
                        return;
                    }
                }
                e.reply(MessageUtil.err(null, "Invalid argument.\nPlease use `" + prefix
                        + "server channels remove <#channel/channel ID>`"));
                return;
            }
            e.reply(MessageUtil.err(null, "Invalid argument"));
            return;
        }
        if (arg.equals("stop")) {
            if (GameManager.hasGame(guildId)) {
                if (args.length > 1) {
                    if (args[1].equalsIgnoreCase("confirm")) {
                        GameManager.getGame(guildId).shutdownGame("Shutdown by " + u.getAsTag());
                        e.reply(MessageUtil.success(null, u.getAsMention()
                                + " » Successfully shutdown the game of this server."));
                        return;
                    }
                }
                e.reply(MessageUtil.info(null, u.getAsMention()
                        + " » This command will shutdown the current game of this server" + ".\nPlease confirm using `"
                        + prefix + "server stop confirm`"));
                return;
            }
            e.reply(MessageUtil.err(null, u.getAsMention() + " » There is no active game in this server."));
            return;
        }
        e.reply(MessageUtil.err(null, u.getAsMention() + " » Invalid argument."));
    }
}