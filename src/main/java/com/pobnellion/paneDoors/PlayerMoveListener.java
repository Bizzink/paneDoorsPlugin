package com.pobnellion.paneDoors;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public class PlayerMoveListener implements Listener {

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        Block block = player.getWorld().getBlockAt(event.getTo());

        if (!PaneDoor.isValidDoorBlock(block, null)) {
            PlayerVelocity.updateLocation(player);
            return;
        }

        for (PaneDoor door: Main.getDoors()) {
            if (door.contains(block)) {
                double distance;

                if (player.isSprinting()) {
                    distance = 0.4;
                }
                else if (player.isSneaking()) {
                    distance = 0.2;
                }
                else {
                    distance = 0.3;
                }

                if (door.getAxis() == Axis.NS) {
                    double velX = PlayerVelocity.getVelX(player);

                    if (velX > 0 && event.getTo().getX() - (event.getTo().getBlockX()) < 0.2) {
                        player.teleport(event.getTo().add(distance, 0, 0));
                    }
                    else if (velX < 0 && event.getTo().getX() - (event.getTo().getBlockX()) > 0.8) {
                        player.teleport(event.getTo().add(-distance, 0, 0));
                    }
                }
                else if (door.getAxis() == Axis.EW) {
                    double velZ = PlayerVelocity.getVelZ(player);

                    if (velZ > 0 && event.getTo().getZ() - (event.getTo().getBlockZ()) < 0.2) { 
                        player.teleport(event.getTo().add(0, 0, distance));
                    }
                    else if (velZ < 0 && event.getTo().getZ() - (event.getTo().getBlockZ()) > 0.8) { 
                        player.teleport(event.getTo().add(0, 0, -distance));
                    }
                }

                break;
            }
        }

        PlayerVelocity.updateLocation(player);
        return;
    }
}
