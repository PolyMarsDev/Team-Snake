package me.polymarsdev.teamsnake.manager;

import me.polymarsdev.teamsnake.handler.GameHandler;
import net.dv8tion.jda.api.entities.Message;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class GameManager {

    private static final HashMap<String, GameHandler> games = new HashMap<>();

    public static GameHandler getGame(String guildId) {
        return games.get(guildId);
    }

    public static boolean hasGame(String guildId) {
        final boolean inMap = games.containsKey(guildId);
        boolean hasGame = inMap;
        GameHandler theGame = getGame(guildId);
        boolean isSnakeDead = hasGame && theGame.game.getGrid().getSnake().isDead();
        if (inMap && isSnakeDead) hasGame = !theGame.isRepending() && !theGame.isPending();
        return hasGame;
    }

    public static void setGame(String guildId, GameHandler gameHandler) {
        games.put(guildId, gameHandler);
    }

    public static void removeGame(String guildId) {
        games.remove(guildId);
    }

    //

    public static void shutdownAllGames(String reason) {
        int shutdown = 0;
        List<GameHandler> shutdowns = new ArrayList<>(games.values());
        for (GameHandler gameHandler : shutdowns) {
            gameHandler.shutdownGame(reason);
            shutdown++;
        }
        System.out.println("[SHUTDOWN] Shutdown " + shutdown + " snake games.");
    }

    private static final ScheduledExecutorService gameTimer = Executors.newSingleThreadScheduledExecutor();
    private static final ExecutorService gamer = Executors.newWorkStealingPool();

    private static final Runnable gameTimerRun = () -> {
        List<GameHandler> gameHandlers = new ArrayList<>(games.values());
        for (GameHandler gameHandler : gameHandlers) {
            if (gameHandler.isPending()) continue;
            Message message = gameHandler.message;
            if (message != null) {
                if (gameHandler.isRepending()) {
                    gameHandler.repend();
                }
                gameHandler.run(gameHandler.message.getTextChannel(), "update-timer");
            }
        }
    };

    public static void runGameTimer() {
        gameTimer.scheduleAtFixedRate(() -> gamer.execute(gameTimerRun), 0, 5, TimeUnit.SECONDS);
    }
}