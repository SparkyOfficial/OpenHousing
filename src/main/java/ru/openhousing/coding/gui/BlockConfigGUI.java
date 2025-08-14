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
import ru.openhousing.utils.ItemBuilder;

import java.util.Arrays;
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
        inventory.setItem(startSlot, new ItemBuilder(Material.GOLDEN_SWORD)
            .name("§eТип действия игрока")
            .lore("§7Клик для настройки")
            .build());
    }
    
    private void setupEntityActionSettings(int startSlot) {
        inventory.setItem(startSlot, new ItemBuilder(Material.LEAD)
            .name("§eТип действия сущности")
            .lore("§7Клик для настройки")
            .build());
    }
    
    private void setupWorldActionSettings(int startSlot) {
        inventory.setItem(startSlot, new ItemBuilder(Material.TNT)
            .name("§eТип действия мира")
            .lore("§7Клик для настройки")
            .build());
    }
    
    private void setupIfPlayerSettings(int startSlot) {
        inventory.setItem(startSlot, new ItemBuilder(Material.COMPARATOR)
            .name("§eУсловие игрока")
            .lore("§7Клик для настройки")
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
        inventory.setItem(startSlot, new ItemBuilder(Material.REDSTONE)
            .name("§eМатематическая операция")
            .lore("§7Клик для настройки")
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
            player.sendMessage("§eНастройка дополнительных условий");
            // TODO: Реализовать настройку условий
        }
    }
    
    /**
     * Заглушки для остальных обработчиков
     */
    private void handleEntityEventClick(int slot, boolean isShiftClick) {
        player.sendMessage("§eНастройка события сущности (в разработке)");
    }
    
    private void handleWorldEventClick(int slot, boolean isShiftClick) {
        player.sendMessage("§eНастройка события мира (в разработке)");
    }
    
    private void handlePlayerActionClick(int slot, boolean isShiftClick) {
        player.sendMessage("§eНастройка действия игрока (в разработке)");
    }
    
    private void handleEntityActionClick(int slot, boolean isShiftClick) {
        player.sendMessage("§eНастройка действия сущности (в разработке)");
    }
    
    private void handleWorldActionClick(int slot, boolean isShiftClick) {
        player.sendMessage("§eНастройка действия мира (в разработке)");
    }
    
    private void handleIfPlayerClick(int slot, boolean isShiftClick) {
        player.sendMessage("§eНастройка условия игрока (в разработке)");
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
        player.sendMessage("§eНастройка математического блока (в разработке)");
    }
    
    private void handleTextOperationClick(int slot, boolean isShiftClick) {
        player.sendMessage("§eНастройка текстового блока (в разработке)");
    }
}
