package me.polymarsdev.teamsnake.util;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.internal.utils.Checks;

import javax.annotation.Nonnull;
import java.awt.*;

public class MessageUtil {

    public static MessageEmbed main(String title, @Nonnull String description, String footer) {
        Checks.notNull(description, "description");
        EmbedBuilder eb = new EmbedBuilder();
        eb.setColor(0xaa8ed6);
        if (title != null) eb.setTitle(title);
        eb.setDescription(description);
        if (footer != null) eb.setFooter(footer);
        return eb.build();
    }

    public static MessageEmbed info(String title, String message) {
        EmbedBuilder eb = new EmbedBuilder();
        if (title != null) eb.setTitle(title);
        eb.setDescription(message);
        eb.setColor(new Color(0, 119, 255));
        return eb.build();
    }

    public static MessageEmbed success(String title, String message) {
        EmbedBuilder eb = new EmbedBuilder();
        if (title != null) eb.setTitle(title);
        eb.setDescription(message);
        eb.setColor(new Color(2, 199, 13));
        return eb.build();
    }

    public static MessageEmbed err(String title, String message) {
        EmbedBuilder eb = new EmbedBuilder();
        if (title != null) eb.setTitle(title);
        eb.setDescription(message);
        eb.setColor(new Color(255, 52, 40));
        return eb.build();
    }
}