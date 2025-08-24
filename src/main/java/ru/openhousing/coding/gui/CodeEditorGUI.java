package ru.openhousing.coding.gui;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import ru.openhousing.OpenHousing;
import ru.openhousing.coding.blocks.BlockType;
import ru.openhousing.coding.blocks.CodeBlock;
import ru.openhousing.coding.script.CodeScript;
import ru.openhousing.coding.script.CodeLine;
import ru.openhousing.utils.ItemBuilder;
import ru.openhousing.utils.AnvilGUIHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * GUI редактора кода
 */
public class CodeEditorGUI implements InventoryHolder, Listener {
    
    private final OpenHousing plugin;
    private final Player player;
    private final CodeScript script;
    private final Inventory inventory;
    
    // Состояние GUI
    private EditorMode mode;
    private BlockType.BlockCategory selectedCategory;
    private int page;
    private CodeBlock selectedBlock;
    private CodeLine currentTargetLine;
    
    public enum EditorMode {
        MAIN,           // Главное меню
        CATEGORIES,     // Категории блоков
        BLOCKS,         // Список блоков в категории
        SCRIPT,         // Просмотр кода
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
        plugin.getSoundEffects().playOpenGUI(player);
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }
    
    /**
     * Закрытие GUI
     */
    public void close() {
        HandlerList.unregisterAll(this);
        player.closeInventory();
        plugin.getSoundEffects().playCloseGUI(player);
    }
    
    /**
     * Получение скрипта
     */
    public CodeScript getScript() {
        return script;
    }
    
    /**
     * Получение текущей целевой строки
     */
    public CodeLine getCurrentTargetLine() {
        return currentTargetLine;
    }
    
    /**
     * Установка целевой строки
     */
    public void setCurrentTargetLine(CodeLine line) {
        this.currentTargetLine = line;
        if (mode == EditorMode.SCRIPT) {
            updateInventory();
        }
    }
    
    /**
     * Обновление инвентаря
     */
    public void updateInventory() {
        boolean debugMode = plugin.getConfigManager().getMainConfig().getBoolean("general.debug", false);
        
        try {
            if (debugMode) plugin.getLogger().info("[DEBUG] Updating inventory for mode: " + mode);
            inventory.clear();
            
            switch (mode) {
                case MAIN:
                    if (debugMode) plugin.getLogger().info("[DEBUG] Setting up main menu...");
                    setupMainMenu();
                    break;
                case CATEGORIES:
                    if (debugMode) plugin.getLogger().info("[DEBUG] Setting up categories menu...");
                    setupCategoriesMenu();
                    break;
                case BLOCKS:
                    if (debugMode) plugin.getLogger().info("[DEBUG] Setting up blocks menu for category: " + selectedCategory);
                    setupBlocksMenu();
                    break;
                case SCRIPT:
                    if (debugMode) plugin.getLogger().info("[DEBUG] Setting up script menu...");
                    setupScriptMenu();
                    break;
                case BLOCK_EDIT:
                    setupBlockEdit();
                    break;
            }
            
            if (debugMode) plugin.getLogger().info("[DEBUG] Adding navigation items...");
            addNavigationItems();
            
            if (debugMode) plugin.getLogger().info("[DEBUG] Inventory update completed successfully");
                
            // Принудительно обновляем инвентарь для игрока
            if (player.getOpenInventory().getTopInventory().equals(inventory)) {
                player.updateInventory();
            }
        } catch (Exception e) {
            plugin.getLogger().severe("Error updating inventory: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Обработчик кликов в инвентаре
     */
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getInventory().equals(inventory)) return;
        if (!event.getWhoClicked().equals(player)) return;
        
        event.setCancelled(true);
        
        int slot = event.getRawSlot();
        
        switch (mode) {
            case MAIN:
                handleMainMenuClick(slot);
                break;
            case CATEGORIES:
                handleCategoriesClick(slot);
                break;
            case BLOCKS:
                handleBlocksClick(slot);
                break;
            case SCRIPT:
                handleScriptClick(slot);
                break;
            case BLOCK_EDIT:
                handleBlockEditClick(slot);
                break;
        }
    }
    
    /**
     * Обработчик закрытия инвентаря
     */
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getInventory().equals(inventory) && event.getPlayer().equals(player)) {
            close();
        }
    }
    
    /**
     * Настройка главного меню
     */
    private void setupMainMenu() {
        // Информация о коде
        CodeScript.ScriptStats stats = script.getStats();
        
        inventory.setItem(10, new ItemBuilder(Material.BOOK)
            .name("§6Мой код")
            .lore(Arrays.asList(
                "§7Блоков: §f" + stats.getTotalBlocks(),
                "§7События: §f" + stats.getEventBlocks(),
                "§7Условия: §f" + stats.getConditionBlocks(),
                "§7Действия: §f" + stats.getActionBlocks(),
                "§7Функции: §f" + stats.getFunctionBlocks(),
                "§7Переменные: §f" + stats.getVariableCount(),
                "",
                stats.hasErrors() ? "§cЕсть ошибки!" : "§aВсе в порядке",
                "",
                "§eНажмите, чтобы просмотреть код"
            ))
            .build());
        
        // Добавить блок
        inventory.setItem(12, new ItemBuilder(Material.COMMAND_BLOCK)
            .name("§aДобавить блок")
            .lore(Arrays.asList(
                "§7Добавьте новый блок в ваш код",
                "",
                "§eНажмите, чтобы открыть категории"
            ))
            .build());
        
        // Выполнить код
        inventory.setItem(14, new ItemBuilder(Material.REPEATING_COMMAND_BLOCK)
            .name("§2Выполнить код")
            .lore(Arrays.asList(
                "§7Запустить выполнение вашего кода",
                "",
                script.isEnabled() ? "§aКод включен" : "§cКод отключен",
                "",
                "§eНажмите, чтобы выполнить"
            ))
            .build());
        
        // Поиск блоков
        inventory.setItem(16, new ItemBuilder(Material.SPYGLASS)
            .name("§eПоиск блоков")
            .lore(Arrays.asList(
                "§7Быстрый поиск блоков",
                "§7по названию или типу",
                "",
                "§eНажмите для поиска"
            ))
            .build());
            
        // Настройки
        inventory.setItem(28, new ItemBuilder(Material.REDSTONE)
            .name("§6Настройки кода")
            .lore(Arrays.asList(
                "§7Статус: " + (script.isEnabled() ? "§aВключен" : "§cОтключен"),
                "",
                "§7Включить/выключить код",
                "§7Очистить код",
                "§7Статистика и диагностика",
                "",
                "§eНажмите, чтобы открыть"
            ))
            .build());
        
        // Поделиться кодом
        inventory.setItem(30, new ItemBuilder(Material.PAPER)
            .name("§bПоделиться кодом")
            .lore(Arrays.asList(
                "§7Поделитесь своим кодом с другими игроками",
                "",
                "§eНажмите, чтобы создать ссылку"
            ))
            .build());
        
        // Импорт кода
        inventory.setItem(32, new ItemBuilder(Material.WRITTEN_BOOK)
            .name("§9Импорт кода")
            .lore(Arrays.asList(
                "§7Импортируйте код от другого игрока",
                "",
                "§eНажмите, чтобы ввести код"
            ))
            .build());
        
        // Очистить код
        inventory.setItem(33, new ItemBuilder(Material.BARRIER)
            .name("§cОчистить код")
            .lore(Arrays.asList(
                "§7Удалить все блоки из кода",
                "",
                "§c⚠ Это действие нельзя отменить!",
                "",
                "§eНажмите, чтобы очистить"
            ))
            .build());
        
        // Справка
        inventory.setItem(35, new ItemBuilder(Material.ENCHANTED_BOOK)
            .name("§eСправка")
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
                    "§eЛевый клик - добавить в код",
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
     * Настройка просмотра кода
     */
    private void setupScriptMenu() {
        // Статистика кода
        CodeScript.ScriptStats stats = script.getStats();
        inventory.setItem(4, new ItemBuilder(Material.BOOK)
            .name("§6Статистика кода")
            .lore(Arrays.asList(
                "§7Всего строк: §e" + script.getLines().size(),
                "§7Всего блоков: §e" + stats.getTotalBlocks(),
                "§7Блоков событий: §e" + stats.getEventBlocks(),
                "§7Блоков действий: §e" + stats.getActionBlocks(),
                "§7Блоков условий: §e" + stats.getConditionBlocks(),
                "§7Размер: " + (stats.getTotalBlocks() < 50 ? "§aНебольшой" : 
                               stats.getTotalBlocks() < 100 ? "§eСредний" : "§cБольшой")
            ))
            .build());
        
        // Кнопка добавления новой строки
        inventory.setItem(1, new ItemBuilder(Material.LIME_DYE)
            .name("§aДобавить строку")
            .lore(Arrays.asList(
                "§7Создать новую строку кода",
                "",
                "§eКлик для создания"
            ))
            .build());
            
        // Кнопка поиска блоков
        inventory.setItem(7, new ItemBuilder(Material.COMPASS)
            .name("§6Поиск блоков")
            .lore(Arrays.asList(
                "§7Поиск блоков по типу",
                "§7или параметрам",
                "",
                "§eВ разработке"
            ))
            .build());
            
        // Кнопка выбора целевой строки
        Material targetMaterial = currentTargetLine != null ? Material.GREEN_CONCRETE : Material.GRAY_CONCRETE;
        String targetName = currentTargetLine != null ? 
            "§aЦелевая строка: " + currentTargetLine.getLineNumber() : 
            "§7Выбрать целевую строку";
        List<String> targetLore = new ArrayList<>();
        if (currentTargetLine != null) {
            targetLore.add("§7Текущая цель: §e" + currentTargetLine.getName());
            targetLore.add("§7Блоков в строке: §e" + currentTargetLine.getBlockCount());
            targetLore.add("");
            targetLore.add("§eКлик для сброса");
        } else {
            targetLore.add("§7Выберите строку для добавления");
            targetLore.add("§7новых блоков");
            targetLore.add("");
            targetLore.add("§eКлик для выбора");
        }
        
        inventory.setItem(3, new ItemBuilder(targetMaterial)
            .name(targetName)
            .lore(targetLore)
            .build());
        
        // Отображение строк с пагинацией
        List<CodeLine> lines = script.getLines();
        int maxLines = 21; // 3 ряда по 7 строк
        int startIndex = page * maxLines;
        int endIndex = Math.min(startIndex + maxLines, lines.size());
        
        int slot = 18; // Начинаем с третьего ряда
        for (int i = startIndex; i < endIndex; i++) {
            CodeLine line = lines.get(i);
            
            Material lineMaterial = line.isEnabled() ? 
                (line.isEmpty() ? Material.PAPER : Material.WRITTEN_BOOK) : 
                Material.BOOK;
            
            // Анализируем связи строки
            List<String> lore = new ArrayList<>();
            lore.add("§7Номер: §f#" + line.getLineNumber());
            lore.add("§7Блоков: §f" + line.getBlockCount());
            lore.add("§7Состояние: " + (line.isEnabled() ? "§aВключена" : "§cВыключена"));
            lore.add("§7Описание: §f" + (line.getDescription().isEmpty() ? "Нет" : line.getDescription()));
            
            // Добавляем информацию о связях
            if (line.getBlockCount() > 0) {
                lore.add("");
                lore.add("§6Структура строки:");
                
                // Показываем первые несколько блоков
                for (int j = 0; j < Math.min(3, line.getBlocks().size()); j++) {
                    CodeBlock block = line.getBlocks().get(j);
                    String blockIcon = getBlockIcon(block.getType());
                    lore.add("§8" + (j + 1) + ". " + blockIcon + " §f" + block.getType().getDisplayName());
                }
                
                if (line.getBlocks().size() > 3) {
                    lore.add("§8... и еще " + (line.getBlocks().size() - 3) + " блоков");
                }
            }
            
            lore.add("");
            lore.add("§eЛевый клик - просмотр блоков");
            lore.add("§eПравый клик - настройки строки");
            lore.add("§eShift+Клик - выбрать как цель");
            
            ItemStack lineItem = new ItemBuilder(lineMaterial)
                .name("§e" + line.getName())
                .lore(lore)
                .build();
            
            inventory.setItem(slot, lineItem);
            slot++;
            
            // Пропускаем границы инвентаря
            if (slot == 26) slot = 27;
            if (slot == 35) slot = 36;
            if (slot == 44) slot = 45;
        }
        
        // Навигация по страницам
        if (page > 0) {
            inventory.setItem(45, new ItemBuilder(Material.ARROW)
                .name("§7Предыдущая страница")
                .build());
        }
        
        if (endIndex < lines.size()) {
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
        inventory.setItem(0, new ItemBuilder(Material.ARROW)
            .name("§cНазад")
            .lore("§7Вернуться назад")
            .build());
            
        // Переменные (железный слиток в 9 слот)
        inventory.setItem(8, new ItemBuilder(Material.IRON_INGOT)
            .name("§6Переменные")
            .lore(Arrays.asList(
                "§7Получить переменную для",
                "§7использования в коде",
                "",
                "§eКлик для открытия"
            ))
            .build());
        
        // Домой
        inventory.setItem(17, new ItemBuilder(Material.BARRIER)
            .name("§cЗакрыть")
            .lore("§7Закрыть редактор")
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
     * Обработка кликов в главном меню
     */
    private void handleMainMenuClick(int slot) {
        switch (slot) {
            case 10: // Мой код
                mode = EditorMode.SCRIPT;
                updateInventory();
                break;
            case 12: // Добавить блок
                mode = EditorMode.CATEGORIES;
                updateInventory();
                break;
            case 14: // Выполнить код
                executeScript();
                break;
            case 16: // Поиск блоков
                openBlockSearch();
                break;
            case 28: // Настройки кода
                openCodeSettings();
                break;
            case 30: // Поделиться кодом
                shareCode();
                break;
            case 32: // Импорт кода
                openDebugger();
                break;
            case 33: // Очистить код
                clearScript();
                break;
            case 35: // Справка
                openHelp();
                break;
        }
    }
    
    /**
     * Обработка кликов в меню категорий
     */
    private void handleCategoriesClick(int slot) {
        if (slot >= 9 && slot < 45) {
            int categoryIndex = slot - 9 + (page * 36);
            BlockType.BlockCategory[] categories = BlockType.BlockCategory.values();
            
            if (categoryIndex < categories.length) {
                selectedCategory = categories[categoryIndex];
                mode = EditorMode.BLOCKS;
                page = 0;
                updateInventory();
            }
        } else if (slot == 45) { // Предыдущая страница
            if (page > 0) {
                page--;
                updateInventory();
            }
        } else if (slot == 53) { // Следующая страница
            BlockType.BlockCategory[] categories = BlockType.BlockCategory.values();
            int maxPages = (categories.length - 1) / 36;
            if (page < maxPages) {
                page++;
                updateInventory();
            }
        }
    }
    
    /**
     * Обработка кликов в меню блоков
     */
    private void handleBlocksClick(int slot) {
        if (slot >= 9 && slot < 45) {
            int blockIndex = slot - 9 + (page * 36);
            List<BlockType> blocksInCategory = getBlocksInCategory(selectedCategory);
            
            if (blockIndex < blocksInCategory.size()) {
                BlockType blockType = blocksInCategory.get(blockIndex);
                addBlockToScript(blockType);
            }
        } else if (slot == 45) { // Предыдущая страница
            if (page > 0) {
                page--;
                updateInventory();
            }
        } else if (slot == 53) { // Следующая страница
            List<BlockType> blocksInCategory = getBlocksInCategory(selectedCategory);
            int maxPages = (blocksInCategory.size() - 1) / 36;
            if (page < maxPages) {
                page++;
                updateInventory();
            }
        }
    }
    
    /**
     * Обработка кликов в меню скрипта
     */
    private void handleScriptClick(int slot) {
        if (slot >= 9 && slot < 45) {
            int lineIndex = slot - 9 + (page * 36);
            List<CodeLine> lines = script.getLines();
            
            if (lineIndex < lines.size()) {
                CodeLine line = lines.get(lineIndex);
                openLineEditor(line);
            }
        } else if (slot == 45) { // Предыдущая страница
            if (page > 0) {
                page--;
                updateInventory();
            }
        } else if (slot == 53) { // Следующая страница
            List<CodeLine> lines = script.getLines();
            int maxPages = (lines.size() - 1) / 36;
            if (page < maxPages) {
                page++;
                updateInventory();
            }
        }
    }
    
    /**
     * Обработка кликов в редакторе блоков
     */
    private void handleBlockEditClick(int slot) {
        // Обработка редактирования блока
        if (selectedBlock != null) {
            openBlockConfig(selectedBlock);
        }
    }
    
    /**
     * Выполнение скрипта
     */
    public void executeScript() {
        try {
            script.execute(new CodeBlock.ExecutionContext(player));
            player.sendMessage("§aКод выполнен успешно!");
        } catch (Exception e) {
            player.sendMessage("§cОшибка выполнения кода: " + e.getMessage());
            plugin.getLogger().warning("Error executing script for " + player.getName() + ": " + e.getMessage());
        }
    }
    
    /**
     * Открытие поиска блоков
     */
    public void openBlockSearch() {
        // TODO: Реализовать поиск блоков
        player.sendMessage("§eПоиск блоков (в разработке)");
    }
    
    /**
     * Открытие настроек кода
     */
    public void openCodeSettings() {
        // TODO: Реализовать настройки кода
        player.sendMessage("§eНастройки кода (в разработке)");
    }
    
    /**
     * Поделиться кодом
     */
    public void shareCode() {
        // TODO: Реализовать функцию "поделиться кодом"
        player.sendMessage("§eФункция 'Поделиться кодом' (в разработке)");
    }
    
    /**
     * Открытие отладчика
     */
    private void openDebugger() {
        close();
        CodeBlock.ExecutionContext context = new CodeBlock.ExecutionContext(player);
        new DebugGUI(plugin, player, context).open();
    }
    
    /**
     * Открытие справки
     */
    public void openHelp() {
        // TODO: Реализовать справку
        player.sendMessage("§eСправка (в разработке)");
    }
    
    /**
     * Очистка скрипта
     */
    public void clearScript() {
        script.clear();
        player.sendMessage("§aКод очищен!");
        updateInventory();
    }
    
    /**
     * Открытие редактора строки
     */
    public void openLineEditor(CodeLine line) {
        close();
        new LineSelectorGUI(plugin, player, script, null).open();
    }
    
    /**
     * Открытие конфигурации блока
     */
    public void openBlockConfig(CodeBlock block) {
        close();
        new BlockConfigGUI(plugin, player, block, (savedBlock) -> {
            // Callback после сохранения
            updateInventory();
        }).open();
    }
    
    /**
     * Добавление блока в скрипт
     */
    public void addBlockToScript(BlockType blockType) {
        try {
            CodeBlock newBlock = ru.openhousing.coding.blocks.CodeBlockFactory.createBlock(blockType);
            
            if (currentTargetLine != null) {
                // Добавляем в выбранную строку
                currentTargetLine.addBlock(newBlock);
                player.sendMessage("§aБлок '" + blockType.getDisplayName() + "' добавлен в строку '" + currentTargetLine.getName() + "'");
            } else {
                // Создаем новую строку
                CodeLine newLine = new CodeLine(script.getLines().size() + 1, "Новая строка " + (script.getLines().size() + 1));
                newLine.addBlock(newBlock);
                script.addLine(newLine);
                player.sendMessage("§aСоздана новая строка с блоком '" + blockType.getDisplayName() + "'");
            }
            
            // Сохраняем скрипт
            plugin.getDatabaseManager().saveCodeScriptAsync(script, () -> {});
            
            // Возвращаемся к просмотру скрипта
            mode = EditorMode.SCRIPT;
            updateInventory();
            
        } catch (Exception e) {
            player.sendMessage("§cОшибка создания блока: " + e.getMessage());
            plugin.getLogger().warning("Error creating block " + blockType + " for " + player.getName() + ": " + e.getMessage());
        }
    }
    
    /**
     * Получение блоков в категории
     */
    private List<BlockType> getBlocksInCategory(BlockType.BlockCategory category) {
        List<BlockType> blocks = new ArrayList<>();
        for (BlockType type : BlockType.values()) {
            if (type.getCategory() == category) {
                blocks.add(type);
            }
        }
        return blocks;
    }
    
    /**
     * Получить иконку для типа блока
     */
    private String getBlockIcon(BlockType blockType) {
        return switch (blockType.getCategory()) {
            case EVENT -> "⚡";
            case ACTION -> "🔧";
            case CONDITION -> "❓";
            case CONTROL -> "🔄";
            case UTILITY -> "📦";
            case FUNCTION -> "📋";
            default -> "▪";
        };
    }
    
    /**
     * Получить материал для типа блока
     */
    private Material getBlockTypeMaterial(BlockType blockType) {
        return switch (blockType.getCategory()) {
            case EVENT -> Material.LIGHTNING_ROD;
            case ACTION -> Material.PISTON;
            case CONDITION -> Material.COMPARATOR;
            case CONTROL -> Material.REPEATER;
            case UTILITY -> Material.CHEST;
            case FUNCTION -> Material.BOOK;
            default -> Material.STONE;
        };
    }
    
    /**
     * Показать результаты поиска блоков
     */
    public void showSearchResults(List<BlockType> foundBlocks, String searchTerm) {
        Inventory searchInventory = Bukkit.createInventory(null, 54, "§6Результаты поиска: " + searchTerm);
        
        int slot = 10;
        for (BlockType blockType : foundBlocks) {
            if (slot >= 44) break; // Ограничиваем количество результатов
            
            Material material = getBlockTypeMaterial(blockType);
            searchInventory.setItem(slot, new ItemBuilder(material)
                .name("§e" + blockType.getDisplayName())
                .lore(Arrays.asList(
                    "§7" + blockType.getDescription(),
                    "§7Категория: §f" + blockType.getCategory().getDisplayName(),
                    "",
                    "§eЛевый клик - добавить в текущую строку",
                    "§eПравый клик - создать новую строку"
                ))
                .build());
                
            slot++;
            if (slot == 17) slot = 19; // Пропускаем ряд
            if (slot == 26) slot = 28;
            if (slot == 35) slot = 37;
        }
        
        // Кнопка назад
        searchInventory.setItem(49, new ItemBuilder(Material.ARROW)
            .name("§7Назад к редактору")
            .build());
            
        player.openInventory(searchInventory);
    }
    
    @Override
    public Inventory getInventory() {
        return inventory;
    }
    
    // Геттеры для тестирования
    public Player getPlayer() {
        return player;
    }
    
    public EditorMode getMode() {
        return mode;
    }
    
    public void setMode(EditorMode mode) {
        this.mode = mode;
    }
    
    public int getPage() {
        return page;
    }
    
    public void setPage(int page) {
        this.page = page;
    }
    
    public BlockType.BlockCategory getSelectedCategory() {
        return selectedCategory;
    }
    
    public void setSelectedCategory(BlockType.BlockCategory category) {
        this.selectedCategory = category;
    }
}

