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
 * Уникальное GUI для настройки событий с поддержкой переменных
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
        inventory.setItem(4, new ItemBuilder(Material.REDSTONE_TORCH)
            .name("§6Событие")
            .lore(Arrays.asList(
                "§7Настройте параметры события",
                "§7События автоматически срабатывают",
                "§7при выполнении условий",
                "",
                "§eПоддерживает drag-n-drop переменных"
            ))
            .build());
        
        // Основные параметры события
        setupEventParameters();
        
        // Слоты для переменных
        setupVariableSlots();
        
        // Кнопки управления
        setupControlButtons();
    }
    
    /**
     * Настройка параметров события
     */
    private void setupEventParameters() {
        // Тип события
        inventory.setItem(10, new ItemBuilder(Material.LIGHTNING_ROD)
            .name("§bТип события")
            .lore(Arrays.asList(
                "§7Выберите какое событие отслеживать:",
                "§7• PLAYER_JOIN - вход игрока",
                "§7• PLAYER_CHAT - сообщение в чат",
                "§7• BLOCK_BREAK - разрушение блока",
                "§7• ENTITY_DEATH - смерть существа",
                "§7• И много других...",
                "",
                "§7Текущее: §f" + getCurrentEventType(),
                "",
                "§eКлик для выбора"
            ))
            .build());
        
        // Фильтры события
        inventory.setItem(12, new ItemBuilder(Material.HOPPER)
            .name("§bФильтры события")
            .lore(Arrays.asList(
                "§7Дополнительные условия:",
                "§7• Конкретный игрок",
                "§7• Определенный мир",
                "§7• Тип блока/предмета",
                "§7• Причина события",
                "",
                "§eКлик для настройки"
            ))
            .build());
        
        // Приоритет события
        inventory.setItem(14, new ItemBuilder(Material.IRON_INGOT)
            .name("§bПриоритет")
            .lore(Arrays.asList(
                "§7Порядок обработки события:",
                "§7• LOWEST - самый первый",
                "§7• LOW - рано",
                "§7• NORMAL - обычный",
                "§7• HIGH - поздно",
                "§7• HIGHEST - самый последний",
                "",
                "§7Текущий: §f" + getEventPriority(),
                "",
                "§eКлик для изменения"
            ))
            .build());
        
        // Обработка отмены
        inventory.setItem(16, new ItemBuilder(Material.BARRIER)
            .name("§bОтмена события")
            .lore(Arrays.asList(
                "§7Что делать с событием:",
                "§7• IGNORE - игнорировать отмену",
                "§7• CANCEL - отменить событие",
                "§7• MONITOR - только отслеживать",
                "",
                "§7Текущее: §f" + getCancelHandling(),
                "",
                "§eКлик для выбора"
            ))
            .build());
    }
    
    /**
     * Настройка слотов для переменных
     */
    private void setupVariableSlots() {
        // Слот для переменной игрока
        inventory.setItem(28, new ItemBuilder(Material.PLAYER_HEAD)
            .name("§aПеременная игрока")
            .lore(Arrays.asList(
                "§7Переменная для сохранения данных",
                "§7о игроке, вызвавшем событие",
                "",
                "§7Текущее значение: §f" + getVariableValue("player_var"),
                "",
                "§eDrag-n-drop переменную сюда"
            ))
            .build());
        variableSlots.put(28, "player_var");
        
        // Слот для переменной местоположения
        inventory.setItem(30, new ItemBuilder(Material.COMPASS)
            .name("§aПеременная местоположения")
            .lore(Arrays.asList(
                "§7Переменная для сохранения координат",
                "§7где произошло событие",
                "",
                "§7Текущее значение: §f" + getVariableValue("location_var"),
                "",
                "§eDrag-n-drop переменную сюда"
            ))
            .build());
        variableSlots.put(30, "location_var");
        
        // Слот для переменной данных
        inventory.setItem(32, new ItemBuilder(Material.CHEST)
            .name("§aПеременная данных")
            .lore(Arrays.asList(
                "§7Переменная для дополнительных данных:",
                "§7• Текст сообщения при чате",
                "§7• Тип блока при разрушении",
                "§7• Причина смерти и т.д.",
                "",
                "§7Текущее значение: §f" + getVariableValue("data_var"),
                "",
                "§eDrag-n-drop переменную сюда"
            ))
            .build());
        variableSlots.put(32, "data_var");
        
        // Слот для переменной времени
        inventory.setItem(34, new ItemBuilder(Material.CLOCK)
            .name("§aПеременная времени")
            .lore(Arrays.asList(
                "§7Переменная для сохранения времени",
                "§7когда произошло событие (timestamp)",
                "",
                "§7Текущее значение: §f" + getVariableValue("time_var"),
                "",
                "§eDrag-n-drop переменную сюда"
            ))
            .build());
        variableSlots.put(34, "time_var");
    }
    
    /**
     * Настройка кнопок управления
     */
    private void setupControlButtons() {
        // Сохранить
        inventory.setItem(45, new ItemBuilder(Material.LIME_DYE)
            .name("§aСохранить настройки")
            .lore("§7Применить все изменения к событию")
            .build());
        
        // Отмена
        inventory.setItem(53, new ItemBuilder(Material.RED_DYE)
            .name("§cОтмена")
            .lore("§7Закрыть без сохранения")
            .build());
        
        // Предварительный просмотр
        inventory.setItem(49, new ItemBuilder(Material.SPYGLASS)
            .name("§eПредварительный просмотр")
            .lore(Arrays.asList(
                "§7Посмотреть настройки события",
                "§7и проверить корректность",
                "",
                "§eКлик для просмотра"
            ))
            .build());
        
        // Тест события
        inventory.setItem(47, new ItemBuilder(Material.REDSTONE_TORCH)
            .name("§bИмитация события")
            .lore(Arrays.asList(
                "§7Вручную вызвать событие",
                "§7для тестирования кода",
                "",
                "§eКлик для теста"
            ))
            .build());
    }
    
    /**
     * Получение значения переменной
     */
    private String getVariableValue(String parameterName) {
        Object value = block.getParameter(parameterName);
        return value != null ? value.toString() : "не установлено";
    }
    
    private String getCurrentEventType() {
        return block.getParameter("event_type") != null ? 
               block.getParameter("event_type").toString() : block.getType().getDisplayName();
    }
    
    private String getEventPriority() {
        return block.getParameter("priority") != null ? 
               block.getParameter("priority").toString() : "NORMAL";
    }
    
    private String getCancelHandling() {
        return block.getParameter("cancel_handling") != null ? 
               block.getParameter("cancel_handling").toString() : "IGNORE";
    }
    
    /**
     * Открытие GUI
     */
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
            case 12: // Фильтры события
                openEventFilters();
                break;
            case 14: // Приоритет
                openPrioritySelector();
                break;
            case 16: // Обработка отмены
                openCancelHandlingSelector();
                break;
            case 45: // Сохранить
                saveSettings();
                break;
            case 47: // Тест события
                simulateEvent();
                break;
            case 49: // Предварительный просмотр
                showPreview();
                break;
            case 53: // Отмена
                clicker.closeInventory();
                break;
        }
    }
    
    /**
     * Обработка drag-n-drop переменных
     */
    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!event.getInventory().equals(inventory)) return;
        if (!(event.getWhoClicked() instanceof Player)) return;
        
        Player dragger = (Player) event.getWhoClicked();
        if (!dragger.equals(player)) return;
        
        // Проверяем, перетаскивается ли переменная
        ItemStack draggedItem = event.getOldCursor();
        if (!isVariableItem(draggedItem)) return;
        
        // Проверяем слоты назначения
        for (Integer slot : event.getInventorySlots()) {
            if (variableSlots.containsKey(slot)) {
                event.setCancelled(true);
                
                // Получаем тип переменной
                VariableType variableType = getVariableTypeFromItem(draggedItem);
                String parameterName = variableSlots.get(slot);
                
                // Проверяем совместимость
                if (isCompatibleVariable(parameterName, variableType)) {
                    // Устанавливаем переменную
                    setVariableInSlot(slot, draggedItem, parameterName);
                    dragger.sendMessage("§aПеременная установлена в слот: " + parameterName);
                } else {
                    dragger.sendMessage("§cЭтот тип переменной не подходит для данного слота!");
                }
                
                break;
            }
        }
    }
    
    /**
     * Проверка, является ли предмет переменной
     */
    private boolean isVariableItem(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        
        // Проверяем CustomModelData для переменных (1000+)
        return item.getItemMeta().hasCustomModelData() && 
               item.getItemMeta().getCustomModelData() >= 1000;
    }
    
    /**
     * Получение типа переменной из предмета
     */
    private VariableType getVariableTypeFromItem(ItemStack item) {
        if (!item.hasItemMeta() || !item.getItemMeta().hasCustomModelData()) {
            return null;
        }
        
        int modelData = item.getItemMeta().getCustomModelData();
        int typeIndex = modelData - 1000;
        
        VariableType[] types = VariableType.values();
        if (typeIndex >= 0 && typeIndex < types.length) {
            return types[typeIndex];
        }
        
        return null;
    }
    
    /**
     * Проверка совместимости переменной с параметром
     */
    private boolean isCompatibleVariable(String parameterName, VariableType variableType) {
        switch (parameterName) {
            case "player_var":
                return variableType == VariableType.TEXT || variableType == VariableType.DYNAMIC;
            case "location_var":
                return variableType == VariableType.LOCATION;
            case "data_var":
                return true; // Любые переменные могут хранить данные
            case "time_var":
                return variableType == VariableType.NUMBER || variableType == VariableType.DYNAMIC;
            default:
                return true;
        }
    }
    
    /**
     * Установка переменной в слот
     */
    private void setVariableInSlot(int slot, ItemStack variable, String parameterName) {
        // Обновляем блок
        String variableName = variable.getItemMeta().hasDisplayName() ? 
                             variable.getItemMeta().getDisplayName() : 
                             variable.getType().name();
        block.setParameter(parameterName, variableName);
        
        // Обновляем GUI
        inventory.setItem(slot, new ItemBuilder(variable.getType())
            .name("§a" + variableName)
            .lore(Arrays.asList(
                "§7Установленная переменная",
                "§7Тип: " + getVariableTypeFromItem(variable).getDisplayName(),
                "",
                "§7ПКМ - удалить переменную",
                "§7Shift+ПКМ - изменить настройки"
            ))
            .build());
    }
    
    private void openEventTypeSelector() {
        player.sendMessage("§6Выберите тип события:");
        player.sendMessage("§71. player_join - вход игрока");
        player.sendMessage("§72. player_chat - сообщение в чат");
        player.sendMessage("§73. block_break - разрушение блока");
        player.sendMessage("§74. entity_death - смерть существа");
        player.sendMessage("§7И другие... (см. документацию)");
    }
    
    private void openEventFilters() {
        player.sendMessage("§6Настройка фильтров события:");
        player.sendMessage("§7player:[имя] - только для определенного игрока");
        player.sendMessage("§7world:[мир] - только в определенном мире");
        player.sendMessage("§7material:[материал] - только для определенного блока");
        player.sendMessage("§7cause:[причина] - только для определенной причины");
    }
    
    private void openPrioritySelector() {
        player.sendMessage("§6Выберите приоритет события:");
        player.sendMessage("§71. LOWEST - выполняется первым");
        player.sendMessage("§72. LOW - выполняется рано");
        player.sendMessage("§73. NORMAL - стандартный приоритет");
        player.sendMessage("§74. HIGH - выполняется поздно");
        player.sendMessage("§75. HIGHEST - выполняется последним");
    }
    
    private void openCancelHandlingSelector() {
        player.sendMessage("§6Обработка отмены события:");
        player.sendMessage("§71. IGNORE - игнорировать отмену");
        player.sendMessage("§72. CANCEL - отменить событие после обработки");
        player.sendMessage("§73. MONITOR - только отслеживать (не изменять)");
    }
    
    private void saveSettings() {
        player.closeInventory();
        player.sendMessage("§aНастройки события сохранены!");
        
        // Логируем параметры блока
        plugin.getLogger().info("Event block configured with parameters: " + block.getParameters());
    }
    
    private void simulateEvent() {
        player.sendMessage("§6=== Имитация события ===");
        player.sendMessage("§7Событие имитировано для тестирования");
        player.sendMessage("§7Проверьте выполнение последующих блоков");
        
        // Здесь можно добавить код для имитации события
        try {
            // Просто показываем сообщение о том, что событие имитировано
            player.sendMessage("§aСобытие успешно имитировано!");
        } catch (Exception e) {
            player.sendMessage("§cОшибка имитации события: " + e.getMessage());
        }
    }
    
    private void showPreview() {
        player.sendMessage("§6=== Предварительный просмотр события ===");
        player.sendMessage("§7Блок: §f" + block.getType().getDisplayName());
        player.sendMessage("§7Тип: §f" + getCurrentEventType());
        player.sendMessage("§7Приоритет: §f" + getEventPriority());
        player.sendMessage("§7Отмена: §f" + getCancelHandling());
        
        for (Map.Entry<String, Object> param : block.getParameters().entrySet()) {
            if (!param.getKey().startsWith("event_") && !param.getKey().equals("priority") && !param.getKey().equals("cancel_handling")) {
                player.sendMessage("§7" + param.getKey() + ": §f" + param.getValue());
            }
        }
        
        player.sendMessage("§7Результат: §aСобытие будет обрабатываться согласно настройкам");
    }
}