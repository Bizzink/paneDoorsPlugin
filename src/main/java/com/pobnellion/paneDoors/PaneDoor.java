package com.pobnellion.paneDoors;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.type.GlassPane;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class PaneDoor {
    private final List<Block> doorBlocks;
    private final Axis axis;
    private int highlightTaskID = -1;

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
        this.findDoorBlocks(startBlock);
        this.enableHighlight();
    }

    public PaneDoor(List<Block> blocks, Axis axis) {
        this.doorBlocks = blocks;
        this.axis = axis;
    }

    private boolean isValidDoorBlock(Block block) {
        if (!(block.getBlockData() instanceof GlassPane)) {
            return false;
        }

        Set<BlockFace> faces = ((GlassPane) block.getBlockData()).getFaces();

        if (this.axis == Axis.NS) {
            return faces.size() == 2 && faces.stream().allMatch(face -> face == BlockFace.NORTH || face == BlockFace.SOUTH);
        }
        else {
            return faces.size() == 2 && faces.stream().allMatch(face -> face == BlockFace.EAST || face == BlockFace.WEST);
        }
    }

    private void findDoorBlocks(Block block) {
        if (!this.doorBlocks.contains(block) && isValidDoorBlock(block)) {
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

    public void enableHighlight() {
        if (this.highlightTaskID == -1) {
            this.highlightTaskID = Bukkit.getScheduler().scheduleSyncRepeatingTask(Main.getInstance(), this::highlightDoorBlocks, 0L, 2L);
        }
    }

    public void disableHighlight() {
        if (this.highlightTaskID != -1) {
            Bukkit.getScheduler().cancelTask(this.highlightTaskID);
        }

        this.highlightTaskID = -1;
    }

    private void highlightDoorBlocks() {
        Particle.DustOptions dustOptions = new Particle.DustOptions(Color.fromRGB(135, 30, 255), 1);
        World world = this.doorBlocks.get(0).getWorld();

        int count = 1;
        float interval = 5.0f;


        if (this.axis == Axis.NS) {
            for (Block block : this.doorBlocks) {
                // top
                if (!isDoorBlock(block.getLocation().add(0, 1, 0))) {
                    for (float i = 0.0f; i < 1; i += 1.0f / interval) {
                        world.spawnParticle(Particle.REDSTONE, block.getLocation().add(0.4, 1, i), count, dustOptions);
                        world.spawnParticle(Particle.REDSTONE, block.getLocation().add(0.6, 1, i), count, dustOptions);
                    }
                }
                // bottom
                if (!isDoorBlock(block.getLocation().add(0, -1, 0))) {
                    for (float i = 0.0f; i < 1; i += 1.0f / interval) {
                        world.spawnParticle(Particle.REDSTONE, block.getLocation().add(0.4, 0, i), count, dustOptions);
                        world.spawnParticle(Particle.REDSTONE, block.getLocation().add(0.6, 0, i), count, dustOptions);
                    }
                }
                // north
                if (!isDoorBlock(block.getLocation().add(0, 0, -1))) {
                    for (float i = 0.0f; i < 1; i += 1.0f / interval) {
                        world.spawnParticle(Particle.REDSTONE, block.getLocation().add(0.4, i, 0), count, dustOptions);
                        world.spawnParticle(Particle.REDSTONE, block.getLocation().add(0.6, i, 0), count, dustOptions);
                    }
                }
                // south
                if (!isDoorBlock(block.getLocation().add(0, 0, 1))) {
                    for (float i = 0.0f; i < 1; i += 1.0f / interval) {
                        world.spawnParticle(Particle.REDSTONE, block.getLocation().add(0.4, i, 1), count, dustOptions);
                        world.spawnParticle(Particle.REDSTONE, block.getLocation().add(0.6, i, 1), count, dustOptions);
                    }
                }
            }
        }
        else {
            for (Block block : this.doorBlocks) {
                // top
                if (!isDoorBlock(block.getLocation().add(0, 1, 0))) {
                    for (float i = 0.0f; i < 1; i += 1.0f / interval) {
                        world.spawnParticle(Particle.REDSTONE, block.getLocation().add(i, 1, 0.4), count, dustOptions);
                        world.spawnParticle(Particle.REDSTONE, block.getLocation().add(i, 1, 0.6), count, dustOptions);
                    }
                }
                // bottom
                if (!isDoorBlock(block.getLocation().add(0, -1, 0))) {
                    for (float i = 0.0f; i < 1; i += 1.0f / interval) {
                        world.spawnParticle(Particle.REDSTONE, block.getLocation().add(i, 0, 0.4), count, dustOptions);
                        world.spawnParticle(Particle.REDSTONE, block.getLocation().add(i, 0, 0.6), count, dustOptions);
                    }
                }
                // north
                if (!isDoorBlock(block.getLocation().add(-1, 0, 0))) {
                    for (float i = 0.0f; i < 1; i += 1.0f / interval) {
                        world.spawnParticle(Particle.REDSTONE, block.getLocation().add(0, i, 0.4), count, dustOptions);
                        world.spawnParticle(Particle.REDSTONE, block.getLocation().add(0, i, 0.6), count, dustOptions);
                    }
                }
                // south
                if (!isDoorBlock(block.getLocation().add(1, 0, 0))) {
                    for (float i = 0.0f; i < 1; i += 1.0f / interval) {
                        world.spawnParticle(Particle.REDSTONE, block.getLocation().add(1, i, 0.4), count, dustOptions);
                        world.spawnParticle(Particle.REDSTONE, block.getLocation().add(1, i, 0.6), count, dustOptions);
                    }
                }
            }
        }
    }

    public void updateDoorBlocks() {
        this.doorBlocks.removeIf(block -> !isValidDoorBlock(block));
    }

    public List<Block> getDoorBlocks() {
        return this.doorBlocks;
    }

    public boolean isDoorBlock(Block block) {
        this.updateDoorBlocks();
        return this.doorBlocks.contains(block);
    }

    public boolean isDoorBlock(Location location) {
        this.updateDoorBlocks();

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
