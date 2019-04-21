package de.kaleidox.jumpcube.util;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import de.kaleidox.jumpcube.JumpCube;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

public final class BukkitUtil {
    private BukkitUtil() {
    }

    public static int schedule(Runnable runnable, long time, TimeUnit unit) {
        assert JumpCube.instance != null;

        return Bukkit.getScheduler()
                .scheduleSyncDelayedTask(JumpCube.instance, runnable, unit.toSeconds(time) * 20);
    }

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

}
