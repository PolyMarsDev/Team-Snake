package me.polymarsdev.teamsnake.commands;

import me.polymarsdev.teamsnake.Bot;
import me.polymarsdev.teamsnake.event.CommandEvent;
import me.polymarsdev.teamsnake.handler.GameHandler;
import me.polymarsdev.teamsnake.manager.GameManager;
import me.polymarsdev.teamsnake.objects.Command;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;

public class PlayCommand extends Command {

    public PlayCommand() {
        super("play", 0.5, "start", "snake", "snek");
    }

    @Override
    public void execute(CommandEvent event) {
        User user = event.getAuthor();
        //String[] args = event.getArgs();
        Guild guild = event.getGuild();
        String guildId = guild.getId();
        Message message = event.getMessage();
        Bot.debug("Received play command: " + event.getMessage().getContentRaw());
        if (GameManager.hasGame(guildId)) {
            GameHandler gameHandler = GameManager.getGame(guildId);
            if (gameHandler.message != null) {
                event.reply(
                        user.getAsMention() + ", this guild already has a running snake game in " + gameHandler.message
                                .getTextChannel().getAsMention() + ". Please finish the current game "
                                + "before starting a new one.");
                return;
            } else GameManager.removeGame(guildId);
        }
        GameHandler gameHandler = new GameHandler(guildId, Bot.getMinPlayer(guild.getMemberCount()));
        gameHandler.run(event.getTextChannel(), "start-pending"); // this parameter is important
        GameManager.setGame(guildId, gameHandler);
        message.delete().queue();
    }
}
