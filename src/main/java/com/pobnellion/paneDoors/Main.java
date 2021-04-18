package com.pobnellion.paneDoors;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Main extends JavaPlugin {
    private static Main instance;
    private static List<PaneDoor> doors;
    private static FileConfiguration config;
    private static int highlightViewDist;
    private static Color highlightColour;
    private static int searchRadius;

    @Override
    public void onEnable() {
        instance = this;
        doors = new ArrayList<>();

        this.getCommand("panedoor").setExecutor(new CommandPaneDoor());
        getServer().getPluginManager().registerEvents(new DoorToolListener(), this);
        getServer().getPluginManager().registerEvents(new DoorBlockListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerMoveListener(), this);

        saveDefaultConfig();
        config = getConfig();
        searchRadius = config.getInt("searchradius");
        highlightViewDist = config.getInt("highlight.dist");
        highlightColour = Color.fromRGB(
                config.getInt("highlight.r"),
                config.getInt("highlight.g"),
                config.getInt("highlight.b")
        );

        int doorCount = loadDoors();
        getLogger().info("Loaded " + doorCount + " doors from config.");
    }

    public static Main getInstance() {
        return instance;
    }

    private int loadDoors() {
        int i = -1;

        while (config.get("doors." + ++i) != null) {
            Axis axis = Axis.valueOf(config.getString("doors." + i + ".axis"));
            List<Block> doorBlocks = new ArrayList<>();

            for (String blockLocation: config.getStringList("doors." + i + ".blocks")) {
                String[] pos = blockLocation.split(", ");

                Block block = Bukkit.getWorlds().get(0).getBlockAt(
                        Integer.parseInt(pos[0]),
                        Integer.parseInt(pos[1]),
                        Integer.parseInt(pos[2])
                );
                doorBlocks.add(block);
            }

            Set<String> allowTags = new HashSet<>();
            allowTags.addAll(config.getStringList("doors." + i + ".allow"));

            doors.add(new PaneDoor(doorBlocks, axis, allowTags));
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

        config.set("doors." + doors.indexOf(door) + ".allow", new ArrayList<>(door.getAllowTags()));

        saveConfig();
    }

    public static void addDoor(PaneDoor door) {
        doors.add(door);
        instance.addDoorToConfig(door);
    }

    public static void deleteDoor(PaneDoor door) {
        door.setHighlightColor(Color.fromRGB(255, 0, 0));
        door.disableHighlight(null);
        door.displayDeleteHighlight();
        doors.remove(door);
        config.set("doors", null);

        for (PaneDoor d: doors) {
            instance.addDoorToConfig(d);
        }

        instance.saveConfig();
    }

    public static void updateDoor(PaneDoor door) {
        // update door allow tags
        config.set("doors." + doors.indexOf(door) + ".allow", new ArrayList<>(door.getAllowTags()));
    }

    public static List<PaneDoor> getDoors() {
        return doors;
    }

    public static int getHighlightViewDist() {
        return highlightViewDist;
    }

    public static void setHighlightViewDist(int dist) {
        highlightViewDist = dist;
        config.set("highlight.dist", dist);
        instance.saveConfig();
    }

    public static void setHighlightColour(int r, int g, int b) {
        highlightColour = Color.fromRGB(r, g, b);

        for (PaneDoor door: doors) {
            door.setHighlightColor(highlightColour);
        }

        config.set("highlight.r", r);
        config.set("highlight.g", g);
        config.set("highlight.b", b);
        instance.saveConfig();
    }

    public static Color getHighlightColour() {
        return highlightColour;
    }

    // only modifiable by directly editing the config
    public static int getSearchRadius() {
        return searchRadius;
    }
}
