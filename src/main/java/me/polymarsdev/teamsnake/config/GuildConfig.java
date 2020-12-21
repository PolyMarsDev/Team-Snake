package me.polymarsdev.teamsnake.config;

import me.polymarsdev.teamsnake.Bot;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class GuildConfig {

    private static final HashMap<String, GuildConfig> cache = new HashMap<>();

    private final String guildId;
    private boolean channelLimited = false;
    private boolean limitNotify = false;
    private final List<String> channels = new ArrayList<>();

    public static boolean isCached(String guildId) {
        return cache.containsKey(guildId);
    }

    public GuildConfig(String guildId) {
        this.guildId = guildId;
        load();
        cache();
    }

    public void addChannel(String channelId) {
        channels.add(channelId);
        cache();
        Bot.getDatabase().update("INSERT INTO guildsettings VALUES (?, ?, ?);", guildId, "gc-channelId", channelId);
    }

    public void removeChannel(String channelId) {
        channels.remove(channelId);
        cache();
        Bot.getDatabase()
           .update("DELETE FROM guildsettings WHERE guildId=? AND setting=? AND value=?;", guildId, "gc-channelId",
                   channelId);
    }

    public void setChannelLimited(boolean channelLimited) {
        this.channelLimited = channelLimited;
        cache();
        Bot.getDatabase()
           .update("DELETE FROM guildsettings WHERE guildId=? AND setting=?;", guildId, "gc-channelLimited");
        Bot.getDatabase().update("INSERT INTO guildsettings VALUES (?, ?, ?);", guildId, "gc-channelLimited",
                                 String.valueOf(channelLimited));
    }

    public void setLimitNotify(boolean limitNotify) {
        this.limitNotify = limitNotify;
        cache();
        Bot.getDatabase().update("DELETE FROM guildsettings WHERE guildId=? AND setting=?;", guildId, "gc-limitNotify");
        Bot.getDatabase().update("INSERT INTO guildsettings VALUES (?, ?, ?);", guildId, "gc-limitNotify",
                                 String.valueOf(limitNotify));
    }

    public boolean isChannelLimited() {
        if (channels.size() > 0) return channelLimited;
        return false; // In case all limited channels are removed, you will be able to run commands
    }

    public boolean isLimitNotify() {
        return limitNotify;
    }

    public List<String> getChannels() {
        return channels;
    }

    private void load() {
        if (cache.containsKey(guildId)) {
            GuildConfig cached = cache.get(guildId);
            channelLimited = cached.isChannelLimited();
            limitNotify = cached.isLimitNotify();
            channels.clear();
            channels.addAll(cached.getChannels());
            return;
        }
        try (ResultSet rs = Bot.getDatabase()
                               .query("SELECT * FROM guildsettings WHERE guildId=? AND setting LIKE 'gc-%';",
                                      guildId)) {
            while (rs.next()) {
                String setting = rs.getString("setting");
                String value = rs.getString("value");
                if (setting.equals("gc-channelLimited")) channelLimited = Boolean.parseBoolean(value);
                if (setting.equals("gc-channelId")) channels.add(value);
                if (setting.equals("gc-limitNotify")) limitNotify = Boolean.parseBoolean(value);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private void cache() {
        cache.put(guildId, this);
    }
}