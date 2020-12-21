package me.polymarsdev.teamsnake.util;

import me.polymarsdev.teamsnake.Bot;
import me.polymarsdev.teamsnake.database.Database;

import javax.annotation.Nullable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;

public class StatsUtil {

    private static final HashMap<String, HashMap<String, String>> statsCache = new HashMap<>();

    /**
     * Set or update a statistic record
     *
     * @param guildId   The ID of the guild which the statistic should be stored to
     * @param statName  The name of the statistic-record (will always be converted to lowercase)
     * @param statValue The statistic value which should be stored
     */
    public static void setStats(String guildId, String statName, String statValue) {
        statName = statName.toLowerCase();
        HashMap<String, String> guildStats = statsCache.getOrDefault(guildId, new HashMap<>());
        guildStats.put(statName, statValue);
        statsCache.put(guildId, guildStats);
        if (Bot.isDatabaseEnabled()) {
            Database db = Bot.getDatabase();
            db.update("DELETE FROM statistics WHERE guildId=? AND statName=?;", guildId, statName);
            db.update("INSERT INTO statistics VALUES (?, ?, ?);", guildId, statName, statValue);
        }
    }

    /**
     * Retrieve a statistic as a string.
     *
     * @param guildId  The ID of the guild stored in the statistic-record
     * @param statName The name of the statistic-record (will always be converted to lowercase)
     * @return The statistic value, or null if not stored.
     */
    @Nullable
    public static String getStatString(String guildId, String statName) {
        statName = statName.toLowerCase();
        HashMap<String, String> guildStats = statsCache.getOrDefault(guildId, new HashMap<>());
        if (guildStats.containsKey(statName)) {
            return guildStats.get(statName);
        }
        String statValue = null;
        if (Bot.isDatabaseEnabled()) {
            Database db = Bot.getDatabase();
            try (ResultSet rs = db
                    .query("SELECT statValue FROM statistics WHERE guildId=? AND statName=?;", guildId, statName)) {
                if (rs.next()) statValue = rs.getString("statValue");
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
        if (statValue != null) {
            guildStats.put(statName, statValue);
            statsCache.put(guildId, guildStats);
        }
        return statValue;
    }

    /**
     * Retrieve a statistic as an integer.
     *
     * @param guildId  The ID of the guild stored in the statistic-record
     * @param statName The name of the statistic-record (will always be converted to lowercase)
     * @return The statistic value, or {@code -1} if not stored.
     */
    public static int getStatInt(String guildId, String statName) {
        statName = statName.toLowerCase();
        HashMap<String, String> guildStats = statsCache.getOrDefault(guildId, new HashMap<>());
        if (guildStats.containsKey(statName)) {
            return Integer.parseInt(guildStats.get(statName));
        }
        if (Bot.isDatabaseEnabled()) {
            Database db = Bot.getDatabase();
            try (ResultSet rs = db
                    .query("SELECT statValue FROM statistics WHERE guildId=? AND statName=?;", guildId, statName)) {
                if (rs.next()) {
                    int statValue = Integer.parseInt(rs.getString("statValue"));
                    guildStats.put(statName, String.valueOf(statValue));
                    statsCache.put(guildId, guildStats);
                    return statValue;
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
        return -1;
    }
}