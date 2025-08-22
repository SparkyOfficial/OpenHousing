package ru.openhousing.listeners;

import org.bukkit.Bukkit;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
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
    
    @EventHandler(priority = EventPriority.NORMAL)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        if (event.isCancelled()) return;
        
        Player player = (Player) event.getWhoClicked();
        boolean debugMode = plugin.getConfigManager().getConfig().getBoolean("general.debug", false);
        
        try {
            String title = null;
            try {
                title = event.getView().getTitle();
            } catch (NoSuchMethodError e) {
                // Fallback для старых версий Bukkit
                if (event.getView().getTopInventory() != null) {
                    InventoryHolder holder = event.getView().getTopInventory().getHolder();
                    if (holder instanceof CodeEditorGUI) {
                        title = "§6Редактор кода";
                    }
                }
            }
            
            if (title == null) return;
            
            if (debugMode) plugin.getLogger().info("[DEBUG] Inventory click - Title: " + title + ", Slot: " + event.getSlot() + ", Player: " + player.getName());
            
            // Проверка наших GUI
            if (title.startsWith("§6Редактор кода") || 
                title.startsWith("§6Настройка блока") ||
                title.startsWith("§6Настройки строки") ||
                title.startsWith("§6Выбор строки") ||
                title.contains("OpenHousing")) {
                
                event.setCancelled(true);
                if (debugMode) plugin.getLogger().info("[DEBUG] Event cancelled for OpenHousing GUI");
                
                // Обработка редактора кода
                if (title.startsWith("§6Редактор кода") || title.contains("OpenHousing")) {
                    CodeEditorGUI editorGUI = plugin.getCodeManager().getEditorGUI(player);
                    if (editorGUI != null) {
                        try {
                            if (debugMode) plugin.getLogger().info("[DEBUG] Handling click in CodeEditorGUI - slot: " + event.getSlot());
                            editorGUI.handleClick(event.getSlot(), event.isRightClick(), event.isShiftClick());
                        } catch (Exception e) {
                            plugin.getLogger().severe("Error handling code editor click: " + e.getMessage());
                            e.printStackTrace();
                            // Переоткрываем редактор при ошибке
                            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                                plugin.getCodeManager().openCodeEditor(player);
                            }, 1L);
                        }
                    } else {
                        if (debugMode) plugin.getLogger().info("[DEBUG] CodeEditorGUI not found, creating new one");
                        // Если GUI не найден, создаем новый
                        plugin.getCodeManager().openCodeEditor(player);
                    }
                }
            }
        } catch (Exception e) {
            if (debugMode) plugin.getLogger().severe("[DEBUG] Error in InventoryListener: " + e.getMessage());
            if (debugMode) e.printStackTrace();
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
