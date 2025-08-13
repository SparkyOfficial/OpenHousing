package ru.openhousing.housing;

import org.bukkit.entity.Player;
import ru.openhousing.OpenHousing;

/**
 * Менеджер системы Housing (заглушка для будущей реализации)
 */
public class HousingManager {
    
    private final OpenHousing plugin;
    
    public HousingManager(OpenHousing plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Инициализация менеджера
     */
    public void initialize() {
        plugin.getLogger().info("HousingManager initialized successfully!");
    }
    
    /**
     * Сохранение всех данных
     */
    public void saveAll() {
        // Здесь будет реализация сохранения всех домов
        plugin.getLogger().info("All housing data saved!");
    }
}
