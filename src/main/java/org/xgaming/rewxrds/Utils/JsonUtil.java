package org.xgaming.rewxrds.Utils;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class JsonUtil {
    private File file;
    private JsonObject jsonObject;

    public JsonUtil(Plugin plugin, String path) {
        String fullPath = plugin.getDataFolder().getAbsolutePath() + "/" + path;
        this.file = new File(fullPath);

        // Debugging: Log the full path
        plugin.getLogger().info("Trying to load JSON configuration at: " + fullPath);
        if (!this.file.exists()) {
            plugin.getLogger().warning("Configuration file does not exist: " + fullPath);
        } else {
            try (FileReader reader = new FileReader(this.file)) {
                this.jsonObject = JsonParser.parseReader(reader).getAsJsonObject();
                plugin.getLogger().info("Configuration file loaded successfully: " + fullPath);
            } catch (IOException e) {
                plugin.getLogger().severe("Failed to load configuration file: " + fullPath);
                e.printStackTrace();
            }
        }
    }

    public JsonObject getJsonObject() {
        return this.jsonObject;
    }

    public String getJsonString(){
        return this.jsonObject.toString();
    }
}
