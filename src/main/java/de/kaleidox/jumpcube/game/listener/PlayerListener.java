package de.kaleidox.jumpcube.game.listener;

import de.kaleidox.jumpcube.JumpCube;
import de.kaleidox.jumpcube.cube.Cube;
import de.kaleidox.jumpcube.game.GameManager;
import de.kaleidox.jumpcube.util.BukkitUtil;

import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

import static de.kaleidox.jumpcube.chat.Chat.message;
import static de.kaleidox.jumpcube.chat.MessageLevel.WARN;
import static de.kaleidox.jumpcube.util.WorldUtil.expandVert;
import static de.kaleidox.jumpcube.util.WorldUtil.inside;
import static de.kaleidox.jumpcube.util.WorldUtil.xyz;

public class PlayerListener extends ListenerBase implements Listener {
    private final GameManager manager;

    public PlayerListener(GameManager manager, Cube cube) {
        super(cube);
        this.manager = manager;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerMove(PlayerMoveEvent event) {
        Location moveTo = event.getTo();
        if (moveTo == null) return;
        int[][] expand = expandVert(cube.getPositions());
        if (!inside(expand, xyz(moveTo))) return;

        if (moveTo.getBlockY() >= cube.getHeight())
            manager.reached(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        if (event.getPlayer().hasPermission(JumpCube.Permission.TELEPORT_OUT)) return;
        int[] before = xyz(event.getFrom());
        if (!inside(expandVert(cube.getPositions()), before)
                || !manager.activeGame
                || manager.leaving.contains(BukkitUtil.getUuid(event.getPlayer()))) return;
        event.setCancelled(true);
        message(event.getPlayer(), WARN, "Use /jumpcube leave to leave the cube!");
    }
}
