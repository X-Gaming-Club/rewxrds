package org.xgaming.rewxrds.Utils;

import org.bukkit.plugin.Plugin;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class JsonFileUtil {
    private final Plugin plugin;

    public JsonFileUtil(Plugin plugin) {
        this.plugin = plugin;
    }

    public boolean exists(String path) {
        File file = new File(plugin.getDataFolder(), path);
        return file.exists() && file.isFile();
    }

    public List<String> getFileList(String directory) {
        List<String> files = new ArrayList<>();
        File dir = new File(plugin.getDataFolder(), directory);
        
        if (dir.exists() && dir.isDirectory()) {
            File[] fileList = dir.listFiles((dir1, name) -> name.toLowerCase().endsWith(".json"));
            if (fileList != null) {
                for (File file : fileList) {
                    files.add(file.getName());
                }
            }
        }
        
        return files;
    }
}