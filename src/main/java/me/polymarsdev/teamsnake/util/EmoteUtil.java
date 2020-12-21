package me.polymarsdev.teamsnake.util;

public class EmoteUtil {

    public static String getEmoteFromDirection(String direction) {
        switch (direction) {
            case "left":
                return "\u2B05";
            case "right":
                return "\u27A1";
            case "up":
                return "\u2B06";
            case "down":
                return "\u2B07";
            default:
                return "-";
        }
    }

    public static String getDirectionFromEmote(String emote) {
        switch (emote) {
            case "\u2B05":
            case "U+2B05":
                return "left";
            case "\u27A1":
            case "U+27A1":
                return "right";
            case "\u2B06":
            case "U+2B06":
                return "up";
            case "\u2B07":
            case "U+2B07":
                return "down";
            default:
                return "-";
        }
    }
}