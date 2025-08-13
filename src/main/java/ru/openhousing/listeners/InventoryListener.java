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
    
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        
        Player player = (Player) event.getWhoClicked();
        String title = event.getView().getTitle();
        
        // Логирование для отладки
        plugin.getLogger().info("Inventory click: " + title + " by " + player.getName() + " at slot " + event.getSlot());
        
        // Проверка наших GUI по заголовкам
        if (title.startsWith("§6Редактор кода") || 
            title.startsWith("§6Настройка блока") ||
            title.contains("OpenHousing")) {
            
            event.setCancelled(true);
            plugin.getLogger().info("Cancelled inventory click for OpenHousing GUI");
            
            // Обработка редактора кода
            if (title.startsWith("§6Редактор кода")) {
                CodeEditorGUI editorGUI = plugin.getCodeManager().getEditorGUI(player);
                if (editorGUI != null) {
                    plugin.getLogger().info("Handling click in CodeEditorGUI");
                    editorGUI.handleClick(event.getSlot(), event.isRightClick(), event.isShiftClick());
                } else {
                    plugin.getLogger().warning("CodeEditorGUI is null for player " + player.getName());
                }
            }
            
            // Обработка настроек блока будет добавлена позже
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
