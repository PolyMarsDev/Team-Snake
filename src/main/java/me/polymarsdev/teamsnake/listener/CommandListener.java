package me.polymarsdev.teamsnake.listener;

import me.polymarsdev.teamsnake.Bot;
import me.polymarsdev.teamsnake.commands.*;
import me.polymarsdev.teamsnake.config.GuildConfig;
import me.polymarsdev.teamsnake.event.CommandEvent;
import me.polymarsdev.teamsnake.handler.GameHandler;
import me.polymarsdev.teamsnake.manager.GameManager;
import me.polymarsdev.teamsnake.objects.Command;
import me.polymarsdev.teamsnake.util.CooldownUtil;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.*;
import java.util.concurrent.TimeUnit;

public class CommandListener extends ListenerAdapter {
    private static final HashMap<String, Command> commands = new HashMap<>();

    public CommandListener() {
        List<Command> botCommands = new ArrayList<>(
                Arrays.asList(new PrefixCommand(), new InfoCommand(), new PlayCommand(), new LeaderboardCommand(),
                              new GameModesCommand(), new AdminCommand(), new ServerCommand()));
        for (Command command : botCommands) {
            commands.put(command.getName().toLowerCase(), command);
            String[] aliases = command.getAliases();
            if (aliases != null && aliases.length > 0) {
                for (String alias : aliases) commands.put(alias.toLowerCase(), command);
            }
        }
        System.out.println("[INFO] Loaded " + commands.size() + " commands (including aliases)");
    }

    @Override
    public void onGuildMessageReactionAdd(GuildMessageReactionAddEvent event) {
        User user = event.getUser();
        if (user.isBot()) return;
        Guild guild = event.getGuild();
        String guildId = guild.getId();
        TextChannel channel = event.getChannel();
        Message message = channel.retrieveMessageById(event.getMessageId()).complete();
        if (message == null) return;
        if (message.getAuthor().getId().equals(event.getJDA().getSelfUser().getId())) {
            GameHandler gameHandler = GameManager.getGame(guildId);
            if (gameHandler == null) {
                gameHandler = new GameHandler(guildId, guild.getMemberCount());
                GameManager.setGame(guildId, gameHandler);
            }
            boolean joinReaction = false;
            boolean reactionCommand = true;
            String userInput = "";
            switch (event.getReactionEmote().toString()) {
                case "RE:U+2705":
                    joinReaction = true;
                    reactionCommand = false;
                    break;
                case "RE:U+2b05":
                    userInput = "left";
                    break;
                case "RE:U+27a1":
                    userInput = "right";
                    break;
                case "RE:U+2b06":
                    userInput = "up";
                    break;
                case "RE:U+2b07":
                    userInput = "down";
                    break;
                default:
                    reactionCommand = false;
                    break;
            }
            if (joinReaction) gameHandler.join(user.getId());
            if (reactionCommand) gameHandler.vote(user.getId(), userInput);
        }
    }

    @Override
    public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
        User user = event.getAuthor();
        Message message = event.getMessage();
        TextChannel channel = event.getChannel();
        Guild guild = event.getGuild();
        String msgRaw = message.getContentRaw();
        String[] args = msgRaw.split("\\s+");
        if (args.length > 0) {
            boolean isMention = msgRaw.equals("<@" + event.getJDA().getSelfUser().getId() + ">") || msgRaw
                    .equals("<@!" + event.getJDA().getSelfUser().getId() + ">");
            String prefix = Bot.getPrefix(guild);
            String arg = args[0].toLowerCase();
            boolean isCommand;
            if (isMention) isCommand = true;
            else {
                if (arg.startsWith(prefix)) {
                    String commandName = arg.substring(prefix.length()).toLowerCase();
                    isCommand = commands.containsKey(commandName);
                    if (isCommand) arg = commandName;
                } else isCommand = false;
            }
            if (isCommand) {
                Bot.debug("Command received: " + arg);
                GuildConfig guildConfig = new GuildConfig(guild.getId());
                if (guildConfig.isChannelLimited()) {
                    List<String> channels = guildConfig.getChannels();
                    if (!channels.contains(channel.getId())) {
                        if (guildConfig.isLimitNotify() && channel.canTalk()) channel.sendMessage(
                                user.getAsMention() + " » Team Snake is not allowed in this "
                                        + "channel.\n*This message auto-deletes itself after 5 seconds.*")
                                                                                     .queue(msg -> msg.delete()
                                                                                                      .queueAfter(
                                                                                                              5,
                                                                                                              TimeUnit.SECONDS));
                        return;
                    }
                }
                Command command = commands.get(arg);
                if (!hasPermissions(guild, channel, command)) {
                    Bot.debug("Not enough permissions to run command: " + arg);
                    sendInvalidPermissionsMessage(user, channel);
                    return;
                }
                if (isMention) command = commands.get("info");
                if (command == null) {
                    Bot.debug("Received command does not exist: " + arg);
                    return;
                }
                long cooldownMS = Double.valueOf(command.getCooldown() * 1000.0).longValue();
                long cmdcd = CooldownUtil.getCooldown(user.getId(), "cmd_cd_" + command.getName().toLowerCase());
                long now = System.currentTimeMillis();
                if (cmdcd > now) return;
                CooldownUtil.addCooldown(user.getId(), "cmd_cd_" + command.getName().toLowerCase(), now + cooldownMS,
                                         false);
                Bot.debug("Executing command: " + arg);
                command.execute(new CommandEvent(event, Arrays.copyOfRange(msgRaw.split("\\s+"), 1, args.length)));
            }
        }
    }

    private static final Collection<Permission> requiredPermissions = Arrays
            .asList(Permission.MESSAGE_ADD_REACTION, Permission.MESSAGE_EMBED_LINKS, Permission.MESSAGE_MANAGE,
                    Permission.MESSAGE_WRITE, Permission.MESSAGE_READ, Permission.MESSAGE_HISTORY);

    private boolean hasPermissions(Guild guild, TextChannel channel, Command command) {
        if (command instanceof InfoCommand) return true;
        Member self = guild.getSelfMember();
        if (self.hasPermission(Permission.ADMINISTRATOR)) return true;
        return self.hasPermission(channel, requiredPermissions);
    }

    private void sendInvalidPermissionsMessage(User user, TextChannel channel) {
        if (channel.canTalk()) {
            StringBuilder requiredPermissionsDisplay = new StringBuilder();
            for (Permission requiredPermission : requiredPermissions) {
                requiredPermissionsDisplay.append("`").append(requiredPermission.getName()).append("`, ");
            }
            if (requiredPermissionsDisplay.toString().endsWith(", ")) requiredPermissionsDisplay = new StringBuilder(
                    requiredPermissionsDisplay.substring(0, requiredPermissionsDisplay.length() - 2));
            channel.sendMessage(user.getAsMention() + ", I don't have enough permissions to work properly.\nMake "
                                        + "sure I have the following permissions: " + requiredPermissionsDisplay
                                        + "\nIf you think this is "
                                        + "an error, please contact a server administrator.").queue();
        }
    }
}