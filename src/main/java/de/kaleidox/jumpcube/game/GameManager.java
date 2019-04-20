package de.kaleidox.jumpcube.game;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.IntStream;

import de.kaleidox.jumpcube.cube.ExistingCube;
import de.kaleidox.jumpcube.interfaces.Startable;
import de.kaleidox.jumpcube.util.BukkitUtil;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import static java.util.concurrent.TimeUnit.SECONDS;
import static de.kaleidox.jumpcube.chat.Chat.broadcast;
import static de.kaleidox.jumpcube.chat.Chat.message;
import static de.kaleidox.jumpcube.chat.MessageLevel.INFO;
import static de.kaleidox.jumpcube.chat.MessageLevel.WARN;

public class GameManager implements Startable {
    private final ExistingCube cube;
    private final List<UUID> attemptedJoin = new ArrayList<>();
    private final List<Player> joined = new ArrayList<>();
    private final int baseTime = 30;
    private int remaining = 30;
    private ScheduledExecutorService scheduler;
    private AtomicReference<ScheduledFuture<?>> timeBroadcastFuture;

    public GameManager(ExistingCube cube) {
        this.cube = cube;
    }

    public void join(CommandSender sender) {
        UUID uuid = BukkitUtil.getUuid(sender);

        if (attemptedJoin.removeIf(id -> id.equals(uuid))) {
            // join user
            message(sender, INFO, "Joining cube %s...", cube.getCubeName());

            cube.teleportIn(BukkitUtil.getPlayer(sender));
            startTimer();
        } else {
            // warn user
            message(sender, WARN, "Warning: You might die in the game! " +
                    "If you still want to play, use the command again.");

            attemptedJoin.add(uuid);
        }
    }

    @Override
    public void start() {
        cube.start();
    }

    private void startTimer() {
        this.scheduler = Executors.newSingleThreadScheduledExecutor();

        assert baseTime % 10 == 0;

        IntStream.range(1, baseTime / 10)
                .forEach(x -> {
                    scheduler.schedule(new BroadcastRemaining(x), baseTime - x, SECONDS);
                    scheduler.schedule(new BroadcastRemaining(x * 10), baseTime - (x * 10), SECONDS);
                });

        scheduler.schedule(cube::prepare, baseTime / 3, SECONDS);
    }

    private class BroadcastRemaining implements Runnable {
        private final int val;

        private BroadcastRemaining(int val) {
            this.val = val;
        }

        @Override
        public void run() {
            broadcast(INFO, "Time remaining until cube %s will start: %s seconds", cube.getCubeName(), val);
        }
    }
}
