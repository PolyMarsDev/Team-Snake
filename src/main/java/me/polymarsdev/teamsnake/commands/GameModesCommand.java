package me.polymarsdev.teamsnake.commands;

import me.polymarsdev.teamsnake.Bot;
import me.polymarsdev.teamsnake.event.CommandEvent;
import me.polymarsdev.teamsnake.objects.Command;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;

public class GameModesCommand extends Command {

    public GameModesCommand() {
        super("modes", 0.75, "gamemodes", "mode", "gamemode", "snakes");
    }

    @Override
    public void execute(CommandEvent event) {
        Bot.debug("Received info command (or bot mention)");
        Guild guild = event.getGuild();
        EmbedBuilder info = new EmbedBuilder();
        final String prefix = Bot.getPrefix(guild);
        info.setTitle("Snake Modes");
        info.setThumbnail(
                "https://media.discordapp.net/attachments/776637471894667275/781749828731535380/snakemodes.png");
        info.setDescription(
                "Special snake modes change up the gameplay and have a 25% chance of activating after each move.");
        info.setColor(0xaa8ed6);
        info.addField(":smiling_imp::purple_square::purple_square::purple_circle:",
                      "**Devil**\nMoves in the opposite direction of the chosen move!", true);
        info.addField(":nauseated_face::green_square::green_square::green_circle:",
                      "**Glutted**\nDies if it eats an apple!", true);
        info.addField(":rage::red_square::red_square::red_circle:",
                      "**Charged**\nContinues to move until reaching an obstacle!", true);
        event.reply(info.build());
    }
}
