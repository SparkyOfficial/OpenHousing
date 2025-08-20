package ru.openhousing.coding.gui;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import ru.openhousing.OpenHousing;
import ru.openhousing.utils.ItemBuilder;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

/**
 * GUI для выбора строкового значения из списка
 */
public class StringSelectorGUI implements Listener {
    
    private final OpenHousing plugin;
    private final Player player;
    private final String title;
    private final List<String> options;
    private final Consumer<String> onSelect;
    private Inventory inventory;
    
    public StringSelectorGUI(OpenHousing plugin, Player player, String title, List<String> options, Consumer<String> onSelect) {
        this.plugin = plugin;
        this.player = player;
        this.title = title;
        this.options = options;
        this.onSelect = onSelect;
        this.inventory = Bukkit.createInventory(null, 54, "§6" + title);
        
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }
    
    public void open() {
        setupInventory();
        player.openInventory(inventory);
    }
    
    private void setupInventory() {
        inventory.clear();
        
        for (int i = 0; i < options.size() && i < 45; i++) {
            String option = options.get(i);
            
            ItemStack item = new ItemBuilder(Material.PAPER)
                .name("§e" + option)
                .lore(Arrays.asList(
                    "§7Клик для выбора"
                ))
                .build();
                
            inventory.setItem(i, item);
        }
        
        // Кнопка отмены
        inventory.setItem(49, new ItemBuilder(Material.REDSTONE_BLOCK)
            .name("§cОтмена")
            .lore("§7Вернуться назад")
            .build());
    }
    
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        String eventTitle = null;
        try {
            eventTitle = event.getView().getTitle();
        } catch (NoSuchMethodError e) {
            return; // Пропускаем если не можем получить title
        }
        
        if (eventTitle == null || !eventTitle.startsWith("§6" + title)) return;
        if (!(event.getWhoClicked() instanceof Player)) return;
        
        Player clicker = (Player) event.getWhoClicked();
        if (!clicker.equals(player)) return;
        
        event.setCancelled(true);
        
        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null || clickedItem.getType() == Material.AIR) return;
        
        int slot = event.getSlot();
        
        if (slot == 49) {
            // Отмена
            player.closeInventory();
            return;
        }
        
        if (slot < options.size()) {
            String selectedOption = options.get(slot);
            player.closeInventory();
            
            if (onSelect != null) {
                onSelect.accept(selectedOption);
            }
        }
    }
}
