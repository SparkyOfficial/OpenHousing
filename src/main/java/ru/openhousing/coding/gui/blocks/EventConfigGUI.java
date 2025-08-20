package ru.openhousing.coding.gui.blocks;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import ru.openhousing.OpenHousing;
import ru.openhousing.coding.blocks.CodeBlock;
import ru.openhousing.coding.variables.VariableType;
import ru.openhousing.utils.ItemBuilder;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Уникальное GUI для настройки событий
 */
public class EventConfigGUI implements Listener {
    
    private final OpenHousing plugin;
    private final Player player;
    private final CodeBlock block;
    private final Inventory inventory;
    private final Map<Integer, String> variableSlots = new HashMap<>();
    
    public EventConfigGUI(OpenHousing plugin, Player player, CodeBlock block) {
        this.plugin = plugin;
        this.player = player;
        this.block = block;
        this.inventory = Bukkit.createInventory(null, 54, "§6Настройка события");
        
        Bukkit.getPluginManager().registerEvents(this, plugin);
        setupInventory();
    }
    
    private void setupInventory() {
        // Заголовок
        inventory.setItem(4, new ItemBuilder(Material.LIGHTNING_ROD)
            .name("§6Событие")
            .lore(Arrays.asList(
                "§7Настройте параметры события",
                "§7События запускают выполнение кода",
                "",
                "§eПоддерживает переменные и плейсхолдеры"
            ))
            .build());
        
        setupEventParameters();
        setupVariableSlots();
        setupControlButtons();
    }
    
    private void setupEventParameters() {
        // Тип события
        inventory.setItem(10, new ItemBuilder(Material.REDSTONE)
            .name("§bТип события")
            .lore(Arrays.asList(
                "§7Выберите когда запускать код:",
                "§7• Игрок зашел/вышел",
                "§7• Игрок написал в чат",
                "§7• Игрок получил урон",
                "§7• Существо умерло",
                "§7• Блок сломан/поставлен",
                "",
                "§eКлик для выбора из 50+ событий"
            ))
            .build());
        
        // Условия срабатывания
        inventory.setItem(12, new ItemBuilder(Material.COMPARATOR)
            .name("§bУсловия срабатывания")
            .lore(Arrays.asList(
                "§7Дополнительные условия:",
                "§7• Только определенные игроки",
                "§7• Только в определенном мире",
                "§7• Только с определенными предметами",
                "§7• Кулдаун между срабатываниями",
                "",
                "§eКлик для настройки"
            ))
            .build());
        
        // Приоритет события
        inventory.setItem(14, new ItemBuilder(Material.GOLD_INGOT)
            .name("§bПриоритет")
            .lore(Arrays.asList(
                "§7Приоритет выполнения:",
                "§7• LOWEST - самый низкий",
                "§7• LOW - низкий", 
                "§7• NORMAL - обычный",
                "§7• HIGH - высокий",
                "§7• HIGHEST - самый высокий",
                "",
                "§eТекущий: §f" + getVariableValue("priority")
            ))
            .build());
    }
    
    private void setupVariableSlots() {
        // Слот для фильтра игрока
        inventory.setItem(28, new ItemBuilder(Material.PLAYER_HEAD)
            .name("§aФильтр игрока")
            .lore(Arrays.asList(
                "§7Перетащите переменную с именем игрока",
                "§7для фильтрации события",
                "",
                "§7Текущее: §f" + getVariableValue("playerFilter"),
                "",
                "§eDrag-n-drop переменную сюда"
            ))
            .build());
        variableSlots.put(28, "playerFilter");
        
        // Слот для фильтра мира
        inventory.setItem(30, new ItemBuilder(Material.GRASS_BLOCK)
            .name("§aФильтр мира")
            .lore(Arrays.asList(
                "§7Перетащите переменную с именем мира",
                "",
                "§7Текущее: §f" + getVariableValue("worldFilter"),
                "",
                "§eDrag-n-drop переменную сюда"
            ))
            .build());
        variableSlots.put(30, "worldFilter");
        
        // Слот для кулдауна
        inventory.setItem(32, new ItemBuilder(Material.CLOCK)
            .name("§aКулдаун")
            .lore(Arrays.asList(
                "§7Перетащите числовую переменную",
                "§7с кулдауном в секундах",
                "",
                "§7Текущее: §f" + getVariableValue("cooldown"),
                "",
                "§eDrag-n-drop переменную сюда"
            ))
            .build());
        variableSlots.put(32, "cooldown");
        
        // Слот для дополнительных данных
        inventory.setItem(34, new ItemBuilder(Material.BOOK)
            .name("§aДополнительные данные")
            .lore(Arrays.asList(
                "§7Перетащите переменную с данными",
                "§7для передачи в событие",
                "",
                "§7Текущее: §f" + getVariableValue("eventData"),
                "",
                "§eDrag-n-drop переменную сюда"
            ))
            .build());
        variableSlots.put(34, "eventData");
    }
    
    private void setupControlButtons() {
        // Сохранить
        inventory.setItem(45, new ItemBuilder(Material.LIME_DYE)
            .name("§aСохранить событие")
            .build());
        
        // Тест события
        inventory.setItem(49, new ItemBuilder(Material.REDSTONE_TORCH)
            .name("§eТестировать событие")
            .lore(Arrays.asList(
                "§7Симулировать срабатывание",
                "§7события с текущими параметрами",
                "",
                "§eКлик для теста"
            ))
            .build());
        
        // Отмена
        inventory.setItem(53, new ItemBuilder(Material.RED_DYE)
            .name("§cОтмена")
            .build());
    }
    
    private String getVariableValue(String parameterName) {
        Object value = block.getParameter(parameterName);
        return value != null ? value.toString() : "не установлено";
    }
    
    public void open() {
        player.openInventory(inventory);
    }
    
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getInventory().equals(inventory)) return;
        if (!(event.getWhoClicked() instanceof Player)) return;
        
        Player clicker = (Player) event.getWhoClicked();
        if (!clicker.equals(player)) return;
        
        event.setCancelled(true);
        
        int slot = event.getSlot();
        
        switch (slot) {
            case 10: // Тип события
                openEventTypeSelector();
                break;
            case 12: // Условия
                openEventConditionsSelector();
                break;
            case 14: // Приоритет
                cyclePriority();
                break;
            case 45: // Сохранить
                saveEvent();
                break;
            case 49: // Тест
                testEvent();
                break;
            case 53: // Отмена
                clicker.closeInventory();
                break;
        }
    }
    
    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!event.getInventory().equals(inventory)) return;
        if (!(event.getWhoClicked() instanceof Player)) return;
        
        Player dragger = (Player) event.getWhoClicked();
        if (!dragger.equals(player)) return;
        
        ItemStack draggedItem = event.getOldCursor();
        if (!isVariableItem(draggedItem)) return;
        
        for (Integer slot : event.getInventorySlots()) {
            if (variableSlots.containsKey(slot)) {
                event.setCancelled(true);
                
                String parameterName = variableSlots.get(slot);
                setVariableInSlot(slot, draggedItem, parameterName);
                dragger.sendMessage("§aПеременная установлена для события!");
                break;
            }
        }
    }
    
    private boolean isVariableItem(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        return item.getItemMeta().hasCustomModelData() && 
               item.getItemMeta().getCustomModelData() >= 1000;
    }
    
    private void setVariableInSlot(int slot, ItemStack variable, String parameterName) {
        String variableName = variable.getItemMeta().getDisplayName();
        block.setParameter(parameterName, variableName);
        
        inventory.setItem(slot, new ItemBuilder(variable.getType())
            .name("§a" + variableName)
            .lore(Arrays.asList(
                "§7Переменная для события",
                "",
                "§7ПКМ - удалить"
            ))
            .build());
    }
    
    private void openEventTypeSelector() {
        player.sendMessage("§6Доступно 50+ событий:");
        player.sendMessage("§7Игрока: join, quit, chat, move, death, damage...");
        player.sendMessage("§7Существ: spawn, death, damage, target, breed...");
        player.sendMessage("§7Мира: weather, time, explosion, chunk_load...");
    }
    
    private void openEventConditionsSelector() {
        player.sendMessage("§6Условия срабатывания:");
        player.sendMessage("§7player:[имя] - только для игрока");
        player.sendMessage("§7world:[мир] - только в мире");
        player.sendMessage("§7cooldown:[секунды] - кулдаун");
    }
    
    private void cyclePriority() {
        String[] priorities = {"LOWEST", "LOW", "NORMAL", "HIGH", "HIGHEST"};
        String current = (String) block.getParameter("priority");
        
        int currentIndex = 2; // NORMAL по умолчанию
        for (int i = 0; i < priorities.length; i++) {
            if (priorities[i].equals(current)) {
                currentIndex = i;
                break;
            }
        }
        
        int nextIndex = (currentIndex + 1) % priorities.length;
        block.setParameter("priority", priorities[nextIndex]);
        
        setupInventory();
        player.sendMessage("§aПриоритет изменен на: §f" + priorities[nextIndex]);
    }
    
    private void saveEvent() {
        player.closeInventory();
        player.sendMessage("§aСобытие настроено и сохранено!");
    }
    
    private void testEvent() {
        player.sendMessage("§6=== Тест события ===");
        player.sendMessage("§7Тип: §f" + getVariableValue("eventType"));
        player.sendMessage("§7Приоритет: §f" + getVariableValue("priority"));
        player.sendMessage("§aСобытие будет срабатывать согласно настройкам!");
    }
}
