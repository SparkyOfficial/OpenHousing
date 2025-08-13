package ru.openhousing.coding.values;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.entity.Player;
import ru.openhousing.coding.blocks.CodeBlock;

/**
 * Текстовое значение с поддержкой плейсхолдеров
 */
public class TextValue extends Value {
    
    public TextValue(String rawValue) {
        super(ValueType.TEXT, rawValue != null ? rawValue : "");
    }
    
    @Override
    public Object getValue(Player player, CodeBlock.ExecutionContext context) {
        String text = rawValue;
        
        // Замена встроенных плейсхолдеров
        if (player != null) {
            text = text.replace("%player%", player.getName());
            text = text.replace("%player_uuid%", player.getUniqueId().toString());
            text = text.replace("%player_world%", player.getWorld().getName());
            text = text.replace("%player_x%", String.valueOf(player.getLocation().getBlockX()));
            text = text.replace("%player_y%", String.valueOf(player.getLocation().getBlockY()));
            text = text.replace("%player_z%", String.valueOf(player.getLocation().getBlockZ()));
        }
        
        // Замена переменных из контекста
        if (context != null) {
            for (String variable : context.getVariables().keySet()) {
                Object value = context.getVariable(variable);
                if (value != null) {
                    text = text.replace("%" + variable + "%", String.valueOf(value));
                }
            }
        }
        
        // PlaceholderAPI (если доступен)
        try {
            if (player != null && org.bukkit.Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
                text = PlaceholderAPI.setPlaceholders(player, text);
            }
        } catch (Exception e) {
            // PlaceholderAPI не доступен, продолжаем без него
        }
        
        // Замена цветовых кодов
        text = text.replace("&", "§");
        
        return text;
    }
    
    @Override
    public String getDisplayValue() {
        if (rawValue.length() > 30) {
            return "\"" + rawValue.substring(0, 27) + "...\"";
        }
        return "\"" + rawValue + "\"";
    }
    
    @Override
    public boolean isValid() {
        return rawValue != null;
    }
}
