package org.xgaming.rewxrds.Commands;

import com.google.gson.JsonParseException;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.xgaming.rewxrds.CustomItem.ItemGenerator;
import org.xgaming.rewxrds.Utils.JsonFileUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class RewardCommand implements CommandExecutor, TabCompleter {
    private final Plugin plugin;
    private final ItemGenerator itemGenerator;
    private final JsonFileUtil jsonUtil;

    public RewardCommand(Plugin plugin) {
        this.plugin = plugin;
        this.itemGenerator = new ItemGenerator(plugin);
        this.jsonUtil = new JsonFileUtil(plugin);
        
        // Create the Items directory if it doesn't exist
        File itemsDir = new File(plugin.getDataFolder(), "Items");
        if (!itemsDir.exists()) {
            itemsDir.mkdirs();
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.isOp()) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to use this command!");
            return true;
        }

        if (args.length < 1 || args.length > 2) {
            sender.sendMessage(ChatColor.RED + "Usage: /rewxrdf <json-file> OR /rewxrdf <player> <json-file>");
            return true;
        }

        try {
            // Handle both command formats
            if (args.length == 1) {
                // Format: /rewxrdf <json-file>
                if (!(sender instanceof Player)) {
                    sender.sendMessage(ChatColor.RED + "This command format can only be used by players!");
                    return true;
                }
                handleReward((Player) sender, args[0], sender);
            } else {
                // Format: /rewxrdf <player> <json-file>
                Player target = Bukkit.getPlayer(args[0]);
                if (target == null) {
                    sender.sendMessage(ChatColor.RED + "Player " + args[0] + " is not online!");
                    return true;
                }
                handleReward(target, args[1], sender);
            }
            return true;

        } catch (Exception e) {
            sender.sendMessage(ChatColor.RED + "An error occurred: " + e.getMessage());
            e.printStackTrace();
            return true;
        }
    }

    private void handleReward(Player player, String fileName, CommandSender sender) {
        // Check if the file exists
        if (!jsonUtil.exists("Items/" + fileName)) {
            sender.sendMessage(ChatColor.RED + "Item file '" + fileName + "' not found!");
            return;
        }

        try {
            // Generate the item
            ItemStack item = itemGenerator.generateFromFile("Items/" + fileName);
            if (item == null) {
                sender.sendMessage(ChatColor.RED + "Failed to generate item from file: " + fileName);
                return;
            }

            // Give the item to the player
            player.getInventory().addItem(item).forEach((index, leftover) -> {
                player.getWorld().dropItemNaturally(player.getLocation(), leftover);
            });

            // Send success messages
            player.sendMessage(ChatColor.GREEN + "You received an item!");
            if (sender != player) {
                sender.sendMessage(ChatColor.GREEN + "Successfully gave item to " + player.getName());
            }

        } catch (JsonParseException e) {
            sender.sendMessage(ChatColor.RED + "Invalid JSON in file: " + fileName);
        } catch (Exception e) {
            sender.sendMessage(ChatColor.RED + "Error giving item: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.isOp()) {
            return new ArrayList<>();
        }

        if (args.length == 1) {
            // If first argument, suggest either player names or JSON files
            List<String> suggestions = new ArrayList<>();
            
            // Add online player names
            suggestions.addAll(Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .collect(Collectors.toList()));
            
            // Add JSON files from Items directory
            suggestions.addAll(jsonUtil.getFileList("Items"));
            
            // Filter based on current input
            return suggestions.stream()
                    .filter(suggestion -> suggestion.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        } else if (args.length == 2) {
            // If second argument, suggest only JSON files
            return jsonUtil.getFileList("Items").stream()
                    .filter(file -> file.toLowerCase().startsWith(args[1].toLowerCase()))
                    .collect(Collectors.toList());
        }

        return new ArrayList<>();
    }
}