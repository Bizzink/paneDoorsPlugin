package com.pobnellion.paneDoors;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.type.GlassPane;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PaneDoor {
    private final List<Block> doorBlocks;
    private final Axis axis;
    private int highlightTaskID = -1;
    private final List<Location> highlightParticleLocations;
    private Color highlightColor;
    private  final Set<Player> highlightViewers = new HashSet<>();

    public PaneDoor(Block startBlock) {
        if (!(startBlock.getBlockData() instanceof GlassPane)) {
            throw new IllegalArgumentException("Block must be an instance of GlassPane!");
        }

        Set<BlockFace> faces = ((GlassPane) startBlock.getBlockData()).getFaces();

        if (faces.size() == 2 && faces.stream().allMatch(face -> face == BlockFace.NORTH || face == BlockFace.SOUTH)) {
            this.axis = Axis.NS;
        }
        else if (faces.size() == 2 && faces.stream().allMatch(face -> face == BlockFace.EAST || face == BlockFace.WEST)) {
            this.axis = Axis.EW;
        }
        else {
            throw new IllegalArgumentException("Pane must be either north & south or east & west!");
        }

        this.doorBlocks = new ArrayList<>();
        this.highlightParticleLocations = new ArrayList<>();
        this.highlightColor = Color.fromRGB(135, 30, 255);
        this.findDoorBlocks(startBlock);
        this.updateDoorBlocks();
    }

    public PaneDoor(List<Block> blocks, Axis axis) {
        this.doorBlocks = blocks;
        this.axis = axis;
        this.highlightParticleLocations = new ArrayList<>();
        this.highlightColor = Color.fromRGB(135, 30, 255);
        this.updateDoorBlocks();
    }

    public static boolean isValidDoorBlock(Block block, Axis axis) {
        if (!(block.getBlockData() instanceof GlassPane)) {
            return false;
        }

        Set<BlockFace> faces = ((GlassPane) block.getBlockData()).getFaces();

        boolean ns = faces.size() == 2 && faces.stream().allMatch(face -> face == BlockFace.NORTH || face == BlockFace.SOUTH);
        boolean ew = faces.size() == 2 && faces.stream().allMatch(face -> face == BlockFace.EAST || face == BlockFace.WEST);

        if (axis == null) {
            return ns || ew;
        }
        else if (axis == Axis.NS) {
            return ns;
        }
        else if (axis == Axis.EW) {
            return ew;
        }
        else {
            return false;
        }
    }

    private void findDoorBlocks(Block block) {
        if (!this.doorBlocks.contains(block) && isValidDoorBlock(block, this.axis)) {
            this.doorBlocks.add(block);

            World world = block.getWorld();

            // TODO: max search size

            // up
            findDoorBlocks(world.getBlockAt(block.getLocation().add(0, 1, 0)));
            // down
            findDoorBlocks(world.getBlockAt(block.getLocation().add(0, -1, 0)));

            if (this.axis == Axis.NS) {
                // north
                findDoorBlocks(world.getBlockAt(block.getLocation().add(0, 0, -1)));
                // south
                findDoorBlocks(world.getBlockAt(block.getLocation().add(0, 0, 1)));
            }
            else {
                // east
                findDoorBlocks(world.getBlockAt(block.getLocation().add(1, 0, 0)));
                // west
                findDoorBlocks(world.getBlockAt(block.getLocation().add(-1, 0, 0)));
            }
        }
    }

    public void enableHighlight(Player player) {
        this.highlightViewers.add(player);

        if (this.highlightTaskID == -1) {
            this.highlightTaskID = Bukkit.getScheduler().scheduleSyncRepeatingTask(Main.getInstance(), this::highlightDoorBlocks, 0L, 2L);
        }
    }

    public void disableHighlight(Player player) {
        if (player == null) {
            this.highlightViewers.clear();
        }
        else {
            this.highlightViewers.remove(player);
        }

        if (this.highlightViewers.size() == 0 && this.highlightTaskID != -1) {
            Bukkit.getScheduler().cancelTask(this.highlightTaskID);
            this.highlightTaskID = -1;
        }
    }

    public void toggleHighlight(Player player) {
        if (this.highlightViewers.contains(player)) {
            this.disableHighlight(player);
        }
        else {
            this.enableHighlight(player);
        }
    }

    public boolean isHighlightedForPlayer(Player player) {
        return this.highlightViewers.contains(player);
    }

    private void highlightDoorBlocks() {
        Particle.DustOptions dustOptions = new Particle.DustOptions(this.highlightColor, 1);

        for (Player highlightViewer: highlightViewers) {
            for (Location particleLocation : this.highlightParticleLocations) {
                highlightViewer.spawnParticle(Particle.REDSTONE, particleLocation, 1, dustOptions);
            }
        }
    }

    public void setHighlightColor(int r, int g, int b) {
        this.highlightColor = Color.fromRGB(r, g, b);
    }

    public void updateDoorBlocks() {
        this.doorBlocks.removeIf(block -> !isValidDoorBlock(block, this.axis));

        this.highlightParticleLocations.clear();
        float interval = 5.0f;

        if (this.axis == Axis.NS) {
            for (Block block : this.doorBlocks) {
                // top
                if (!isDoorBlock(block.getLocation().add(0, 1, 0))) {
                    for (float i = 0.0f; i < 1; i += 1.0f / interval) {
                        this.highlightParticleLocations.add(block.getLocation().add(0.4, 1, i));
                        this.highlightParticleLocations.add(block.getLocation().add(0.6, 1, i));
                    }
                }
                // bottom
                if (!isDoorBlock(block.getLocation().add(0, -1, 0))) {
                    for (float i = 0.0f; i < 1; i += 1.0f / interval) {
                        this.highlightParticleLocations.add(block.getLocation().add(0.4, 0, i));
                        this.highlightParticleLocations.add(block.getLocation().add(0.6, 0, i));
                    }
                }
                // north
                if (!isDoorBlock(block.getLocation().add(0, 0, -1))) {
                    for (float i = 0.0f; i < 1; i += 1.0f / interval) {
                        this.highlightParticleLocations.add(block.getLocation().add(0.4, i, 0));
                        this.highlightParticleLocations.add(block.getLocation().add(0.6, i, 0));
                    }
                }
                // south
                if (!isDoorBlock(block.getLocation().add(0, 0, 1))) {
                    for (float i = 0.0f; i < 1; i += 1.0f / interval) {
                        this.highlightParticleLocations.add(block.getLocation().add(0.4, i, 1));
                        this.highlightParticleLocations.add(block.getLocation().add(0.6, i, 1));
                    }
                }
            }
        }
        else {
            for (Block block : this.doorBlocks) {
                // top
                if (!isDoorBlock(block.getLocation().add(0, 1, 0))) {
                    for (float i = 0.0f; i < 1; i += 1.0f / interval) {
                        this.highlightParticleLocations.add(block.getLocation().add(i, 1, 0.4));
                        this.highlightParticleLocations.add(block.getLocation().add(i, 1, 0.6));
                    }
                }
                // bottom
                if (!isDoorBlock(block.getLocation().add(0, -1, 0))) {
                    for (float i = 0.0f; i < 1; i += 1.0f / interval) {
                        this.highlightParticleLocations.add(block.getLocation().add(i, 0, 0.4));
                        this.highlightParticleLocations.add(block.getLocation().add(i, 0, 0.6));
                    }
                }
                // north
                if (!isDoorBlock(block.getLocation().add(-1, 0, 0))) {
                    for (float i = 0.0f; i < 1; i += 1.0f / interval) {
                        this.highlightParticleLocations.add(block.getLocation().add(0, i, 0.4));
                        this.highlightParticleLocations.add(block.getLocation().add(0, i, 0.6));
                    }
                }
                // south
                if (!isDoorBlock(block.getLocation().add(1, 0, 0))) {
                    for (float i = 0.0f; i < 1; i += 1.0f / interval) {
                        this.highlightParticleLocations.add(block.getLocation().add(1, i, 0.4));
                        this.highlightParticleLocations.add(block.getLocation().add(1, i, 0.6));
                    }
                }
            }
        }
    }

    public List<Block> getDoorBlocks() {
        this.updateDoorBlocks();
        return this.doorBlocks;
    }

    public boolean isDoorBlock(Block block) {
        this.updateDoorBlocks();
        return this.doorBlocks.contains(block);
    }

    public boolean isDoorBlock(Location location) {

        for (Block doorBlock: this.doorBlocks) {
            if (doorBlock.getLocation().equals(location)) {
                return true;
            }
        }

        return false;
    }

    public Axis getAxis() {
        return this.axis;
    }
}
