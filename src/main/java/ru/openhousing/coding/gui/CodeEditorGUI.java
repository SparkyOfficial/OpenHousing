package ru.openhousing.coding.gui;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import ru.openhousing.OpenHousing;
import ru.openhousing.coding.blocks.BlockType;
import ru.openhousing.coding.blocks.CodeBlock;
import ru.openhousing.coding.script.CodeScript;
import ru.openhousing.utils.ItemBuilder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * GUI редактора кода
 */
public class CodeEditorGUI implements InventoryHolder {
    
    private final OpenHousing plugin;
    private final Player player;
    private final CodeScript script;
    private final Inventory inventory;
    
    // Состояние GUI
    private EditorMode mode;
    private BlockType.BlockCategory selectedCategory;
    private int page;
    private CodeBlock selectedBlock;
    
    public enum EditorMode {
        MAIN,           // Главное меню
        CATEGORIES,     // Категории блоков
        BLOCKS,         // Список блоков в категории
        SCRIPT,         // Просмотр скрипта
        BLOCK_EDIT      // Редактирование блока
    }
    
    public CodeEditorGUI(OpenHousing plugin, Player player, CodeScript script) {
        this.plugin = plugin;
        this.player = player;
        this.script = script;
        this.inventory = Bukkit.createInventory(this, 54, "§6OpenHousing §8| §fРедактор кода");
        this.mode = EditorMode.MAIN;
        this.page = 0;
        
        updateInventory();
    }
    
    /**
     * Открытие GUI
     */
    public void open() {
        player.openInventory(inventory);
    }
    
    /**
     * Закрытие GUI
     */
    public void close() {
        player.closeInventory();
    }
    
    /**
     * Обновление содержимого инвентаря
     */
    public void updateInventory() {
        inventory.clear();
        
        switch (mode) {
            case MAIN:
                setupMainMenu();
                break;
            case CATEGORIES:
                setupCategoriesMenu();
                break;
            case BLOCKS:
                setupBlocksMenu();
                break;
            case SCRIPT:
                setupScriptView();
                break;
            case BLOCK_EDIT:
                setupBlockEdit();
                break;
        }
        
        // Добавляем навигационные элементы
        addNavigationItems();
    }
    
    /**
     * Настройка главного меню
     */
    private void setupMainMenu() {
        // Информация о скрипте
        CodeScript.ScriptStats stats = script.getStats();
        
        inventory.setItem(10, new ItemBuilder(Material.BOOK)
            .name("§6Мой скрипт")
            .lore(Arrays.asList(
                "§7Блоков: §f" + stats.getTotalBlocks(),
                "§7События: §f" + stats.getEventBlocks(),
                "§7Условия: §f" + stats.getConditionBlocks(),
                "§7Действия: §f" + stats.getActionBlocks(),
                "§7Функции: §f" + stats.getFunctionCount(),
                "§7Переменные: §f" + stats.getVariableCount(),
                "",
                stats.hasErrors() ? "§cЕсть ошибки!" : "§aВсе в порядке",
                "",
                "§eНажмите, чтобы просмотреть скрипт"
            ))
            .build());
        
        // Добавить блок
        inventory.setItem(12, new ItemBuilder(Material.COMMAND_BLOCK)
            .name("§aДобавить блок")
            .lore(Arrays.asList(
                "§7Добавьте новый блок в ваш скрипт",
                "",
                "§eНажмите, чтобы открыть категории"
            ))
            .build());
        
        // Выполнить скрипт
        inventory.setItem(14, new ItemBuilder(Material.REPEATING_COMMAND_BLOCK)
            .name("§2Выполнить скрипт")
            .lore(Arrays.asList(
                "§7Запустить выполнение вашего скрипта",
                "",
                script.isEnabled() ? "§aСкрипт включен" : "§cСкрипт отключен",
                "",
                "§eНажмите, чтобы выполнить"
            ))
            .build());
        
        // Настройки
        inventory.setItem(16, new ItemBuilder(Material.REDSTONE)
            .name("§6Настройки")
            .lore(Arrays.asList(
                "§7Настройки вашего скрипта",
                "",
                "§7Статус: " + (script.isEnabled() ? "§aВключен" : "§cОтключен"),
                "",
                "§eНажмите, чтобы открыть настройки"
            ))
            .build());
        
        // Поделиться скриптом
        inventory.setItem(28, new ItemBuilder(Material.PAPER)
            .name("§bПоделиться скриптом")
            .lore(Arrays.asList(
                "§7Поделитесь своим скриптом с другими игроками",
                "",
                "§eНажмите, чтобы создать ссылку"
            ))
            .build());
        
        // Импорт скрипта
        inventory.setItem(30, new ItemBuilder(Material.WRITTEN_BOOK)
            .name("§9Импорт скрипта")
            .lore(Arrays.asList(
                "§7Импортируйте скрипт от другого игрока",
                "",
                "§eНажмите, чтобы ввести код"
            ))
            .build());
        
        // Очистить скрипт
        inventory.setItem(32, new ItemBuilder(Material.BARRIER)
            .name("§cОчистить скрипт")
            .lore(Arrays.asList(
                "§7Удалить все блоки из скрипта",
                "",
                "§c⚠ Это действие нельзя отменить!",
                "",
                "§eНажмите, чтобы очистить"
            ))
            .build());
        
        // Справка
        inventory.setItem(34, new ItemBuilder(Material.ENCHANTED_BOOK)
            .name("§eSправка")
            .lore(Arrays.asList(
                "§7Руководство по использованию",
                "§7визуального редактора кода",
                "",
                "§eНажмите, чтобы открыть справку"
            ))
            .build());
    }
    
    /**
     * Настройка меню категорий
     */
    private void setupCategoriesMenu() {
        int slot = 10;
        
        for (BlockType.BlockCategory category : BlockType.BlockCategory.values()) {
            inventory.setItem(slot, new ItemBuilder(category.getMaterial())
                .name("§6" + category.getDisplayName())
                .lore(Arrays.asList(
                    "§7" + category.getDescription(),
                    "",
                    "§eНажмите, чтобы просмотреть блоки"
                ))
                .build());
            
            slot += 2;
            if (slot > 16) {
                slot = 19;
            }
            if (slot > 25) break;
        }
    }
    
    /**
     * Настройка меню блоков
     */
    private void setupBlocksMenu() {
        if (selectedCategory == null) return;
        
        List<BlockType> blocksInCategory = new ArrayList<>();
        for (BlockType blockType : BlockType.values()) {
            if (blockType.getCategory() == selectedCategory) {
                blocksInCategory.add(blockType);
            }
        }
        
        int startIndex = page * 28;
        int endIndex = Math.min(startIndex + 28, blocksInCategory.size());
        
        int slot = 10;
        for (int i = startIndex; i < endIndex; i++) {
            BlockType blockType = blocksInCategory.get(i);
            
            inventory.setItem(slot, new ItemBuilder(blockType.getMaterial())
                .name("§6" + blockType.getDisplayName())
                .lore(Arrays.asList(
                    "§7" + blockType.getDescription(),
                    "",
                    "§eЛевый клик - добавить в скрипт",
                    "§eПравый клик - предварительный просмотр"
                ))
                .build());
            
            slot++;
            if (slot == 17) slot = 19;
            if (slot == 26) slot = 28;
            if (slot == 35) slot = 37;
            if (slot >= 44) break;
        }
        
        // Навигация по страницам
        if (page > 0) {
            inventory.setItem(45, new ItemBuilder(Material.ARROW)
                .name("§7Предыдущая страница")
                .build());
        }
        
        if (endIndex < blocksInCategory.size()) {
            inventory.setItem(53, new ItemBuilder(Material.ARROW)
                .name("§7Следующая страница")
                .build());
        }
    }
    
    /**
     * Настройка просмотра скрипта
     */
    private void setupScriptView() {
        List<CodeBlock> blocks = script.getBlocks();
        int startIndex = page * 28;
        int endIndex = Math.min(startIndex + 28, blocks.size());
        
        int slot = 10;
        for (int i = startIndex; i < endIndex; i++) {
            CodeBlock block = blocks.get(i);
            
            ItemStack item = new ItemBuilder(block.getType().getMaterial())
                .name("§6" + block.getType().getDisplayName())
                .lore(block.getDescription())
                .build();
            
            // Добавляем индекс блока в NBT
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(meta.getDisplayName() + " §8(#" + (i + 1) + ")");
                item.setItemMeta(meta);
            }
            
            inventory.setItem(slot, item);
            
            slot++;
            if (slot == 17) slot = 19;
            if (slot == 26) slot = 28;
            if (slot == 35) slot = 37;
            if (slot >= 44) break;
        }
        
        // Навигация по страницам
        if (page > 0) {
            inventory.setItem(45, new ItemBuilder(Material.ARROW)
                .name("§7Предыдущая страница")
                .build());
        }
        
        if (endIndex < blocks.size()) {
            inventory.setItem(53, new ItemBuilder(Material.ARROW)
                .name("§7Следующая страница")
                .build());
        }
    }
    
    /**
     * Настройка редактирования блока
     */
    private void setupBlockEdit() {
        if (selectedBlock == null) return;
        
        // Информация о блоке
        inventory.setItem(4, new ItemBuilder(selectedBlock.getType().getMaterial())
            .name("§6" + selectedBlock.getType().getDisplayName())
            .lore(selectedBlock.getDescription())
            .build());
        
        // Параметры блока
        int slot = 19;
        for (String paramKey : selectedBlock.getParameters().keySet()) {
            Object value = selectedBlock.getParameter(paramKey);
            
            inventory.setItem(slot, new ItemBuilder(Material.NAME_TAG)
                .name("§e" + paramKey)
                .lore(Arrays.asList(
                    "§7Значение: §f" + (value != null ? value.toString() : "не установлено"),
                    "",
                    "§eНажмите, чтобы изменить"
                ))
                .build());
            
            slot++;
            if (slot > 25) break;
        }
        
        // Действия
        inventory.setItem(40, new ItemBuilder(Material.LIME_DYE)
            .name("§aСохранить изменения")
            .build());
        
        inventory.setItem(42, new ItemBuilder(Material.RED_DYE)
            .name("§cУдалить блок")
            .build());
    }
    
    /**
     * Добавление навигационных элементов
     */
    private void addNavigationItems() {
        // Назад
        if (mode != EditorMode.MAIN) {
            inventory.setItem(0, new ItemBuilder(Material.ARROW)
                .name("§7Назад")
                .build());
        }
        
        // Главное меню
        inventory.setItem(4, new ItemBuilder(Material.NETHER_STAR)
            .name("§6Главное меню")
            .build());
        
        // Закрыть
        inventory.setItem(8, new ItemBuilder(Material.BARRIER)
            .name("§cЗакрыть")
            .build());
        
        // Разделители
        ItemStack glass = new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE)
            .name(" ")
            .build();
        
        for (int i = 1; i < 8; i++) {
            if (inventory.getItem(i) == null) {
                inventory.setItem(i, glass);
            }
        }
        
        for (int i = 45; i < 54; i++) {
            if (inventory.getItem(i) == null) {
                inventory.setItem(i, glass);
            }
        }
    }
    
    /**
     * Обработка клика по слоту
     */
    public void handleClick(int slot, boolean isRightClick, boolean isShiftClick) {
        ItemStack item = inventory.getItem(slot);
        if (item == null || item.getType() == Material.AIR) return;
        
        switch (mode) {
            case MAIN:
                handleMainMenuClick(slot);
                break;
            case CATEGORIES:
                handleCategoriesClick(slot);
                break;
            case BLOCKS:
                handleBlocksClick(slot, isRightClick);
                break;
            case SCRIPT:
                handleScriptClick(slot, isRightClick);
                break;
            case BLOCK_EDIT:
                handleBlockEditClick(slot);
                break;
        }
        
        // Общие навигационные элементы
        handleNavigationClick(slot);
    }
    
    private void handleMainMenuClick(int slot) {
        switch (slot) {
            case 10: // Просмотр скрипта
                mode = EditorMode.SCRIPT;
                page = 0;
                updateInventory();
                break;
            case 12: // Добавить блок
                mode = EditorMode.CATEGORIES;
                updateInventory();
                break;
            case 14: // Выполнить скрипт
                plugin.getCodeManager().executeScript(player);
                player.sendMessage("§aСкрипт выполнен!");
                break;
            case 16: // Настройки
                script.setEnabled(!script.isEnabled());
                player.sendMessage(script.isEnabled() ? 
                    "§aСкрипт включен!" : "§cСкрипт отключен!");
                updateInventory();
                break;
            case 32: // Очистить скрипт
                script.clear();
                player.sendMessage("§cСкрипт очищен!");
                updateInventory();
                break;
        }
    }
    
    private void handleCategoriesClick(int slot) {
        BlockType.BlockCategory[] categories = BlockType.BlockCategory.values();
        int categoryIndex = getCategoryIndexFromSlot(slot);
        
        if (categoryIndex >= 0 && categoryIndex < categories.length) {
            selectedCategory = categories[categoryIndex];
            mode = EditorMode.BLOCKS;
            page = 0;
            updateInventory();
        }
    }
    
    private void handleBlocksClick(int slot, boolean isRightClick) {
        int blockIndex = getBlockIndexFromSlot(slot);
        if (blockIndex >= 0 && selectedCategory != null) {
            List<BlockType> blocksInCategory = new ArrayList<>();
            for (BlockType blockType : BlockType.values()) {
                if (blockType.getCategory() == selectedCategory) {
                    blocksInCategory.add(blockType);
                }
            }
            
            int actualIndex = page * 28 + blockIndex;
            if (actualIndex < blocksInCategory.size()) {
                BlockType blockType = blocksInCategory.get(actualIndex);
                
                if (isRightClick) {
                    // Предварительный просмотр
                    player.sendMessage("§eПредварительный просмотр: §f" + blockType.getDisplayName());
                    player.sendMessage("§7" + blockType.getDescription());
                } else {
                    // Добавить блок
                    addBlockToScript(blockType);
                    player.sendMessage("§aБлок добавлен: §f" + blockType.getDisplayName());
                }
            }
        }
    }
    
    private void handleScriptClick(int slot, boolean isRightClick) {
        int blockIndex = getBlockIndexFromSlot(slot);
        if (blockIndex >= 0) {
            int actualIndex = page * 28 + blockIndex;
            List<CodeBlock> blocks = script.getBlocks();
            
            if (actualIndex < blocks.size()) {
                selectedBlock = blocks.get(actualIndex);
                
                if (isRightClick) {
                    // Удалить блок
                    script.removeBlock(selectedBlock);
                    player.sendMessage("§cБлок удален!");
                    updateInventory();
                } else {
                    // Редактировать блок
                    mode = EditorMode.BLOCK_EDIT;
                    updateInventory();
                }
            }
        }
    }
    
    private void handleBlockEditClick(int slot) {
        switch (slot) {
            case 40: // Сохранить
                plugin.getCodeManager().saveScript(player, script);
                player.sendMessage("§aИзменения сохранены!");
                mode = EditorMode.SCRIPT;
                updateInventory();
                break;
            case 42: // Удалить
                if (selectedBlock != null) {
                    script.removeBlock(selectedBlock);
                    player.sendMessage("§cБлок удален!");
                    mode = EditorMode.SCRIPT;
                    updateInventory();
                }
                break;
        }
    }
    
    private void handleNavigationClick(int slot) {
        switch (slot) {
            case 0: // Назад
                goBack();
                break;
            case 4: // Главное меню
                if (mode != EditorMode.MAIN) {
                    mode = EditorMode.MAIN;
                    updateInventory();
                }
                break;
            case 8: // Закрыть
                close();
                break;
            case 45: // Предыдущая страница
                if (page > 0) {
                    page--;
                    updateInventory();
                }
                break;
            case 53: // Следующая страница
                page++;
                updateInventory();
                break;
        }
    }
    
    private void goBack() {
        switch (mode) {
            case CATEGORIES:
                mode = EditorMode.MAIN;
                break;
            case BLOCKS:
                mode = EditorMode.CATEGORIES;
                break;
            case SCRIPT:
            case BLOCK_EDIT:
                mode = EditorMode.MAIN;
                break;
        }
        page = 0;
        updateInventory();
    }
    
    private void addBlockToScript(BlockType blockType) {
        try {
            // Создаем экземпляр блока
            CodeBlock block = createBlockInstance(blockType);
            if (block != null) {
                script.addBlock(block);
            }
        } catch (Exception e) {
            player.sendMessage("§cОшибка создания блока: " + e.getMessage());
        }
    }
    
    private CodeBlock createBlockInstance(BlockType blockType) {
        switch (blockType) {
            case PLAYER_EVENT:
                return new ru.openhousing.coding.blocks.events.PlayerEventBlock();
            case IF_PLAYER:
                return new ru.openhousing.coding.blocks.conditions.IfPlayerBlock();
            case PLAYER_ACTION:
                return new ru.openhousing.coding.blocks.actions.PlayerActionBlock();
            // Добавьте другие типы блоков по мере их создания
            default:
                return null;
        }
    }
    
    private int getCategoryIndexFromSlot(int slot) {
        if (slot >= 10 && slot <= 16) return (slot - 10) / 2;
        if (slot >= 19 && slot <= 25) return (slot - 19) / 2 + 4;
        return -1;
    }
    
    private int getBlockIndexFromSlot(int slot) {
        if (slot >= 10 && slot <= 16) return slot - 10;
        if (slot >= 19 && slot <= 25) return slot - 19 + 7;
        if (slot >= 28 && slot <= 34) return slot - 28 + 14;
        if (slot >= 37 && slot <= 43) return slot - 37 + 21;
        return -1;
    }
    
    @Override
    public Inventory getInventory() {
        return inventory;
    }
}
