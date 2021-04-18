package com.pobnellion.paneDoors;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.type.GlassPane;
import org.bukkit.entity.Player;

import java.util.*;

public class PaneDoor {
    private final List<Block> doorBlocks;
    private final Axis axis;
    private int highlightTaskID = -1;
    private Location center;
    private final List<Location> highlightParticleLocations;
    private Color highlightColor;
    private final Set<Player> highlightViewers = new HashSet<>();

    public PaneDoor(Block startBlock, boolean respectColour) {
        if (!isValidDoorBlock(startBlock, null)) {
            throw new IllegalArgumentException("Invalid block!");
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
        this.highlightColor = Main.getHighlightColour();
        this.findDoorBlocks(startBlock, startBlock, respectColour);
        this.updateDoorBlocks();
    }

    public PaneDoor(List<Block> blocks, Axis axis) {
        this.axis = axis;
        this.doorBlocks = blocks;
        this.highlightParticleLocations = new ArrayList<>();
        this.highlightColor = Main.getHighlightColour();
        this.updateDoorBlocks();
    }

    public static boolean isValidDoorBlock(Block block, Axis axis) {
        if (!(block.getBlockData() instanceof GlassPane)) {
            return false;
        }

        Set<BlockFace> faces = ((GlassPane) block.getBlockData()).getFaces();

        if (faces.size() != 2) {
            return false;
        }

        boolean ns = faces.contains(BlockFace.NORTH) && faces.contains(BlockFace.SOUTH);
        boolean ew = faces.contains(BlockFace.EAST) && faces.contains(BlockFace.WEST);

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

    private void findDoorBlocks(Block block, Block startBlock, boolean respectColour) {
        // check distance
        if (Math.abs(block.getX() - startBlock.getX()) > Main.getSearchRadius() ||
            Math.abs(block.getY() - startBlock.getY()) > Main.getSearchRadius() ||
            Math.abs(block.getZ() - startBlock.getZ()) > Main.getSearchRadius()
        ) {
            return;
        }

        // check colour
        if (respectColour) {
            if (block.getBlockData().getMaterial() != startBlock.getBlockData().getMaterial()) {
                return;
            }
        }

        if (!this.doorBlocks.contains(block) && isValidDoorBlock(block, this.axis)) {
            this.doorBlocks.add(block);
            World world = block.getWorld();

            // up
            findDoorBlocks(world.getBlockAt(block.getLocation().add(0, 1, 0)), startBlock, respectColour);
            // down
            findDoorBlocks(world.getBlockAt(block.getLocation().add(0, -1, 0)), startBlock, respectColour);

            if (this.axis == Axis.NS) {
                // north
                findDoorBlocks(world.getBlockAt(block.getLocation().add(0, 0, -1)), startBlock, respectColour);
                // south
                findDoorBlocks(world.getBlockAt(block.getLocation().add(0, 0, 1)), startBlock, respectColour);
            }
            else {
                // east
                findDoorBlocks(world.getBlockAt(block.getLocation().add(1, 0, 0)), startBlock, respectColour);
                // west
                findDoorBlocks(world.getBlockAt(block.getLocation().add(-1, 0, 0)), startBlock, respectColour);
            }
        }
    }

    public void enableHighlight(Player player) {
        this.highlightViewers.add(player);

        if (this.highlightTaskID == -1) {
            this.highlightTaskID = Bukkit.getScheduler().scheduleSyncRepeatingTask(Main.getInstance(), this::highlightDoorBlocks, 0L, 3L);
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
        Particle.DustOptions dustOptions = new Particle.DustOptions(this.highlightColor, 0.75f);

        for (Player highlightViewer: highlightViewers) {
            if (highlightViewer.getLocation().distance(this.center) <= Main.getHighlightViewDist()) {

                // stagger particle spawning so they appear more consistently on the door
                for (int i = 0; i < 3; i ++) {
                    for (int j = 0; j < this.highlightParticleLocations.size() / 3; j++) {
                        highlightViewer.spawnParticle(Particle.REDSTONE,
                                this.highlightParticleLocations.get((3 * j ) + i),
                                1, dustOptions);
                    }
                }
            }
        }
    }

    public void displayDeleteHighlight() {
        Particle.DustOptions dustOptions = new Particle.DustOptions(this.highlightColor, 0.75f);

        // stagger particle spawning so they appear more consistently on the door
        for (int i = 0; i < 3; i ++) {
            for (int j = 0; j < this.highlightParticleLocations.size() / 3; j++) {
                this.doorBlocks.get(0).getWorld().spawnParticle(Particle.REDSTONE,
                        this.highlightParticleLocations.get((3 * j ) + i),
                        1, dustOptions);
            }
        }
    }

    public void setHighlightColor(Color color) {
        this.highlightColor = color;
    }

    public void updateDoorBlocks() {
        World world = this.doorBlocks.get(0).getWorld();
        this.center = new Location(world, 0, 0, 0);

        int min_y = this.doorBlocks.stream().min(Comparator.comparing(Block::getY)).get().getY();
        int max_y = this.doorBlocks.stream().max(Comparator.comparing(Block::getY)).get().getY();
        int avg_y = min_y + ((max_y - min_y) / 2);
        this.center.setY(avg_y);

        if (this.axis == Axis.NS) {
            int min_z = this.doorBlocks.stream().min(Comparator.comparing(Block::getZ)).get().getZ();
            int max_z = this.doorBlocks.stream().max(Comparator.comparing(Block::getZ)).get().getZ();
            int avg_z = min_z + ((max_z - min_z) / 2);
            this.center.setZ(avg_z);
            this.center.setX(this.doorBlocks.get(0).getX());
        }
        else {
            int min_x = this.doorBlocks.stream().min(Comparator.comparing(Block::getX)).get().getX();
            int max_x = this.doorBlocks.stream().max(Comparator.comparing(Block::getX)).get().getX();
            int avg_x = min_x + ((max_x - min_x) / 2);
            this.center.setX(avg_x);
            this.center.setZ(this.doorBlocks.get(0).getZ());
        }

        float interval = 7.0f;

        if (this.axis == Axis.NS) {
            for (Block block : this.doorBlocks) {
                // top
                if (!this.contains(world.getBlockAt(block.getLocation().add(0, 1, 0)))) {
                    for (float i = 0.0f; i < 1; i += 1.0f / interval) {
                        this.highlightParticleLocations.add(block.getLocation().add(0.4, 0.95, i));
                        this.highlightParticleLocations.add(block.getLocation().add(0.6, 0.95, i));
                    }
                }
                // bottom
                if (!this.contains(world.getBlockAt(block.getLocation().add(0, -1, 0)))) {
                    for (float i = 0.0f; i < 1; i += 1.0f / interval) {
                        this.highlightParticleLocations.add(block.getLocation().add(0.4, 0.05, i));
                        this.highlightParticleLocations.add(block.getLocation().add(0.6, 0.05, i));
                    }
                }
                // north
                if (!this.contains(world.getBlockAt(block.getLocation().add(0, 0, -1)))) {
                    for (float i = 0.0f; i < 1; i += 1.0f / interval) {
                        this.highlightParticleLocations.add(block.getLocation().add(0.4, i, 0.05));
                        this.highlightParticleLocations.add(block.getLocation().add(0.6, i, 0.05));
                    }
                }
                // south
                if (!this.contains(world.getBlockAt(block.getLocation().add(0, 0, 1)))) {
                    for (float i = 0.0f; i < 1; i += 1.0f / interval) {
                        this.highlightParticleLocations.add(block.getLocation().add(0.4, i, 0.95));
                        this.highlightParticleLocations.add(block.getLocation().add(0.6, i, 0.95));
                    }
                }
            }
        }
        else {
            for (Block block : this.doorBlocks) {
                // top
                if (!this.contains(world.getBlockAt(block.getLocation().add(0, 1, 0)))) {
                    for (float i = 0.0f; i < 1; i += 1.0f / interval) {
                        this.highlightParticleLocations.add(block.getLocation().add(i, 0.95, 0.4));
                        this.highlightParticleLocations.add(block.getLocation().add(i, 0.95, 0.6));
                    }
                }
                // bottom
                if (!this.contains(world.getBlockAt(block.getLocation().add(0, -1, 0)))) {
                    for (float i = 0.0f; i < 1; i += 1.0f / interval) {
                        this.highlightParticleLocations.add(block.getLocation().add(i, 0.05, 0.4));
                        this.highlightParticleLocations.add(block.getLocation().add(i, 0.05, 0.6));
                    }
                }
                // east
                if (!this.contains(world.getBlockAt(block.getLocation().add(-1, 0, 0)))) {
                    for (float i = 0.0f; i < 1; i += 1.0f / interval) {
                        this.highlightParticleLocations.add(block.getLocation().add(0.05, i, 0.4));
                        this.highlightParticleLocations.add(block.getLocation().add(0.05, i, 0.6));
                    }
                }
                // west
                if (!this.contains(world.getBlockAt(block.getLocation().add(1, 0, 0)))) {
                    for (float i = 0.0f; i < 1; i += 1.0f / interval) {
                        this.highlightParticleLocations.add(block.getLocation().add(0.95, i, 0.4));
                        this.highlightParticleLocations.add(block.getLocation().add(0.95, i, 0.6));
                    }
                }
            }
        }
    }

    public List<Block> getDoorBlocks() {
        return this.doorBlocks;
    }

    public boolean contains(Block block) {
        return this.doorBlocks.contains(block);
    }

    public Axis getAxis() {
        return this.axis;
    }
}
