package me.polymarsdev.teamsnake.objects;

import me.polymarsdev.teamsnake.Bot;
import me.polymarsdev.teamsnake.handler.GameHandler;
import me.polymarsdev.teamsnake.manager.GameManager;
import me.polymarsdev.teamsnake.util.EmoteUtil;

import java.io.Serializable;

public class Game implements Serializable {
    final int N = 0;
    final int E = 1;
    final int S = 2;
    final int W = 3;
    Grid grid;

    public Game(int width, int height) {
        grid = new Grid(width, height);
    }

    public Grid getGrid() {
        return grid;
    }

    public String run(String direction, GameHandler gameHandler) {
        if (gameHandler.stopped) return "";
        if (gameHandler.isPending()) {
            return "Please use the :white_check_mark: reaction to join.\n\n:flushed: **" + gameHandler.getMinPlayers()
                    + "** Players are required to start the game.\n\n*Make sure to **[vote and review Team Snake]"
                    + "(https://top.gg/bot/335051227324743682 \"Vote and Review!\")** at top.gg!*";
        }
        if (direction.equals("update-timer")) {
            long diff = gameHandler.getNextRun() - System.currentTimeMillis();
            if (gameHandler.getNextRun() <= System.currentTimeMillis() || diff < 1000) {
                gameHandler.process();
                gameHandler.setNextRun(System.currentTimeMillis() + 30000);
            }
            return direction;
        }
        if (direction.equals("left")) grid.getSnake().setDirection(W);
        else if (direction.equals("right")) grid.getSnake().setDirection(E);
        else if (direction.equals("up")) grid.getSnake().setDirection(N);
        else if (direction.equals("down")) grid.getSnake().setDirection(S);
        // "get-grid" should only return the current grid without any action
        // if the @param direction is not get-grid, run an action, otherwise don't
        if (!direction.equals("get-grid")) {
            grid.getSnake().setYummed(
                    false); //updating the game before getting the grid (instead of at the same time) messed up the
            // yum emoji lmao but moving this here this fixes it
            grid.getSnake().move();
            grid.getSnake().changeModes();
        }
        //
        long timeTillNextRun = gameHandler.getNextRun() - System.currentTimeMillis();
        int secondsTillNextRun = (int) (timeTillNextRun / 1000.0);
        if (secondsTillNextRun % 5 != 0)
            secondsTillNextRun++; // avoiding 9.99 seconds being displayed as 9 instead of 10
        String leftVotes = EmoteUtil.getEmoteFromDirection("left") + "  " + gameHandler.votes.getOrDefault("left", 0);
        String rightVotes = EmoteUtil.getEmoteFromDirection("right") + "  " + gameHandler.votes
                .getOrDefault("right", 0);
        String upVotes = EmoteUtil.getEmoteFromDirection("up") + "  " + gameHandler.votes.getOrDefault("up", 0);
        String downVotes = EmoteUtil.getEmoteFromDirection("down") + "  " + gameHandler.votes.getOrDefault("down", 0);
        String voteDisplay = leftVotes + "   " + rightVotes + "   " + upVotes + "   " + downVotes
                + "\n\n**Next move:** " + EmoteUtil.getEmoteFromDirection(gameHandler.getWinningDirection());
        String extraContent = ":alarm_clock: " + secondsTillNextRun + " seconds\n\n" + voteDisplay;
        //
        if (grid.getSnake().isDead()) {
            String guildId = gameHandler.guildId;
            GameManager.removeGame(guildId);
            return grid.toString() + "**Game over!**\n*Restart using `" + Bot.getPrefix(guildId) + "play`*";
        }
        return grid.toString() + "\n" + extraContent;
    }

    public String getFooter(GameHandler gameHandler) {
        return getScore(gameHandler);
    }

    public void setBestScore() {
    }

    public String getScore(GameHandler gameHandler) {
        return "Score: " + grid.getSnake().getScore() + "\nBest: " + grid.getSnake().getBestScore(gameHandler);
    }
}
