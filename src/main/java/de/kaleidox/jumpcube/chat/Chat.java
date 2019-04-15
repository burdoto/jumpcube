package de.kaleidox.jumpcube.chat;

import de.kaleidox.jumpcube.util.BukkitUtil;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Chat {
    public static void message(CommandSender sender, MessageLevel msgLevel, String message) {
        message(BukkitUtil.getPlayer(sender), msgLevel, message);
    }

    public static void message(Player player, MessageLevel msgLevel, String message) {
        player.sendMessage(ChatColor.DARK_GRAY + "[" +
                ChatColor.BLUE + "JumpCube" +
                ChatColor.DARK_GRAY + "] " +
                msgLevel.chatColor + message);
    }
}
