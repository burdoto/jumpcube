package de.kaleidox.jumpcube.cube;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import de.kaleidox.jumpcube.JumpCube;
import de.kaleidox.jumpcube.exception.DuplicateCubeException;
import de.kaleidox.jumpcube.world.Generatable;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.Nullable;

import static de.kaleidox.jumpcube.chat.Chat.message;
import static de.kaleidox.jumpcube.chat.MessageLevel.ERROR;
import static de.kaleidox.jumpcube.chat.MessageLevel.INFO;
import static de.kaleidox.jumpcube.cube.BlockBar.MaterialGroup.WALLS;

public class ExistingCube implements Cube, Generatable {
    private final static Map<String, Cube> instances = new ConcurrentHashMap<>();
    private final String name;
    private final World world;
    private final int[][] pos;
    private final BlockBar bar;

    private ExistingCube(String name, World world, int[][] positions, BlockBar bar) {
        this.name = name;
        this.world = world;
        this.pos = positions;
        this.bar = bar;

        if (instances.containsKey(name)) throw new DuplicateCubeException(name);
        instances.put(name, this);
    }

    @Override
    public String getCubeName() {
        return name;
    }

    @Override
    public void delete() {
        assert JumpCube.getInstance() != null;

        // remove from maps
        instances.remove(name, this);
        JumpCube.getInstance().selections.forEach((key, value) -> {
            if (value == this)
                JumpCube.getInstance().selections.remove(key, value);
        });
    }

    @Override
    public int[][] getPositions() {
        return pos;
    }

    @Override
    public void generate() {
        bar.validate();

        // generate outter walls
        int higherY = (pos[0][1] < pos[1][1] ? pos[1][1] : pos[0][1]);

        boolean alongZ = pos[0][0] < pos[1][0];
        int offsetX = alongZ ? 1 : -1;
        for (int movX = pos[0][0]; (alongZ ? movX < pos[1][0] + offsetX : movX > pos[1][0] + offsetX); movX += offsetX)
            for (int movY = higherY; movY > 0; movY--) {
                world.getBlockAt(movX, movY, pos[0][2]).setType(bar.getRandomMaterial(WALLS));
                //System.out.println("CYCLE 1: movX = " + movX + " && movY = " + movY);
            }
        for (int movX = pos[1][0]; (alongZ ? movX > pos[0][0] - offsetX : movX < pos[0][0] - offsetX); movX -= offsetX)
            for (int movY = higherY; movY > 0; movY--) {
                world.getBlockAt(movX, movY, pos[1][2]).setType(bar.getRandomMaterial(WALLS));
                //System.out.println("CYCLE 2: movX = " + movX + " && movY = " + movY);
            }

        boolean alongX = pos[0][2] < pos[1][2];
        int offsetZ = alongX ? 1 : -1;
        for (int movZ = pos[0][2] + offsetZ; (alongX ? movZ < pos[1][2] : movZ > pos[1][2]); movZ += offsetZ)
            for (int movY = higherY; movY > 0; movY--) {
                world.getBlockAt(pos[0][0], movY, movZ).setType(bar.getRandomMaterial(WALLS));
                //System.out.println("CYCLE 3: movZ = " + movZ + " && movY = " + movY);
            }
        for (int movZ = pos[1][2] - offsetZ; (alongX ? movZ > pos[0][2] : movZ < pos[0][2]); movZ -= offsetZ)
            for (int movY = higherY; movY > 0; movY--) {
                world.getBlockAt(pos[1][0], movY, movZ).setType(bar.getRandomMaterial(WALLS));
                //System.out.println("CYCLE 4: movZ = " + movZ + " && movY = " + movY);
            }

        // clear inner area
        for (int movX = pos[0][0] + offsetX; (alongZ ? movX < pos[1][0] : movX > pos[1][0]); movX += offsetX)
            for (int movZ = pos[0][2] + offsetZ; (alongX ? movZ < pos[1][2] : movZ > pos[1][2]); movZ += offsetX)
                for (int movY = higherY; movY > 0; movY--) {
                    world.getBlockAt(movX, movY, movZ).setType(Material.AIR);
                    //System.out.println(String.format("CYCLE 5: movX = %d && movY = %d && movZ = %d", movX, movY, movZ));
                }
    }

    @Nullable
    public static ExistingCube get(String name) {
        return (ExistingCube) instances.get(name);
    }

    public static boolean exists(String name) {
        return instances.containsKey(name);
    }

    public static ExistingCube load(final FileConfiguration config, String name, @Nullable BlockBar bar)
            throws DuplicateCubeException {
        final String basePath = "cubes." + name + ".";

        if (bar == null) bar = BlockBar.create(config, basePath + "bar.");

        // get world
        World world = Bukkit.getWorld(Objects.requireNonNull(config.getString(basePath + "world"),
                "No world defined for cube: " + name));

        // get positions
        int[][] locs = new int[2][3];

        locs[0][0] = config.getInt(basePath + "pos1.x");
        locs[0][1] = config.getInt(basePath + "pos1.y");
        locs[0][2] = config.getInt(basePath + "pos1.z");

        locs[1][0] = config.getInt(basePath + "pos2.x");
        locs[1][1] = config.getInt(basePath + "pos2.y");
        locs[1][2] = config.getInt(basePath + "pos2.z");

        assert world != null : "Unknown world: " + config.getString(basePath + "world");

        return new ExistingCube(name, world, locs, bar);
    }

    public final static class Commands {
        public static void regenerate(CommandSender sender, Cube sel) {
            if (!validateSelection(sender, sel)) return;

            ((ExistingCube) sel).generate();
            message(sender, INFO, "Cube was regenerated!");
        }

        private static boolean validateSelection(CommandSender sender, Cube sel) {
            if (sel == null) {
                message(sender, ERROR, "No cube selected!");
                return false;
            }
            if (!(sel instanceof ExistingCube)) {
                message(sender, ERROR, "Cube " + sel.getCubeName() + " is not finished!");
                return false;
            }
            return true;
        }
    }
}
