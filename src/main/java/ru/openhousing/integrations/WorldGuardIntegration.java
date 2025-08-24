package ru.openhousing.integrations;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import ru.openhousing.OpenHousing;
import ru.openhousing.housing.House;

import java.util.UUID;

/**
 * Интеграция с WorldGuard для защиты домов
 * Временно упрощена для совместимости без WorldGuard
 */
public class WorldGuardIntegration {
    
    private final OpenHousing plugin;
    private boolean enabled = false;
    
    public WorldGuardIntegration(OpenHousing plugin) {
        this.plugin = plugin;
        initializeIntegration();
    }
    
    /**
     * Инициализация интеграции
     */
    private void initializeIntegration() {
        try {
            if (Bukkit.getPluginManager().getPlugin("WorldGuard") != null) {
                enabled = true;
                plugin.getLogger().info("WorldGuard integration enabled!");
            } else {
                plugin.getLogger().info("WorldGuard not found, region protection disabled");
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to initialize WorldGuard integration: " + e.getMessage());
            enabled = false;
        }
    }
    
    /**
     * Проверка активности интеграции
     */
    public boolean isEnabled() {
        return enabled;
    }
    
    /**
     * Создание региона для дома
     */
    public boolean createHouseRegion(House house) {
        if (!enabled) return false;
        
        // WorldGuard недоступен - возвращаем false
        return false;
    }
    
    /**
     * Удаление региона дома
     */
    public boolean deleteHouseRegion(House house) {
        if (!enabled) return false;
        
        // WorldGuard недоступен - возвращаем false
        return false;
    }
    
    /**
     * Обновление региона дома
     */
    public boolean updateHouseRegion(House house) {
        if (!enabled) return false;
        
        // WorldGuard недоступен - возвращаем false
        return false;
    }
    
    /**
     * Проверка прав игрока на строительство
     */
    public boolean canPlayerBuild(Player player, Location location) {
        if (!enabled) return true; // Если WorldGuard недоступен, разрешаем все
        
        // WorldGuard недоступен - разрешаем все
        return true;
    }
    
    /**
     * Проверка прав игрока на вход
     */
    public boolean canPlayerEnter(Player player, Location location) {
        if (!enabled) return true; // Если WorldGuard недоступен, разрешаем все
        
        // WorldGuard недоступен - разрешаем все
        return true;
    }
    
    /**
     * Получение ID региона для дома
     */
    public String getRegionId(House house) {
        return "house_" + house.getId();
    }
    
    /**
     * Получение имени региона для дома
     */
    public String getRegionName(House house) {
        return house.getName();
    }
    
    /**
     * Проверка активности WorldGuard (для совместимости)
     */
    public boolean isWorldGuardEnabled() {
        return isEnabled();
    }
    
    /**
     * Проверка доступа игрока к дому (для совместимости)
     */
    public boolean canPlayerAccess(Player player, House house) {
        if (!enabled) return true;
        
        // Проверяем, является ли игрок владельцем
        if (house.getOwnerId().equals(player.getUniqueId())) {
            return true;
        }
        
        // Проверяем, разрешен ли игрок
        if (house.getAllowedPlayers().contains(player.getUniqueId())) {
            return true;
        }
        
        // Проверяем, заблокирован ли игрок
        if (house.getBannedPlayers().contains(player.getUniqueId())) {
            return false;
        }
        
        // Проверяем публичность дома
        return house.isPublic();
    }
}