package org.xgaming.rewxrds.Commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.xgaming.rewxrds.CustomItem.RewardSystem;

public class RewardCommandPub implements CommandExecutor {
    private final RewardSystem rewardSystem;

    public RewardCommandPub(RewardSystem rewardSystem) {
        this.rewardSystem = rewardSystem;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Check permission
        if (!sender.hasPermission("xg.reward") && !sender.isOp()) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to use this command!");
            return true;
        }

        // Check arguments
        if (args.length < 1) {
            sender.sendMessage(ChatColor.RED + "Usage: /reward <description>");
            return true;
        }

        // Combine all arguments into the description/goal
        String goal = String.join(" ", args);

        // Process the reward
        rewardSystem.processReward(sender, goal);
        return true;
    }
}