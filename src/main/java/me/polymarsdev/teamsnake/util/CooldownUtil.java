package me.polymarsdev.teamsnake.util;

import me.polymarsdev.teamsnake.Bot;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;

public class CooldownUtil {

    private static final HashMap<String, HashMap<String, Long>> cooldowns = new HashMap<>();

    public static void addCooldown(String uid, String name, long until, boolean sql) {
        HashMap<String, Long> userCooldowns = cooldowns.getOrDefault(uid, new HashMap<>());
        userCooldowns.put(name.toLowerCase(), until);
        cooldowns.put(uid, userCooldowns);
        if (sql) {
            Bot.getDatabase()
               .update("INSERT INTO cooldowns VALUES (?, ?, ?);", uid, name.toLowerCase(), String.valueOf(until));
        }
    }

    public static void removeCooldown(String uid, String name) {
        HashMap<String, Long> userCooldowns = cooldowns.getOrDefault(uid, new HashMap<>());
        userCooldowns.remove(name.toLowerCase());
        cooldowns.put(uid, userCooldowns);
        Bot.getDatabase().update("DELETE FROM cooldowns WHERE userId=? AND cooldownName=?;", uid, name.toLowerCase());
    }

    public static boolean hasCooldown(String uid, String name) {
        return cooldowns.getOrDefault(uid, new HashMap<>()).getOrDefault(name, -1L) > System.currentTimeMillis();
    }

    public static long getCooldown(String uid, String name) {
        HashMap<String, Long> userCooldowns = cooldowns.getOrDefault(uid, new HashMap<>());
        return userCooldowns.getOrDefault(name, -1L);
    }

    public static String format(long milliseconds) {
        long seconds = milliseconds / 1000;
        long minutes = 0;
        long hours = 0;
        while (seconds >= 60) {
            seconds -= 60;
            minutes++;
        }
        while (minutes >= 60) {
            minutes -= 60;
            hours++;
        }
        String hoursString = String.valueOf(hours);
        String minutesString = String.valueOf(minutes);
        String secondsString = String.valueOf(seconds);
        if (hoursString.length() == 1) hoursString = "0" + hoursString;
        if (minutesString.length() == 1) minutesString = "0" + minutesString;
        if (secondsString.length() == 1) secondsString = "0" + secondsString;
        return hoursString + ":" + minutesString + ":" + secondsString;
    }

    public static void loadCooldowns() {
        if (Bot.isDatabaseEnabled()) {
            long now = System.currentTimeMillis();
            Bot.getDatabase()
               .update("CREATE TABLE IF NOT EXISTS cooldowns (userId VARCHAR(18) NOT NULL, cooldownName VARCHAR(32) "
                               + "NOT " + "NULL, cooldownEnd VARCHAR(32) NOT NULL);");
            try (ResultSet rs = Bot.getDatabase().query("SELECT * FROM cooldowns;")) {
                while (rs.next()) {
                    String uid = rs.getString("userId");
                    String cdName = rs.getString("cooldownName");
                    String cdEnd = rs.getString("cooldownEnd");
                    long cdEndLong = Long.parseLong(cdEnd);
                    if (cdEndLong <= now) {
                        Bot.getDatabase()
                           .update("DELETE FROM cooldowns WHERE userId=? AND cooldownName=? AND cooldownEnd=?;", uid,
                                   cdName, cdEnd);
                        continue;
                    }
                    addCooldown(uid, cdName, cdEndLong, false);
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }
}