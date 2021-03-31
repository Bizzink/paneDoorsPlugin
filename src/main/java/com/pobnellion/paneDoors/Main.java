package com.pobnellion.paneDoors;

import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class Main extends JavaPlugin {
    // :)
    private static Main instance;
    private static List<PaneDoor> doors;
    private static FileConfiguration config;
    private static Logger logger;

    @Override
    public void onEnable() {
        instance = this;
        doors = new ArrayList<>();
        logger = getLogger();

        this.getCommand("panedoor").setExecutor(new CommandPaneDoor());

        saveDefaultConfig();
        config = getConfig();
        int doorCount = loadDoors();

        logger.info("Loaded " + doorCount + " doors from config.");
    }

    public static Main getInstance() {
        return instance;
    }

    public static void log(String msg) {
        logger.info(msg);
    }

    private int loadDoors() {
        int i = -1;

        while (config.get("doors." + ++i) != null) {
            Axis axis;
            try {
                axis = Axis.valueOf(config.getString("doors." + i + ".axis"));
            }
            catch (NullPointerException e) {
                logger.info("Missing axis in door " + i + "!");
                continue;
            }

            List<Block> doorBlocks = new ArrayList<>();
            List<String> blocks = config.getStringList("doors." + i + ".blocks");

            for (String blockLocation: blocks) {
                int x = Integer.parseInt(blockLocation.split(", ")[0]);
                int y = Integer.parseInt(blockLocation.split(", ")[1]);
                int z = Integer.parseInt(blockLocation.split(", ")[2]);

                Block block = Bukkit.getWorlds().get(0).getBlockAt(x, y, z);
                doorBlocks.add(block);
            }

            doors.add(new PaneDoor(doorBlocks, axis));
        }

        return i;
    }

    private void addDoorToConfig(PaneDoor door) {
        config.set("doors." + doors.indexOf(door) + ".axis", door.getAxis().toString());

        List<String> doorBlocks = new ArrayList<>();

        for (Block block: door.getDoorBlocks()) {
            int x = block.getLocation().getBlockX();
            int y = block.getLocation().getBlockY();
            int z = block.getLocation().getBlockZ();

            doorBlocks.add(x + ", " + y + ", " + z);
        }

        config.set("doors." + doors.indexOf(door) + ".blocks", doorBlocks);
        saveConfig();
    }

    public void addDoor(PaneDoor door) {
        doors.add(door);
        addDoorToConfig(door);
    }

    public void deleteDoor(PaneDoor door) {
        door.disableHighlight();
        doors.remove(door);

        config.set("doors", null);

        for (PaneDoor d: doors) {
            addDoorToConfig(d);
        }

        saveConfig();
    }

    public static List<PaneDoor> getDoors() {
        return doors;
    }
}
