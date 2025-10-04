package me.kythera.chatLevels.rewards;

import org.bukkit.Material;

public class Reward {
    private final Material material;
    private final String command;
    private final int amount;

    public Reward(Material material, int amount) {
        this.material = material;
        this.amount = amount;
        this.command = null;
    }

    public Reward(String command) {
        this.material = null;
        this.command = command;
        this.amount = 0;
    }

    public boolean isMaterial() {
        return material != null;
    }

    public boolean isCommand() {
        return command != null;
    }

    public Material getMaterial() {
        return material;
    }

    public int getAmount() {
        return amount;
    }

    public String getCommand() {
        return command;
    }

    @Override
    public String toString() {
        if (isMaterial()) {
            return material.name() + ":" + amount;
        } else {
            return command;
        }
    }
}

