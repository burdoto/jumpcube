package de.kaleidox.jumpcube;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import de.kaleidox.jumpcube.chat.MessageLevel;
import de.kaleidox.jumpcube.cube.BlockBar;
import de.kaleidox.jumpcube.cube.Cube;
import de.kaleidox.jumpcube.cube.CubeCreationTool;
import de.kaleidox.jumpcube.cube.ExistingCube;
import de.kaleidox.jumpcube.exception.InnerCommandException;
import de.kaleidox.jumpcube.exception.InvalidArgumentCountException;
import de.kaleidox.jumpcube.game.GameManager;
import de.kaleidox.jumpcube.util.BukkitUtil;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static de.kaleidox.jumpcube.chat.Chat.message;
import static de.kaleidox.jumpcube.chat.MessageLevel.ERROR;
import static de.kaleidox.jumpcube.chat.MessageLevel.INFO;

public class JumpCube extends JavaPlugin {
    public static final Random rng = new Random();
    @Nullable private static JumpCube instance;

    public Map<UUID, Cube> selections = new ConcurrentHashMap<>();
    private Logger logger;

    @Override
    public boolean onCommand(@NotNull CommandSender sender,
                             @NotNull Command command,
                             @NotNull String label,
                             @NotNull String[] args) {
        UUID senderUuid = BukkitUtil.getUuid(sender);
        switch (label) {
            case "jumpcube":
            case "jc":
                if (!checkPerm(sender, Permission.USER)) return true;
                if (args.length == 0) {
                    message(sender, INFO, "JumpCube version 0.0.1");
                    return true;
                } else if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
                    message(sender, ERROR, "Reloading not yet implemented");
                    return true;
                } else {
                    subCommand(sender, args[0], Arrays.copyOfRange(args, 1, args.length));
                    return true;
                }
        }

        return super.onCommand(sender, command, label, args);
    }

    @Override
    public void onLoad() {
        super.onLoad();

        this.logger = getLogger();

        saveConfig();
        saveDefaultConfig();

        final FileConfiguration config = getConfig();
        if (!config.isSet("cube.defaults.bar.a"))
            config.set("cube.defaults.bar.a", "red_wool");
        if (!config.isSet("cube.defaults.bar.b"))
            config.set("cube.defaults.bar.b", "yellow_wool");
        if (!config.isSet("cube.defaults.bar.c"))
            config.set("cube.defaults.bar.c", "blue_wool");
        if (!config.isSet("cube.defaults.bar.placeable"))
            config.set("cube.defaults.bar.placeable", "pumpkin");
        if (!config.isSet("cube.defaults.bar.aFix"))
            config.set("cube.defaults.bar.aFix", "red_concrete");
        if (!config.isSet("cube.defaults.bar.bFix"))
            config.set("cube.defaults.bar.bFix", "yellow_concrete");
        if (!config.isSet("cube.defaults.bar.cFix"))
            config.set("cube.defaults.bar.cFix", "blue_concrete");
        if (!config.isSet("cube.defaults.bar.dFix"))
            config.set("cube.defaults.bar.dFix", "light_gray_concrete");
        if (!config.isSet("cube.defaults.height"))
            config.set("cube.defaults.height", 120);

        saveConfig();

        BlockBar.initConfig(config);

        logger.info("JumpCube loaded!");
    }

    @Override
    public void onDisable() {
        super.onDisable();
        instance = null;

        logger.info("JumpCube disabled!");
    }

    @Override
    public void onEnable() {
        super.onEnable();
        instance = this;

        final FileConfiguration config = getConfig();

        Optional.ofNullable(config.getString("cubes.created"))
                .map(str -> str.split(";"))
                .map(Arrays::asList)
                .ifPresent(list -> list.forEach(cubeName -> {
                    ExistingCube.load(config, cubeName, null);
                    logger.info("Loaded cube: " + cubeName);
                }));

        logger.info("JumpCube enabled!");
        logger.info("Please report bugs at https://github.com/burdoto/jumpcube/issues");
    }

    public boolean checkPerm(CommandSender sender, String permission) {
        if (sender.hasPermission(permission))
            return true;
        else {
            messagePerm(sender, permission);
            return false;
        }
    }

    private void subCommand(CommandSender sender, String subCommand, String[] args) {
        final UUID senderUuid = BukkitUtil.getUuid(sender);
        final Cube sel = selections.get(senderUuid);

        try {
            switch (subCommand.toLowerCase()) {
                case "create":
                    if (!checkPerm(sender, Permission.ADMIN)) return;
                    if (args.length != 1) throw new InvalidArgumentCountException(1, args.length);

                    if (ExistingCube.exists(args[0])) {
                        message(sender, ERROR, "A cube with that name already exists!");
                        return;
                    }

                    if (sel instanceof CubeCreationTool && !((CubeCreationTool) sel).isReady()) {
                        // delete old, nonready selection first
                        sel.delete();
                        selections.remove(senderUuid);
                    }

                    CubeCreationTool creationTool = new CubeCreationTool(BukkitUtil.getPlayer(sender));
                    creationTool.setName(args[0]);
                    selections.put(senderUuid, creationTool);
                    message(sender, INFO, "Cube " + args[0] + " created and selected!");
                    return;
                case "sel":
                case "select":
                    if (!checkPerm(sender, Permission.USER)) return;
                    if (args.length != 1) throw new InvalidArgumentCountException(1, args.length);

                    if (sel != null && sel.getCubeName().equals(args[0])) {
                        message(sender, INFO, "Cube " + args[0] + " is already selected!");
                        return;
                    }

                    if (!ExistingCube.exists(args[0])) {
                        message(sender, ERROR, "Cube " + args[0] + " does not exist!");
                        return;
                    }

                    ExistingCube cube = ExistingCube.get(args[0]);
                    assert cube != null;
                    selections.put(senderUuid, cube);
                    message(sender, INFO, "Cube " + args[0] + " selected!");
                    return;
                case "pos":
                case "pos1":
                case "pos2":
                    if (!checkPerm(sender, Permission.ADMIN)) return;
                    CubeCreationTool.Commands.pos(sender, sel, subCommand, args);
                    return;
                case "bar":
                    if (!checkPerm(sender, Permission.ADMIN)) return;
                    if (args.length != 0) throw new InvalidArgumentCountException(0, args.length);
                    CubeCreationTool.Commands.bar(sender, sel, args);
                    return;
                case "confirm":
                    if (!checkPerm(sender, Permission.ADMIN)) return;
                    if (args.length != 0) throw new InvalidArgumentCountException(0, args.length);
                    CubeCreationTool.Commands.confirm(sender, sel);
                    return;
                // Game commands
                case "regenerate":
                case "regen":
                    if (!checkPerm(sender, Permission.REGENERATE)) return;
                    if (!validateSelection(sender, sel)) return;
                    if (args.length != 0) throw new InvalidArgumentCountException(0, args.length);
                    ExistingCube.Commands.regenerate(sender, sel, false);
                    return;
                case "regenerate-complete":
                case "regen-complete":
                    if (!checkPerm(sender, Permission.REGENERATE)) return;
                    if (!validateSelection(sender, sel)) return;
                    if (args.length != 0) throw new InvalidArgumentCountException(0, args.length);
                    ExistingCube.Commands.regenerate(sender, sel, true);
                    return;
                case "join":
                    if (!checkPerm(sender, Permission.USER)) return;
                    if (!validateSelection(sender, sel)) return;
                    if (args.length != 0) throw new InvalidArgumentCountException(0, args.length);
                    ((ExistingCube) sel).manager.join(sender);
                    return;
            }
        } catch (InnerCommandException cEx) {
            message(sender, cEx.getLevel(), cEx.getIngameText());
            if (cEx.getLevel() == MessageLevel.EXCEPTION)
                cEx.printStackTrace(System.out);
        }
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

    private void messagePerm(CommandSender sender, String permission) {
        message(BukkitUtil.getPlayer(sender), ERROR,
                "You are missing the permission: " + ChatColor.GRAY + permission);
    }

    @Nullable
    public static JumpCube getInstance() {
        return instance;
    }

    public static final class Permission {
        public static final String USER = "jumpcube.user";

        public static final String REGENERATE = "jumpcube.mod.regenerate";

        public static final String ADMIN = "jumpcube.admin";
    }
}
