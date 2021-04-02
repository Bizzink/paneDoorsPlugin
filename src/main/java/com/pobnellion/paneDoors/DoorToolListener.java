package com.pobnellion.paneDoors;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class DoorToolListener implements Listener {
    private final Material doorTool = Material.WOODEN_SHOVEL;
    private static final Set<Player> persistentHighlight = new HashSet<>();

    @EventHandler
    public void playerInteract(PlayerInteractEvent event) {
        if (event.getHand() == null || event.getHand() == EquipmentSlot.OFF_HAND) {
            return;
        }

        Player player = event.getPlayer();
        Block block = event.getClickedBlock();

        if (!player.hasPermission("panedoors.admin") && !player.isOp()) {
            return;
        }

        if (!player.getInventory().getItemInMainHand().getType().equals(doorTool)) {
            return;
        }

        if (block != null) {
            event.setCancelled(true);

            if (PaneDoor.isValidDoorBlock(block, null)) {
                Optional<PaneDoor> targetedDoor = Main.getDoors().stream().filter(door -> door.contains(block)).findFirst();

                if (targetedDoor.isPresent()) {
                    if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
                        Main.deleteDoor(targetedDoor.get());
                    }
                    else {
                        targetedDoor.get().toggleHighlight(player);
                    }
                } else if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
                    PaneDoor door = new PaneDoor(block, false);
                    door.enableHighlight(player);
                    Main.addDoor(door);
                }
            }
        }
    }

    @EventHandler
    public void playerHoldTool(PlayerItemHeldEvent event) {
        Player player = event.getPlayer();

        if (!player.hasPermission("panedoors.admin") && !player.isOp()) {
            return;
        }

        ItemStack item = player.getInventory().getItem(event.getNewSlot());

        if (item != null && item.getType().equals(doorTool)) {
            for (PaneDoor door: Main.getDoors()) {
                door.enableHighlight(player);
            }
        }
        else if (!persistentHighlight.contains(player)) {
            for (PaneDoor door: Main.getDoors()) {
                door.disableHighlight(player);
            }
        }
    }

    public static void addPersistentHighlight(Player player) {
        persistentHighlight.add(player);
    }

    public static void removePersistentHighlight(Player player) {
        persistentHighlight.remove(player);
    }

}
