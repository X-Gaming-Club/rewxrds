package org.xgaming.rewxrds.CustomItem;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ItemGenerator {
    private final Plugin plugin;
    private final Gson gson;

    public ItemGenerator(Plugin plugin) {
        this.plugin = plugin;
        this.gson = new Gson();
    }

    public ItemStack generateFromFile(String path) {
        try {
            File file = new File(plugin.getDataFolder(), path);
            return generateFromJson(new FileReader(file));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public ItemStack generateFromString(String jsonString) {
        try {
            return generateFromJson(jsonString);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private ItemStack generateFromJson(FileReader reader) {
        JsonObject json = gson.fromJson(reader, JsonObject.class);
        return createItem(json);
    }

    private ItemStack generateFromJson(String jsonString) {
        JsonObject json = gson.fromJson(jsonString, JsonObject.class);
        return createItem(json);
    }

    private ItemStack createItem(JsonObject json) {
        // Get material (required)
        Material material = Material.valueOf(json.get("material").getAsString().toUpperCase());
        
        // Get quantity (optional, default 1)
        int quantity = json.has("quantity") ? json.get("quantity").getAsInt() : 1;
        
        // Create ItemStack
        ItemStack item = new ItemStack(material, quantity);
        ItemMeta meta = item.getItemMeta();
        
        if (meta != null) {
            // Set custom name (optional)
            if (json.has("name")) {
                meta.setDisplayName(json.get("name").getAsString());
            }
            
            // Set lore (optional)
            if (json.has("lore")) {
                List<String> lore = new ArrayList<>();
                json.get("lore").getAsJsonArray().forEach(line -> 
                    lore.add(line.getAsString()));
                meta.setLore(lore);
            }
            
            // Set custom model data (optional)
            if (json.has("customModelData")) {
                meta.setCustomModelData(json.get("customModelData").getAsInt());
            }
            
            // Apply enchantments (optional)
            if (json.has("enchantments")) {
                JsonObject enchants = json.get("enchantments").getAsJsonObject();
                for (Map.Entry<String, JsonElement> entry : enchants.entrySet()) {
                    Enchantment ench = Enchantment.getByKey(NamespacedKey.minecraft(entry.getKey().toLowerCase()));
                    if (ench != null) {
                        meta.addEnchant(ench, entry.getValue().getAsInt(), true);
                    }
                }
            }
            
            // Apply potion effects (optional)
            if (json.has("effects") && meta instanceof PotionMeta) {
                PotionMeta potionMeta = (PotionMeta) meta;
                JsonObject effects = json.get("effects").getAsJsonObject();
                
                for (Map.Entry<String, JsonElement> entry : effects.entrySet()) {
                    JsonObject effectData = entry.getValue().getAsJsonObject();
                    PotionEffectType effectType = PotionEffectType.getByName(entry.getKey().toUpperCase());
                    
                    if (effectType != null) {
                        int duration = effectData.get("duration").getAsInt();
                        int amplifier = effectData.get("level").getAsInt() - 1;
                        potionMeta.addCustomEffect(new PotionEffect(effectType, duration, amplifier), true);
                    }
                }
            }
            
            item.setItemMeta(meta);
        }
        
        return item;
    }
}