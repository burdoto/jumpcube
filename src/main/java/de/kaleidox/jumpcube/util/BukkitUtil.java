package de.kaleidox.jumpcube.util;

import java.util.Optional;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

public class BukkitUtil {
    public static UUID getUuid(CommandSender cmdSender) {
        for (Player onlinePlayer : Bukkit.getOnlinePlayers())
            if (onlinePlayer.getName().equals(cmdSender.getName()))
                return onlinePlayer.getUniqueId();
        throw new AssertionError("Sender is not online!");
    }

    public static Player getPlayer(CommandSender cmdSender) {
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            if (onlinePlayer.getName().equals(cmdSender.getName()))
                return onlinePlayer;
        }
        throw new AssertionError("Sender is not online!");
    }

    public static Optional<Material> getMaterial(@Nullable String name) {
        if (name == null) return Optional.empty();

        Material val = Material.getMaterial(name);
        if (val != null) return Optional.of(val);

        for (Material value : Material.values())
            if (value.name().equalsIgnoreCase(name))
                return Optional.of(value);
        return Optional.empty();
    }

    @Range(from = 3, to = 3)
    public static int[] getXYZ(Location location) {
        return new int[]{location.getBlockX(), location.getBlockY(), location.getBlockZ()};
    }
}
