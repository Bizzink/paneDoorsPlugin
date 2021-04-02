package com.pobnellion.paneDoors;

import org.bukkit.ChatColor;
import org.bukkit.FluidCollisionMode;
import org.bukkit.block.Block;
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

        if (!sender.hasPermission("panedoors.admin") && !sender.isOp()) {
            sender.sendMessage("You do not have permission to use this command.");
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage("Usage: /" + label + " [create | show | hide | delete | highlight | help]");
            return true;
        }

        Block block = ((Player) sender).getTargetBlockExact(16, FluidCollisionMode.NEVER);
        PaneDoor door;

        switch (args[0].toLowerCase()) {
            case "create":
                if (block != null) {
                    boolean respectColour = false;

                    if (args.length == 2) {
                        respectColour = Boolean.parseBoolean(args[1]);
                    }

                    try {
                        door = new PaneDoor(block, respectColour);
                        door.enableHighlight((Player) sender);
                        Main.addDoor(door);
                        break;
                    } catch (IllegalArgumentException ignored) {}
                }

                sender.sendMessage("Not a valid door block!");
                break;

            case "show":
                if (args.length == 1) {
                    if (block != null && (door = getDoor(block)) != null) {
                        door.enableHighlight((Player) sender);
                        DoorToolListener.addPersistentHighlight((Player) sender);
                        break;
                    }
                }
                else if (args.length == 2 && args[1].equals("all")) {
                    if (Main.getDoors().size() > 0) {
                        for (PaneDoor d: Main.getDoors()) {
                          d.enableHighlight((Player) sender);
                        }

                        DoorToolListener.addPersistentHighlight((Player) sender);
                    }
                    sender.sendMessage("Showed all doors");
                }
                break;

            case "hide":
                if (args.length == 1) {
                    if (block != null && (door = getDoor(block)) != null) {
                        door.disableHighlight((Player) sender);

                        if (Main.getDoors().stream().noneMatch(d -> d.isHighlightedForPlayer((Player) sender))) {
                            DoorToolListener.removePersistentHighlight((Player) sender);
                        }
                    }
                }
                else if (args.length == 2 && args[1].equals("all")) {
                    for (PaneDoor d: Main.getDoors()) {
                        d.disableHighlight((Player) sender);
                    }

                    DoorToolListener.removePersistentHighlight((Player) sender);
                    sender.sendMessage("Hid all doors");
                }
                break;

            case "delete":
                if (block != null && (door = getDoor(block)) != null) {
                    Main.deleteDoor(door);
                    sender.sendMessage("Door deleted");
                    break;
                }

                sender.sendMessage("No door found!");
                break;

            case "highlight":
                switch (args.length) {
                    case 1:
                        sender.sendMessage("Usage: /" + label + " highlight [distance | colour]");
                        break;

                    case 2:
                        switch (args[1].toLowerCase()) {
                            case "distance":
                                sender.sendMessage("Usage: /" + label + " highlight distance <value>");
                                break;

                            case "colour":
                                sender.sendMessage("Usage: /" + label + " highlight colour <r> <g> <b>");
                                break;

                            default:
                                sender.sendMessage("Invalid argument \"" + args[1] + "\"");
                        }
                        break;

                    case 3:
                        switch (args[1].toLowerCase()) {
                            case "distance":
                                try {
                                    int dist = Integer.parseInt(args[2]);
                                    Main.setHighlightViewDist(dist);
                                    sender.sendMessage("Set highlight view distance to " + dist);
                                }
                                catch (NumberFormatException e) {
                                    sender.sendMessage("\"" + args[2] + "\" is not a valid integer!");
                                }
                                break;

                            case "colour":
                                sender.sendMessage("Usage: /" + label + " highlight colour <r> <g> <b>");
                                break;

                            default:
                                sender.sendMessage("Invalid argument \"" + args[1] + "\"");
                        }

                    default:
                        if (args[1].equals("colour")) {
                            if (args.length == 5) {
                                try {
                                    int r = Integer.parseInt(args[2]);
                                    int g = Integer.parseInt(args[3]);
                                    int b = Integer.parseInt(args[4]);

                                    Main.setHighlightColour(r, g, b);
                                    sender.sendMessage("Set highlight colour to (" + r + ", " + g + ", " + b + ")");
                                }
                                catch (NumberFormatException e) {
                                    sender.sendMessage("Invalid value");
                                }
                            }
                            else {
                                sender.sendMessage("Usage: /" + label + " highlight colour <r> <g> <b>");
                            }
                        }
                }
                break;

            case "help":
                sender.sendMessage("Usage: /panedoor, /pd");
                sender.sendMessage("");
                sender.sendMessage("Args:");
                sender.sendMessage("");
                sender.sendMessage("create: create a new door where the player is looking");
                sender.sendMessage("show: highlight the door the player is looking at");
                sender.sendMessage("hide: stop highlighting the door the player is looking at");
                sender.sendMessage("highlight: change door highlight properties (global)");
                sender.sendMessage("delete: delete the door where the player is looking");
                break;

            default:
                sender.sendMessage("Invalid argument \"" + args[0] + "\"");
        }

        return true;
    }

    private PaneDoor getDoor(Block block) {
        if (PaneDoor.isValidDoorBlock(block, null)) {
            for (PaneDoor door : Main.getDoors()) {
                if (door.contains(block)) {
                    return door;
                }
            }
        }

        return null;
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String label, String[] args) {
        List<String> options = new ArrayList<>();
        List<String> completions = new ArrayList<>();

        if (!(commandSender instanceof Player)) {
            return null;
        }

        if (!commandSender.hasPermission("panedoors.admin") && !commandSender.isOp()) {
            return null;
        }

        if (args.length == 1) {
            options.add("create");
            options.add("show");
            options.add("hide");
            options.add("delete");
            options.add("highlight");
            options.add("help");

            StringUtil.copyPartialMatches(args[0], options, completions);
        }

        if (args.length == 2) {
            switch (args[0].toLowerCase()) {
                case "create":
                    options.add("true");
                    options.add("false");
                    break;

                case "show":
                case "hide":
                    options.add("all");
                    break;

                case "highlight":
                    options.add("distance");
                    options.add("colour");
                    break;
            }

            StringUtil.copyPartialMatches(args[1], options, completions);
        }

        Collections.sort(completions);
        return completions;
    }
}
