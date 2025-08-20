package ru.openhousing.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import ru.openhousing.OpenHousing;

/**
 * Специальный блокировщик для предотвращения конфликтов с ReActions плагином
 */
public class ReActionsBlocker implements Listener {
    
    private final OpenHousing plugin;
    
    public ReActionsBlocker(OpenHousing plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
    public void blockReActionsFromOurGUIs(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        
        try {
            String title = null;
            try {
                title = event.getView().getTitle();
            } catch (NoSuchMethodError e) {
                // Fallback для старых версий Bukkit - используем holder
                return; // Пропускаем если не можем получить title
            }
            
            if (title == null) return;
            
            // Если это наш GUI - немедленно блокируем
            if (isOpenHousingGUI(title)) {
                event.setCancelled(true);
                // Маркер больше не нужен, просто блокируем
            }
        } catch (Exception e) {
            // Тихо игнорируем любые ошибки
        }
    }
    
    private boolean isOpenHousingGUI(String title) {
        return title != null && (
            title.startsWith("§6Редактор кода") || 
            title.startsWith("§6Настройка блока") ||
            title.startsWith("§6Настройки строки") ||
            title.startsWith("§6Выбор строки") ||
            title.contains("OpenHousing") ||
            title.contains("Настройки дома") ||
            title.contains("Выбор игрока") ||
            title.contains("Выбор локации")
        );
    }
}
