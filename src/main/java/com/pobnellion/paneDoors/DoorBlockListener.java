package com.pobnellion.paneDoors;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

public class DoorBlockListener implements Listener {

    @EventHandler
    public void doorBlockBreak(BlockBreakEvent event) {
        if (PaneDoor.isValidDoorBlock(event.getBlock(), null)) {
            for (PaneDoor door: Main.getDoors()) {
                if (door.contains(event.getBlock())) {
                    Main.deleteDoor(door);
                    event.setCancelled(true);
                    return;
                }
            }
        }
    }

}
