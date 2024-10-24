package org.xgaming.rewxrds.CustomItem;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import kong.unirest.json.JSONObject;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.xgaming.rewxrds.External.Server;
import org.xgaming.rewxrds.Rewxrds;
import org.xgaming.rewxrds.Utils.ConfigUtil;
import org.xgaming.rewxrds.Utils.JsonFileUtil;

public class RewardSystem {
    private final Plugin plugin;
    private final ItemGenerator itemGenerator;
    private final Gson gson;
    private final String apiKey;
    private final String decisionUrl;

    public RewardSystem(Plugin plugin) {
        this.plugin = plugin;
        this.itemGenerator = new ItemGenerator(plugin);
        this.gson = new Gson();
        
        // Load configuration
        ConfigUtil config = new ConfigUtil(plugin, "config.yml");
        this.apiKey = config.getConfig().getString("api-key", "default-key");
        this.decisionUrl = config.getConfig().getString("decision", "https://authdev.xgaming.club/xquest/generate");
    }

    // API Request class
    private static class DecisionRequest {
        String goal;
        String key;
        String[] fields;

        public DecisionRequest(String goal, String key) {
            this.goal = goal;
            this.key = key;
            this.fields = new String[]{"item"};
        }
    }
    public void processReward(CommandSender sender, String goal) {
        // Create request object
        DecisionRequest request = new DecisionRequest(goal, apiKey);
        String requestJson = gson.toJson(request);

        // Make async API call
        new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    // Make API call
                    boolean debug = Rewxrds.isDebug();
                    String response = Server.serverRequest(requestJson, decisionUrl, debug, debug);
                    JSONObject jsonResponse = new JSONObject(response);

                    if (jsonResponse.has("data") && jsonResponse.getJSONObject("data").has("item")) {
                        String itemFile = jsonResponse.getJSONObject("data").getString("item");
                        
                        // Ensure the item file has .json extension
                        if (!itemFile.toLowerCase().endsWith(".json")) {
                            itemFile += ".json";
                        }

                        // Switch back to main thread for Bukkit operations
                        final String finalItemFile = itemFile;
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                giveRewardToPlayer(sender, finalItemFile);
                            }
                        }.runTask(plugin);
                    } else {
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                sender.sendMessage(ChatColor.RED + "No valid reward found for this goal.");
                            }
                        }.runTask(plugin);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            sender.sendMessage(ChatColor.RED + "An error occurred while processing your reward.");
                        }
                    }.runTask(plugin);
                }
            }
        }.runTaskAsynchronously(plugin);
    }

    private void giveRewardToPlayer(CommandSender sender, String itemFile) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be used by players!");
            return;
        }

        Player player = (Player) sender;
        
        // Check if the file exists
        JsonFileUtil jsonUtil = new JsonFileUtil(plugin);
        if (!jsonUtil.exists("Items/" + itemFile)) {
            sender.sendMessage(ChatColor.RED + "Reward item file not found: " + itemFile);
            return;
        }

        try {
            ItemStack item = itemGenerator.generateFromFile("Items/" + itemFile);
            if (item == null) {
                sender.sendMessage(ChatColor.RED + "Failed to generate reward item.");
                return;
            }

            // Give item to player
            player.getInventory().addItem(item).forEach((index, leftover) -> {
                player.getWorld().dropItemNaturally(player.getLocation(), leftover);
            });

            player.sendMessage(ChatColor.GREEN + "You received a reward!");

        } catch (Exception e) {
            e.printStackTrace();
            sender.sendMessage(ChatColor.RED + "Error giving reward: " + e.getMessage());
        }
    }
}