package com.pobnellion.paneDoors;

import java.util.HashMap;

import org.bukkit.Location;
import org.bukkit.entity.Player;

public class PlayerVelocity {
    private static HashMap<Player, Location> playerPrevPositions = new HashMap<>();

    public static void updateLocation(Player player) {
        playerPrevPositions.put(player, player.getLocation());
        return;
    }

    public static double getVelX(Player player) {
        Location currPos = player.getLocation();
        Location prevPos = playerPrevPositions.getOrDefault(player, player.getLocation());

        return currPos.getX() - prevPos.getX();
    }

    public static double getVelZ(Player player) {
        Location currPos = player.getLocation();
        Location prevPos = playerPrevPositions.getOrDefault(player, player.getLocation());

        return currPos.getZ() - prevPos.getZ();
    }
}
