package ru.openhousing.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
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
    
    // Удален старый обработчик кликов, так как теперь каждый GUI имеет собственный обработчик
    
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
