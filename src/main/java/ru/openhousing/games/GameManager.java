package ru.openhousing.games;

import ru.openhousing.OpenHousing;

/**
 * Менеджер игровой системы (заглушка для будущей реализации)
 */
public class GameManager {
    
    private final OpenHousing plugin;
    
    public GameManager(OpenHousing plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Инициализация менеджера
     */
    public void initialize() {
        plugin.getLogger().info("GameManager initialized successfully!");
    }
    
    /**
     * Остановка всех игр
     */
    public void stopAllGames() {
        // Здесь будет реализация остановки всех активных игр
        plugin.getLogger().info("All games stopped!");
    }
}
