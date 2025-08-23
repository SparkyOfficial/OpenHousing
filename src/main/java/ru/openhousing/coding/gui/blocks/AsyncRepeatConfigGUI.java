package ru.openhousing.coding.gui.blocks;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import ru.openhousing.OpenHousing;
import ru.openhousing.coding.blocks.control.AsyncRepeatBlock;
import ru.openhousing.coding.blocks.control.AsyncRepeatBlock.RepeatType;
import ru.openhousing.coding.constants.BlockParams;
import ru.openhousing.coding.gui.BaseBlockConfigGUI;
import ru.openhousing.coding.gui.ValueSelectorGUI;
import ru.openhousing.coding.values.Value;

import java.util.Arrays;

/**
 * GUI для настройки AsyncRepeatBlock (асинхронных циклов)
 */
public class AsyncRepeatConfigGUI extends BaseBlockConfigGUI {
    
    // Слоты GUI
    private static final int REPEAT_TYPE_SLOT = 10;
    private static final int VALUE_SLOT = 12;
    private static final int MAX_ITERATIONS_SLOT = 14;
    private static final int DELAY_TICKS_SLOT = 16;
    
    public AsyncRepeatConfigGUI(OpenHousing plugin, Player player, AsyncRepeatBlock block, Runnable onSaveCallback) {
        super(plugin, player, block, onSaveCallback);
    }
    
    @Override
    public void setupInventory() {
        this.inventory = Bukkit.createInventory(this, 36, "§8Настройка асинхронного цикла");
        
        // Заполняем фон
        for (int i = 0; i < inventory.getSize(); i++) {
            inventory.setItem(i, new ItemStack(Material.GRAY_STAINED_GLASS_PANE));
        }
        
        // Заголовок
        ItemStack headerItem = new ItemStack(Material.CLOCK);
        ItemMeta headerMeta = headerItem.getItemMeta();
        headerMeta.setDisplayName("§6§lАсинхронный цикл");
        headerMeta.setLore(Arrays.asList(
            "§7Настройте параметры цикла",
            "§7для предотвращения server lag"
        ));
        headerItem.setItemMeta(headerMeta);
        inventory.setItem(4, headerItem);
        
        // Тип повторения
        RepeatType currentType = (RepeatType) block.getParameter(BlockParams.REPEAT_TYPE);
        ItemStack typeItem = new ItemStack(Material.REPEATER);
        ItemMeta typeMeta = typeItem.getItemMeta();
        typeMeta.setDisplayName("§eТип повторения");
        typeMeta.setLore(Arrays.asList(
            "§7Текущий: §f" + (currentType != null ? currentType.getDisplayName() : "Не выбран"),
            "",
            "§7Кликните для выбора типа",
            "§7цикла"
        ));
        typeItem.setItemMeta(typeMeta);
        inventory.setItem(REPEAT_TYPE_SLOT, typeItem);
        
        // Значение (количество, условие)
        String currentValue = (String) block.getParameter(BlockParams.VALUE);
        ItemStack valueItem = new ItemStack(Material.PAPER);
        ItemMeta valueMeta = valueItem.getItemMeta();
        valueMeta.setDisplayName("§eЗначение");
        valueMeta.setLore(Arrays.asList(
            "§7Текущее: §f" + (currentValue != null ? currentValue : "Не указано"),
            "",
            "§7Для TIMES: количество итераций",
            "§7Для WHILE: условие",
            "§7Для FOR_EACH: переменная со списком",
            "",
            "§7Кликните для изменения"
        ));
        valueItem.setItemMeta(valueMeta);
        inventory.setItem(VALUE_SLOT, valueItem);
        
        // Максимум итераций
        String maxIterations = (String) block.getParameter(BlockParams.MAX_ITERATIONS);
        ItemStack maxItem = new ItemStack(Material.BARRIER);
        ItemMeta maxMeta = maxItem.getItemMeta();
        maxMeta.setDisplayName("§cМаксимум итераций");
        maxMeta.setLore(Arrays.asList(
            "§7Текущий: §f" + (maxIterations != null ? maxIterations : "1000"),
            "",
            "§7Защита от бесконечных циклов",
            "§7Рекомендуется: 100-1000",
            "",
            "§7Кликните для изменения"
        ));
        maxItem.setItemMeta(maxMeta);
        inventory.setItem(MAX_ITERATIONS_SLOT, maxItem);
        
        // Задержка между итерациями
        String delayTicks = (String) block.getParameter("delay_ticks");
        ItemStack delayItem = new ItemStack(Material.HOPPER);
        ItemMeta delayMeta = delayItem.getItemMeta();
        delayMeta.setDisplayName("§aЗадержка (тики)");
        delayMeta.setLore(Arrays.asList(
            "§7Текущая: §f" + (delayTicks != null ? delayTicks : "1"),
            "",
            "§7Задержка между итерациями",
            "§720 тиков = 1 секунда",
            "§7Рекомендуется: 1-5",
            "",
            "§7Кликните для изменения"
        ));
        delayItem.setItemMeta(delayMeta);
        inventory.setItem(DELAY_TICKS_SLOT, delayItem);
        
        // Информация о производительности
        ItemStack infoItem = new ItemStack(Material.BOOK);
        ItemMeta infoMeta = infoItem.getItemMeta();
        infoMeta.setDisplayName("§bИнформация о производительности");
        infoMeta.setLore(Arrays.asList(
            "§7Этот блок предотвращает server lag",
            "§7путем распределения нагрузки по тикам",
            "",
            "§7• Итерации выполняются асинхронно",
            "§7• Контроль времени выполнения",
            "§7• Автоматическая остановка при ошибках",
            "§7• Лимиты для безопасности"
        ));
        infoItem.setItemMeta(infoMeta);
        inventory.setItem(20, infoItem);
        
        // Настраиваем навигационные элементы
        setupNavigationItems();
    }
    
    @Override
    protected void handleSpecificClick(int slot, boolean isRightClick, boolean isShiftClick) {
        switch (slot) {
            case REPEAT_TYPE_SLOT:
                openRepeatTypeSelector();
                break;
                
            case VALUE_SLOT:
                openValueSelector();
                break;
                
            case MAX_ITERATIONS_SLOT:
                openMaxIterationsInput();
                break;
                
            case DELAY_TICKS_SLOT:
                openDelayTicksInput();
                break;
        }
    }
    
    @Override
    protected String getParameterPrompt(String parameterName) {
        return switch (parameterName) {
            case BlockParams.REPEAT_TYPE -> "Выберите тип цикла";
            case BlockParams.VALUE -> "Введите значение для цикла";
            case BlockParams.MAX_ITERATIONS -> "Введите максимум итераций (1-10000)";
            case "delay_ticks" -> "Введите задержку в тиках (1-100)";
            default -> "Введите значение для параметра " + parameterName;
        };
    }
    
    /**
     * Открытие селектора типа повторения
     */
    private void openRepeatTypeSelector() {
        Inventory selector = Bukkit.createInventory(null, 54, "§8Выберите тип цикла");
        
        // Заполняем фон
        for (int i = 0; i < selector.getSize(); i++) {
            selector.setItem(i, new ItemStack(Material.GRAY_STAINED_GLASS_PANE));
        }
        
        // Заголовок
        ItemStack headerItem = new ItemStack(Material.REPEATER);
        ItemMeta headerMeta = headerItem.getItemMeta();
        headerMeta.setDisplayName("§6§lТип цикла");
        headerMeta.setLore(Arrays.asList("§7Выберите тип повторения"));
        headerItem.setItemMeta(headerMeta);
        selector.setItem(4, headerItem);
        
        // Типы циклов
        int slot = 19;
        for (RepeatType type : RepeatType.values()) {
            if (slot >= 53) break;
            
            ItemStack typeItem = new ItemStack(getMaterialForType(type));
            ItemMeta typeMeta = typeItem.getItemMeta();
            typeMeta.setDisplayName("§e" + type.getDisplayName());
            typeMeta.setLore(Arrays.asList(
                "§7" + type.getDescription(),
                "",
                "§7Кликните для выбора"
            ));
            typeMeta.setItemMeta(typeMeta);
            selector.setItem(slot, typeItem);
            
            slot++;
        }
        
        // Кнопка назад
        ItemStack backItem = new ItemStack(Material.ARROW);
        ItemMeta backMeta = backItem.getItemMeta();
        backMeta.setDisplayName("§cНазад");
        backMeta.setLore(Arrays.asList("§7Вернуться к настройкам"));
        backItem.setItemMeta(backMeta);
        selector.setItem(49, backItem);
        
        player.openInventory(selector);
    }
    
    /**
     * Получение материала для типа цикла
     */
    private Material getMaterialForType(RepeatType type) {
        return switch (type) {
            case TIMES -> Material.CLOCK;
            case WHILE -> Material.REDSTONE_TORCH;
            case FOR_EACH -> Material.HOPPER;
            case FOREVER -> Material.BARRIER;
            case UNTIL -> Material.REDSTONE_TORCH;
        };
    }
    
    /**
     * Открытие селектора значения
     */
    private void openValueSelector() {
        RepeatType currentType = (RepeatType) block.getParameter(BlockParams.REPEAT_TYPE);
        if (currentType == null) {
            player.sendMessage("§cСначала выберите тип цикла!");
            return;
        }
        
        // Открываем ValueSelectorGUI с подходящими типами
        ValueSelectorGUI.openValueSelector(player, "Значение для " + currentType.getDisplayName(), 
            new String[]{"TEXT", "VARIABLE", "NUMBER"}, this::onValueSelected);
    }
    
    /**
     * Открытие ввода максимального количества итераций
     */
    private void openMaxIterationsInput() {
        player.sendMessage("§eВведите максимальное количество итераций (1-10000):");
        player.sendMessage("§7Используйте команду: /openhousing set " + block.getId() + " max_iterations <число>");
        
        // Альтернативно можно использовать AnvilGUI
        openAnvilInput("Максимум итераций", 
            (String) block.getParameter(BlockParams.MAX_ITERATIONS), 
            this::onMaxIterationsSet);
    }
    
    /**
     * Открытие ввода задержки
     */
    private void openDelayTicksInput() {
        player.sendMessage("§eВведите задержку между итерациями в тиках (1-100):");
        player.sendMessage("§7Используйте команду: /openhousing set " + block.getId() + " delay_ticks <число>");
        
        // Альтернативно можно использовать AnvilGUI
        openAnvilInput("Задержка (тики)", 
            (String) block.getParameter("delay_ticks"), 
            this::onDelayTicksSet);
    }
    
    /**
     * Обработка выбора типа повторения
     */
    private void onRepeatTypeSelected(RepeatType type) {
        block.setParameter(BlockParams.REPEAT_TYPE, type);
        player.sendMessage("§aТип цикла установлен: " + type.getDisplayName());
        refreshInventory();
    }
    
    /**
     * Обработка выбора значения
     */
    private void onValueSelected(Value value) {
        block.setParameter(BlockParams.VALUE, value.getRawValue());
        player.sendMessage("§aЗначение установлено: " + value.getDisplayValue());
        refreshInventory();
    }
    
    /**
     * Обработка установки максимального количества итераций
     */
    private void onMaxIterationsSet(String value) {
        try {
            int iterations = Integer.parseInt(value);
            if (iterations < 1 || iterations > 10000) {
                player.sendMessage("§cКоличество итераций должно быть от 1 до 10000!");
                return;
            }
            block.setParameter(BlockParams.MAX_ITERATIONS, String.valueOf(iterations));
            player.sendMessage("§aМаксимум итераций установлен: " + iterations);
            refreshInventory();
        } catch (NumberFormatException e) {
            player.sendMessage("§cВведите корректное число!");
        }
    }
    
    /**
     * Обработка установки задержки
     */
    private void onDelayTicksSet(String value) {
        try {
            int delay = Integer.parseInt(value);
            if (delay < 1 || delay > 100) {
                player.sendMessage("§cЗадержка должна быть от 1 до 100 тиков!");
                return;
            }
            block.setParameter("delay_ticks", String.valueOf(delay));
            player.sendMessage("§aЗадержка установлена: " + delay + " тиков");
            refreshInventory();
        } catch (NumberFormatException e) {
            player.sendMessage("§cВведите корректное число!");
        }
    }
    
    /**
     * Сохранение и закрытие
     */
    private void saveAndClose() {
        if (validateBlock()) {
            player.sendMessage("§aНастройки асинхронного цикла сохранены!");
            player.closeInventory();
        } else {
            player.sendMessage("§cОшибка валидации блока! Проверьте настройки.");
        }
    }
    
    /**
     * Валидация блока
     */
    private boolean validateBlock() {
        return block.validate();
    }
    
    /**
     * Обновление инвентаря
     */
    private void refreshInventory() {
        player.openInventory(createInventory());
    }
}
