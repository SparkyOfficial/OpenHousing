package ru.openhousing.placeholders;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import ru.openhousing.OpenHousing;
import ru.openhousing.coding.script.CodeScript;
import ru.openhousing.housing.House;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

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
        List<House> playerHouses = plugin.getHousingManager().getPlayerHouses(player.getUniqueId());
        House currentHouse = plugin.getHousingManager().getHouseAt(player.getLocation());

        // Плейсхолдеры для скриптов
        if (params.toLowerCase().startsWith("script_")) {
            return handleScriptPlaceholders(player, script, params);
        }
        
        // Плейсхолдеры для домов
        if (params.toLowerCase().startsWith("house_")) {
            return handleHousePlaceholders(player, playerHouses, currentHouse, params);
        }
        
        // Плейсхолдеры для статистики
        if (params.toLowerCase().startsWith("stats_")) {
            return handleStatsPlaceholders(player, params);
        }
        
        // Плейсхолдеры для экономики
        if (params.toLowerCase().startsWith("economy_")) {
            return handleEconomyPlaceholders(player, params);
        }
        
        return "";
    }
    
    /**
     * Обработка плейсхолдеров для скриптов
     */
    private String handleScriptPlaceholders(Player player, CodeScript script, String params) {
        if (script == null) {
            switch (params.toLowerCase()) {
                case "script_enabled":
                case "has_script":
                    return "false";
                case "script_blocks":
                case "script_variables":
                case "script_functions":
                case "script_lines":
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
                
            case "script_lines":
                return script != null ? String.valueOf(script.getLines().size()) : "0";
            
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
                
            case "script_errors":
                if (script != null && !script.isEmpty()) {
                    return String.valueOf(script.validate().size());
                }
                return "0";
                
            case "script_world":
                if (script != null && script.getBoundWorld() != null) {
                    return script.getBoundWorld();
                }
                return "Не привязан";
            
            default:
                return "";
        }
    }
    
    /**
     * Обработка плейсхолдеров для домов
     */
    private String handleHousePlaceholders(Player player, List<House> playerHouses, House currentHouse, String params) {
        switch (params.toLowerCase()) {
            case "house_count":
                return String.valueOf(playerHouses.size());
                
            case "house_current":
                if (currentHouse != null) {
                    return currentHouse.getName();
                }
                return "Не в доме";
                
            case "house_owner":
                if (currentHouse != null) {
                    return currentHouse.getOwnerName();
                }
                return "";
                
            case "house_public":
                if (currentHouse != null) {
                    return String.valueOf(currentHouse.isPublic());
                }
                return "false";
                
            case "house_visitors":
                if (currentHouse != null) {
                    return String.valueOf(currentHouse.getAllowedPlayers().size());
                }
                return "0";
                
            case "house_size":
                if (currentHouse != null) {
                    return currentHouse.getSize().getDisplayName();
                }
                return "";
                
            case "house_created":
                if (currentHouse != null) {
                    SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
                    return sdf.format(new Date(currentHouse.getCreatedAt()));
                }
                return "";
                
            case "house_mode":
                if (currentHouse != null) {
                    return currentHouse.getMode().getDisplayName();
                }
                return "";
                
            case "house_description":
                if (currentHouse != null) {
                    String desc = currentHouse.getDescription();
                    return desc != null ? desc : "Нет описания";
                }
                return "";
                
            case "house_rating":
                if (currentHouse != null) {
                    Object rating = currentHouse.getSetting("rating");
                    if (rating instanceof Number) {
                        return String.format("%.1f", ((Number) rating).doubleValue());
                    }
                    return "0.0";
                }
                return "0.0";
                
            case "house_visits":
                if (currentHouse != null) {
                    Object visits = currentHouse.getSetting("total_visits");
                    if (visits instanceof Number) {
                        return String.valueOf(((Number) visits).intValue());
                    }
                    return "0";
                }
                return "0";
                
            case "house_is_owner":
                if (currentHouse != null) {
                    return String.valueOf(currentHouse.getOwnerId().equals(player.getUniqueId()));
                }
                return "false";
                
            case "house_is_allowed":
                if (currentHouse != null) {
                    return String.valueOf(currentHouse.getAllowedPlayers().contains(player.getUniqueId()));
                }
                return "false";
                
            case "house_is_banned":
                if (currentHouse != null) {
                    return String.valueOf(currentHouse.getBannedPlayers().contains(player.getUniqueId()));
                }
                return "false";
                
            default:
                return "";
        }
    }
    
    /**
     * Обработка плейсхолдеров для статистики
     */
    private String handleStatsPlaceholders(Player player, String params) {
        switch (params.toLowerCase()) {
            case "stats_total_houses":
                return String.valueOf(plugin.getHousingManager().getAllHouses().size());
                
            case "stats_public_houses":
                return String.valueOf(plugin.getHousingManager().getPublicHouses().size());
                
            case "stats_online_players":
                return String.valueOf(plugin.getServer().getOnlinePlayers().size());
                
            case "stats_my_houses":
                return String.valueOf(plugin.getHousingManager().getPlayerHouses(player.getUniqueId()).size());
                
            case "stats_my_visits":
                // Можно добавить отслеживание посещений
                return "0";
                
            case "stats_server_uptime":
                // Упрощенная версия - возвращаем текущее время
                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
                return sdf.format(new Date());
                
            default:
                return "";
        }
    }
    
    /**
     * Обработка плейсхолдеров для экономики
     */
    private String handleEconomyPlaceholders(Player player, String params) {
        if (plugin.getEconomy() == null) {
            return "Экономика отключена";
        }
        
        switch (params.toLowerCase()) {
            case "economy_balance":
                return String.format("%.2f", plugin.getEconomy().getBalance(player));
                
            case "economy_balance_formatted":
                return plugin.getEconomy().format(plugin.getEconomy().getBalance(player));
                
            case "economy_house_creation_cost":
                return String.format("%.2f", plugin.getHousingManager().getCreationCost());
                
            case "economy_can_create_house":
                double balance = plugin.getEconomy().getBalance(player);
                double cost = plugin.getHousingManager().getCreationCost();
                return String.valueOf(balance >= cost);
                
            default:
                return "";
        }
    }
}
