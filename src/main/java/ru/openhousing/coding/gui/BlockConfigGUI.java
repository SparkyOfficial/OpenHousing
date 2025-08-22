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
import ru.openhousing.coding.values.VariableValue;
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
        // Проверяем, нужно ли использовать специализированный GUI
        if (shouldUseSpecializedGUI()) {
            openSpecializedGUI();
            return;
        }
        
        // Используем стандартный GUI для остальных блоков
        setupInventory();
        player.openInventory(inventory);
    }
    
    /**
     * Проверяет, нужно ли использовать специализированный GUI для данного блока
     */
    private boolean shouldUseSpecializedGUI() {
        BlockType.BlockCategory category = block.getType().getCategory();
        String typeName = block.getType().name();
        
        // Все действия используют специализированные GUI
        if (category == BlockType.BlockCategory.ACTION) {
            return true;
        }
        
        // Все условия используют специализированный GUI
        if (category == BlockType.BlockCategory.CONDITION) {
            return true;
        }
        
        // События используют специализированный GUI
        if (category == BlockType.BlockCategory.EVENT) {
            return true;
        }
        
        // Функции используют специализированный GUI
        if (category == BlockType.BlockCategory.FUNCTION) {
            return true;
        }
        
        // Элементы управления (циклы) используют специализированный GUI
        if (category == BlockType.BlockCategory.CONTROL && block.getType() == BlockType.REPEAT) {
            return true;
        }
        
        // Математика использует специализированный GUI
        if (block.getType() == BlockType.MATH) {
            return true;
        }
        
        return false;
    }
    
    /**
     * Открывает специализированный GUI для блока
     */
    private void openSpecializedGUI() {
        // Отменяем регистрацию текущего листенера
        org.bukkit.event.HandlerList.unregisterAll(this);
        
        BlockType.BlockCategory category = block.getType().getCategory();
        String typeName = block.getType().name();
        
        Runnable saveCallback = () -> {
            if (onSave != null) {
                onSave.accept(block);
            } else if (editorGUI != null) {
                editorGUI.open();
            }
        };
        
        switch (category) {
            case ACTION:
                if (typeName.startsWith("PLAYER_")) {
                    new ru.openhousing.coding.gui.blocks.PlayerActionConfigGUI(plugin, player, block).open();
                } else if (typeName.startsWith("VAR_")) {
                    new ru.openhousing.coding.gui.blocks.VariableActionConfigGUI(plugin, player, block).open();
                } else if (typeName.startsWith("GAME_")) {
                    new ru.openhousing.coding.gui.blocks.GameActionConfigGUI(plugin, player, block).open();
                } else {
                    // Используем PlayerAction GUI как универсальный для действий
                    new ru.openhousing.coding.gui.blocks.PlayerActionConfigGUI(plugin, player, block).open();
                }
                break;
                
            case CONDITION:
                new ru.openhousing.coding.gui.blocks.ConditionConfigGUI(plugin, player, block).open();
                break;
                
            case EVENT:
                new ru.openhousing.coding.gui.blocks.EventConfigGUI(plugin, player, block).open();
                break;
                
            case FUNCTION:
                new ru.openhousing.coding.gui.blocks.FunctionConfigGUI(plugin, player, block).open();
                break;
                
            case CONTROL:
                if (block.getType() == BlockType.REPEAT) {
                    new ru.openhousing.coding.gui.blocks.LoopConfigGUI(plugin, player, block).open();
                } else {
                    // Для остальных элементов управления используем стандартный GUI
                    setupInventory();
                    player.openInventory(inventory);
                }
                break;
                
            default:
                if (block.getType() == BlockType.MATH) {
                    new ru.openhousing.coding.gui.blocks.MathConfigGUI(plugin, player, block).open();
                } else {
                    // Используем стандартный GUI
                    setupInventory();
                    player.openInventory(inventory);
                }
                break;
        }
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
        if (block.getType().name().startsWith("PLAYER_") && block.getType().getCategory() == BlockType.BlockCategory.EVENT) {
            setupPlayerEventSettings(10);
        } else if (block.getType().name().startsWith("ENTITY_") && block.getType().getCategory() == BlockType.BlockCategory.EVENT) {
            setupEntityEventSettings(10);
        } else if (block.getType().name().startsWith("WORLD_") && block.getType().getCategory() == BlockType.BlockCategory.EVENT) {
            setupWorldEventSettings(10);
        } else if (block.getType().name().startsWith("PLAYER_") && block.getType().getCategory() == BlockType.BlockCategory.ACTION) {
            setupPlayerActionSettings(10);
        } else if (block.getType().name().startsWith("GAME_") && block.getType().getCategory() == BlockType.BlockCategory.ACTION) {
            setupWorldActionSettings(10);
        } else if (block.getType().name().startsWith("IF_PLAYER")) {
            setupIfPlayerSettings(10);
        } else if (block.getType().name().startsWith("IF_ENTITY")) {
            setupIfEntitySettings(10);
        } else if (block.getType().name().startsWith("IF_VARIABLE")) {
            setupIfVariableSettings(10);
        } else if (block.getType() == BlockType.FUNCTION) {
            setupFunctionSettings(10);
        } else if (block.getType() == BlockType.CALL_FUNCTION) {
            setupCallFunctionSettings(10);
        } else if (block.getType().name().startsWith("VAR_")) {
            setupVariableActionSettings(10);
        } else if (block.getType() == BlockType.MATH) {
            setupMathSettings(10);
        } else if (block.getType() == BlockType.TEXT_OPERATION) {
            setupTextOperationSettings(10);
        } else {
            setupGenericSettings(10);
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
        
        // Значение для сравнения (drag-n-drop слот для переменной)
        updateVariableSlot(startSlot + 1, "value", block.getParameter("value"));
            
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
            
        // Основное значение (drag-n-drop слот для переменной)
        updateVariableSlot(startSlot + 1, "value", block.getParameter("value"));
            
        // Дополнительные параметры (drag-n-drop слоты)
        updateVariableSlot(startSlot + 2, "extra1", block.getParameter("extra1"));
        updateVariableSlot(startSlot + 3, "extra2", block.getParameter("extra2"));
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
            
        // Значение для сравнения (drag-n-drop слот для переменной)
        updateVariableSlot(startSlot + 1, "value", block.getParameter("value"));
            
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
        // Имя переменной (drag-n-drop слот для переменной)
        updateVariableSlot(startSlot, "variableName", block.getParameter("variableName"));
        
        // Значение для сравнения (drag-n-drop слот для переменной)
        updateVariableSlot(startSlot + 1, "compareValue", block.getParameter("compareValue"));
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
        // Тип действия с переменной
        inventory.setItem(startSlot, new ItemBuilder(Material.NAME_TAG)
            .name("§eДействие с переменной")
            .lore(Arrays.asList(
                "§7Текущий: §f" + block.getParameter("actionType"),
                "",
                "§7Клик для изменения"
            ))
            .build());
        
        // Имя переменной (drag-n-drop слот для переменной)
        updateVariableSlot(startSlot + 1, "variableName", block.getParameter("variableName"));
        
        // Значение для установки (drag-n-drop слот для переменной)
        updateVariableSlot(startSlot + 2, "setValue", block.getParameter("setValue"));
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
            
        // Первое значение (drag-n-drop слот для переменной)
        updateVariableSlot(startSlot + 1, "value1", block.getParameter("value1"));
            
        // Второе значение (drag-n-drop слот для переменной)
        updateVariableSlot(startSlot + 2, "value2", block.getParameter("value2"));
            
        // Переменная результата (drag-n-drop слот для переменной)
        updateVariableSlot(startSlot + 3, "resultVariable", block.getParameter("resultVariable"));
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
        if (event.getView().getTopInventory() != inventory) return;
        if (event.getClickedInventory() == null) return;
        if (!(event.getWhoClicked() instanceof Player)) return;
        
        Player clicker = (Player) event.getWhoClicked();
        if (!clicker.equals(player)) return;
        
        event.setCancelled(true);
        
        int slot = event.getSlot();
        boolean isShiftClick = event.isShiftClick();
        
        handleClick(slot, isShiftClick);
    }
    
    /**
     * Проверяет, является ли слот слотом для переменной
     */
    private boolean isVariableSlot(int slot) {
        // Слоты 10-25 могут быть слотами для переменных
        return slot >= 10 && slot <= 25;
    }
    
    /**
     * Обработка клика по слоту переменной
     */
    private void handleVariableSlotClick(int slot, boolean isRightClick) {
        if (isRightClick) {
            // Правый клик - очистить переменную
            clearVariableSlot(slot);
        } else {
            // Левый клик - открыть селектор переменных
            openVariableSelector(slot);
        }
    }
    
    /**
     * Очистка слота переменной
     */
    private void clearVariableSlot(int slot) {
        // Находим параметр для этого слота
        String paramName = getParameterNameForSlot(slot);
        if (paramName != null) {
            block.setParameter(paramName, null);
            updateVariableSlot(slot, paramName, null);
            player.sendMessage("§7Переменная очищена: " + paramName);
        }
    }
    
    /**
     * Открытие селектора переменных
     */
    private void openVariableSelector(int slot) {
        String paramName = getParameterNameForSlot(slot);
        if (paramName == null) {
            player.sendMessage("§cНе удалось определить параметр для слота " + slot);
            return;
        }
        
        player.closeInventory();
        
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            VariableSelectorGUI variableSelector = new VariableSelectorGUI(
                plugin,
                player,
                (variableValue) -> {
                    // Устанавливаем переменную в блок
                    block.setParameter(paramName, variableValue);
                    
                    // Обновляем отображение
                    updateVariableSlot(slot, paramName, variableValue);
                    
                    player.sendMessage("§aПеременная установлена: §f" + paramName + " = " + variableValue.getVariableName());
                    
                    // Возвращаемся к настройке блока
                    Bukkit.getScheduler().runTaskLater(plugin, this::open, 1L);
                },
                false
            );
            variableSelector.open();
        }, 1L);
    }
    
    /**
     * Получение имени параметра для слота
     */
    private String getParameterNameForSlot(int slot) {
        // Маппинг слотов на параметры в зависимости от типа блока
        if (block.getType().name().startsWith("PLAYER_") && block.getType().getCategory() == BlockType.BlockCategory.ACTION) {
            switch (slot) {
                case 10: return "actionType";
                case 11: return "value";
                case 12: return "extra1";
                case 13: return "extra2";
                case 14: return "target";
            }
        } else if (block.getType().name().startsWith("IF_")) {
            switch (slot) {
                case 10: return "condition";
                case 11: return "value";
                case 12: return "compareTo";
            }
        } else if (block.getType() == BlockType.MATH) {
            switch (slot) {
                case 10: return "operation";
                case 11: return "value1";
                case 12: return "value2";
                case 13: return "result";
            }
        }
        
        // По умолчанию используем номер слота
        return "param" + (slot - 9);
    }
    
    /**
     * Обновление слота переменной
     */
    private void updateVariableSlot(int slot, String paramName, Object value) {
        if (value == null) {
            // Пустой слот
            inventory.setItem(slot, new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE)
                .name("§7" + paramName)
            .lore(Arrays.asList(
                    "§7Перетащите переменную сюда",
                    "",
                    "§eЛКМ - выбрать переменную",
                    "§eПКМ - очистить"
                ))
                .build());
        } else {
            // Слот с переменной
            String displayValue = value.toString();
            if (value instanceof VariableValue) {
                displayValue = ((VariableValue) value).getVariableName();
            }
            
            inventory.setItem(slot, new ItemBuilder(Material.EMERALD)
                .name("§a" + paramName + " = " + displayValue)
            .lore(Arrays.asList(
                    "§7Текущее значение",
                    "",
                    "§eЛКМ - изменить переменную",
                    "§eПКМ - очистить"
                ))
                .build());
        }
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
        if (block.getType().name().startsWith("PLAYER_") && block.getType().getCategory() == BlockType.BlockCategory.EVENT) {
            handleEventClick(slot, isShiftClick);
        } else if (block.getType().name().startsWith("ENTITY_") && block.getType().getCategory() == BlockType.BlockCategory.EVENT) {
            handleEventClick(slot, isShiftClick);
        } else if (block.getType().name().startsWith("WORLD_") && block.getType().getCategory() == BlockType.BlockCategory.EVENT) {
            handleEventClick(slot, isShiftClick);
        } else if (block.getType().name().startsWith("PLAYER_") && block.getType().getCategory() == BlockType.BlockCategory.ACTION) {
            handlePlayerActionClick(slot, isShiftClick);
        } else if (block.getType().name().startsWith("GAME_") && block.getType().getCategory() == BlockType.BlockCategory.ACTION) {
            handleWorldActionClick(slot, isShiftClick);
        } else if (block.getType().name().startsWith("IF_PLAYER")) {
            handleIfPlayerClick(slot, isShiftClick);
        } else if (block.getType().name().startsWith("IF_ENTITY")) {
            handleIfEntityClick(slot, isShiftClick);
        } else if (block.getType().name().startsWith("IF_VARIABLE")) {
            handleIfVariableClick(slot, isShiftClick);
        } else if (block.getType() == BlockType.FUNCTION) {
            handleFunctionClick(slot, isShiftClick);
        } else if (block.getType() == BlockType.CALL_FUNCTION) {
            handleCallFunctionClick(slot, isShiftClick);
        } else if (block.getType().name().startsWith("VAR_")) {
            handleVariableActionClick(slot, isShiftClick);
        } else if (block.getType() == BlockType.MATH) {
            handleMathClick(slot, isShiftClick);
        } else if (block.getType() == BlockType.TEXT_OPERATION) {
            handleTextOperationClick(slot, isShiftClick);
        } else {
            player.sendMessage("§cОбработчик для этого типа блока еще не реализован!");
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
        // Открываем специализированное GUI для действий игрока
        player.closeInventory();
        new ru.openhousing.coding.gui.blocks.PlayerActionConfigGUI(plugin, player, block).open();
    }
    
    private void handleEntityActionClick(int slot, boolean isShiftClick) {
        // Открываем специализированное GUI для действий с существами
        player.closeInventory();
        new ru.openhousing.coding.gui.blocks.EntityActionConfigGUI(plugin, player, block).open();
    }
    
    private void handleWorldActionClick(int slot, boolean isShiftClick) {
        // Открываем специализированное GUI для игровых действий
        player.closeInventory();
        new ru.openhousing.coding.gui.blocks.GameActionConfigGUI(plugin, player, block).open();
    }
    
    private void handleIfPlayerClick(int slot, boolean isShiftClick) {
        // Открываем специализированное GUI для условий
        player.closeInventory();
        new ru.openhousing.coding.gui.blocks.ConditionConfigGUI(plugin, player, block).open();
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
        // Открываем специализированное GUI для условий существ
        player.closeInventory();
        new ru.openhousing.coding.gui.blocks.ConditionConfigGUI(plugin, player, block).open();
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
        // Открываем специализированное GUI для функций
        player.closeInventory();
        new ru.openhousing.coding.gui.blocks.FunctionConfigGUI(plugin, player, block).open();
    }
    
    private void handleCallFunctionClick(int slot, boolean isShiftClick) {
        // Открываем специализированное GUI для вызова функций
        player.closeInventory();
        new ru.openhousing.coding.gui.blocks.FunctionConfigGUI(plugin, player, block).open();
    }
    
    private void handleVariableActionClick(int slot, boolean isShiftClick) {
        // Открываем специализированное GUI для действий с переменными
        player.closeInventory();
        new ru.openhousing.coding.gui.blocks.VariableActionConfigGUI(plugin, player, block).open();
    }
    
    
    private void handleEventClick(int slot, boolean isShiftClick) {
        // Открываем специализированное GUI для событий
        player.closeInventory();
        new ru.openhousing.coding.gui.blocks.EventConfigGUI(plugin, player, block).open();
    }
    
    private void handleOldVariableActionClick(int slot, boolean isShiftClick) {
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
        // Открываем специализированное GUI для математических операций
        player.closeInventory();
        new ru.openhousing.coding.gui.blocks.MathConfigGUI(plugin, player, block).open();
    }
    
    private void handleTextOperationClick(int slot, boolean isShiftClick) {
        // Открываем специализированное GUI для текстовых операций
        player.closeInventory();
        new ru.openhousing.coding.gui.blocks.MathConfigGUI(plugin, player, block).open(); // Пока используем MathConfigGUI
    }
    
    private void handleOldMathClick(int slot, boolean isShiftClick) {
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
