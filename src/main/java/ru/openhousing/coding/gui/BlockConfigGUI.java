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
import ru.openhousing.coding.blocks.BlockType;
import ru.openhousing.coding.blocks.CodeBlock;
import ru.openhousing.coding.gui.helpers.AnvilGUIHelper;
import ru.openhousing.utils.ItemBuilder;

import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;
import java.util.function.Consumer;

/**
 * GUI для настройки параметров блока кода
 */
public class BlockConfigGUI implements Listener {
    
    private final OpenHousing plugin;
    private final Player player;
    private final CodeBlock block;
    private final CodeEditorGUI editorGUI;
    private final Consumer<CodeBlock> onSave;
    private Inventory inventory;
    
    public BlockConfigGUI(OpenHousing plugin, Player player, CodeBlock block, CodeEditorGUI editorGUI) {
        this.plugin = plugin;
        this.player = player;
        this.block = block;
        this.editorGUI = editorGUI;
        this.onSave = null;
        this.inventory = Bukkit.createInventory(null, 54, "§6Настройка блока: " + block.getType().getDisplayName());
        
        // Регистрируем листенер
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }
    
    public BlockConfigGUI(OpenHousing plugin, Player player, CodeBlock block, Consumer<CodeBlock> onSave) {
        this.plugin = plugin;
        this.player = player;
        this.block = block;
        this.editorGUI = null;
        this.onSave = onSave;
        this.inventory = Bukkit.createInventory(null, 54, "§6Настройка блока: " + block.getType().getDisplayName());
        
        // Регистрируем листенер
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }
    
    /**
     * Открытие GUI
     */
    public void open() {
        setupInventory();
        player.openInventory(inventory);
    }
    
    private void setupInventory() {
        inventory.clear();
        
        // Информация о блоке
        inventory.setItem(4, new ItemBuilder(block.getType().getMaterial())
            .name("§6" + block.getType().getDisplayName())
            .lore(Arrays.asList(
                "§7Категория: §f" + block.getType().getCategory().getDisplayName(),
                "§7Описание: §f" + block.getType().getDescription(),
                "",
                "§7Настройте параметры блока ниже"
            ))
            .build());
        
        // Настройки в зависимости от типа блока
        switch (block.getType()) {
            case PLAYER_EVENT -> setupPlayerEventSettings(10);
            case ENTITY_EVENT -> setupEntityEventSettings(10);
            case WORLD_EVENT -> setupWorldEventSettings(10);
            case PLAYER_ACTION -> setupPlayerActionSettings(10);
            case ENTITY_ACTION -> setupEntityActionSettings(10);
            case WORLD_ACTION -> setupWorldActionSettings(10);
            case IF_PLAYER -> setupIfPlayerSettings(10);
            case IF_ENTITY -> setupIfEntitySettings(10);
            case IF_VARIABLE -> setupIfVariableSettings(10);
            case FUNCTION -> setupFunctionSettings(10);
            case CALL_FUNCTION -> setupCallFunctionSettings(10);
            case VARIABLE_ACTION -> setupVariableActionSettings(10);
            case MATH -> setupMathSettings(10);
            case TEXT_OPERATION -> setupTextOperationSettings(10);
            default -> setupGenericSettings(10);
        }
        
        // Кнопки управления
        inventory.setItem(48, new ItemBuilder(Material.EMERALD_BLOCK)
            .name("§aСохранить")
            .lore("§7Сохранить изменения")
            .build());
            
        inventory.setItem(49, new ItemBuilder(Material.REDSTONE_BLOCK)
            .name("§cОтмена")
            .lore("§7Отменить изменения")
            .build());
    }
    
    /**
     * Настройки события игрока
     */
    private void setupPlayerEventSettings(int startSlot) {
        // Тип события
        ItemStack eventType = new ItemBuilder(Material.DIAMOND)
            .name("§eТип события")
            .lore(Arrays.asList(
                "§7Текущий: §f" + block.getParameter("eventType"),
                "",
                "§7Клик для изменения"
            ))
            .build();
        inventory.setItem(startSlot, eventType);
        
        // Дополнительные условия
        ItemStack conditions = new ItemBuilder(Material.PAPER)
            .name("§eДополнительные условия")
            .lore(Arrays.asList(
                "§7Текущие: §f" + block.getParameter("conditions"),
                "",
                "§7Клик для настройки"
            ))
            .build();
        inventory.setItem(startSlot + 1, conditions);
    }
    
    /**
     * Настройки остальных типов блоков (заглушки)
     */
    private void setupEntityEventSettings(int startSlot) {
        inventory.setItem(startSlot, new ItemBuilder(Material.EGG)
            .name("§eТип события сущности")
            .lore("§7Клик для настройки")
            .build());
    }
    
    private void setupWorldEventSettings(int startSlot) {
        inventory.setItem(startSlot, new ItemBuilder(Material.GRASS_BLOCK)
            .name("§eТип события мира")
            .lore("§7Клик для настройки")
            .build());
    }
    
    private void setupPlayerActionSettings(int startSlot) {
        // Тип действия
        inventory.setItem(startSlot, new ItemBuilder(Material.GOLDEN_SWORD)
            .name("§eТип действия игрока")
            .lore(Arrays.asList(
                "§7Текущий: §f" + getActionTypeDisplay(),
                "",
                "§7Клик для изменения"
            ))
            .build());
            
        // Основное значение (зависит от типа действия)
        inventory.setItem(startSlot + 1, new ItemBuilder(Material.NAME_TAG)
            .name("§eОсновное значение")
            .lore(Arrays.asList(
                "§7Текущее: §f" + block.getParameter("value"),
                "",
                "§7Клик для изменения"
            ))
            .build());
            
        // Дополнительные параметры
        inventory.setItem(startSlot + 2, new ItemBuilder(Material.PAPER)
            .name("§eДополнительный параметр 1")
            .lore(Arrays.asList(
                "§7Текущий: §f" + block.getParameter("extra1"),
                "",
                "§7Клик для изменения"
            ))
            .build());
            
        inventory.setItem(startSlot + 3, new ItemBuilder(Material.PAPER)
            .name("§eДополнительный параметр 2")
            .lore(Arrays.asList(
                "§7Текущий: §f" + block.getParameter("extra2"),
                "",
                "§7Клик для изменения"
            ))
            .build());
    }
    
    private void setupEntityActionSettings(int startSlot) {
        // Тип действия
        inventory.setItem(startSlot, new ItemBuilder(Material.ZOMBIE_HEAD)
            .name("§eТип действия сущности")
            .lore(Arrays.asList(
                "§7Текущий: §f" + getEntityActionTypeDisplay(),
                "",
                "§7Клик для изменения"
            ))
            .build());
            
        // Основное значение
        inventory.setItem(startSlot + 1, new ItemBuilder(Material.BOOK)
            .name("§eОсновное значение")
            .lore(Arrays.asList(
                "§7Текущее: §f" + block.getParameter("value"),
                "",
                "§7Клик для изменения"
            ))
            .build());
            
        // Дополнительный параметр 1
        inventory.setItem(startSlot + 2, new ItemBuilder(Material.BOOK)
            .name("§eДоп. параметр 1")
            .lore(Arrays.asList(
                "§7Текущий: §f" + block.getParameter("extra1"),
                "",
                "§7Клик для изменения"
            ))
            .build());
            
        // Дополнительный параметр 2
        inventory.setItem(startSlot + 3, new ItemBuilder(Material.BOOK)
            .name("§eДоп. параметр 2")
            .lore(Arrays.asList(
                "§7Текущий: §f" + block.getParameter("extra2"),
                "",
                "§7Клик для изменения"
            ))
            .build());
    }
    
    private void setupWorldActionSettings(int startSlot) {
        // Тип действия
        inventory.setItem(startSlot, new ItemBuilder(Material.TNT)
            .name("§eТип действия мира")
            .lore(Arrays.asList(
                "§7Текущий: §f" + getWorldActionTypeDisplay(),
                "",
                "§7Клик для изменения"
            ))
            .build());
            
        // Основное значение
        inventory.setItem(startSlot + 1, new ItemBuilder(Material.BOOK)
            .name("§eОсновное значение")
            .lore(Arrays.asList(
                "§7Текущее: §f" + block.getParameter("value"),
                "",
                "§7Клик для изменения"
            ))
            .build());
            
        // Дополнительный параметр 1
        inventory.setItem(startSlot + 2, new ItemBuilder(Material.BOOK)
            .name("§eДоп. параметр 1")
            .lore(Arrays.asList(
                "§7Текущий: §f" + block.getParameter("extra1"),
                "",
                "§7Клик для изменения"
            ))
            .build());
            
        // Дополнительный параметр 2
        inventory.setItem(startSlot + 3, new ItemBuilder(Material.BOOK)
            .name("§eДоп. параметр 2")
            .lore(Arrays.asList(
                "§7Текущий: §f" + block.getParameter("extra2"),
                "",
                "§7Клик для изменения"
            ))
            .build());
    }
    
    private void setupIfPlayerSettings(int startSlot) {
        // Тип условия
        inventory.setItem(startSlot, new ItemBuilder(Material.COMPARATOR)
            .name("§eТип условия")
            .lore(Arrays.asList(
                "§7Текущий: §f" + block.getParameter("conditionType"),
                "",
                "§7Клик для изменения"
            ))
            .build());
            
        // Значение для сравнения
        inventory.setItem(startSlot + 1, new ItemBuilder(Material.BOOK)
            .name("§eЗначение для сравнения")
            .lore(Arrays.asList(
                "§7Текущее: §f" + block.getParameter("value"),
                "",
                "§7Клик для изменения"
            ))
            .build());
            
        // Оператор сравнения
        inventory.setItem(startSlot + 2, new ItemBuilder(Material.REDSTONE_TORCH)
            .name("§eОператор сравнения")
            .lore(Arrays.asList(
                "§7Текущий: §f" + block.getParameter("operator"),
                "",
                "§7Клик для изменения"
            ))
            .build());
    }
    
    private void setupIfEntitySettings(int startSlot) {
        inventory.setItem(startSlot, new ItemBuilder(Material.COMPARATOR)
            .name("§eУсловие сущности")
            .lore("§7Клик для настройки")
            .build());
    }
    
    private void setupIfVariableSettings(int startSlot) {
        inventory.setItem(startSlot, new ItemBuilder(Material.BOOK)
            .name("§eИмя переменной")
            .lore("§7Клик для выбора")
            .build());
    }
    
    private void setupFunctionSettings(int startSlot) {
        inventory.setItem(startSlot, new ItemBuilder(Material.ENCHANTED_BOOK)
            .name("§eИмя функции")
            .lore("§7Клик для настройки")
            .build());
    }
    
    private void setupCallFunctionSettings(int startSlot) {
        inventory.setItem(startSlot, new ItemBuilder(Material.BOOK)
            .name("§eВызов функции")
            .lore("§7Клик для настройки")
            .build());
    }
    
    private void setupVariableActionSettings(int startSlot) {
        inventory.setItem(startSlot, new ItemBuilder(Material.NAME_TAG)
            .name("§eДействие с переменной")
            .lore("§7Клик для настройки")
            .build());
        
        inventory.setItem(startSlot + 1, new ItemBuilder(Material.BOOK)
            .name("§eИмя переменной")
            .lore("§7Клик для выбора")
            .build());
    }
    
    private void setupMathSettings(int startSlot) {
        // Операция
        inventory.setItem(startSlot, new ItemBuilder(Material.REDSTONE)
            .name("§eМатематическая операция")
            .lore(Arrays.asList(
                "§7Текущая: §f" + block.getParameter("operation"),
                "",
                "§7Клик для изменения"
            ))
            .build());
            
        // Первое значение
        inventory.setItem(startSlot + 1, new ItemBuilder(Material.BOOK)
            .name("§eПервое значение")
            .lore(Arrays.asList(
                "§7Текущее: §f" + block.getParameter("value1"),
                "",
                "§7Клик для изменения"
            ))
            .build());
            
        // Второе значение
        inventory.setItem(startSlot + 2, new ItemBuilder(Material.BOOK)
            .name("§eВторое значение")
            .lore(Arrays.asList(
                "§7Текущее: §f" + block.getParameter("value2"),
                "",
                "§7Клик для изменения"
            ))
            .build());
            
        // Переменная результата
        inventory.setItem(startSlot + 3, new ItemBuilder(Material.NAME_TAG)
            .name("§eПеременная результата")
            .lore(Arrays.asList(
                "§7Текущая: §f" + block.getParameter("resultVariable"),
                "",
                "§7Клик для выбора"
            ))
            .build());
    }
    
    private void setupTextOperationSettings(int startSlot) {
        inventory.setItem(startSlot, new ItemBuilder(Material.PAPER)
            .name("§eТекстовая операция")
            .lore("§7Клик для настройки")
            .build());
    }
    
    private void setupGenericSettings(int startSlot) {
        inventory.setItem(startSlot, new ItemBuilder(Material.STONE)
            .name("§eОбщие настройки")
            .lore("§7Этот блок пока не имеет настроек")
            .build());
    }
    
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getView().getTitle().startsWith("§6Настройка блока:")) return;
        if (!(event.getWhoClicked() instanceof Player)) return;
        
        Player clicker = (Player) event.getWhoClicked();
        if (!clicker.equals(player)) {
            return;
        }
        
        event.setCancelled(true);
        
        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null || clickedItem.getType() == Material.AIR) {
            return;
        }
        
        int slot = event.getSlot();
        boolean isShiftClick = event.isShiftClick();
        
        handleClick(slot, isShiftClick);
    }
    
    /**
     * Обработка клика по GUI
     */
    private void handleClick(int slot, boolean isShiftClick) {
        // Кнопки управления
        if (slot == 48) {
            // Сохранить и закрыть
            if (onSave != null) {
                onSave.accept(block);
            }
            player.closeInventory();
            return;
        }
        
        if (slot == 49) {
            // Отмена
            player.closeInventory();
            return;
        }
        
        // Обработка клика по настройкам в зависимости от типа блока
        switch (block.getType()) {
            case PLAYER_EVENT -> handlePlayerEventClick(slot, isShiftClick);
            case ENTITY_EVENT -> handleEntityEventClick(slot, isShiftClick);
            case WORLD_EVENT -> handleWorldEventClick(slot, isShiftClick);
            case PLAYER_ACTION -> handlePlayerActionClick(slot, isShiftClick);
            case ENTITY_ACTION -> handleEntityActionClick(slot, isShiftClick);
            case WORLD_ACTION -> handleWorldActionClick(slot, isShiftClick);
            case IF_PLAYER -> handleIfPlayerClick(slot, isShiftClick);
            case IF_ENTITY -> handleIfEntityClick(slot, isShiftClick);
            case IF_VARIABLE -> handleIfVariableClick(slot, isShiftClick);
            case FUNCTION -> handleFunctionClick(slot, isShiftClick);
            case CALL_FUNCTION -> handleCallFunctionClick(slot, isShiftClick);
            case VARIABLE_ACTION -> handleVariableActionClick(slot, isShiftClick);
            case MATH -> handleMathClick(slot, isShiftClick);
            case TEXT_OPERATION -> handleTextOperationClick(slot, isShiftClick);
            default -> player.sendMessage("§cОбработчик для этого типа блока еще не реализован!");
        }
    }
    
    /**
     * Обработка клика по событию игрока
     */
    private void handlePlayerEventClick(int slot, boolean isShiftClick) {
        if (slot == 10) { // Тип события
            // Открываем GUI выбора типа события
            player.closeInventory();
            new PlayerEventSelectorGUI(plugin, player, (eventType) -> {
                block.setParameter("eventType", eventType.name());
                player.sendMessage("§aТип события выбран: " + eventType.getDisplayName());
                this.open(); // Возвращаемся к настройкам блока
            }).open();
            
        } else if (slot == 11) { // Дополнительные условия
            openConditionSettingsGUI();
        }
    }
    
    /**
     * Обработчики для событий сущностей и мира
     */
    private void handleEntityEventClick(int slot, boolean isShiftClick) {
        if (slot == 10) { // Тип события
            // Открываем GUI выбора типа события сущности
            player.closeInventory();
            new EntityEventSelectorGUI(plugin, player, (eventType) -> {
                block.setParameter("eventType", eventType.name());
                player.sendMessage("§aТип события выбран: " + eventType.getDisplayName());
                this.open(); // Возвращаемся к настройкам блока
            }).open();
        } else {
            player.sendMessage("§eДополнительные настройки события сущности (в разработке)");
        }
    }
    
    private void handleWorldEventClick(int slot, boolean isShiftClick) {
        if (slot == 10) { // Тип события
            // Открываем GUI выбора типа события мира
            player.closeInventory();
            new WorldEventSelectorGUI(plugin, player, (eventType) -> {
                block.setParameter("eventType", eventType.name());
                player.sendMessage("§aТип события выбран: " + eventType.getDisplayName());
                this.open(); // Возвращаемся к настройкам блока
            }).open();
        } else {
            player.sendMessage("§eДополнительные настройки события мира (в разработке)");
        }
    }
    
    private void handlePlayerActionClick(int slot, boolean isShiftClick) {
        if (slot == 10) { // Тип действия
            player.closeInventory();
            new PlayerActionSelectorGUI(plugin, player, (actionType) -> {
                block.setParameter("actionType", actionType.name());
                player.sendMessage("§aТип действия выбран: " + actionType.getDisplayName());
                this.open();
            }).open();
            
        } else if (slot == 11) { // Основное значение
            handleValueInput(getValuePrompt(), "value");
            
        } else if (slot == 12) { // Дополнительный параметр 1
            handleValueInput(getExtra1Prompt(), "extra1");
            
        } else if (slot == 13) { // Дополнительный параметр 2
            handleValueInput(getExtra2Prompt(), "extra2");
        }
    }
    
    private void handleEntityActionClick(int slot, boolean isShiftClick) {
        if (slot == 10) { // Тип действия
            player.closeInventory();
            new EntityActionSelectorGUI(plugin, player, (actionType) -> {
                block.setParameter("actionType", actionType.name());
                player.sendMessage("§aТип действия выбран: " + actionType.getDisplayName());
                this.open();
            }).open();
            
        } else if (slot == 11) { // Основное значение
            handleValueInput(getEntityValuePrompt(), "value");
            
        } else if (slot == 12) { // Дополнительный параметр 1
            handleValueInput(getEntityExtra1Prompt(), "extra1");
            
        } else if (slot == 13) { // Дополнительный параметр 2
            handleValueInput(getEntityExtra2Prompt(), "extra2");
        }
    }
    
    private void handleWorldActionClick(int slot, boolean isShiftClick) {
        if (slot == 10) { // Тип действия
            player.closeInventory();
            new WorldActionSelectorGUI(plugin, player, (actionType) -> {
                block.setParameter("actionType", actionType.name());
                player.sendMessage("§aТип действия выбран: " + actionType.getDisplayName());
                this.open();
            }).open();
            
        } else if (slot == 11) { // Основное значение
            handleValueInput(getWorldValuePrompt(), "value");
            
        } else if (slot == 12) { // Дополнительный параметр 1
            handleValueInput(getWorldExtra1Prompt(), "extra1");
            
        } else if (slot == 13) { // Дополнительный параметр 2
            handleValueInput(getWorldExtra2Prompt(), "extra2");
        }
    }
    
    private void handleIfPlayerClick(int slot, boolean isShiftClick) {
        if (slot == 10) { // Тип условия
            new StringSelectorGUI(plugin, player, "Выберите тип условия", Arrays.asList(
                "HEALTH", "FOOD", "LEVEL", "EXPERIENCE", "GAMEMODE", "PERMISSION", 
                "LOCATION", "ITEM_IN_HAND", "INVENTORY_CONTAINS", "WORLD"
            ), (conditionType) -> {
                block.setParameter("conditionType", conditionType);
                player.sendMessage("§aТип условия выбран: " + conditionType);
                this.open();
            }).open();
            
        } else if (slot == 11) { // Значение для сравнения
            String conditionType = String.valueOf(block.getParameter("conditionType"));
            String prompt = getIfPlayerValuePrompt(conditionType);
            handleValueInput(prompt, "value");
            
        } else if (slot == 12) { // Оператор сравнения
            new StringSelectorGUI(plugin, player, "Выберите оператор", Arrays.asList(
                "EQUALS", "NOT_EQUALS", "GREATER", "LESS", "GREATER_OR_EQUAL", 
                "LESS_OR_EQUAL", "CONTAINS", "NOT_CONTAINS"
            ), (operator) -> {
                block.setParameter("operator", operator);
                player.sendMessage("§aОператор выбран: " + operator);
                this.open();
            }).open();
        }
    }
    
    /**
     * Открыть GUI настройки условий
     */
    private void openConditionSettingsGUI() {
        Inventory conditionInventory = Bukkit.createInventory(null, 27, "§6Настройка условий");
        
        // Добавить условие
        conditionInventory.setItem(10, new ItemBuilder(Material.LIME_DYE)
            .name("§a+ Добавить условие")
            .lore(Arrays.asList(
                "§7Добавить новое условие",
                "§7для выполнения блока",
                "",
                "§eКлик для добавления"
            ))
            .build());
        
        // Список существующих условий
        List<String> conditions = getBlockConditions();
        int slot = 12;
        for (String condition : conditions) {
            if (slot >= 17) break;
            
            conditionInventory.setItem(slot, new ItemBuilder(Material.PAPER)
                .name("§e" + condition)
                .lore(Arrays.asList(
                    "§7Условие: " + condition,
                    "",
                    "§eЛКМ - редактировать",
                    "§cПКМ - удалить"
                ))
                .build());
            slot++;
        }
        
        // Кнопка назад
        conditionInventory.setItem(22, new ItemBuilder(Material.ARROW)
            .name("§7← Назад")
            .build());
        
        player.openInventory(conditionInventory);
    }
    
    /**
     * Получить список условий блока
     */
    private java.util.List<String> getBlockConditions() {
        Object conditionsObj = block.getParameter("conditions");
        if (conditionsObj instanceof java.util.List) {
            return (java.util.List<String>) conditionsObj;
        }
        return new ArrayList<>();
    }
    
    private String getIfPlayerValuePrompt(String conditionType) {
        if (conditionType == null) return "Введите значение:";
        
        switch (conditionType) {
            case "HEALTH": return "Введите значение здоровья (1-20):";
            case "FOOD": return "Введите уровень голода (0-20):";
            case "LEVEL": return "Введите уровень опыта:";
            case "EXPERIENCE": return "Введите количество опыта:";
            case "GAMEMODE": return "Введите игровой режим (SURVIVAL/CREATIVE/ADVENTURE/SPECTATOR):";
            case "PERMISSION": return "Введите разрешение:";
            case "LOCATION": return "Введите координаты (x,y,z или world,x,y,z):";
            case "ITEM_IN_HAND": return "Введите предмет (MATERIAL):";
            case "INVENTORY_CONTAINS": return "Введите предмет для поиска (MATERIAL:количество):";
            case "WORLD": return "Введите название мира:";
            default: return "Введите значение:";
        }
    }
    
    private void handleIfEntityClick(int slot, boolean isShiftClick) {
        player.sendMessage("§eНастройка условия сущности (в разработке)");
    }
    
    private void handleIfVariableClick(int slot, boolean isShiftClick) {
        if (slot == 10) { // Имя переменной
            new VariableSelectorGUI(plugin, player, (variable) -> {
                block.setParameter("variableName", variable.getVariableName());
                player.sendMessage("§aПеременная выбрана: " + variable.getVariableName());
                this.open(); // Возвращаемся к настройкам
            }, false).open();
        }
    }
    
    private void handleFunctionClick(int slot, boolean isShiftClick) {
        player.sendMessage("§eНастройка функции (в разработке)");
    }
    
    private void handleCallFunctionClick(int slot, boolean isShiftClick) {
        player.sendMessage("§eНастройка вызова функции (в разработке)");
    }
    
    private void handleVariableActionClick(int slot, boolean isShiftClick) {
        if (slot == 10) { // Действие с переменной
            player.sendMessage("§eВыбор действия с переменной (в разработке)");
        } else if (slot == 11) { // Имя переменной
            new VariableSelectorGUI(plugin, player, (variable) -> {
                block.setParameter("variableName", variable.getVariableName());
                player.sendMessage("§aПеременная выбрана: " + variable.getVariableName());
                this.open(); // Возвращаемся к настройкам
            }, false).open();
        }
    }
    
    private void handleMathClick(int slot, boolean isShiftClick) {
        if (slot == 10) { // Операция
            new StringSelectorGUI(plugin, player, "Выберите операцию", Arrays.asList(
                "ADD", "SUBTRACT", "MULTIPLY", "DIVIDE", "MODULO", "POWER", "SQRT", "ABS"
            ), (operation) -> {
                block.setParameter("operation", operation);
                player.sendMessage("§aОперация выбрана: " + operation);
                this.open();
            }).open();
            
        } else if (slot == 11) { // Первое значение
            handleValueInput("Введите первое значение:", "value1");
            
        } else if (slot == 12) { // Второе значение
            handleValueInput("Введите второе значение:", "value2");
            
        } else if (slot == 13) { // Переменная результата
            new VariableSelectorGUI(plugin, player, (variable) -> {
                block.setParameter("resultVariable", variable.getVariableName());
                player.sendMessage("§aПеременная результата: " + variable.getVariableName());
                this.open();
            }, true).open();
        }
    }
    
    private void handleTextOperationClick(int slot, boolean isShiftClick) {
        player.sendMessage("§eНастройка текстового блока (в разработке)");
    }
    
    /**
     * Получение отображаемого названия типа действия
     */
    private String getActionTypeDisplay() {
        Object actionType = block.getParameter("actionType");
        if (actionType instanceof ru.openhousing.coding.blocks.actions.PlayerActionBlock.PlayerActionType) {
            return ((ru.openhousing.coding.blocks.actions.PlayerActionBlock.PlayerActionType) actionType).getDisplayName();
        }
        return actionType != null ? actionType.toString() : "Не выбран";
    }
    
    private String getEntityActionTypeDisplay() {
        Object actionType = block.getParameter("actionType");
        if (actionType instanceof ru.openhousing.coding.blocks.actions.EntityActionBlock.EntityActionType) {
            return ((ru.openhousing.coding.blocks.actions.EntityActionBlock.EntityActionType) actionType).getDisplayName();
        }
        return actionType != null ? actionType.toString() : "Не выбран";
    }
    
    private String getWorldActionTypeDisplay() {
        Object actionType = block.getParameter("actionType");
        if (actionType instanceof ru.openhousing.coding.blocks.actions.WorldActionBlock.WorldActionType) {
            return ((ru.openhousing.coding.blocks.actions.WorldActionBlock.WorldActionType) actionType).getDisplayName();
        }
        return actionType != null ? actionType.toString() : "Не выбран";
    }
    
    /**
     * Получение подсказки для основного значения
     */
    private String getValuePrompt() {
        Object actionType = block.getParameter("actionType");
        if (actionType instanceof ru.openhousing.coding.blocks.actions.PlayerActionBlock.PlayerActionType) {
            ru.openhousing.coding.blocks.actions.PlayerActionBlock.PlayerActionType type = 
                (ru.openhousing.coding.blocks.actions.PlayerActionBlock.PlayerActionType) actionType;
            switch (type) {
                case SEND_MESSAGE: return "Введите сообщение:";
                case TELEPORT: return "Введите координаты (x,y,z или world,x,y,z):";
                case GIVE_ITEM: return "Введите предмет (MATERIAL:количество):";
                case TAKE_ITEM: return "Введите предмет для забора (MATERIAL:количество):";
                case SET_HEALTH: return "Введите здоровье (1-20):";
                case SET_FOOD: return "Введите уровень голода (0-20):";
                case SET_LEVEL: return "Введите уровень опыта:";
                case ADD_EXPERIENCE: return "Введите количество опыта:";
                case PLAY_SOUND: return "Введите звук (SOUND_NAME):";
                case SHOW_TITLE: return "Введите заголовок:";
                case SEND_ACTIONBAR: return "Введите текст для панели действий:";
                case KICK_PLAYER: return "Введите причину кика:";
                case ADD_POTION_EFFECT: return "Введите эффект (EFFECT_TYPE):";
                case REMOVE_POTION_EFFECT: return "Введите тип эффекта для удаления:";
                case SET_FLY: return "Введите true/false для полета:";
                case SPAWN_PARTICLE: return "Введите тип частиц:";
                case SET_FIRE: return "Введите время горения (секунды):";
                case GIVE_MONEY: return "Введите сумму денег:";
                case TAKE_MONEY: return "Введите сумму для списания:";
                case SEND_TO_SERVER: return "Введите название сервера:";
                default: return "Введите значение:";
            }
        }
        return "Введите значение:";
    }
    
    /**
     * Получение подсказки для дополнительного параметра 1
     */
    private String getExtra1Prompt() {
        Object actionType = block.getParameter("actionType");
        if (actionType instanceof ru.openhousing.coding.blocks.actions.PlayerActionBlock.PlayerActionType) {
            ru.openhousing.coding.blocks.actions.PlayerActionBlock.PlayerActionType type = 
                (ru.openhousing.coding.blocks.actions.PlayerActionBlock.PlayerActionType) actionType;
            switch (type) {
                case SHOW_TITLE: return "Введите подзаголовок:";
                case ADD_POTION_EFFECT: return "Введите длительность (секунды):";
                case SPAWN_PARTICLE: return "Введите количество частиц:";
                case PLAY_SOUND: return "Введите громкость (0.0-2.0):";
                default: return "Дополнительный параметр:";
            }
        }
        return "Дополнительный параметр:";
    }
    
    /**
     * Получение подсказки для дополнительного параметра 2
     */
    private String getExtra2Prompt() {
        Object actionType = block.getParameter("actionType");
        if (actionType instanceof ru.openhousing.coding.blocks.actions.PlayerActionBlock.PlayerActionType) {
            ru.openhousing.coding.blocks.actions.PlayerActionBlock.PlayerActionType type = 
                (ru.openhousing.coding.blocks.actions.PlayerActionBlock.PlayerActionType) actionType;
            switch (type) {
                case ADD_POTION_EFFECT: return "Введите силу эффекта (0-255):";
                case PLAY_SOUND: return "Введите высоту тона (0.5-2.0):";
                case SHOW_TITLE: return "Введите время показа (тики):";
                default: return "Дополнительный параметр 2:";
            }
        }
        return "Дополнительный параметр 2:";
    }
    
    /**
     * Получение подсказки для основного значения EntityAction
     */
    private String getEntityValuePrompt() {
        Object actionType = block.getParameter("actionType");
        if (actionType instanceof ru.openhousing.coding.blocks.actions.EntityActionBlock.EntityActionType) {
            ru.openhousing.coding.blocks.actions.EntityActionBlock.EntityActionType type = 
                (ru.openhousing.coding.blocks.actions.EntityActionBlock.EntityActionType) actionType;
            switch (type) {
                case SPAWN: return "Введите тип существа (ZOMBIE, COW, VILLAGER и т.д.):";
                case DAMAGE: return "Введите урон:";
                case HEAL: return "Введите количество здоровья для восстановления:";
                case SET_HEALTH: return "Введите здоровье:";
                case SET_MAX_HEALTH: return "Введите максимальное здоровье:";
                case TELEPORT: return "Введите координаты (x,y,z или world,x,y,z):";
                case SET_VELOCITY: return "Введите скорость (x,y,z):";
                case PUSH: return "Введите силу толчка:";
                case LAUNCH: return "Введите силу запуска:";
                case SET_FIRE: return "Введите время горения (тики):";
                case SET_NAME: return "Введите имя существа:";
                case SET_NAME_VISIBLE: return "Введите true/false для видимости имени:";
                case SET_GLOWING: return "Введите true/false для свечения:";
                case SET_INVISIBLE: return "Введите true/false для невидимости:";
                case SET_SILENT: return "Введите true/false для безмолвия:";
                case SET_GRAVITY: return "Введите true/false для гравитации:";
                case SET_INVULNERABLE: return "Введите true/false для неуязвимости:";
                case ADD_POTION_EFFECT: return "Введите тип эффекта (SPEED, STRENGTH и т.д.):";
                case REMOVE_POTION_EFFECT: return "Введите тип эффекта для удаления:";
                case SET_TARGET: return "Введите имя игрока или UUID цели:";
                case SET_ANGRY: return "Введите true/false для агрессии:";
                case TAME: return "Введите true/false для приручения:";
                case SET_AGE: return "Введите возраст:";
                case EXPLODE: return "Введите силу взрыва:";
                case PLAY_SOUND: return "Введите звук (ENTITY_SOUND_NAME):";
                case SPAWN_PARTICLES: return "Введите тип частиц:";
                case DROP_ITEM: return "Введите предмет (MATERIAL:количество):";
                case GIVE_ITEM: return "Введите предмет (MATERIAL:количество):";
                case SET_EQUIPMENT: return "Введите слот и предмет (HELMET:DIAMOND_HELMET):";
                case FOLLOW_PLAYER: return "Введите имя игрока:";
                case FACE_LOCATION: return "Введите координаты (x,y,z):";
                case SET_AI: return "Введите true/false для ИИ:";
                case SET_COLLIDABLE: return "Введите true/false для столкновений:";
                default: return "Введите значение:";
            }
        }
        return "Введите значение:";
    }
    
    private String getEntityExtra1Prompt() {
        Object actionType = block.getParameter("actionType");
        if (actionType instanceof ru.openhousing.coding.blocks.actions.EntityActionBlock.EntityActionType) {
            ru.openhousing.coding.blocks.actions.EntityActionBlock.EntityActionType type = 
                (ru.openhousing.coding.blocks.actions.EntityActionBlock.EntityActionType) actionType;
            switch (type) {
                case SPAWN: return "Введите координаты спавна (x,y,z):";
                case ADD_POTION_EFFECT: return "Введите длительность (секунды):";
                case PLAY_SOUND: return "Введите громкость (0.0-2.0):";
                case SPAWN_PARTICLES: return "Введите количество частиц:";
                case EXPLODE: return "Введите радиус взрыва:";
                default: return "Дополнительный параметр:";
            }
        }
        return "Дополнительный параметр:";
    }
    
    private String getEntityExtra2Prompt() {
        Object actionType = block.getParameter("actionType");
        if (actionType instanceof ru.openhousing.coding.blocks.actions.EntityActionBlock.EntityActionType) {
            ru.openhousing.coding.blocks.actions.EntityActionBlock.EntityActionType type = 
                (ru.openhousing.coding.blocks.actions.EntityActionBlock.EntityActionType) actionType;
            switch (type) {
                case SPAWN: return "Введите дополнительные параметры:";
                case ADD_POTION_EFFECT: return "Введите силу эффекта (0-255):";
                case PLAY_SOUND: return "Введите высоту тона (0.5-2.0):";
                case SPAWN_PARTICLES: return "Введите скорость частиц:";
                case EXPLODE: return "Введите true/false для поджигания:";
                default: return "Дополнительный параметр 2:";
            }
        }
        return "Дополнительный параметр 2:";
    }
    
    /**
     * Получение подсказки для основного значения WorldAction
     */
    private String getWorldValuePrompt() {
        Object actionType = block.getParameter("actionType");
        if (actionType instanceof ru.openhousing.coding.blocks.actions.WorldActionBlock.WorldActionType) {
            ru.openhousing.coding.blocks.actions.WorldActionBlock.WorldActionType type = 
                (ru.openhousing.coding.blocks.actions.WorldActionBlock.WorldActionType) actionType;
            switch (type) {
                case SET_BLOCK: return "Введите тип блока и координаты (MATERIAL:x,y,z):";
                case BREAK_BLOCK: return "Введите координаты блока (x,y,z):";
                case FILL_AREA: return "Введите область и материал (x1,y1,z1:x2,y2,z2:MATERIAL):";
                case REPLACE_BLOCKS: return "Введите область (x1,y1,z1:x2,y2,z2):";
                case SET_TIME: return "Введите время (0-24000):";
                case ADD_TIME: return "Введите добавляемое время:";
                case SET_WEATHER: return "Введите погоду (CLEAR/RAIN/STORM):";
                case SET_STORM: return "Введите true/false для грозы:";
                case STRIKE_LIGHTNING: return "Введите координаты (x,y,z):";
                case PLAY_SOUND: return "Введите звук (SOUND_NAME):";
                case PLAY_SOUND_ALL: return "Введите звук для всех:";
                case STOP_SOUND: return "Введите звук для остановки:";
                case SPAWN_PARTICLE: return "Введите тип частиц:";
                case CREATE_EXPLOSION: return "Введите координаты взрыва (x,y,z):";
                case SEND_MESSAGE: return "Введите сообщение:";
                case SEND_MESSAGE_ALL: return "Введите сообщение для всех:";
                case SEND_TITLE: return "Введите заголовок:";
                case SEND_ACTIONBAR: return "Введите текст actionbar:";
                case TELEPORT_PLAYER: return "Введите имя игрока:";
                case HEAL_PLAYER: return "Введите имя игрока:";
                case FEED_PLAYER: return "Введите имя игрока:";
                case GIVE_ITEM: return "Введите игрока и предмет (игрок:MATERIAL:количество):";
                case TAKE_ITEM: return "Введите игрока и предмет (игрок:MATERIAL:количество):";
                case CLEAR_INVENTORY: return "Введите имя игрока:";
                case SET_GAMEMODE: return "Введите игрока и режим (игрок:GAMEMODE):";
                case SET_FLY: return "Введите игрока и true/false (игрок:true):";
                case KICK_PLAYER: return "Введите имя игрока:";
                case BAN_PLAYER: return "Введите имя игрока:";
                case RUN_COMMAND: return "Введите команду:";
                case RUN_COMMAND_CONSOLE: return "Введите команду консоли:";
                case SET_SPAWN: return "Введите координаты спавна (x,y,z):";
                case LOAD_CHUNK: return "Введите координаты чанка (x,z):";
                case UNLOAD_CHUNK: return "Введите координаты чанка (x,z):";
                case SET_DIFFICULTY: return "Введите сложность (PEACEFUL/EASY/NORMAL/HARD):";
                case SET_GAME_RULE: return "Введите правило и значение (rule:value):";
                case CREATE_FIREWORK: return "Введите координаты (x,y,z):";
                case SET_WALKSPEED: return "Введите игрока и скорость (игрок:скорость):";
                case SET_FLYSPEED: return "Введите игрока и скорость (игрок:скорость):";
                case PUSH_PLAYER: return "Введите игрока и силу (игрок:сила):";
                case LAUNCH_PLAYER: return "Введите игрока и силу (игрок:сила):";
                case HIDE_PLAYER: return "Введите имена игроков (скрываемый:от_кого):";
                case SHOW_PLAYER: return "Введите имена игроков (показываемый:кому):";
                case SET_EXPERIENCE: return "Введите игрока и опыт (игрок:уровень):";
                case GIVE_EXPERIENCE: return "Введите игрока и опыт (игрок:количество):";
                case TAKE_EXPERIENCE: return "Введите игрока и опыт (игрок:количество):";
                case WAIT: return "Введите время ожидания (секунды):";
                case SEND_TO_SERVER: return "Введите игрока и сервер (игрок:сервер):";
                default: return "Введите значение:";
            }
        }
        return "Введите значение:";
    }
    
    private String getWorldExtra1Prompt() {
        Object actionType = block.getParameter("actionType");
        if (actionType instanceof ru.openhousing.coding.blocks.actions.WorldActionBlock.WorldActionType) {
            ru.openhousing.coding.blocks.actions.WorldActionBlock.WorldActionType type = 
                (ru.openhousing.coding.blocks.actions.WorldActionBlock.WorldActionType) actionType;
            switch (type) {
                case REPLACE_BLOCKS: return "Введите заменяемый материал:";
                case PLAY_SOUND: return "Введите координаты (x,y,z):";
                case SPAWN_PARTICLE: return "Введите координаты (x,y,z):";
                case SPAWN_PARTICLE_LINE: return "Введите конечные координаты (x,y,z):";
                case SPAWN_PARTICLE_CIRCLE: return "Введите радиус:";
                case SPAWN_PARTICLE_SPHERE: return "Введите радиус:";
                case CREATE_EXPLOSION: return "Введите силу взрыва:";
                case SEND_TITLE: return "Введите подзаголовок:";
                case CREATE_FIREWORK: return "Введите цвета (RED,BLUE,GREEN):";
                default: return "Дополнительный параметр:";
            }
        }
        return "Дополнительный параметр:";
    }
    
    private String getWorldExtra2Prompt() {
        Object actionType = block.getParameter("actionType");
        if (actionType instanceof ru.openhousing.coding.blocks.actions.WorldActionBlock.WorldActionType) {
            ru.openhousing.coding.blocks.actions.WorldActionBlock.WorldActionType type = 
                (ru.openhousing.coding.blocks.actions.WorldActionBlock.WorldActionType) actionType;
            switch (type) {
                case REPLACE_BLOCKS: return "Введите новый материал:";
                case SPAWN_PARTICLE: return "Введите количество частиц:";
                case SPAWN_PARTICLE_LINE: return "Введите количество частиц:";
                case SPAWN_PARTICLE_CIRCLE: return "Введите количество частиц:";
                case SPAWN_PARTICLE_SPHERE: return "Введите количество частиц:";
                case CREATE_EXPLOSION: return "Введите true/false для поджигания:";
                case SEND_TITLE: return "Введите время показа (тики):";
                case CREATE_FIREWORK: return "Введите эффекты (BURST/BALL/STAR):";
                default: return "Дополнительный параметр 2:";
            }
        }
        return "Дополнительный параметр 2:";
    }
    
    /**
     * Обработка ввода значения через AnvilGUI
     */
    private void handleValueInput(String prompt, String parameterName) {
        player.closeInventory();
        new AnvilGUIHelper(plugin, player, prompt, (input) -> {
            block.setParameter(parameterName, input);
            player.sendMessage("§aЗначение установлено: " + input);
            this.open();
        }).open();
    }
}
