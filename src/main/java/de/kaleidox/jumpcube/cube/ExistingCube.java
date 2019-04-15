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

import static java.lang.System.nanoTime;
import static de.kaleidox.jumpcube.chat.Chat.message;
import static de.kaleidox.jumpcube.chat.MessageLevel.ERROR;
import static de.kaleidox.jumpcube.chat.MessageLevel.INFO;
import static de.kaleidox.jumpcube.cube.BlockBar.MaterialGroup.CUBE;
import static de.kaleidox.jumpcube.cube.BlockBar.MaterialGroup.WALLS;

public class ExistingCube implements Cube, Generatable {
    private final static Map<String, Cube> instances = new ConcurrentHashMap<>();
    private final String name;
    private final World world;
    private final int[][] pos;
    private final BlockBar bar;
    private long startNanos = -1;

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
    public BlockBar getBlockBar() {
        return bar;
    }

    public void generateFull() {
        startNanos = nanoTime();

        // generate outter walls
        int highestY = (pos[0][1] < pos[1][1] ? pos[1][1] : pos[0][1]);

        boolean alongZ = pos[0][0] < pos[1][0];
        int offsetX = alongZ ? 1 : -1;
        for (int movX = pos[0][0]; (alongZ ? movX < pos[1][0] + offsetX : movX > pos[1][0] + offsetX); movX += offsetX)
            for (int movY = highestY; movY > 0; movY--) {
                world.getBlockAt(movX, movY, pos[0][2]).setType(bar.getRandomMaterial(WALLS));
                //System.out.println("[CYCLE 1]\tx = " + movX + " && y = " + movY + " && z = " + pos[0][2]);
            }
        for (int movX = pos[1][0]; (alongZ ? movX > pos[0][0] - offsetX : movX < pos[0][0] - offsetX); movX -= offsetX)
            for (int movY = highestY; movY > 0; movY--) {
                world.getBlockAt(movX, movY, pos[1][2]).setType(bar.getRandomMaterial(WALLS));
                //System.out.println("[CYCLE 2]\tx = " + movX + " && y = " + movY + " && z = " + pos[1][2]);
            }

        boolean alongX = pos[0][2] < pos[1][2];
        int offsetZ = alongX ? 1 : -1;
        for (int movZ = pos[0][2] + offsetZ; (alongX ? movZ < pos[1][2] : movZ > pos[1][2]); movZ += offsetZ)
            for (int movY = highestY; movY > 0; movY--) {
                world.getBlockAt(pos[0][0], movY, movZ).setType(bar.getRandomMaterial(WALLS));
                //System.out.println("[CYCLE 3]\tx = " + pos[0][0] + " && y = " + movY + " && z = " + movZ);
            }
        for (int movZ = pos[1][2] - offsetZ; (alongX ? movZ > pos[0][2] : movZ < pos[0][2]); movZ -= offsetZ)
            for (int movY = highestY; movY > 0; movY--) {
                world.getBlockAt(pos[1][0], movY, movZ).setType(bar.getRandomMaterial(WALLS));
                //System.out.println("[CYCLE 4]\tx = " + pos[1][0] + " && y = " + movY + " && z = " + movZ);
            }

        generate();
    }

    @Override
    public void generate() {
        if (startNanos == -1) startNanos = nanoTime();

        int highest = (pos[0][1] < pos[1][1] ? pos[1][1] : pos[0][1]);
        boolean alongZ = pos[0][0] < pos[1][0];
        int offsetX = alongZ ? 1 : -1;
        boolean alongX = pos[0][2] < pos[1][2];
        int offsetZ = alongX ? 1 : -1;

        // clear inner area
        for (int movX = pos[0][0] + offsetX; (alongZ ? movX < pos[1][0] : movX > pos[1][0]); movX += offsetX)
            for (int movZ = pos[0][2] + offsetZ; (alongX ? movZ < pos[1][2] : movZ > pos[1][2]); movZ += offsetX)
                for (int movY = 255; movY > 0; movY--) {
                    world.getBlockAt(movX, movY, movZ).setType(Material.AIR);
                    /*
                    if (movX > 300 || movY > 255 || movZ < -300)
                        System.err.println("[CYCLE 5]\tx = " + movX + " && y = " + movY + " && z = " + movZ);
                        */
                }

        int sizeX = pos[0][0] - pos[1][0], sizeZ = pos[0][2] - pos[1][2];
        if (sizeX < 0) sizeX = sizeX * -1;
        if (sizeZ < 0) sizeZ = sizeZ * -1;

        final double density = 0.183; // todo Add changeable density
        final int height = 110; // todo Add changeable height
        final double spacing = 0.2;

        final Material[][][] matrix = new Material[sizeX - (int) (sizeX * (spacing * 2))][height][sizeZ - (int) (sizeZ * (spacing * 2))];

        for (int x = 0; x < matrix.length; x++)
            for (int y = 0; y < matrix[x].length; y++)
                for (int z = 0; z < matrix[x][y].length; z++) {
                    if (JumpCube.rng.nextDouble() % 1 > density)
                        matrix[x][y][z] = Material.AIR;
                    else matrix[x][y][z] = bar.getRandomMaterial(CUBE);
                    //System.out.println("[MATRIX]\tx = " + x + " && y = " + y + " && z = " + z);
                }

        int mX = pos[alongZ ? 0 : 1][0] + (int) (sizeX * spacing);
        int mZ = pos[alongZ ? 0 : 1][2] + (int) (sizeZ * spacing);
        for (int x = 0; x < matrix.length; x++)
            for (int y = 0; y < matrix[x].length; y++)
                for (int z = 0; z < matrix[x][y].length; z++) {
                    int uX = mX + x, uY = y + 10, uZ = mZ + z;
                    world.getBlockAt(uX, uY, uZ).setType(matrix[x][y][z]);
                    /*
                    if (uX > 300 || uY > 255 || uZ < -300)
                        System.err.println("[CYCLE 6]\tx = " + uX + " && y = " + uY + " && z = " + uZ);
                        */
                }

        assert JumpCube.getInstance() != null;
        JumpCube.getInstance().getLogger().info("Cube " + name + " was generated, took "
                + (nanoTime() - startNanos) + " nanoseconds.");
        startNanos = -1;
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
        public static void regenerate(CommandSender sender, Cube sel, boolean full) {
            if (!validateSelection(sender, sel)) return;

            sel.getBlockBar().validate();
            if (full) ((ExistingCube) sel).generateFull();
            else ((ExistingCube) sel).generate();

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
