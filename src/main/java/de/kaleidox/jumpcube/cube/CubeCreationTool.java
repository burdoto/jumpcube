package de.kaleidox.jumpcube.cube;

import de.kaleidox.jumpcube.JumpCube;
import de.kaleidox.jumpcube.exception.InvalidArgumentCountException;
import de.kaleidox.jumpcube.util.BukkitUtil;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import static de.kaleidox.jumpcube.chat.Chat.message;
import static de.kaleidox.jumpcube.chat.MessageLevel.ERROR;
import static de.kaleidox.jumpcube.chat.MessageLevel.INFO;

public class CubeCreationTool implements Cube {
    public final Player player;
    private final World world;
    private String name;
    private int[][] pos = new int[2][3];
    private BlockBar bar;

    public CubeCreationTool(Player player) {
        this.player = player;
        this.world = player.getWorld();
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
        pos[y - 1] = BukkitUtil.getXYZ(location);
    }

    @Override
    public String getCubeName() {
        return name;
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public void delete() {
        // release pointers
        name = null;
        pos = null;
        bar = null;
    }

    @Override
    public int[][] getPositions() {
        return pos;
    }

    @Override
    public BlockBar getBlockBar() {
        return bar;
    }

    public ExistingCube create() {
        assert JumpCube.getInstance() != null;

        final String basePath = "cubes." + name + ".";
        final FileConfiguration config = JumpCube.getInstance().getConfig();

        // save world
        config.set(basePath + "world", world.getName());

        // save first position
        config.set(basePath + "pos1.x", pos[0][0]);
        config.set(basePath + "pos1.y", pos[0][1]);
        config.set(basePath + "pos1.z", pos[0][2]);

        // save second position
        config.set(basePath + "pos2.x", pos[1][0]);
        config.set(basePath + "pos2.y", pos[1][1]);
        config.set(basePath + "pos2.z", pos[1][2]);

        // save bar
        bar.save(config, basePath + "bar.");

        // add name to list
        config.set("cubes.created", (config.isSet("cubes.created")
                ? (config.getString("cubes.created") + ";")
                : "") + name);

        JumpCube.getInstance().saveConfig();

        return ExistingCube.load(config, name, bar);
    }

    public static final class Commands {
        public static void pos(CommandSender sender, Cube sel, String subCommand, String[] args) {
            if (!validateEditability(sender, sel)) return;

            if ((args.length != 1 && subCommand.equals("pos"))
                    || (args.length > 0 && !subCommand.equals("pos")))
                throw new InvalidArgumentCountException(subCommand.equals("pos") ? 1 : 0, args.length);
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

            int[][] pos = sel.getPositions();
            if (pos[0] != null && pos[1] != null) {
                double dist = Cube.dist(pos);
                if (dist < 0) dist = dist * -1;
                if (dist < 32) message(sender, INFO, "Size: " + ERROR.chatColor + (int) dist
                        + INFO.chatColor + " (Cannot be smaller than 32)");
                else if (dist > 64) message(sender, INFO, "Size: " + ERROR.chatColor + (int) dist
                        + INFO.chatColor + " (Cannot be larger than 64)");
                else message(sender, INFO, "Size: " + ChatColor.GREEN + (int) dist
                            + INFO.chatColor + " (Even sizes are recommended)");
            }
        }

        public static void bar(CommandSender sender, Cube sel, String[] args) {
            if (!validateEditability(sender, sel)) return;

            Player player = BukkitUtil.getPlayer(sender);
            ((CubeCreationTool) sel).bar = new BlockBar(player);

            message(sender, INFO, "The BlockBar has been pasted relative to you.");
        }

        public static void confirm(CommandSender sender, Cube sel) {
            if (!validateEditability(sender, sel)) return;

            if (!((CubeCreationTool) sel).isReady()) {
                message(sender, ERROR, "Cube setup isn't complete yet!");
                return;
            }

            if (Cube.dist(sel.getPositions()) < 32) {
                message(sender, ERROR, "Cube must be at least 32 blocks wide!");
                return;
            } else if (Cube.dist(sel.getPositions()) > 64) {
                message(sender, ERROR, "Cube must cant be wider than 64 blocks!");
                return;
            }

            ExistingCube cube = ((CubeCreationTool) sel).create();
            cube.generateFull();

            message(sender, INFO, "Cube " + sel.getCubeName() + " was created!");
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
