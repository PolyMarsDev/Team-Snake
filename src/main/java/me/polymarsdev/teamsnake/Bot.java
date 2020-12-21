package me.polymarsdev.teamsnake;

import me.polymarsdev.teamsnake.config.Config;
import me.polymarsdev.teamsnake.config.SettingsConfig;
import me.polymarsdev.teamsnake.database.Database;
import me.polymarsdev.teamsnake.listener.CommandListener;
import me.polymarsdev.teamsnake.listener.GuildListener;
import me.polymarsdev.teamsnake.manager.GameManager;
import me.polymarsdev.teamsnake.util.CooldownUtil;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder;
import net.dv8tion.jda.api.sharding.ShardManager;
import net.dv8tion.jda.api.utils.cache.CacheFlag;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class Bot extends ListenerAdapter {
    private static final HashMap<String, String> prefixes = new HashMap<>();

    /**
     * You can enable the database here.
     * Set the DB Type to MySQL or SQLite, which you want to use. (MySQL is recommended)
     * -
     * You can configure login data in the Database class.
     */
    private static final boolean enableDatabase = true;
    private static final Database.DBType dbType = Database.DBType.MySQL;

    public static boolean debug = false;

    private static ShardManager shardManager;
    private static Database database = null;
    public static String avatarUrl = null;
    private static SettingsConfig settings;

    public static void main(String[] args) throws Exception {
        // Load token
        String token = null;
        try {
            File tokenFile = Paths.get("token.txt").toFile();
            if (!tokenFile.exists()) {
                System.out.println("[ERROR] Could not find token.txt file");
                System.out.print("Please paste in your bot token: ");
                Scanner s = new Scanner(System.in);
                token = s.nextLine();
                System.out.println();
                System.out.println("[INFO] Creating token.txt - please wait");
                if (!tokenFile.createNewFile()) {
                    System.out.println(
                            "[ERROR] Could not create token.txt - please create this file and paste in your token"
                                    + ".");
                    s.close();
                    return;
                }
                Files.write(tokenFile.toPath(), token.getBytes());
                s.close();
            }
            token = new String(Files.readAllBytes(tokenFile.toPath()));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        if (token == null) return;
        // Load resources and configurations
        boolean loaded = loadResource("settings.yml"); // Awaiting result
        if (!loaded) {
            System.out.println("[ERROR] Fatal: Could not load resource \"settings.yml\" - exiting");
            System.exit(0);
            return;
        } else {
            System.out.println("[INFO] Loaded resource \"settings.yml\"");
        }
        // Load database
        if (enableDatabase) database = new Database(dbType);
        if (database != null) {
            if (!database.isConnected()) {
                database = null;
                System.out.println("[ERROR] Database connection failed. Continuing without database.");
            } else {
                database.update(
                        "CREATE TABLE IF NOT EXISTS guildprefix (guildId VARCHAR(18) NOT NULL, prefix VARCHAR(8) NOT "
                                + "NULL);");
                database.update(
                        "CREATE TABLE IF NOT EXISTS statistics (guildId VARCHAR(18) NOT NULL, statName VARCHAR(64) "
                                + "NOT NULL, statValue VARCHAR(512) NOT NULL);");
                database.update(
                        "CREATE TABLE IF NOT EXISTS guildsettings (guildId VARCHAR(18) NOT NULL, setting VARCHAR(32) "
                                + "NOT NULL, value VARCHAR(128) NOT NULL);");
            }
        }
        File settingsFile = new File("settings.yml");
        Config settingsCfg = Config.getConfig(settingsFile);
        settings = settingsCfg.getProvider().bind("settings", SettingsConfig.class);
        // Build shard manager
        List<GatewayIntent> intents = new ArrayList<>(
                Arrays.asList(GatewayIntent.GUILD_MESSAGES, GatewayIntent.GUILD_EMOJIS,
                              GatewayIntent.GUILD_MESSAGE_REACTIONS));
        DefaultShardManagerBuilder builder = DefaultShardManagerBuilder.create(token, intents);
        builder.setStatus(OnlineStatus.ONLINE);
        builder.setActivity(Activity.playing("@Team Snake for info!"));
        builder.addEventListeners(new CommandListener(), new GuildListener());
        builder.disableCache(
                CacheFlag.CLIENT_STATUS, CacheFlag.ACTIVITY, CacheFlag.MEMBER_OVERRIDES, CacheFlag.VOICE_STATE);
        shardManager = builder.build();
        // Start console
        Thread consoleThread = new Thread(() -> {
            Scanner s = new Scanner(System.in);
            while (s.hasNextLine()) {
                String cmd = s.nextLine();
                if (!cmd.isEmpty()) processCommand(cmd);
            }
        });
        consoleThread.setDaemon(true);
        consoleThread.setName("Console Thread");
        consoleThread.start();
        // Post-load
        GameManager.runGameTimer();
        CooldownUtil.loadCooldowns();
    }

    public static int getMinPlayer(int memberCount) {
        if (memberCount < settings.mediumServer()) return settings.smallPlayer();
        else if (memberCount < settings.bigServer()) return settings.mediumPlayer();
        return settings.bigPlayer();
    }

    public static void processCommand(String cmd) {
        if (cmd.equalsIgnoreCase("help")) {
            System.out.println("Commands:\nstop - Shuts down the bot and exits the program\ndebug - Toggle debug mode");
            return;
        }
        if (cmd.equalsIgnoreCase("debug")) {
            debug = !debug;
            String response = debug ? "on" : "off";
            System.out.println("[INFO] Turned " + response + " debug mode");
            Bot.debug("Make sure to turn off debug mode after necessary information has been collected.");
            return;
        }
        if (cmd.equalsIgnoreCase("stop")) {
            System.out.println("Stopping all games...");
            GameManager.shutdownAllGames("The bot is restarting...");
            System.out.println("Shutting down...");
            shardManager.shutdown();
            if (database != null) {
                System.out.println("Disconnecting database...");
                database.disconnect();
            }
            System.out.println("Bye!");
            System.exit(0);
            return;
        }
        System.out.println("Unknown command. Please use \"help\" for a list of commands.");
    }

    /*
    Debug Info for Developer information
    > Limit update to 10 seconds minimum because of JDA shard checks
     */
    private static long lastDebugInfoUpdate = -1L;
    private static String debugInfo = "";

    private static void updateDebugInfo() {
        long now = System.currentTimeMillis();
        if (now - lastDebugInfoUpdate < 10000) return;
        lastDebugInfoUpdate = now;
        int a = enableDatabase ? 1 : 0;
        int b = enableDatabase ? database.isConnected() ? 1 : 0 : 0;
        int c = 0;
        int d = shardManager.getShardsTotal();
        for (JDA shard : shardManager.getShards()) if (shard.getStatus() == JDA.Status.CONNECTED) c++;
        debugInfo = a + b + c + d + "";
    }

    // Print a message when debug is on
    public static void debug(String log) {
        if (debug) {
            updateDebugInfo();
            System.out.println("[DEBUG " + debugInfo + "] " + log);
        }
    }

    public static ShardManager getShardManager() {
        return shardManager;
    }

    public static boolean isDatabaseEnabled() {
        return enableDatabase;
    }

    public static Database getDatabase() {
        return database;
    }

    public static SettingsConfig getSettings() {
        return settings;
    }

    public static void removePrefix(String guildId) {
        prefixes.remove(guildId);
        if (database != null) {
            database.update("DELETE FROM guildprefix WHERE guildId=?;", String.valueOf(guildId));
        }
    }

    public static void setPrefix(Guild guild, String prefix) {
        prefixes.put(guild.getId(), prefix);
        if (database != null) {
            database.update("DELETE FROM guildprefix WHERE guildId=?;", guild.getId());
            database.update("INSERT INTO guildprefix VALUES (?, ?);", guild.getId(), prefix);
        }
    }

    public static String getPrefix(Guild guild) {
        return getPrefix(guild.getId());
    }

    public static String getPrefix(String guildId) {
        if (prefixes.containsKey(guildId)) return prefixes.get(guildId);
        if (database != null) {
            try (ResultSet rs = database.query("SELECT prefix FROM guildprefix WHERE guildId=?;", guildId)) {
                if (rs.next()) {
                    String prefix = rs.getString("prefix");
                    prefixes.put(guildId, prefix);
                    return prefix;
                }
                prefixes.put(guildId, "s!");
                return "s!";
            } catch (SQLException ex) {
                System.out.println(
                        "[ERROR] Error at retrieving guild prefix of guild id " + guildId + ": " + ex.getMessage());
            }
        }
        return "s!";
    }

    private static boolean loadResource(String resource) throws Exception {
        File targetFile = new File("settings.yml");
        if (targetFile.exists()) {
            System.out.println("[INFO] Resource already exists - skipping");
            return true;
        }
        InputStream is = Bot.class.getClassLoader().getResourceAsStream(resource);
        if (is == null) {
            System.out.println("[ERROR] Failed to load resource " + resource + ": Invalid InputStream");
            return false;
        }
        byte[] buffer = new byte[is.available()];
        int read = is.read(buffer);
        System.out.println("[INFO] Read " + read + "B from resource " + resource);
        OutputStream outStream = new FileOutputStream(targetFile);
        outStream.write(buffer);
        outStream.flush();
        outStream.close();
        is.close();
        return true;
    }
}