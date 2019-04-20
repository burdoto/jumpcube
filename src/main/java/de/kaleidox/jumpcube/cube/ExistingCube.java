package de.kaleidox.jumpcube.cube;

import java.util.Comparator;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import de.kaleidox.jumpcube.JumpCube;
import de.kaleidox.jumpcube.exception.DuplicateCubeException;
import de.kaleidox.jumpcube.exception.NoSuchCubeException;
import de.kaleidox.jumpcube.game.GameManager;
import de.kaleidox.jumpcube.interfaces.Generatable;
import de.kaleidox.jumpcube.interfaces.Startable;
import de.kaleidox.jumpcube.util.WorldUtil;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.lang.System.nanoTime;
import static de.kaleidox.jumpcube.chat.Chat.message;
import static de.kaleidox.jumpcube.chat.MessageLevel.INFO;
import static de.kaleidox.jumpcube.cube.BlockBar.MaterialGroup.CUBE;
import static de.kaleidox.jumpcube.cube.BlockBar.MaterialGroup.GALLERY;
import static de.kaleidox.jumpcube.cube.BlockBar.MaterialGroup.WALLS;
import static de.kaleidox.jumpcube.util.WorldUtil.mid;
import static de.kaleidox.jumpcube.util.WorldUtil.xyz;
import static org.bukkit.Material.AIR;
import static org.bukkit.Material.GLASS;
import static org.bukkit.Material.GLASS_PANE;
import static org.bukkit.Material.LAVA;

public class ExistingCube implements Cube, Generatable, Startable {
    private final static Map<String, Cube> instances = new ConcurrentHashMap<>();
    public final GameManager manager;
    private final String name;
    private final World world;
    private final int[][] pos;
    private final BlockBar bar;
    private final int galleryHeight = 19;
    private final double density = 0.183; // todo Add changeable density
    private final int height = 110; // todo Add changeable height
    private final double spacing = 0.2;
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

    @Override
    public World getWorld() {
        return world;
    }

    public void teleportIn(Player player) {
        if (tpCycle++ > 3) tpCycle = 0;
        Location location = WorldUtil.getLocation(world, tpPos[tpCycle]);
        player.teleport(location.add(0, 1.2, 0));
    }

    public void generateFull() {
        startNanos = nanoTime();

        final int maxY = max(pos[0][1], pos[1][1]);
        final boolean smallX = pos[0][0] < pos[1][0];
        final boolean smallZ = pos[0][2] < pos[1][2];

        int minX = min(pos[0][0], pos[1][0]);
        int maxX = max(pos[0][0], pos[1][0]);
        int minZ = min(pos[0][2], pos[1][2]);
        int maxZ = max(pos[0][2], pos[1][2]);

        int x, y, z;

        for (x = minX; x < maxX; x++)
            for (z = minZ; z < maxZ; z++)
                for (y = 255; y > 0; y--)
                    world.getBlockAt(x, y, z).setType(AIR);

        for (int off : new int[]{0, 1, 2}) {
            minX = min(pos[0][0], pos[1][0]) + off;
            maxX = max(pos[0][0], pos[1][0]) - off;
            minZ = min(pos[0][2], pos[1][2]) + off;
            maxZ = max(pos[0][2], pos[1][2]) - off;

            for (x = minX; x <= maxX; x++)
                for (z = minZ; z <= maxZ; z++) {
                    if (x == minX || x == maxX || z == minZ || z == maxZ) {
                        if (off == 0)
                            for (y = maxY; y > 0; y--)
                                world.getBlockAt(x, y, z).setType(bar.getRandomMaterial(WALLS));
                        else {
                            world.getBlockAt(x, galleryHeight, z).setType(bar.getRandomMaterial(GALLERY));
                            if (off == 1)
                                world.getBlockAt(x, galleryHeight + 3, z).setType(GLASS);
                            if (off == 2)
                                world.getBlockAt(x, galleryHeight + 1, z).setType(GLASS_PANE);
                        }
                    }

                    if (off == 1) {
                        world.getBlockAt(x, 1, z).setType(LAVA);
                        world.getBlockAt(x, 2, z).setType(LAVA);
                        world.getBlockAt(x, 3, z).setType(LAVA);
                    }
                }
        }

        generate();
    }

    @Override
    public void generate() {
        if (startNanos == -1) startNanos = nanoTime();

        final int spaceX = (int) (Math.abs(pos[1][0] - pos[0][0]) * spacing);
        final int spaceZ = (int) (Math.abs(pos[1][2] - pos[0][2]) * spacing);
        final boolean smallX = pos[0][0] < pos[1][0];
        final boolean smallZ = pos[0][2] < pos[1][2];

        int minX = min(pos[0][0], pos[1][0]) + spaceX;
        int maxX = max(pos[0][0], pos[1][0]) - spaceX;
        int minZ = min(pos[0][2], pos[1][2]) + spaceZ;
        int maxZ = max(pos[0][2], pos[1][2]) - spaceZ;

        int x, y, z;

        //todo: remove starting bridges

        for (x = minX; x < maxX; x++)
            for (z = minZ; z < maxZ; z++)
                for (y = 10; y < height; y++)
                    if (JumpCube.rng.nextDouble() % 1 > density) world.getBlockAt(x, y, z).setType(AIR);
                    else world.getBlockAt(x, y, z).setType(bar.getRandomMaterial(CUBE));

        assert JumpCube.getInstance() != null;
        JumpCube.getInstance().getLogger().info("Cube " + name + " was generated, took "
                + (nanoTime() - startNanos) + " nanoseconds.");
        startNanos = -1;
    }

    @Override
    public void start() {

    }

    @Nullable
    public static ExistingCube get(String name) {
        return (ExistingCube) instances.get(name);
    }

    public static boolean exists(String name) {
        return instances.containsKey(name);
    }

    public static Cube getSelection(Player player) {
        assert JumpCube.getInstance() != null;

        return Optional.ofNullable(JumpCube.getInstance().selections.get(player.getUniqueId()))
                .orElseGet(() -> {
                    if (instances.size() == 1) return instances.entrySet().iterator().next().getValue();
                    return instances.values()
                            .stream()
                            .filter(cube -> cube.getWorld().equals(player.getWorld()))
                            .min(Comparator.comparingDouble(cube ->
                                    WorldUtil.dist(new int[][]{
                                            mid(cube.getPositions()),
                                            xyz(player.getLocation())
                                    })
                            ))
                            .orElseThrow(() -> new NoSuchCubeException(player));
                });
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
