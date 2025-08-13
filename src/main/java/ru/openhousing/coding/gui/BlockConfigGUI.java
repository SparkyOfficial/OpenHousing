package ru.openhousing.coding.gui;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import ru.openhousing.OpenHousing;
import ru.openhousing.coding.blocks.CodeBlock;
import ru.openhousing.utils.ItemBuilder;
import ru.openhousing.utils.MessageUtil;

import java.util.Arrays;
import java.util.List;

/**
 * GUI для настройки параметров блока кода
 */
public class BlockConfigGUI {
    
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
        setupGenericSettings(startSlot, "событие существа");
    }
    
    private void setupWorldEventSettings(int startSlot) {
        setupGenericSettings(startSlot, "событие мира");
    }
    
    private void setupIfPlayerSettings(int startSlot) {
        setupGenericSettings(startSlot, "условие игрока");
    }
    
    private void setupIfEntitySettings(int startSlot) {
        setupGenericSettings(startSlot, "условие существа");
    }
    
    private void setupIfVariableSettings(int startSlot) {
        setupGenericSettings(startSlot, "условие переменной");
    }
    
    private void setupPlayerActionSettings(int startSlot) {
        setupGenericSettings(startSlot, "действие игрока");
    }
    
    private void setupEntityActionSettings(int startSlot) {
        setupGenericSettings(startSlot, "действие существа");
    }
    
    private void setupWorldActionSettings(int startSlot) {
        setupGenericSettings(startSlot, "действие мира");
    }
    
    private void setupFunctionSettings(int startSlot) {
        setupGenericSettings(startSlot, "функция");
    }
    
    private void setupCallFunctionSettings(int startSlot) {
        setupGenericSettings(startSlot, "вызов функции");
    }
    
    /**
     * Общие настройки для блоков
     */
    private void setupGenericSettings(int startSlot, String blockTypeName) {
        ItemStack comingSoon = new ItemBuilder(Material.BARRIER)
            .name("§cВ разработке")
            .lore(Arrays.asList(
                "§7Настройки для " + blockTypeName,
                "§7будут добавлены в следующих",
                "§7обновлениях!"
            ))
            .build();
        inventory.setItem(startSlot, comingSoon);
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
        // Здесь будет реализация настройки конкретных параметров
        MessageUtil.send(player, "&eНастройка параметров будет добавлена в следующих обновлениях!");
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
}
