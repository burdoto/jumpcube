package de.kaleidox.jumpcube.cube;

import de.kaleidox.jumpcube.JumpCube;
import de.kaleidox.jumpcube.util.BukkitUtil;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import static java.lang.Math.pow;
import static java.lang.Math.sqrt;
import static de.kaleidox.jumpcube.chat.Chat.argError;
import static de.kaleidox.jumpcube.chat.Chat.message;
import static de.kaleidox.jumpcube.chat.MessageLevel.ERROR;
import static de.kaleidox.jumpcube.chat.MessageLevel.INFO;

public class CubeCreationTool implements Cube {
    public final Player player;
    private String name;
    private Location[] pos = new Location[2];
    private BlockBar bar;

    public CubeCreationTool(Player player) {
        this.player = player;
    }

    public boolean isReady() {
        return name != null
                && pos[0] != null
                && pos[1] != null
                && bar != null;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPos(int y, Location location) {
        pos[y - 1] = location;
    }

    public void createBar(Location location, Vector vector) {
    }

    @Override
    public String getCubeName() {
        return name;
    }

    @Override
    public void delete() {

    }

    @Override
    public Location[] getPositions() {
        return pos;
    }

    public static final class Commands {
        public static void pos(CommandSender sender, Cube sel, String subCommand, String[] args) {
            if (!validateEditability(sender, sel)) return;

            if ((args.length != 1 && subCommand.equals("pos"))
                    || (args.length > 0 && !subCommand.equals("pos"))) {
                argError(sender, args.length, subCommand.equals("pos") ? 1 : 0);
                return;
            }
            if (subCommand.equals("pos") && !args[0].matches("[12]")) {
                message(sender, ERROR, "Illegal argument: " + args[0]);
                return;
            }

            Location location = BukkitUtil.getPlayer(sender).getLocation();
            switch (subCommand) {
                case "pos":
                    int argInt = Integer.parseInt(args[0]);
                    ((CubeCreationTool) sel).setPos(argInt, location);
                    message(sender, INFO, "Position " + argInt + " was set to your current location!");
                    break;
                case "pos1":
                    ((CubeCreationTool) sel).setPos(1, location);
                    message(sender, INFO, "Position 1 was set to your current location!");
                    break;
                case "pos2":
                    ((CubeCreationTool) sel).setPos(2, location);
                    message(sender, INFO, "Position 2 was set to your current location!");
                    break;
            }

            Location[] pos = sel.getPositions();
            if (pos[0] != null && pos[1] != null) {
                double dist = sqrt(pow(pos[1].getBlockX() - pos[0].getBlockX(), 2) + pow(pos[1].getBlockZ() - pos[0].getBlockZ(), 2));
                if (dist < 0) dist = dist * -1;
                message(sender, INFO, "Size: " + (int) dist + " (Even size is recommended)");
            }
        }

        public static void bar(CommandSender sender, Cube sel, String[] args) {
            if (!validateEditability(sender, sel)) return;

            Player player = BukkitUtil.getPlayer(sender);
            ((CubeCreationTool) sel).bar = new BlockBar(player, null);

            message(sender, INFO, "The BlockBar has been pasted relative to you.");
        }

        private static boolean validateEditability(CommandSender sender, Cube sel) {
            if (sel == null) {
                message(sender, ERROR, "No cube selected!");
                return false;
            }
            if (!(sel instanceof CubeCreationTool)) {
                message(sender, ERROR, "Cube " + sel.getCubeName() + " is not editable!");
                return false;
            }
            return true;
        }
    }
}
