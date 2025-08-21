package ru.openhousing.placeholders;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import ru.openhousing.OpenHousing;
import ru.openhousing.coding.script.CodeScript;

/**
 * PlaceholderAPI интеграция
 */
public class OpenHousingPlaceholders extends PlaceholderExpansion {
    
    private final OpenHousing plugin;
    
    public OpenHousingPlaceholders(OpenHousing plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public @NotNull String getIdentifier() {
        return "openhousing";
    }
    
    @Override
    public @NotNull String getAuthor() {
        return String.join(", ", plugin.getDescription().getAuthors());
    }
    
    @Override
    public @NotNull String getVersion() {
        return plugin.getDescription().getVersion();
    }
    
    @Override
    public boolean persist() {
        return true;
    }
    
    @Override
    public String onPlaceholderRequest(Player player, @NotNull String params) {
        if (player == null) {
            return "";
        }
        
        CodeScript script = plugin.getCodeManager().getScript(player);

        if (script == null) {
            switch (params.toLowerCase()) {
                case "script_enabled":
                case "has_script":
                    return "false";
                case "script_blocks":
                case "script_variables":
                case "script_functions":
                    return "0";
                case "script_status":
                    return "Нет кода";
                case "editor_open":
                     return String.valueOf(plugin.getCodeManager().hasOpenEditor(player));
                default:
                    return "";
            }
        }
        
        switch (params.toLowerCase()) {
            case "script_enabled":
                return script != null ? String.valueOf(script.isEnabled()) : "false";
            
            case "script_blocks":
                return script != null ? String.valueOf(script.getBlockCount()) : "0";
            
            case "script_variables":
                return script != null ? String.valueOf(script.getGlobalVariables().size()) : "0";
            
            case "script_functions":
                return script != null ? String.valueOf(script.getFunctions().size()) : "0";
            
            case "has_script":
                return String.valueOf(script != null && !script.isEmpty());
            
            case "editor_open":
                return String.valueOf(plugin.getCodeManager().hasOpenEditor(player));
            
            case "script_status":
                if (script == null || script.isEmpty()) {
                    return "Нет кода";
                } else if (!script.isEnabled()) {
                    return "Отключен";
                } else if (!script.validate().isEmpty()) {
                    return "Есть ошибки";
                } else {
                    return "Готов";
                }
            
            default:
                return "";
        }
    }
}
