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
 * Уникальное GUI для настройки действий игрока с поддержкой переменных
 */
public class PlayerActionConfigGUI implements Listener {
    
    private final OpenHousing plugin;
    private final Player player;
    private final CodeBlock block;
    private final Inventory inventory;
    private final Map<Integer, String> variableSlots = new HashMap<>();
    
    public PlayerActionConfigGUI(OpenHousing plugin, Player player, CodeBlock block) {
        this.plugin = plugin;
        this.player = player;
        this.block = block;
        this.inventory = Bukkit.createInventory(null, 54, "§6Настройка действия игрока");
        
        Bukkit.getPluginManager().registerEvents(this, plugin);
        setupInventory();
    }
    
    private void setupInventory() {
        // Заголовок
        inventory.setItem(4, new ItemBuilder(Material.PLAYER_HEAD)
            .name("§6Действие игрока")
            .lore(Arrays.asList(
                "§7Настройте параметры действия",
                "§7Перетащите переменные в слоты",
                "",
                "§eПоддерживает drag-n-drop переменных"
            ))
            .build());
        
        // Основные параметры действия
        setupActionParameters();
        
        // Слоты для переменных
        setupVariableSlots();
        
        // Кнопки управления
        setupControlButtons();
    }
    
    /**
     * Настройка параметров действия
     */
    private void setupActionParameters() {
        // Тип действия
        inventory.setItem(10, new ItemBuilder(Material.WRITABLE_BOOK)
            .name("§bТип действия")
            .lore(Arrays.asList(
                "§7Выберите что делать с игроком:",
                "§7• Отправить сообщение",
                "§7• Телепортировать",
                "§7• Дать предмет",
                "§7• Изменить здоровье",
                "§7• Воспроизвести звук",
                "",
                "§eКлик для выбора"
            ))
            .build());
        
        // Цель действия
        inventory.setItem(12, new ItemBuilder(Material.COMPASS)
            .name("§bЦель действия")
            .lore(Arrays.asList(
                "§7Кого выбрать:",
                "§7• %player% - текущий игрок",
                "§7• %selected% - выбранные",
                "§7• %random% - случайный",
                "§7• Конкретный игрок",
                "",
                "§eКлик для выбора"
            ))
            .build());
        
        // Дополнительные параметры
        inventory.setItem(14, new ItemBuilder(Material.REDSTONE)
            .name("§bДополнительно")
            .lore(Arrays.asList(
                "§7Дополнительные настройки:",
                "§7• Задержка выполнения",
                "§7• Условия выполнения",
                "§7• Повторения",
                "",
                "§eКлик для настройки"
            ))
            .build());
    }
    
    /**
     * Настройка слотов для переменных
     */
    private void setupVariableSlots() {
        // Слот для текстовых переменных
        inventory.setItem(28, new ItemBuilder(Material.PAPER)
            .name("§aТекстовая переменная")
            .lore(Arrays.asList(
                "§7Перетащите сюда переменную типа 'Текст'",
                "§7для использования в сообщениях",
                "",
                "§7Текущее значение: §f" + getVariableValue("text"),
                "",
                "§eDrag-n-drop переменную сюда"
            ))
            .build());
        variableSlots.put(28, "text");
        
        // Слот для числовых переменных
        inventory.setItem(30, new ItemBuilder(Material.REDSTONE)
            .name("§aЧисловая переменная")
            .lore(Arrays.asList(
                "§7Перетащите сюда переменную типа 'Число'",
                "§7для математических операций",
                "",
                "§7Текущее значение: §f" + getVariableValue("number"),
                "",
                "§eDrag-n-drop переменную сюда"
            ))
            .build());
        variableSlots.put(30, "number");
        
        // Слот для местоположения
        inventory.setItem(32, new ItemBuilder(Material.COMPASS)
            .name("§aПеременная местоположения")
            .lore(Arrays.asList(
                "§7Перетащите сюда переменную 'Местоположение'",
                "§7для телепортации или проверки координат",
                "",
                "§7Текущее значение: §f" + getVariableValue("location"),
                "",
                "§eDrag-n-drop переменную сюда"
            ))
            .build());
        variableSlots.put(32, "location");
        
        // Слот для динамических переменных
        inventory.setItem(34, new ItemBuilder(Material.CHEST)
            .name("§aДинамическая переменная")
            .lore(Arrays.asList(
                "§7Перетащите сюда динамическую переменную",
                "§7с плейсхолдерами (%player%, %victim% и т.д.)",
                "",
                "§7Текущее значение: §f" + getVariableValue("dynamic"),
                "",
                "§eDrag-n-drop переменную сюда"
            ))
            .build());
        variableSlots.put(34, "dynamic");
    }
    
    /**
     * Настройка кнопок управления
     */
    private void setupControlButtons() {
        // Сохранить
        inventory.setItem(45, new ItemBuilder(Material.LIME_DYE)
            .name("§aСохранить настройки")
            .lore("§7Применить все изменения к блоку")
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
                "§7Посмотреть как будет выглядеть",
                "§7выполнение этого действия",
                "",
                "§eКлик для просмотра"
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
            case 10: // Тип действия
                openActionTypeSelector();
                break;
            case 12: // Цель действия
                openTargetSelector();
                break;
            case 14: // Дополнительные параметры
                openAdditionalSettings();
                break;
            case 45: // Сохранить
                saveSettings();
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
            case "text":
                return variableType == VariableType.TEXT || variableType == VariableType.DYNAMIC;
            case "number":
                return variableType == VariableType.NUMBER || variableType == VariableType.DYNAMIC;
            case "location":
                return variableType == VariableType.LOCATION;
            case "dynamic":
                return variableType == VariableType.DYNAMIC;
            default:
                return true; // Динамические переменные подходят везде
        }
    }
    
    /**
     * Установка переменной в слот
     */
    private void setVariableInSlot(int slot, ItemStack variable, String parameterName) {
        // Обновляем блок
        String variableName = variable.getItemMeta().getDisplayName();
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
    
    private void openActionTypeSelector() {
        // Открываем селектор типа действия
        player.sendMessage("§6Выберите тип действия в чате:");
        player.sendMessage("§71. send_message - отправить сообщение");
        player.sendMessage("§72. teleport - телепортировать");
        player.sendMessage("§73. give_item - дать предмет");
        player.sendMessage("§74. set_health - установить здоровье");
        player.sendMessage("§75. play_sound - воспроизвести звук");
    }
    
    private void openTargetSelector() {
        player.sendMessage("§6Выберите цель в чате:");
        player.sendMessage("§71. %player% - текущий игрок");
        player.sendMessage("§72. %selected% - выбранные игроки");
        player.sendMessage("§73. %random% - случайный игрок");
        player.sendMessage("§74. [имя] - конкретный игрок");
    }
    
    private void openAdditionalSettings() {
        player.sendMessage("§6Дополнительные настройки:");
        player.sendMessage("§7delay:[число] - задержка в тиках");
        player.sendMessage("§7repeat:[число] - количество повторений");
        player.sendMessage("§7condition:[условие] - условие выполнения");
    }
    
    private void saveSettings() {
        player.closeInventory();
        player.sendMessage("§aНастройки действия сохранены!");
        
        // Логируем параметры блока
        plugin.getLogger().info("Player action block configured with parameters: " + block.getParameters());
    }
    
    private void showPreview() {
        player.sendMessage("§6=== Предварительный просмотр ===");
        player.sendMessage("§7Блок: §f" + block.getType().getDisplayName());
        
        for (Map.Entry<String, Object> param : block.getParameters().entrySet()) {
            player.sendMessage("§7" + param.getKey() + ": §f" + param.getValue());
        }
        
        player.sendMessage("§7Результат: §aДействие будет выполнено согласно настройкам");
    }
}
