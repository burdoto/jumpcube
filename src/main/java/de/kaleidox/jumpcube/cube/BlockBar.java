package de.kaleidox.jumpcube.cube;

import de.kaleidox.jumpcube.util.BukkitUtil;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

public class BlockBar {
    private static Material[] configMaterials = new Material[8];
    private Material[] materials;

    public BlockBar(Player player, @Nullable Material[] materials) {
        if (materials != null) this.materials = materials;
        else this.materials = configMaterials;

        place(player);
    }

    private void place(Player nearPlayer) {
        World world = nearPlayer.getWorld();
        Location loc = nearPlayer.getLocation();
        int x = loc.getBlockX(), y = loc.getBlockY(), z = loc.getBlockZ();
        final int[] c = {0};

        for (int addY : new int[]{1, 0})
            for (int addX : new int[]{1, 2, 3, 4})
                world.getBlockAt(x + addX, y + addY, z).setType(materials[c[0]++]);
    }

    public static void initConfig(FileConfiguration config) {
        configMaterials[0] = BukkitUtil.getMaterial(config.getString("cube.defaults.bar.a"))
                .orElse(Material.RED_WOOL);
        configMaterials[1] = BukkitUtil.getMaterial(config.getString("cube.defaults.bar.b"))
                .orElse(Material.YELLOW_WOOL);
        configMaterials[2] = BukkitUtil.getMaterial(config.getString("cube.defaults.bar.c"))
                .orElse(Material.BLUE_WOOL);
        configMaterials[3] = BukkitUtil.getMaterial(config.getString("cube.defaults.bar.placeable"))
                .orElse(Material.PUMPKIN);
        configMaterials[4] = BukkitUtil.getMaterial(config.getString("cube.defaults.bar.aFix"))
                .orElse(Material.RED_CONCRETE);
        configMaterials[5] = BukkitUtil.getMaterial(config.getString("cube.defaults.bar.bFix"))
                .orElse(Material.YELLOW_CONCRETE);
        configMaterials[6] = BukkitUtil.getMaterial(config.getString("cube.defaults.bar.cFix"))
                .orElse(Material.BLUE_CONCRETE);
        configMaterials[7] = BukkitUtil.getMaterial(config.getString("cube.defaults.bar.dFix"))
                .orElse(Material.LIGHT_GRAY_CONCRETE);
    }
}
