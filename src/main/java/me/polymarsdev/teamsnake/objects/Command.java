package me.polymarsdev.teamsnake.objects;

import me.polymarsdev.teamsnake.event.CommandEvent;

public abstract class Command {

    private final String name;
    private final String[] aliases;
    private final double cooldown;

    public Command(String name) {
        this(name, 0.3);
    }

    public Command(String name, String... aliases) {
        this(name, 0.3, aliases);
    }

    public Command(String name, double cooldown, String... aliases) {
        this.name = name;
        this.cooldown = cooldown;
        this.aliases = aliases;
    }

    public String getName() {
        return name;
    }

    public String[] getAliases() {
        return aliases;
    }

    public double getCooldown() {
        return cooldown;
    }

    public abstract void execute(CommandEvent commandEvent);
}
