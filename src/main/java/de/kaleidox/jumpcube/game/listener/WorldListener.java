package de.kaleidox.jumpcube.game.listener;

import de.kaleidox.jumpcube.cube.Cube;

import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.jetbrains.annotations.NotNull;

import static de.kaleidox.jumpcube.chat.Chat.message;
import static de.kaleidox.jumpcube.chat.MessageLevel.HINT;
import static de.kaleidox.jumpcube.chat.MessageLevel.WARN;
import static de.kaleidox.jumpcube.util.WorldUtil.expandVert;
import static de.kaleidox.jumpcube.util.WorldUtil.inside;
import static de.kaleidox.jumpcube.util.WorldUtil.xyz;

public class WorldListener extends ListenerBase implements Listener {
    public WorldListener(Cube cube) {
        super(cube);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        boolean inside = isInside(event.getBlock().getWorld(), xyz(event.getBlock().getLocation()));
        if (inside)
            if (event.isCancelled()) event.setCancelled(false);
            else return;

        if (event.getBlock().getType() != cube.getBlockBar().getPlaceable()) {
            event.setCancelled(true);
            message(event.getPlayer(), WARN, "Don't destroy the cube!");
        } else message(event.getPlayer(), HINT, "Here's your joker!");
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        boolean inside = isInside(event.getBlock().getWorld(), xyz(event.getBlock().getLocation()));
        if (inside)
            if (event.isCancelled()) event.setCancelled(false);
            else return;

        if (event.getBlockPlaced().getType() != cube.getBlockBar().getPlaceable()) {
            event.setCancelled(true);
            message(event.getPlayer(), WARN, "You can only place %s!",
                    cube.getBlockBar().getPlaceable().name().toLowerCase());
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getClickedBlock() == null || event.getItem() == null) return;

        boolean inside = isInside(event.getClickedBlock().getWorld(), xyz(event.getClickedBlock().getLocation()));
        if (inside)
            if (event.isCancelled()) event.setCancelled(false);
            else return;

        switch (event.getItem().getType()) {
            case WATER:
            case WATER_BUCKET:
            case LAVA:
            case LAVA_BUCKET:
                event.setCancelled(true);
        }
    }

    private boolean isInside(@NotNull World world, int[] xyz) {
        return world.equals(cube.getWorld()) && inside(expandVert(cube.getPositions()), xyz);
    }
}
