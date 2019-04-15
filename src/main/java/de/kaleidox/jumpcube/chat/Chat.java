package de.kaleidox.jumpcube.chat;

import de.kaleidox.jumpcube.util.BukkitUtil;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import static de.kaleidox.jumpcube.chat.MessageLevel.ERROR;

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

    public static void argError(CommandSender sender, int actual, int expected) {
        message(sender, ERROR, String.format("Too %s arguments! Expected: %d",
                (actual < expected ? "few" : "many"), expected)
        );
    }
}
