package de.kaleidox.jumpcube.chat;

import org.bukkit.ChatColor;

public enum MessageLevel {
    INFO(ChatColor.AQUA),
    WARN(ChatColor.YELLOW),
    ERROR(ChatColor.RED),
    EXCEPTION(ChatColor.DARK_RED);

    public final ChatColor chatColor;

    MessageLevel(ChatColor chatColor) {
        this.chatColor = chatColor;
    }
}
