package org.xgaming.rewxrds;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.xgaming.rewxrds.Commands.RewardCommand;
import org.xgaming.rewxrds.Commands.RewardCommandPub;
import org.xgaming.rewxrds.CustomItem.RewardSystem;
import org.xgaming.rewxrds.Utils.ConfigUtil;

import java.util.logging.Level;

public final class Rewxrds extends JavaPlugin {

    private RewardSystem rewardSystem;
    private static Plugin plugin;
    private static boolean debug;

    @Override
    public void onEnable() {
        plugin = this;

        saveDefaultConfig();

        rewardSystem = new RewardSystem(this);
        // Register commands
        getCommand("reward").setExecutor(new RewardCommandPub(rewardSystem));

        RewardCommand rewardCommand = new RewardCommand(this);
        getCommand("rewxrdf").setExecutor(rewardCommand);
        getCommand("rewxrdf").setTabCompleter(rewardCommand);

        ConfigUtil configUtil = new ConfigUtil(this,"config.yml");
        FileConfiguration fileConfiguration = configUtil.getConfig();

        debug = fileConfiguration.getBoolean("debug",true);
        printConfig();
    }

    public void printConfig()
    {
        ConfigUtil configUtil = new ConfigUtil(this,"config.yml");
        FileConfiguration fileConfiguration = configUtil.getConfig();

        plugin.getLogger().log(Level.INFO,fileConfiguration.getString("api-key","not found"));
        plugin.getLogger().log(Level.INFO,fileConfiguration.getString("decision","not found"));
        plugin.getLogger().log(Level.INFO, String.valueOf(debug));

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public RewardSystem getRewardSystem() {
        return rewardSystem;
    }

    public static Plugin getPlugin()
    {
        return plugin;
    }

    public static boolean isDebug()
    {
        return debug;
    }

}