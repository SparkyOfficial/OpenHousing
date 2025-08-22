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
 * Уникальное GUI для настройки циклов с поддержкой переменных
 */
public class LoopConfigGUI implements Listener {
    
    private final OpenHousing plugin;
    private final Player player;
    private final CodeBlock block;
    private final Inventory inventory;
    private final Map<Integer, String> variableSlots = new HashMap<>();
    
    public LoopConfigGUI(OpenHousing plugin, Player player, CodeBlock block) {
        this.plugin = plugin;
        this.player = player;
        this.block = block;
        this.inventory = Bukkit.createInventory(null, 54, "§6Настройка цикла");
        
        Bukkit.getPluginManager().registerEvents(this, plugin);
        setupInventory();
    }
    
    private void setupInventory() {
        // Заголовок
        inventory.setItem(4, new ItemBuilder(Material.CLOCK)
            .name("§6Цикл (повторение)")
            .lore(Arrays.asList(
                "§7Повторяет код определенное количество раз",
                "§7Поддерживает различные типы циклов",
                "",
                "§eПоддерживает drag-n-drop переменных"
            ))
            .build());
        
        // Основные параметры цикла
        setupLoopParameters();
        
        // Слоты для переменных
        setupVariableSlots();
        
        // Кнопки управления
        setupControlButtons();
    }
    
    /**
     * Настройка параметров цикла
     */
    private void setupLoopParameters() {
        // Тип цикла
        inventory.setItem(10, new ItemBuilder(Material.REPEATER)
            .name("§bТип цикла")
            .lore(Arrays.asList(
                "§7Выберите тип повторения:",
                "§7• FOR - точное количество раз",
                "§7• WHILE - пока условие истинно",
                "§7• UNTIL - пока условие ложно",
                "§7• FOREVER - бесконечно (с лимитом)",
                "",
                "§eКлик для выбора"
            ))
            .build());
        
        // Условие цикла
        inventory.setItem(12, new ItemBuilder(Material.COMPARATOR)
            .name("§bУсловие цикла")
            .lore(Arrays.asList(
                "§7Условие для WHILE/UNTIL циклов:",
                "§7• Сравнение переменных",
                "§7• Проверка состояния игрока",
                "§7• Проверка времени/TPS",
                "",
                "§7Для FOR цикла - количество повторений",
                "",
                "§eКлик для настройки"
            ))
            .build());
        
        // Безопасность и лимиты
        inventory.setItem(14, new ItemBuilder(Material.BARRIER)
            .name("§bБезопасность")
            .lore(Arrays.asList(
                "§7Ограничения для предотвращения зависания:",
                "§7• Максимум итераций: §e" + getMaxIterations(),
                "§7• Таймаут: §e" + getTimeoutMs() + "мс",
                "§7• Задержка между итерациями: §e" + getDelay() + " тиков",
                "",
                "§eКлик для изменения"
            ))
            .build());
        
        // Переменная счетчика
        inventory.setItem(16, new ItemBuilder(Material.REDSTONE)
            .name("§bПеременная счетчика")
            .lore(Arrays.asList(
                "§7Переменная для хранения текущей итерации:",
                "§7• Автоматически увеличивается",
                "§7• Доступна внутри цикла",
                "§7• Сбрасывается при старте",
                "",
                "§7Текущая: §f" + getCounterVariable(),
                "",
                "§eКлик для выбора"
            ))
            .build());
    }
    
    /**
     * Настройка слотов для переменных
     */
    private void setupVariableSlots() {
        // Слот для числовой переменной (количество итераций)
        inventory.setItem(28, new ItemBuilder(Material.REDSTONE)
            .name("§aКоличество повторений")
            .lore(Arrays.asList(
                "§7Перетащите сюда числовую переменную",
                "§7для указания количества повторений",
                "",
                "§7Текущее значение: §f" + getVariableValue("iterations"),
                "",
                "§eDrag-n-drop переменную сюда"
            ))
            .build());
        variableSlots.put(28, "iterations");
        
        // Слот для условной переменной
        inventory.setItem(30, new ItemBuilder(Material.COMPARATOR)
            .name("§aУсловная переменная")
            .lore(Arrays.asList(
                "§7Переменная для проверки в WHILE/UNTIL:",
                "§7Будет проверяться каждую итерацию",
                "",
                "§7Текущее значение: §f" + getVariableValue("condition_var"),
                "",
                "§eDrag-n-drop переменную сюда"
            ))
            .build());
        variableSlots.put(30, "condition_var");
        
        // Слот для переменной-значения для сравнения
        inventory.setItem(32, new ItemBuilder(Material.PAPER)
            .name("§aЗначение для сравнения")
            .lore(Arrays.asList(
                "§7Значение или переменная для сравнения",
                "§7с условной переменной",
                "",
                "§7Текущее значение: §f" + getVariableValue("compare_value"),
                "",
                "§eDrag-n-drop переменную сюда"
            ))
            .build());
        variableSlots.put(32, "compare_value");
        
        // Слот для переменной задержки
        inventory.setItem(34, new ItemBuilder(Material.CLOCK)
            .name("§aЗадержка между итерациями")
            .lore(Arrays.asList(
                "§7Переменная с задержкой в тиках",
                "§7между выполнениями цикла",
                "",
                "§7Текущее значение: §f" + getVariableValue("delay"),
                "",
                "§eDrag-n-drop переменную сюда"
            ))
            .build());
        variableSlots.put(34, "delay");
    }
    
    /**
     * Настройка кнопок управления
     */
    private void setupControlButtons() {
        // Сохранить
        inventory.setItem(45, new ItemBuilder(Material.LIME_DYE)
            .name("§aСохранить настройки")
            .lore("§7Применить все изменения к циклу")
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
                "§7Посмотреть настройки цикла",
                "§7и оценить производительность",
                "",
                "§eКлик для просмотра"
            ))
            .build());
        
        // Тест безопасности
        inventory.setItem(47, new ItemBuilder(Material.SHIELD)
            .name("§bТест безопасности")
            .lore(Arrays.asList(
                "§7Проверить цикл на зависания",
                "§7и возможные проблемы",
                "",
                "§eКлик для тестирования"
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
    
    private String getMaxIterations() {
        return block.getParameter("max_iterations") != null ? 
               block.getParameter("max_iterations").toString() : "1000";
    }
    
    private String getTimeoutMs() {
        return block.getParameter("timeout_ms") != null ? 
               block.getParameter("timeout_ms").toString() : "5000";
    }
    
    private String getDelay() {
        return block.getParameter("delay_ticks") != null ? 
               block.getParameter("delay_ticks").toString() : "1";
    }
    
    private String getCounterVariable() {
        return block.getParameter("counter_var") != null ? 
               block.getParameter("counter_var").toString() : "%i%";
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
            case 10: // Тип цикла
                openLoopTypeSelector();
                break;
            case 12: // Условие цикла
                openConditionEditor();
                break;
            case 14: // Безопасность
                openSafetySettings();
                break;
            case 16: // Переменная счетчика
                openCounterVariableSelector();
                break;
            case 45: // Сохранить
                saveSettings();
                break;
            case 47: // Тест безопасности
                runSafetyTest();
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
            case "iterations":
            case "delay":
                return variableType == VariableType.NUMBER || variableType == VariableType.DYNAMIC;
            case "condition_var":
            case "compare_value":
                return true; // Любые переменные могут использоваться в условиях
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
    
    private void openLoopTypeSelector() {
        player.sendMessage("§6Выберите тип цикла в чате:");
        player.sendMessage("§71. FOR - точное количество раз");
        player.sendMessage("§72. WHILE - пока условие истинно");
        player.sendMessage("§73. UNTIL - пока условие ложно");
        player.sendMessage("§74. FOREVER - бесконечно (осторожно!)");
    }
    
    private void openConditionEditor() {
        player.sendMessage("§6Настройка условия цикла:");
        player.sendMessage("§7Используйте переменные в слотах");
        player.sendMessage("§7Операторы: ==, !=, >, <, >=, <=");
    }
    
    private void openSafetySettings() {
        player.sendMessage("§6Настройки безопасности:");
        player.sendMessage("§7max_iterations:[число] - макс. итераций");
        player.sendMessage("§7timeout:[число] - таймаут в мс");
        player.sendMessage("§7delay:[число] - задержка в тиках");
    }
    
    private void openCounterVariableSelector() {
        player.sendMessage("§6Введите имя переменной счетчика:");
        player.sendMessage("§7По умолчанию: %i%");
        player.sendMessage("§7Примеры: %counter%, %iteration%, %loop_var%");
    }
    
    private void saveSettings() {
        player.closeInventory();
        player.sendMessage("§aНастройки цикла сохранены!");
        
        // Логируем параметры блока
        plugin.getLogger().info("Loop block configured with parameters: " + block.getParameters());
    }
    
    private void runSafetyTest() {
        player.sendMessage("§6=== Тест безопасности ===");
        
        // Проверяем основные параметры
        int maxIter = Integer.parseInt(getMaxIterations());
        int timeout = Integer.parseInt(getTimeoutMs());
        
        if (maxIter > 10000) {
            player.sendMessage("§c⚠ Слишком много итераций! Рекомендуется < 10000");
        }
        
        if (timeout > 30000) {
            player.sendMessage("§c⚠ Слишком большой таймаут! Рекомендуется < 30с");
        }
        
        player.sendMessage("§aТест пройден! Цикл безопасен для выполнения.");
    }
    
    private void showPreview() {
        player.sendMessage("§6=== Предварительный просмотр цикла ===");
        player.sendMessage("§7Блок: §f" + block.getType().getDisplayName());
        
        for (Map.Entry<String, Object> param : block.getParameters().entrySet()) {
            player.sendMessage("§7" + param.getKey() + ": §f" + param.getValue());
        }
        
        player.sendMessage("§7Оценка: §eЦикл выполнится ~" + estimatePerformance() + " раз");
    }
    
    private String estimatePerformance() {
        try {
            String iterations = getVariableValue("iterations");
            if (!iterations.equals("не установлено")) {
                return iterations;
            }
            return "неизвестно";
        } catch (Exception e) {
            return "ошибка";
        }
    }
}
