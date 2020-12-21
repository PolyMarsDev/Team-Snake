package me.polymarsdev.teamsnake.config;

public interface SettingsConfig {
    /**
     * @return member count in the configuration when a server is considered medium-big
     */
    default int mediumServer() {
        return 750;
    }

    /**
     * @return member count in the configuration when a server is considered big
     */
    default int bigServer() {
        return 1500;
    }

    /**
     * @return minimum player count to start a small-server game
     */
    default int smallPlayer() {
        return 5;
    }

    /**
     * @return minimum player count to start a medium-server game
     */
    default int mediumPlayer() {
        return 20;
    }

    /**
     * @return minimum player count to start a big-server game
     */
    default int bigPlayer() {
        return 50;
    }
}