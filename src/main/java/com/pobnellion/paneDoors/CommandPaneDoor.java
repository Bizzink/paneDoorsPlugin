package com.pobnellion.paneDoors;

import org.bukkit.ChatColor;
import org.bukkit.FluidCollisionMode;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.GlassPane;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CommandPaneDoor implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Command must be run as a player!");
            return false;
        }

        if (args.length == 0) {
            sender.sendMessage("Usage: /" + label + " [create | show | showall | hide | hideall | delete | help]");
        }
        else if (args.length == 1) {
            Block block = ((Player) sender).getTargetBlockExact(16, FluidCollisionMode.NEVER);
            PaneDoor door;

            switch (args[0].toLowerCase()) {
                case "create":
                    if (block != null && block.getBlockData() instanceof GlassPane) {
                        try {
                            door = new PaneDoor(block);
                            door.enableHighlight((Player) sender);
                            Main.addDoor(door);
                        } catch (IllegalArgumentException e) {
                            sender.sendMessage("Not a valid door block!");
                        }
                    }
                    else {
                        sender.sendMessage("Not a valid door block!");
                    }

                    break;

                case "show":
                    if (block != null && block.getBlockData() instanceof GlassPane) {
                        door = getDoor(block);

                        if (door != null) {
                            door.enableHighlight((Player) sender);
                            DoorToolListener.addPersistentHighlight((Player) sender);
                        }
                        else {
                            sender.sendMessage("Not a door!");
                        }
                    }
                    else {
                        sender.sendMessage("No door found!");
                    }

                    break;

                case "showall":
                    if (Main.getDoors().size() > 0) {
                        for (PaneDoor d: Main.getDoors()) {
                            d.enableHighlight((Player) sender);
                        }
                        DoorToolListener.addPersistentHighlight((Player) sender);
                    }

                    sender.sendMessage("Showed all doors");

                    break;

                case "hide":
                    if (block != null && block.getBlockData() instanceof GlassPane) {
                        door = getDoor(block);

                        if (door != null) {
                            door.disableHighlight((Player) sender);

                            boolean hasVisibleDoors = false;

                            for (PaneDoor d: Main.getDoors()) {
                                if (d.isHighlightedForPlayer((Player) sender)) {
                                    hasVisibleDoors = true;
                                }
                            }

                            if (!hasVisibleDoors) {
                                DoorToolListener.removePersistentHighlight((Player) sender);
                            }
                        }
                        else {
                            sender.sendMessage("Not a door!");
                        }
                    }
                    else {
                        sender.sendMessage("No door found!");
                    }

                    break;

                case "hideall":
                    if (Main.getDoors().size() > 0) {
                        for (PaneDoor d: Main.getDoors()) {
                            d.disableHighlight((Player) sender);
                        }
                        DoorToolListener.removePersistentHighlight((Player) sender);
                    }
                    sender.sendMessage("Hid all doors");

                    break;

                case "delete":
                    if (block != null && block.getBlockData() instanceof GlassPane) {
                        door = getDoor(block);

                        if (door != null) {
                            Main.deleteDoor(door);
                            sender.sendMessage("Door deleted");
                        }
                        else {
                            sender.sendMessage("Not a door!");
                        }
                    }
                    else {
                        sender.sendMessage("No door found!");
                    }

                    break;

                case "help":
                    sender.sendMessage("Usage:");
                    sender.sendMessage("/panedoor, /pd");
                    sender.sendMessage("");
                    sender.sendMessage("Args:");
                    sender.sendMessage("");
                    sender.sendMessage("create: create a new door where the player is looking");
                    sender.sendMessage("show: highlight the door the player is looking at");
                    sender.sendMessage("showall: highlight all doors");
                    sender.sendMessage("hide: stop highlighting the door the player is looking at");
                    sender.sendMessage("hideall: stop highlighting all doors");
                    sender.sendMessage("delete: delete the door where the player is looking");

                    break;

                default:
                    sender.sendMessage("Invalid argument \"" + args[0] + "\"");
            }
        }

        return true;
    }

    private PaneDoor getDoor(Block block) {
        for (PaneDoor door: Main.getDoors()) {
            if (door.isDoorBlock(block)) {
                return door;
            }
        }

        return null;
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String label, String[] args) {
        List<String> options = new ArrayList<>();
        List<String> completions = new ArrayList<>();

        //TODO: permissions for these

        options.add("create");
        options.add("show");
        options.add("showall");
        options.add("hide");
        options.add("hideall");
        options.add("delete");
        options.add("help");

        StringUtil.copyPartialMatches(args[0], options, completions);
        Collections.sort(completions);
        return completions;
    }
}
