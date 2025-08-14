package ru.openhousing.coding.gui;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import ru.openhousing.OpenHousing;
import ru.openhousing.coding.blocks.CodeBlock;
import ru.openhousing.coding.values.ValueType;
import ru.openhousing.utils.ItemBuilder;
import ru.openhousing.utils.MessageUtil;
import ru.openhousing.utils.AnvilGUIHelper;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.Arrays;
import java.util.List;

/**
 * GUI для настройки параметров блока кода
 */
public class BlockConfigGUI implements Listener {
    
    private final OpenHousing plugin;
    private final Player player;
    private final CodeBlock block;
    private final CodeEditorGUI editorGUI;
    private Inventory inventory;
    
    public BlockConfigGUI(OpenHousing plugin, Player player, CodeBlock block, CodeEditorGUI editorGUI) {
        this.plugin = plugin;
        this.player = player;
        this.block = block;
        this.editorGUI = editorGUI;
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
    
    /**
     * Настройка интерфейса
     */
    private void setupInventory() {
        // Очистка инвентаря
        inventory.clear();
        
        // Заполнение фона
        ItemStack filler = new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE)
            .name(" ")
            .build();
        
        for (int i = 0; i < inventory.getSize(); i++) {
            inventory.setItem(i, filler);
        }
        
        // Информация о блоке
        ItemStack blockInfo = new ItemBuilder(block.getType().getMaterial())
            .name("§6" + block.getType().getDisplayName())
            .lore(block.getDescription())
            .build();
        inventory.setItem(4, blockInfo);
        
        // Настройки параметров
        setupParameterSettings();
        
        // Кнопки управления
        setupControlButtons();
    }
    
    /**
     * Настройка параметров блока
     */
    private void setupParameterSettings() {
        int slot = 10;
        
        // Основные параметры для всех блоков
        switch (block.getType()) {
            case PLAYER_EVENT:
                setupPlayerEventSettings(slot);
                break;
            case ENTITY_EVENT:
                setupEntityEventSettings(slot);
                break;
            case WORLD_EVENT:
                setupWorldEventSettings(slot);
                break;
            case IF_PLAYER:
                setupIfPlayerSettings(slot);
                break;
            case IF_ENTITY:
                setupIfEntitySettings(slot);
                break;
            case IF_VARIABLE:
                setupIfVariableSettings(slot);
                break;
            case PLAYER_ACTION:
                setupPlayerActionSettings(slot);
                break;
            case ENTITY_ACTION:
                setupEntityActionSettings(slot);
                break;
            case WORLD_ACTION:
                setupWorldActionSettings(slot);
                break;
            case VARIABLE_ACTION:
                setupVariableActionSettings(slot);
                break;
            case REPEAT:
                setupRepeatSettings(slot);
                break;
            case FUNCTION:
                setupFunctionSettings(slot);
                break;
            case CALL_FUNCTION:
                setupCallFunctionSettings(slot);
                break;
        }
    }
    
    /**
     * Настройки события игрока
     */
    private void setupPlayerEventSettings(int startSlot) {
        // Тип события
        ItemStack eventType = new ItemBuilder(Material.DIAMOND)
            .name("§eTип события")
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
     * Настройки действия с переменными
     */
    private void setupVariableActionSettings(int startSlot) {
        // Тип действия
        ItemStack actionType = new ItemBuilder(Material.BOOK)
            .name("§eТип действия")
            .lore(Arrays.asList(
                "§7Текущий: §f" + block.getParameter("actionType"),
                "",
                "§7Клик для изменения"
            ))
            .build();
        inventory.setItem(startSlot, actionType);
        
        // Имя переменной
        ItemStack variableName = new ItemBuilder(Material.NAME_TAG)
            .name("§eИмя переменной")
            .lore(Arrays.asList(
                "§7Текущее: §f" + block.getParameter("variableName"),
                "",
                "§7Клик для изменения"
            ))
            .build();
        inventory.setItem(startSlot + 1, variableName);
        
        // Значение
        ItemStack value = new ItemBuilder(Material.WRITABLE_BOOK)
            .name("§eЗначение")
            .lore(Arrays.asList(
                "§7Текущее: §f" + block.getParameter("value"),
                "",
                "§7Клик для изменения"
            ))
            .build();
        inventory.setItem(startSlot + 2, value);
    }
    
    /**
     * Настройки повторения
     */
    private void setupRepeatSettings(int startSlot) {
        // Тип повторения
        ItemStack repeatType = new ItemBuilder(Material.CLOCK)
            .name("§eТип повторения")
            .lore(Arrays.asList(
                "§7Текущий: §f" + block.getParameter("repeatType"),
                "",
                "§7Клик для изменения"
            ))
            .build();
        inventory.setItem(startSlot, repeatType);
        
        // Значение
        ItemStack value = new ItemBuilder(Material.PAPER)
            .name("§eЗначение")
            .lore(Arrays.asList(
                "§7Текущее: §f" + block.getParameter("value"),
                "",
                "§7Клик для изменения"
            ))
            .build();
        inventory.setItem(startSlot + 1, value);
        
        // Максимальные итерации
        ItemStack maxIterations = new ItemBuilder(Material.BARRIER)
            .name("§eМаксимальные итерации")
            .lore(Arrays.asList(
                "§7Текущее: §f" + block.getParameter("maxIterations"),
                "",
                "§7Клик для изменения"
            ))
            .build();
        inventory.setItem(startSlot + 2, maxIterations);
    }
    
    /**
     * Заглушки для остальных типов блоков
     */
    private void setupEntityEventSettings(int startSlot) {
        // Тип события существа
        inventory.setItem(19, new ItemBuilder(Material.ZOMBIE_HEAD)
            .name("§6Тип события")
            .lore(Arrays.asList(
                "§7Текущий: §e" + block.getParameter("eventType"),
                "",
                "§7Доступные события:",
                "§7• SPAWN - спавн существа",
                "§7• DEATH - смерть существа", 
                "§7• DAMAGE - получение урона",
                "§7• EXPLODE - взрыв существа",
                "§7• FREEZE - замерзание",
                "§7• MOUNT/DISMOUNT - сесть/слезть",
                "",
                "§eКлик для изменения"
            ))
            .build());

        // Тип существа
        inventory.setItem(20, new ItemBuilder(Material.EGG)
            .name("§6Тип существа")
            .lore(Arrays.asList(
                "§7Текущий: §e" + block.getParameter("entityType"),
                "",
                "§7Выберите тип существа:",
                "§7• ANY - любое существо",
                "§7• ZOMBIE, SKELETON, CREEPER",
                "§7• PLAYER - только игроки",
                "",
                "§eКлик для выбора"
            ))
            .build());

        // Мир
        inventory.setItem(21, new ItemBuilder(Material.GRASS_BLOCK)
            .name("§6Мир события")
            .lore(Arrays.asList(
                "§7Текущий: §e" + block.getParameter("world"),
                "",
                "§7Укажите мир или оставьте пустым",
                "§7для любого мира",
                "",
                "§eКлик для изменения"
            ))
            .build());
    }
    
    private void setupWorldEventSettings(int startSlot) {
        // Тип события мира
        inventory.setItem(19, new ItemBuilder(Material.GRASS_BLOCK)
            .name("§6Тип события")
            .lore(Arrays.asList(
                "§7Текущий: §e" + block.getParameter("eventType"),
                "",
                "§7Доступные события:",
                "§7• BLOCK_BREAK - разрушение блока",
                "§7• BLOCK_PLACE - установка блока",
                "§7• WEATHER_CHANGE - смена погоды",
                "§7• EXPLOSION - взрыв",
                "§7• WATER_LEVEL_CHANGE - изменение воды",
                "§7• DROPPER_DROP - сброс дроппера",
                "",
                "§eКлик для изменения"
            ))
            .build());

        // Тип блока
        inventory.setItem(20, new ItemBuilder(Material.STONE)
            .name("§6Тип блока")
            .lore(Arrays.asList(
                "§7Текущий: §e" + block.getParameter("blockType"),
                "",
                "§7Укажите тип блока или оставьте",
                "§7пустым для любого блока",
                "§7Примеры: STONE, DIRT, DIAMOND_ORE",
                "",
                "§eКлик для выбора"
            ))
            .build());

        // Мир
        inventory.setItem(21, new ItemBuilder(Material.COMPASS)
            .name("§6Мир события")
            .lore(Arrays.asList(
                "§7Текущий: §e" + block.getParameter("world"),
                "",
                "§7Укажите мир или оставьте пустым",
                "§7для любого мира",
                "",
                "§eКлик для изменения"
            ))
            .build());
    }
    
    private void setupIfPlayerSettings(int startSlot) {
        // Тип условия
        inventory.setItem(19, new ItemBuilder(Material.COMPARATOR)
            .name("§6Тип условия")
            .lore(Arrays.asList(
                "§7Текущий: §e" + block.getParameter("conditionType"),
                "",
                "§7Доступные условия:",
                "§7• HEALTH - здоровье игрока",
                "§7• LEVEL - уровень игрока",
                "§7• GAMEMODE - режим игры",
                "§7• PERMISSION - права игрока",
                "§7• ITEM_IN_HAND - предмет в руке",
                "",
                "§eКлик для изменения"
            ))
            .build());

        // Игрок для проверки
        inventory.setItem(20, new ItemBuilder(Material.PLAYER_HEAD)
            .name("§6Игрок")
            .lore(Arrays.asList(
                "§7Текущий: §e" + block.getParameter("target"),
                "",
                "§7Выберите игрока для проверки:",
                "§7• @s - сам игрок",
                "§7• @p - ближайший игрок",
                "§7• имя игрока",
                "",
                "§eКлик для выбора"
            ))
            .build());

        // Значение для сравнения
        inventory.setItem(21, new ItemBuilder(Material.PAPER)
            .name("§6Значение сравнения")
            .lore(Arrays.asList(
                "§7Текущее: §e" + block.getParameter("compareValue"),
                "",
                "§7Значение для сравнения с условием",
                "§7Например: 20 (для здоровья)",
                "§7или CREATIVE (для режима игры)",
                "",
                "§eКлик для изменения"
            ))
            .build());
    }
    
    private void setupIfEntitySettings(int startSlot) {
        // Тип условия
        ItemStack conditionType = new ItemBuilder(Material.SPIDER_EYE)
            .name("§6Тип условия")
            .lore(Arrays.asList(
                "§7Текущий: §e" + block.getParameter("conditionType"),
                "",
                "§7Доступные условия:",
                "§7• HEALTH - здоровье",
                "§7• TYPE - тип существа",
                "§7• DISTANCE - расстояние",
                "§7• IS_ALIVE - живое ли",
                "§7• HAS_AI - есть ли ИИ",
                "",
                "§eКлик для изменения"
            ))
            .build();
        inventory.setItem(startSlot, conditionType);
        
        // Существо
        ItemStack entity = new ItemBuilder(Material.EGG)
            .name("§6Существо")
            .lore(Arrays.asList(
                "§7Текущее: §e" + block.getParameter("entity"),
                "",
                "§7Выберите существо для проверки",
                "",
                "§eКлик для выбора"
            ))
            .build();
        inventory.setItem(startSlot + 1, entity);
        
        // Значение для сравнения
        ItemStack compareValue = new ItemBuilder(Material.PAPER)
            .name("§6Значение для сравнения")
            .lore(Arrays.asList(
                "§7Текущее: §e" + block.getParameter("compareValue"),
                "",
                "§7Значение для сравнения с условием",
                "",
                "§eКлик для изменения"
            ))
            .build();
        inventory.setItem(startSlot + 2, compareValue);
    }
    
    private void setupIfVariableSettings(int startSlot) {
        // Имя переменной
        ItemStack variableName = new ItemBuilder(Material.BOOK)
            .name("§6Имя переменной")
            .lore(Arrays.asList(
                "§7Текущее: §e" + block.getParameter("variableName"),
                "",
                "§7Введите имя переменной",
                "",
                "§eКлик для изменения"
            ))
            .build();
        inventory.setItem(startSlot, variableName);
        
        // Оператор сравнения
        ItemStack operator = new ItemBuilder(Material.COMPARATOR)
            .name("§6Оператор сравнения")
            .lore(Arrays.asList(
                "§7Текущий: §e" + block.getParameter("operator"),
                "",
                "§7Доступные операторы:",
                "§7• EQUALS (==) - равно",
                "§7• NOT_EQUALS (!=) - не равно",
                "§7• GREATER (>) - больше",
                "§7• LESS (<) - меньше",
                "§7• CONTAINS - содержит",
                "",
                "§eКлик для выбора"
            ))
            .build();
        inventory.setItem(startSlot + 1, operator);
        
        // Значение для сравнения
        ItemStack compareValue = new ItemBuilder(Material.PAPER)
            .name("§6Значение для сравнения")
            .lore(Arrays.asList(
                "§7Текущее: §e" + block.getParameter("compareValue"),
                "",
                "§7Значение для сравнения с переменной",
                "",
                "§eКлик для изменения"
            ))
            .build();
        inventory.setItem(startSlot + 2, compareValue);
    }
    
    private void setupPlayerActionSettings(int startSlot) {
        // Тип действия
        ItemStack actionType = new ItemBuilder(Material.GOLDEN_SWORD)
            .name("§6Тип действия")
            .lore(Arrays.asList(
                "§7Текущий: §e" + block.getParameter("actionType"),
                "",
                "§7Доступные действия:",
                "§7• SEND_MESSAGE - отправить сообщение",
                "§7• TELEPORT - телепортировать",
                "§7• GIVE_ITEM - выдать предмет",
                "§7• SET_HEALTH - установить здоровье",
                "§7• PLAY_SOUND - воспроизвести звук",
                "",
                "§eКлик для изменения"
            ))
            .build();
        inventory.setItem(startSlot, actionType);
        
        // Цель
        ItemStack target = new ItemBuilder(Material.TARGET)
            .name("§6Цель действия")
            .lore(Arrays.asList(
                "§7Текущая: §e" + block.getParameter("target"),
                "",
                "§7Выберите цель для действия",
                "",
                "§eКлик для выбора"
            ))
            .build();
        inventory.setItem(startSlot + 1, target);
        
        // Значение/параметр
        ItemStack value = new ItemBuilder(Material.PAPER)
            .name("§6Значение")
            .lore(Arrays.asList(
                "§7Текущее: §e" + block.getParameter("value"),
                "",
                "§7Значение для действия",
                "",
                "§eКлик для изменения"
            ))
            .build();
        inventory.setItem(startSlot + 2, value);
    }
    
    private void setupEntityActionSettings(int startSlot) {
        // Тип действия
        ItemStack actionType = new ItemBuilder(Material.BONE)
            .name("§6Тип действия")
            .lore(Arrays.asList(
                "§7Текущий: §e" + block.getParameter("actionType"),
                "",
                "§7Доступные действия:",
                "§7• DAMAGE - нанести урон",
                "§7• HEAL - лечить",
                "§7• REMOVE - удалить",
                "§7• SPAWN - заспавнить",
                "§7• SET_AI - включить/отключить ИИ",
                "",
                "§eКлик для изменения"
            ))
            .build();
        inventory.setItem(startSlot, actionType);
        
        // Существо
        ItemStack entity = new ItemBuilder(Material.EGG)
            .name("§6Цель существо")
            .lore(Arrays.asList(
                "§7Текущее: §e" + block.getParameter("entity"),
                "",
                "§7Выберите существо для действия",
                "",
                "§eКлик для выбора"
            ))
            .build();
        inventory.setItem(startSlot + 1, entity);
        
        // Значение/параметр
        ItemStack value = new ItemBuilder(Material.PAPER)
            .name("§6Значение")
            .lore(Arrays.asList(
                "§7Текущее: §e" + block.getParameter("value"),
                "",
                "§7Значение для действия",
                "",
                "§eКлик для изменения"
            ))
            .build();
        inventory.setItem(startSlot + 2, value);
    }
    
    private void setupWorldActionSettings(int startSlot) {
        // Тип действия
        ItemStack actionType = new ItemBuilder(Material.GRASS_BLOCK)
            .name("§6Тип действия")
            .lore(Arrays.asList(
                "§7Текущий: §e" + block.getParameter("actionType"),
                "",
                "§7Доступные действия:",
                "§7• SET_BLOCK - установить блок",
                "§7• BREAK_BLOCK - сломать блок",
                "§7• EXPLODE - взрыв",
                "§7• SET_TIME - установить время",
                "§7• SET_WEATHER - установить погоду",
                "",
                "§eКлик для изменения"
            ))
            .build();
        inventory.setItem(startSlot, actionType);
        
        // Местоположение
        ItemStack location = new ItemBuilder(Material.COMPASS)
            .name("§6Местоположение")
            .lore(Arrays.asList(
                "§7Текущее: §e" + block.getParameter("location"),
                "",
                "§7Выберите местоположение",
                "",
                "§eКлик для выбора"
            ))
            .build();
        inventory.setItem(startSlot + 1, location);
        
        // Значение/параметр
        ItemStack value = new ItemBuilder(Material.PAPER)
            .name("§6Значение")
            .lore(Arrays.asList(
                "§7Текущее: §e" + block.getParameter("value"),
                "",
                "§7Значение для действия",
                "",
                "§eКлик для изменения"
            ))
            .build();
        inventory.setItem(startSlot + 2, value);
    }
    
    private void setupFunctionSettings(int startSlot) {
        // Имя функции
        ItemStack functionName = new ItemBuilder(Material.COMMAND_BLOCK)
            .name("§6Имя функции")
            .lore(Arrays.asList(
                "§7Текущее: §e" + block.getParameter("functionName"),
                "",
                "§7Уникальное имя для функции",
                "",
                "§eКлик для изменения"
            ))
            .build();
        inventory.setItem(startSlot, functionName);
        
        // Параметры функции
        ItemStack parameters = new ItemBuilder(Material.PAPER)
            .name("§6Параметры функции")
            .lore(Arrays.asList(
                "§7Текущие: §e" + block.getParameter("parameters"),
                "",
                "§7Параметры через запятую",
                "§7Пример: player, message, amount",
                "",
                "§eКлик для настройки"
            ))
            .build();
        inventory.setItem(startSlot + 1, parameters);
        
        // Описание
        ItemStack description = new ItemBuilder(Material.BOOK)
            .name("§6Описание")
            .lore(Arrays.asList(
                "§7Текущее: §e" + block.getParameter("description"),
                "",
                "§7Описание функции",
                "",
                "§eКлик для изменения"
            ))
            .build();
        inventory.setItem(startSlot + 2, description);
    }
    
    private void setupCallFunctionSettings(int startSlot) {
        // Имя функции для вызова
        ItemStack functionName = new ItemBuilder(Material.REPEATING_COMMAND_BLOCK)
            .name("§6Имя функции")
            .lore(Arrays.asList(
                "§7Текущее: §e" + block.getParameter("functionName"),
                "",
                "§7Выберите функцию для вызова",
                "",
                "§eКлик для выбора"
            ))
            .build();
        inventory.setItem(startSlot, functionName);
        
        // Аргументы
        ItemStack arguments = new ItemBuilder(Material.PAPER)
            .name("§6Аргументы")
            .lore(Arrays.asList(
                "§7Текущие: §e" + block.getParameter("arguments"),
                "",
                "§7Аргументы через запятую",
                "§7Пример: {player}, \"Привет!\", 10",
                "",
                "§eКлик для настройки"
            ))
            .build();
        inventory.setItem(startSlot + 1, arguments);
    }
    

    
    /**
     * Общие настройки для блоков
     */
    private void setupGenericSettings(int startSlot, String blockTypeName) {
        inventory.setItem(startSlot, new ItemBuilder(Material.NAME_TAG)
            .name("§6Имя блока")
            .lore(Arrays.asList(
                "§7Имя: §e" + (block.getParameter("name") != null ? 
                    block.getParameter("name").toString() : "Блок " + blockTypeName),
                "",
                "§eКлик для изменения"
            ))
            .build());
            
        inventory.setItem(startSlot + 1, new ItemBuilder(Material.WRITABLE_BOOK)
            .name("§6Описание")
            .lore(Arrays.asList(
                "§7Описание блока",
                "§7для документации",
                "",
                "§eКлик для изменения"
            ))
            .build());
    }
    
    /**
     * Кнопки управления
     */
    private void setupControlButtons() {
        // Сохранить и закрыть
        ItemStack save = new ItemBuilder(Material.EMERALD)
            .name("§aСохранить")
            .lore(Arrays.asList(
                "§7Сохранить настройки",
                "§7и закрыть меню"
            ))
            .build();
        inventory.setItem(48, save);
        
        // Сбросить настройки
        ItemStack reset = new ItemBuilder(Material.TNT)
            .name("§cСбросить")
            .lore(Arrays.asList(
                "§7Сбросить все настройки",
                "§7к значениям по умолчанию"
            ))
            .build();
        inventory.setItem(49, reset);
        
        // Отмена
        ItemStack cancel = new ItemBuilder(Material.BARRIER)
            .name("§cОтмена")
            .lore(Arrays.asList(
                "§7Закрыть без сохранения"
            ))
            .build();
        inventory.setItem(50, cancel);
    }
    
    /**
     * Обработка кликов
     */
    public void handleClick(int slot, boolean isShiftClick) {
        switch (slot) {
            case 48: // Сохранить
                saveAndClose();
                break;
            case 49: // Сбросить
                resetSettings();
                break;
            case 50: // Отмена
                cancel();
                break;
            default:
                // Обработка настроек параметров
                handleParameterClick(slot, isShiftClick);
                break;
        }
    }
    
    /**
     * Обработка кликов по параметрам
     */
    private void handleParameterClick(int slot, boolean isShiftClick) {
        switch (block.getType()) {
            case PLAYER_EVENT:
                handlePlayerEventClick(slot, isShiftClick);
                break;
            case PLAYER_ACTION:
                handlePlayerActionClick(slot, isShiftClick);
                break;
            case IF_PLAYER:
                handleIfPlayerClick(slot, isShiftClick);
                break;
            case ENTITY_EVENT:
                handleEntityEventClick(slot, isShiftClick);
                break;
            case WORLD_EVENT:
                handleWorldEventClick(slot, isShiftClick);
                break;
            case VARIABLE_ACTION:
                handleVariableActionClick(slot, isShiftClick);
                break;
            case ENTITY_ACTION:
                handleEntityActionClick(slot, isShiftClick);
                break;
            case WORLD_ACTION:
                handleWorldActionClick(slot, isShiftClick);
                break;
            case IF_ENTITY:
                handleIfEntityClick(slot, isShiftClick);
                break;
            case IF_VARIABLE:
                handleIfVariableClick(slot, isShiftClick);
                break;
            case FUNCTION:
                handleFunctionClick(slot, isShiftClick);
                break;
            case CALL_FUNCTION:
                handleCallFunctionClick(slot, isShiftClick);
                break;
            default:
                handleGenericClick(slot, isShiftClick);
                break;
        }
    }
    
    /**
     * Обработка кликов для событий игрока
     */
    private void handlePlayerEventClick(int slot, boolean isShiftClick) {
        if (slot == 19) { // Тип события
            player.closeInventory();
            AnvilGUIHelper.openTextInput(plugin, player, "Тип события", "JOIN", (eventType) -> {
                try {
                    // Проверяем, что введенный тип события существует
                    ru.openhousing.coding.blocks.events.PlayerEventBlock.PlayerEventType.valueOf(eventType.toUpperCase());
                    block.setParameter("eventType", eventType.toUpperCase());
                    MessageUtil.send(player, "&aТип события установлен: &e" + eventType.toUpperCase());
                    this.open(); // Возвращаемся к настройкам блока
                } catch (IllegalArgumentException e) {
                    MessageUtil.send(player, "&cНеверный тип события! Доступные: JOIN, QUIT, CHAT, MOVE, DAMAGE, SNEAK, JUMP и др.");
                    this.open();
                }
            });
        } else if (slot == 20) { // Параметры события
            player.closeInventory();
            MessageUtil.send(player, "&eНастройка параметров события");
        }
    }
    
    /**
     * Обработка кликов для действий игрока
     */
    private void handlePlayerActionClick(int slot, boolean isShiftClick) {
        if (slot == 19) { // Тип действия
            player.closeInventory();
            AnvilGUIHelper.openTextInput(plugin, player, "Тип действия", "SEND_MESSAGE", (actionType) -> {
                try {
                    // Проверяем, что введенный тип действия существует
                    ru.openhousing.coding.blocks.actions.PlayerActionBlock.PlayerActionType.valueOf(actionType.toUpperCase());
                    block.setParameter("actionType", actionType.toUpperCase());
                    MessageUtil.send(player, "&aТип действия установлен: &e" + actionType.toUpperCase());
                    this.open(); // Возвращаемся к настройкам блока
                } catch (IllegalArgumentException e) {
                    MessageUtil.send(player, "&cНеверный тип действия! Доступные: SEND_MESSAGE, TELEPORT, GIVE_ITEM, HEAL, DAMAGE и др.");
                    this.open();
                }
            });
        } else if (slot == 20) { // Цель действия
            player.closeInventory();
            ValueType[] playerTypes = {ValueType.TEXT, ValueType.VARIABLE};
            ValueSelectorGUI selector = new ValueSelectorGUI(plugin, player, "Выбор игрока", playerTypes, (value) -> {
                block.setParameter("target", value);
                MessageUtil.send(player, "&aЦель установлена: &e" + value);
                // Возвращаемся к настройке блока
                this.open();
            });
            selector.open();
        } else if (slot == 21) { // Значение
            player.closeInventory();
            ValueType[] valueTypes = {ValueType.TEXT, ValueType.NUMBER, ValueType.VARIABLE, ValueType.LOCATION};
            ValueSelectorGUI selector = new ValueSelectorGUI(plugin, player, "Выбор значения", valueTypes, (value) -> {
                block.setParameter("value", value);
                MessageUtil.send(player, "&aЗначение установлено: &e" + value);
                // Возвращаемся к настройке блока
                this.open();
            });
            selector.open();
        }
    }
    
    /**
     * Обработка кликов для условий игрока
     */
    private void handleIfPlayerClick(int slot, boolean isShiftClick) {
        if (slot == 19) { // Условие
            player.closeInventory();
            ValueType[] conditionTypes = {ValueType.TEXT};
            ValueSelectorGUI selector = new ValueSelectorGUI(plugin, player, "Тип условия", conditionTypes, (value) -> {
                block.setParameter("condition", value);
                MessageUtil.send(player, "&aУсловие установлено: &e" + value);
                this.open();
            });
            selector.open();
        } else if (slot == 20) { // Значение сравнения
            player.closeInventory();
            ValueType[] valueTypes = {ValueType.TEXT, ValueType.NUMBER, ValueType.VARIABLE};
            ValueSelectorGUI selector = new ValueSelectorGUI(plugin, player, "Значение сравнения", valueTypes, (value) -> {
                block.setParameter("compareValue", value);
                MessageUtil.send(player, "&aЗначение сравнения установлено: &e" + value);
                this.open();
            });
            selector.open();
        } else if (slot == 21) { // Операция
            // Циклически переключаем операции
            String[] operations = {"=", ">", "<", ">=", "<=", "!="};
            String currentOp = (String) block.getParameter("operation");
            int currentIndex = 0;
            
            for (int i = 0; i < operations.length; i++) {
                if (operations[i].equals(currentOp)) {
                    currentIndex = i;
                    break;
                }
            }
            
            int nextIndex = (currentIndex + 1) % operations.length;
            block.setParameter("operation", operations[nextIndex]);
            
            MessageUtil.send(player, "&aОперация изменена на: &e" + operations[nextIndex]);
            setupInventory(); // Обновляем GUI
        }
    }
    
    /**
     * Обработка кликов для событий существ
     */
    private void handleEntityEventClick(int slot, boolean isShiftClick) {
        if (slot == 19) { // Тип события
            player.closeInventory();
            AnvilGUIHelper.openTextInput(plugin, player, "Тип события существа", "SPAWN", (eventType) -> {
                try {
                    // Проверяем, что введенный тип события существует
                    ru.openhousing.coding.blocks.events.EntityEventBlock.EntityEventType.valueOf(eventType.toUpperCase());
                    block.setParameter("eventType", eventType.toUpperCase());
                    MessageUtil.send(player, "&aТип события установлен: &e" + eventType.toUpperCase());
                    this.open();
                } catch (IllegalArgumentException e) {
                    MessageUtil.send(player, "&cНеверный тип события! Доступные: SPAWN, DEATH, DAMAGE, EXPLODE, FREEZE и др.");
                    this.open();
                }
            });
        } else if (slot == 20) { // Тип существа
            player.closeInventory();
            AnvilGUIHelper.openTextInput(plugin, player, "Тип существа", "ANY", (entityType) -> {
                block.setParameter("entityType", entityType.toUpperCase());
                MessageUtil.send(player, "&aТип существа установлен: &e" + entityType.toUpperCase());
                this.open();
            });
        } else if (slot == 21) { // Мир
            player.closeInventory();
            AnvilGUIHelper.openTextInput(plugin, player, "Мир события", "", (world) -> {
                block.setParameter("world", world);
                MessageUtil.send(player, "&aМир установлен: &e" + world);
                this.open();
            });
        }
    }
    
    /**
     * Обработка кликов для событий мира
     */
    private void handleWorldEventClick(int slot, boolean isShiftClick) {
        if (slot == 19) { // Тип события
            player.closeInventory();
            AnvilGUIHelper.openTextInput(plugin, player, "Тип события мира", "BLOCK_BREAK", (eventType) -> {
                try {
                    // Проверяем, что введенный тип события существует
                    ru.openhousing.coding.blocks.events.WorldEventBlock.WorldEventType.valueOf(eventType.toUpperCase());
                    block.setParameter("eventType", eventType.toUpperCase());
                    MessageUtil.send(player, "&aТип события установлен: &e" + eventType.toUpperCase());
                    this.open();
                } catch (IllegalArgumentException e) {
                    MessageUtil.send(player, "&cНеверный тип события! Доступные: BLOCK_BREAK, WEATHER_CHANGE, EXPLOSION и др.");
                    this.open();
                }
            });
        } else if (slot == 20) { // Тип блока
            player.closeInventory();
            AnvilGUIHelper.openTextInput(plugin, player, "Тип блока", "ANY", (blockType) -> {
                block.setParameter("blockType", blockType.toUpperCase());
                MessageUtil.send(player, "&aТип блока установлен: &e" + blockType.toUpperCase());
                this.open();
            });
        } else if (slot == 21) { // Мир
            player.closeInventory();
            AnvilGUIHelper.openTextInput(plugin, player, "Мир события", "", (world) -> {
                block.setParameter("world", world);
                MessageUtil.send(player, "&aМир установлен: &e" + world);
                this.open();
            });
        }
    }
    
    /**
     * Обработка кликов для действий с переменными
     */
    private void handleVariableActionClick(int slot, boolean isShiftClick) {
        if (slot == 19) { // Тип действия
            player.closeInventory();
            AnvilGUIHelper.openTextInput(plugin, player, "Тип действия", "SET", (actionType) -> {
                block.setParameter("actionType", actionType.toUpperCase());
                MessageUtil.send(player, "&aТип действия установлен: &e" + actionType.toUpperCase());
                this.open();
            });
        } else if (slot == 20) { // Имя переменной
            player.closeInventory();
            new VariableSelectorGUI(plugin, player, (variableValue) -> {
                block.setParameter("variableName", variableValue.toString());
                MessageUtil.send(player, "&aПеременная выбрана: &e" + variableValue.toString());
                this.open();
            }, true).open(); // true = для установки переменной
        } else if (slot == 21) { // Значение
            player.closeInventory();
            ValueType[] valueTypes = {ValueType.TEXT, ValueType.NUMBER, ValueType.VARIABLE};
            ValueSelectorGUI selector = new ValueSelectorGUI(plugin, player, "Значение переменной", valueTypes, (value) -> {
                block.setParameter("value", value);
                MessageUtil.send(player, "&aЗначение установлено: &e" + value);
                this.open();
            });
            selector.open();
        }
    }
    

    
    /**
     * Обработка кликов для действий существа
     */
    private void handleEntityActionClick(int slot, boolean isShiftClick) {
        if (slot == 10) { // Тип действия
            player.closeInventory();
            AnvilGUIHelper.openTextInput(plugin, player, "Тип действия", "DAMAGE", (actionType) -> {
                block.setParameter("actionType", actionType);
                MessageUtil.send(player, "&aТип действия установлен: &e" + actionType);
                this.open();
            });
        } else if (slot == 11) { // Существо
            new ValueSelectorGUI(plugin, player, "Существо", new ValueType[]{ValueType.TEXT}, (entity) -> {
                block.setParameter("entity", entity);
                MessageUtil.send(player, "&aСущество установлено: &e" + entity);
                this.open();
            }).open();
        } else if (slot == 12) { // Значение
            new ValueSelectorGUI(plugin, player, "Значение", new ValueType[]{ValueType.NUMBER}, (value) -> {
                block.setParameter("value", value);
                MessageUtil.send(player, "&aЗначение установлено: &e" + value);
                this.open();
            }).open();
        }
    }
    
    /**
     * Обработка кликов для действий мира
     */
    private void handleWorldActionClick(int slot, boolean isShiftClick) {
        if (slot == 10) { // Тип действия
            player.closeInventory();
            AnvilGUIHelper.openTextInput(plugin, player, "Тип действия", "SET_BLOCK", (actionType) -> {
                block.setParameter("actionType", actionType);
                MessageUtil.send(player, "&aТип действия установлен: &e" + actionType);
                this.open();
            });
        } else if (slot == 11) { // Местоположение
            new LocationSelectorGUI(plugin, player, (location) -> {
                block.setParameter("location", location);
                MessageUtil.send(player, "&aМестоположение установлено: &e" + location);
                this.open();
            }).open();
        } else if (slot == 12) { // Значение
            new ValueSelectorGUI(plugin, player, "Значение", new ValueType[]{ValueType.TEXT}, (value) -> {
                block.setParameter("value", value);
                MessageUtil.send(player, "&aЗначение установлено: &e" + value);
                this.open();
            }).open();
        }
    }
    
    /**
     * Обработка кликов для условий существа
     */
    private void handleIfEntityClick(int slot, boolean isShiftClick) {
        if (slot == 10) { // Тип условия
            player.closeInventory();
            AnvilGUIHelper.openTextInput(plugin, player, "Тип условия", "HEALTH", (conditionType) -> {
                block.setParameter("conditionType", conditionType);
                MessageUtil.send(player, "&aТип условия установлен: &e" + conditionType);
                this.open();
            });
        } else if (slot == 11) { // Существо
            new ValueSelectorGUI(plugin, player, "Существо", new ValueType[]{ValueType.TEXT}, (entity) -> {
                block.setParameter("entity", entity);
                MessageUtil.send(player, "&aСущество установлено: &e" + entity);
                this.open();
            }).open();
        } else if (slot == 12) { // Значение для сравнения
            new ValueSelectorGUI(plugin, player, "Значение", new ValueType[]{ValueType.NUMBER}, (compareValue) -> {
                block.setParameter("compareValue", compareValue);
                MessageUtil.send(player, "&aЗначение для сравнения установлено: &e" + compareValue);
                this.open();
            }).open();
        }
    }
    
    /**
     * Обработка кликов для условий переменной
     */
    private void handleIfVariableClick(int slot, boolean isShiftClick) {
        if (slot == 10) { // Имя переменной
            new VariableSelectorGUI(plugin, player, (variable) -> {
                block.setParameter("variableName", variable);
                MessageUtil.send(player, "&aПеременная установлена: &e" + variable);
                this.open();
            }, false).open();
        } else if (slot == 11) { // Оператор сравнения
            player.closeInventory();
            AnvilGUIHelper.openTextInput(plugin, player, "Оператор сравнения", "EQUALS", (operator) -> {
                block.setParameter("operator", operator);
                MessageUtil.send(player, "&aОператор установлен: &e" + operator);
                this.open();
            });
        } else if (slot == 12) { // Значение для сравнения
            new ValueSelectorGUI(plugin, player, "Значение", new ValueType[]{ValueType.TEXT}, (compareValue) -> {
                block.setParameter("compareValue", compareValue);
                MessageUtil.send(player, "&aЗначение для сравнения установлено: &e" + compareValue);
                this.open();
            }).open();
        }
    }
    
    /**
     * Обработка кликов для функций
     */
    private void handleFunctionClick(int slot, boolean isShiftClick) {
        if (slot == 10) { // Имя функции
            player.closeInventory();
            AnvilGUIHelper.openTextInput(plugin, player, "Имя функции", "myFunction", (functionName) -> {
                block.setParameter("functionName", functionName);
                MessageUtil.send(player, "&aИмя функции установлено: &e" + functionName);
                this.open();
            });
        } else if (slot == 11) { // Параметры функции
            player.closeInventory();
            AnvilGUIHelper.openTextInput(plugin, player, "Параметры функции", "player, message", (parameters) -> {
                block.setParameter("parameters", parameters);
                MessageUtil.send(player, "&aПараметры функции установлены: &e" + parameters);
                this.open();
            });
        } else if (slot == 12) { // Описание
            player.closeInventory();
            AnvilGUIHelper.openTextInput(plugin, player, "Описание функции", "", (description) -> {
                block.setParameter("description", description);
                MessageUtil.send(player, "&aОписание функции установлено: &e" + description);
                this.open();
            });
        }
    }
    
    /**
     * Обработка кликов для вызова функций
     */
    private void handleCallFunctionClick(int slot, boolean isShiftClick) {
        if (slot == 10) { // Имя функции
            player.closeInventory();
            AnvilGUIHelper.openTextInput(plugin, player, "Имя функции для вызова", "", (functionName) -> {
                block.setParameter("functionName", functionName);
                MessageUtil.send(player, "&aФункция для вызова установлена: &e" + functionName);
                this.open();
            });
        } else if (slot == 11) { // Аргументы
            player.closeInventory();
            AnvilGUIHelper.openTextInput(plugin, player, "Аргументы", "", (arguments) -> {
                block.setParameter("arguments", arguments);
                MessageUtil.send(player, "&aАргументы установлены: &e" + arguments);
                this.open();
            });
        }
    }
    
    /**
     * Обработка общих параметров
     */
    private void handleGenericClick(int slot, boolean isShiftClick) {
        if (slot >= 19 && slot <= 21) {
            player.closeInventory();
            AnvilGUIHelper.openTextInput(plugin, player, "Новое значение", "", (value) -> {
                // Определяем какой параметр настраиваем по слоту
                String paramName = switch (slot) {
                    case 19 -> "param1";
                    case 20 -> "param2"; 
                    case 21 -> "param3";
                    default -> "value";
                };
                
                block.setParameter(paramName, value);
                MessageUtil.send(player, "&aЗначение установлено: &e" + value);
                this.open(); // Возвращаемся к настройкам блока
            });
        }
    }
    
    /**
     * Сохранение и закрытие
     */
    private void saveAndClose() {
        MessageUtil.send(player, "&aНастройки блока сохранены!");
        editorGUI.open();
    }
    
    /**
     * Сброс настроек
     */
    private void resetSettings() {
        MessageUtil.send(player, "&cНастройки сброшены к значениям по умолчанию!");
        setupInventory();
    }
    
    /**
     * Отмена
     */
    private void cancel() {
        MessageUtil.send(player, "&7Настройки не изменены");
        editorGUI.open();
    }
    
    public Inventory getInventory() {
        return inventory;
    }
    
    public Player getPlayer() {
        return player;
    }
    
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getView().getTitle().startsWith("§6Настройка блока:")) {
            return;
        }
        
        event.setCancelled(true);
        
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }
        
        Player clicker = (Player) event.getWhoClicked();
        if (!clicker.equals(player)) {
            return;
        }
        
        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null || clickedItem.getType() == Material.AIR) {
            return;
        }
        
        int slot = event.getSlot();
        boolean isShiftClick = event.isShiftClick();
        
        handleClick(slot, isShiftClick);
    }
}
