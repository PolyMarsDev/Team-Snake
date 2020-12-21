package me.polymarsdev.teamsnake.commands;

import me.polymarsdev.teamsnake.Bot;
import me.polymarsdev.teamsnake.database.Database;
import me.polymarsdev.teamsnake.event.CommandEvent;
import me.polymarsdev.teamsnake.objects.Command;
import me.polymarsdev.teamsnake.util.StatsUtil;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.sql.ResultSet;
import java.sql.SQLException;

public class LeaderboardCommand extends Command {

    public LeaderboardCommand() {
        super("leaderboard", 1.25, "lb", "top");
    }

    @Override
    public void execute(CommandEvent e) {
        String[] args = e.getArgs();
        int page = 1;
        if (args.length > 0) {
            try {
                page = Integer.parseInt(args[0]);
            } catch (NumberFormatException ignored) {
            }
        }
        int offset = (page * 10) - 9;
        int to = offset + 9;
        MessageEmbed result = getTopLevel(offset, to, e.getGuild());
        if (result == null) {
            e.reply("Sorry " + e.getAuthor().getAsMention()
                            + ", but this bot instance has no database, so we can not display a leaderboard.\nPlease "
                            + "contact an administrator if you think this is an error.");
            return;
        }
        e.reply(result);
    }

    private MessageEmbed getTopLevel(int offset, int to, Guild self) {
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle("Top " + offset + "-" + to + " | Level");
        eb.setColor(0xaa8ed6);
        final String selfId = self.getId();
        StringBuilder top = new StringBuilder();
        int selfRank = -1;
        int rank = 0;
        boolean printedSelf = false;
        if (Bot.isDatabaseEnabled()) {
            Database db = Bot.getDatabase();
            try (ResultSet rs = db
                    .query("SELECT guildId, CAST(statValue AS INT) AS statValue FROM statistics WHERE statName='bestscore' ORDER "
                                   + "BY statValue DESC LIMIT 500;")) {
                while (rs.next()) {
                    rank++;
                    String guildId = rs.getString("guildId");
                    if (guildId.equals(selfId)) {
                        printedSelf = true;
                        selfRank = rank;
                        if (rank > to) top.append("\n");
                    }
                    if (rank < offset) continue;
                    if (rank > to && printedSelf) break;
                    if (!printedSelf && rank > to && !guildId.equals(selfId)) continue;
                    Guild g = Bot.getShardManager().getGuildById(guildId);
                    int bestscore = Integer.parseInt(rs.getString("statValue"));
                    if (g == null) {
                        top.append("**#").append(rank).append("** *<unavailable>* | Best Score: `").append(bestscore)
                           .append("`\n");
                        continue;
                    }
                    top.append("**#").append(rank).append("** ").append(g.getName()).append(" | Best Score: `")
                       .append(bestscore).append("`\n");
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        } else {
            return null;
        }
        eb.setDescription(top);
        int selfValue = StatsUtil.getStatInt(selfId, "bestscore");
        if (selfValue == -1) eb.setFooter("This server never played this game.");
        else {
            if (selfRank == -1) eb.setFooter("This server's rank: #500+\nLevel " + selfValue, self.getIconUrl());
            else eb.setFooter("This server's rank: #" + selfRank + "\nLevel " + selfValue, self.getIconUrl());
        }
        return eb.build();
    }
}