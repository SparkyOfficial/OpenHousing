package ru.openhousing.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.InventoryHolder;
import ru.openhousing.OpenHousing;
import ru.openhousing.coding.gui.CodeEditorGUI;

/**
 * Листенер для обработки событий инвентаря
 */
public class InventoryListener implements Listener {
    
    private final OpenHousing plugin;
    
    public InventoryListener(OpenHousing plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler(priority = org.bukkit.event.EventPriority.HIGHEST)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        
        Player player = (Player) event.getWhoClicked();
        String title = event.getView().getTitle();
        
        // Проверка наших GUI по заголовкам с высшим приоритетом
        if (title.startsWith("§6Редактор кода") || 
            title.startsWith("§6Настройка блока") ||
            title.startsWith("§6Настройки строки") ||
            title.startsWith("§6Выбор строки") ||
            title.contains("OpenHousing")) {
            
            event.setCancelled(true);
            
            // Обработка редактора кода
            if (title.startsWith("§6Редактор кода") || title.contains("OpenHousing")) {
                CodeEditorGUI editorGUI = plugin.getCodeManager().getEditorGUI(player);
                if (editorGUI != null) {
                    editorGUI.handleClick(event.getSlot(), event.isRightClick(), event.isShiftClick());
                }
            }
        }
    }
    
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player)) return;
        
        Player player = (Player) event.getPlayer();
        InventoryHolder holder = event.getInventory().getHolder();
        
        // Обработка закрытия редактора кода
        if (holder instanceof CodeEditorGUI) {
            plugin.getCodeManager().closeCodeEditor(player);
        }
    }
}
