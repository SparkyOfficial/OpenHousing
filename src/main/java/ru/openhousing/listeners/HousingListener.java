package ru.openhousing.listeners;

import org.bukkit.event.Listener;
import ru.openhousing.OpenHousing;

/**
 * Листенер для Housing системы (заглушка)
 */
public class HousingListener implements Listener {
    
    private final OpenHousing plugin;
    
    public HousingListener(OpenHousing plugin) {
        this.plugin = plugin;
    }
}
