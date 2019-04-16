package de.kaleidox.jumpcube.game;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import de.kaleidox.jumpcube.chat.Chat;
import de.kaleidox.jumpcube.cube.ExistingCube;
import de.kaleidox.jumpcube.util.BukkitUtil;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import static de.kaleidox.jumpcube.chat.Chat.message;
import static de.kaleidox.jumpcube.chat.MessageLevel.INFO;
import static de.kaleidox.jumpcube.chat.MessageLevel.WARN;

public class GameManager {
    private final ExistingCube cube;
    private final List<UUID> attemptedJoin = new ArrayList<>();
    private final List<Player> joined = new ArrayList<>();
    private int remaining = 30;
    private ScheduledExecutorService scheduler;
    private AtomicReference<ScheduledFuture<?>> timeBroadcastFuture;
    private Runnable broadcastRemaining = new BroadcastRemaining();

    public GameManager(ExistingCube cube) {
        this.cube = cube;
    }

    public void join(CommandSender sender) {
        UUID uuid = BukkitUtil.getUuid(sender);

        if (attemptedJoin.removeIf(id -> id.equals(uuid))) {
            // join user
            message(sender, INFO, "Joining cube " + cube.getCubeName() + "...");

            cube.teleportIn(BukkitUtil.getPlayer(sender));
            startTimer();
        } else {
            // warn user
            message(sender, WARN, "Warning: You might die in the game! " +
                    "If you still want to play, use the command again.");

            attemptedJoin.add(uuid);
        }
    }

    private void startTimer() {
        this.scheduler = Executors.newSingleThreadScheduledExecutor();

        timeBroadcastFuture.set(
                scheduler.scheduleAtFixedRate(
                        () -> {
                            broadcastRemaining.run();

                            if (remaining < 5) {
                                timeBroadcastFuture.get()
                                        .cancel(false);
                                timeBroadcastFuture.set(
                                        scheduler.scheduleAtFixedRate(
                                                broadcastRemaining, 0, 1, TimeUnit.SECONDS
                                        )
                                );
                            }
                        }, 0, 5, TimeUnit.SECONDS
                )
        );
    }

    private class BroadcastRemaining implements Runnable {
        @Override
        public void run() {
            Chat.broadcast(INFO, "Time remaining until cube "
                    + ChatColor.BLUE + cube.getCubeName() + INFO.chatColor + " will start: "
                    + ChatColor.LIGHT_PURPLE + remaining + " seconds");
        }
    }
}
