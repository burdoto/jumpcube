package de.kaleidox.jumpcube.cube;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import de.kaleidox.jumpcube.JumpCube;
import de.kaleidox.jumpcube.exception.DuplicateCubeException;
import de.kaleidox.jumpcube.game.GameManager;
import de.kaleidox.jumpcube.util.BukkitUtil;
import de.kaleidox.jumpcube.world.Generatable;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import static java.lang.System.nanoTime;
import static de.kaleidox.jumpcube.chat.Chat.message;
import static de.kaleidox.jumpcube.chat.MessageLevel.INFO;
import static de.kaleidox.jumpcube.cube.BlockBar.MaterialGroup.CUBE;
import static de.kaleidox.jumpcube.cube.BlockBar.MaterialGroup.GALLERY;
import static de.kaleidox.jumpcube.cube.BlockBar.MaterialGroup.WALLS;

public class ExistingCube implements Cube, Generatable {
    private final static Map<String, Cube> instances = new ConcurrentHashMap<>();
    private final String name;
    private final World world;
    private final int[][] pos;
    private final BlockBar bar;
    private final int galleryHeight = 19;
    private final double density = 0.183; // todo Add changeable density
    private final int height = 110; // todo Add changeable height
    private final double spacing = 0.2;
    public final GameManager manager;
    private int[][] tpPos;
    private int tpCycle = -1;
    private long startNanos = -1;

    private ExistingCube(String name, World world, int[][] positions, BlockBar bar) {
        this.name = name;
        this.world = world;
        this.pos = positions;
        this.bar = bar;

        this.manager = new GameManager(this);

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

    public void teleportIn(Player player) {
        if (tpCycle++ > 3) tpCycle = 0;
        Location location = BukkitUtil.getLocation(world, tpPos[tpCycle]);
        player.teleport(location.add(0, 1.2, 0));
    }

    public void generateFull() {
        startNanos = nanoTime();

        // generate outter walls

        int highest = (pos[0][1] < pos[1][1] ? pos[1][1] : pos[0][1]);
        boolean smallX = pos[0][0] < pos[1][0];
        int offsetX = smallX ? 1 : -1;
        boolean smallZ = pos[0][2] < pos[1][2];
        int offsetZ = smallZ ? 1 : -1;

        for (int movX = pos[0][0]; (smallX ? movX < pos[1][0] + offsetX : movX > pos[1][0] + offsetX); movX += offsetX)
            for (int movY = highest; movY > 0; movY--) {
                world.getBlockAt(movX, movY, pos[0][2]).setType(bar.getRandomMaterial(WALLS));
                //System.out.println("[CYCLE 1]\tx = " + movX + " && y = " + movY + " && z = " + pos[0][2]);
            }
        for (int movX = pos[1][0]; (smallX ? movX > pos[0][0] - offsetX : movX < pos[0][0] - offsetX); movX -= offsetX)
            for (int movY = highest; movY > 0; movY--) {
                world.getBlockAt(movX, movY, pos[1][2]).setType(bar.getRandomMaterial(WALLS));
                //System.out.println("[CYCLE 2]\tx = " + movX + " && y = " + movY + " && z = " + pos[1][2]);
            }

        for (int movZ = pos[0][2] + offsetZ; (smallZ ? movZ < pos[1][2] : movZ > pos[1][2]); movZ += offsetZ)
            for (int movY = highest; movY > 0; movY--) {
                world.getBlockAt(pos[0][0], movY, movZ).setType(bar.getRandomMaterial(WALLS));
                //System.out.println("[CYCLE 3]\tx = " + pos[0][0] + " && y = " + movY + " && z = " + movZ);
            }
        for (int movZ = pos[1][2] - offsetZ; (smallZ ? movZ > pos[0][2] : movZ < pos[0][2]); movZ -= offsetZ)
            for (int movY = highest; movY > 0; movY--) {
                world.getBlockAt(pos[1][0], movY, movZ).setType(bar.getRandomMaterial(WALLS));
                //System.out.println("[CYCLE 4]\tx = " + pos[1][0] + " && y = " + movY + " && z = " + movZ);
            }

        // clear inner area
        for (int movX = pos[0][0] + offsetX; (smallX ? movX < pos[1][0] : movX > pos[1][0]); movX += offsetX)
            for (int movZ = pos[0][2] + offsetZ; (smallZ ? movZ < pos[1][2] : movZ > pos[1][2]); movZ += offsetX)
                for (int movY = 255; movY > 0; movY--) {
                    world.getBlockAt(movX, movY, movZ).setType(Material.AIR);
                    /*
                    if (movX > 300 || movY > 255 || movZ < -300)
                        System.err.println("[CYCLE 5]\tx = " + movX + " && y = " + movY + " && z = " + movZ);
                        */
                }

        for (int off : new int[]{1, 2}) {
            int minX = pos[smallX ? 0 : 1][0] + off;
            int maxX = pos[smallX ? 1 : 0][0] - off;
            int minZ = pos[smallZ ? 0 : 1][2] + off;
            int maxZ = pos[smallZ ? 1 : 0][2] - off;

            for (int x = minX; x <= maxX; x++)
                for (int z = minZ; z <= maxZ; z++) {
                    if (x == minX || x == maxX || z == minZ || z == maxZ) {
                        world.getBlockAt(x, galleryHeight, z).setType(bar.getRandomMaterial(GALLERY));
                        if (off == 1)
                            world.getBlockAt(x, galleryHeight + 3, z).setType(Material.GLASS);
                        if (off == 2)
                            world.getBlockAt(x, galleryHeight + 1, z).setType(Material.GLASS_PANE);
                        //System.out.println("[CYCLE 6]\tx = " + x + " && y = " + galleryHeight + " && z = " + z);
                    }
                    if (off == 1) {
                        world.getBlockAt(x, 1, z).setType(Material.LAVA);
                        world.getBlockAt(x, 2, z).setType(Material.LAVA);
                        world.getBlockAt(x, 3, z).setType(Material.LAVA);
                    }
                }

            if (off == 1)
                tpPos = new int[][]{
                        new int[]{minX, galleryHeight + 1, minZ},
                        new int[]{maxX, galleryHeight + 1, maxZ},
                        new int[]{minX, galleryHeight + 1, maxZ},
                        new int[]{maxX, galleryHeight + 1, minZ}
                };
        }

        generate();
    }

    @Override
    public void generate() {
        if (startNanos == -1) startNanos = nanoTime();

        int highest = (pos[0][1] < pos[1][1] ? pos[1][1] : pos[0][1]);
        boolean smallX = pos[0][0] < pos[1][0];
        int offsetX = smallX ? 1 : -1;
        boolean smallZ = pos[0][2] < pos[1][2];
        int offsetZ = smallZ ? 1 : -1;

        int sizeX = pos[0][0] - pos[1][0], sizeZ = pos[0][2] - pos[1][2];
        if (sizeX < 0) sizeX = sizeX * -1;
        if (sizeZ < 0) sizeZ = sizeZ * -1;

        final Material[][][] matrix = new Material[sizeX - (int) (sizeX * (spacing * 2))][height][sizeZ - (int) (sizeZ * (spacing * 2))];

        for (int x = 0; x < matrix.length; x++)
            for (int y = 0; y < matrix[x].length; y++)
                for (int z = 0; z < matrix[x][y].length; z++) {
                    if (JumpCube.rng.nextDouble() % 1 > density)
                        matrix[x][y][z] = Material.AIR;
                    else matrix[x][y][z] = bar.getRandomMaterial(CUBE);
                    //System.out.println("[MATRIX]\tx = " + x + " && y = " + y + " && z = " + z);
                }

        int mX = pos[smallX ? 0 : 1][0] + (int) (sizeX * spacing);
        int mZ = pos[smallX ? 0 : 1][2] + (int) (sizeZ * spacing);
        for (int x = 0; x < matrix.length; x++)
            for (int y = 0; y < matrix[x].length; y++)
                for (int z = 0; z < matrix[x][y].length; z++) {
                    int uX = mX + x, uY = y + 10, uZ = mZ + z;
                    world.getBlockAt(uX, uY, uZ).setType(matrix[x][y][z]);
                    /*
                    if (uX > 300 || uY > 255 || uZ < -300)
                        System.err.println("[CYCLE 7]\tx = " + uX + " && y = " + uY + " && z = " + uZ);
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
            sel.getBlockBar().validate();
            if (full) ((ExistingCube) sel).generateFull();
            else ((ExistingCube) sel).generate();

            message(sender, INFO, "Cube was regenerated!");
        }
    }
}
