package de.kaleidox.jumpcube.exception;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import static de.kaleidox.jumpcube.chat.MessageLevel.ERROR;
import static de.kaleidox.jumpcube.chat.MessageLevel.EXCEPTION;

public class NoSuchCubeException extends InnerCommandException {
    public NoSuchCubeException(Player selectForPlayer) {
        super("Could not auto-select cube for player: " + selectForPlayer.getDisplayName() + "" +
                "\nPlease use " + ChatColor.LIGHT_PURPLE + "/jc select <Name>" +
                EXCEPTION.chatColor + " to select a cube.");
    }

    public NoSuchCubeException(String name) {
        super(ERROR, "No cube with name " + ChatColor.BLUE + name + ERROR.chatColor + " could be found.");
    }
}
