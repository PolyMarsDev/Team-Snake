package me.polymarsdev.teamsnake.handler;

import me.polymarsdev.teamsnake.Bot;
import me.polymarsdev.teamsnake.entity.Apple;
import me.polymarsdev.teamsnake.manager.GameManager;
import me.polymarsdev.teamsnake.objects.Game;
import me.polymarsdev.teamsnake.util.StatsUtil;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class GameHandler {
    public String guildId;
    public Message message;
    public Game game;
    private boolean repending;
    private boolean pending;
    public boolean stopped = false;
    private final int minPlayers;
    public final HashMap<String, Integer> votes = new HashMap<>();
    private final List<String> voters = new ArrayList<>();
    public final List<String> players = new ArrayList<>();
    public String[] directionStrings = {"up", "right", "down", "left"};
    private long nextRun;

    public GameHandler(String guildId, int minPlayers) {
        this.nextRun = System.currentTimeMillis() + 30000;
        this.guildId = guildId;
        game = new Game(7, 7);
        pending = true;
        this.minPlayers = minPlayers;
    }

    public long getNextRun() {
        return nextRun;
    }

    public void setNextRun(long nextRun) {
        this.nextRun = nextRun;
    }

    public boolean isRepending() {
        return repending;
    }

    public boolean isPending() {
        return pending;
    }

    public void start() {
        nextRun = System.currentTimeMillis() + 30000;
        pending = false;
    }

    private void prepareRepend() {
        repending = true;
        game = new Game(7, 7);
    }

    public void repend() {
        repending = false;
        players.clear();
        message.clearReactions().queue(done -> message.addReaction("U+2705").queue());
        pending = true;
    }

    public int getMinPlayers() {
        return minPlayers;
    }

    public void setGameMessage(Message message) {
        this.message = message;
    }

    public void join(String userId) {
        if (players.contains(userId)) return;
        players.add(userId);
        if (players.size() >= minPlayers) {
            start();
            run(message.getTextChannel(), "start");
        }
    }

    public void vote(String userId, String direction) {
        if (voters.contains(userId)) return;
        voters.add(userId);
        int votesCount = votes.getOrDefault(direction.toLowerCase(), 0);
        votesCount++;
        votes.put(direction.toLowerCase(), votesCount);
    }

    public String getWinningDirection() {
        String voteWon = directionStrings[game.getGrid().getSnake().getDirection()];
        int voteWonCount = 0;
        for (String direction : votes.keySet()) {
            int directionVotes = votes.get(direction);
            if (directionVotes > voteWonCount) {
                voteWon = direction;
                voteWonCount = directionVotes;
            }
        }
        return voteWon;
    }

    public void process() {
        if (stopped) return;
        message.clearReactions().queue(cleared -> {
            message.addReaction("U+2B05").queue();
            message.addReaction("U+27A1").queue();
            message.addReaction("U+2B06").queue();
            message.addReaction("U+2B07").queue();
        });
        String winningDirection = getWinningDirection();
        votes.clear();
        voters.clear();
        run(message.getTextChannel(), winningDirection);
    }

    public void run(TextChannel channel, String direction) {
        if (stopped) return;
        String content = game.run(direction, this);
        Apple apple = game.getGrid().getApple();
        boolean win = false;
        if (apple.getX() == -1 && apple.getY() == -1) {
            content = "Congratulations!\nYou filled up the board and won the game!\n\n*This game will restart in a few"
                    + " seconds...*";
            win = true;
        }
        boolean footer = !isPending() && !win;
        if (message == null) {
            channel.sendMessage(createEmbed("Team Snake", content, footer ? game.getFooter(this) : null)).queue(msg -> {
                if (direction.equals("start-pending")) msg.addReaction("U+2705").queue();
                else {
                    msg.addReaction("U+2B05").queue();
                    msg.addReaction("U+27A1").queue();
                    msg.addReaction("U+2B06").queue();
                    msg.addReaction("U+2B07").queue();
                }
                setGameMessage(msg);
            });
            GameManager.setGame(guildId, this);
        } else {
            if (direction.equals("start")) {
                int bestScore = StatsUtil.getStatInt(guildId, "bestscore");
                if (bestScore < 0) bestScore = 0;
                game.getGrid().getSnake().setBestScore(bestScore);
                message.clearReactions().queue(cleared -> {
                    message.addReaction("U+2B05").queue();
                    message.addReaction("U+27A1").queue();
                    message.addReaction("U+2B06").queue();
                    message.addReaction("U+2B07").queue();
                });
                setGameMessage(message);
            }
            String run = game.run("get-grid", this);
            if (win || !GameManager.hasGame(guildId)) { // Dead
                message.clearReactions().queue();
                prepareRepend();
                GameManager.setGame(guildId, this); // Yes, re-set it because otherwise it would not update.
                // Some people would say, why don't you even remove the game?
                // Good question: Because if we wouldn't remove it, I wouldn't know when a game is done.
            }
            message.editMessage(createEmbed("Team Snake", run, footer ? game.getFooter(this) : null))
                   .queue(this::setGameMessage);
        }
    }

    public void shutdownGame(String reason) {
        stopped = true;
        GameManager.removeGame(guildId);
        if (message != null) {
            message.editMessage(
                    createEmbed("Team Snake", "**Shutdown**\nYour game has been shutdown.\nReason: `" + reason + "`",
                                game.getFooter(this))).queue();
        }
    }

    public MessageEmbed createEmbed(String title, String description, String footer) {
        EmbedBuilder embed = new EmbedBuilder();
        embed.setColor(0xaa8ed6);
        embed.setAuthor(title, null, Bot.avatarUrl);
        // Title appears twice if below line is uncommented (see line above)
        //embed.setTitle(title);
        embed.setDescription(description);
        if (footer != null) embed.setFooter(footer);
        return embed.build();
    }
}
