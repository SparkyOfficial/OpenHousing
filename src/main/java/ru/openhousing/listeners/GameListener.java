package ru.openhousing.listeners;

import org.bukkit.event.Listener;
import ru.openhousing.OpenHousing;

/**
 * Листенер для игровой системы (заглушка)
 */
public class GameListener implements Listener {
    
    private final OpenHousing plugin;
    
    public GameListener(OpenHousing plugin) {
        this.plugin = plugin;
    }
}
