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
import ru.openhousing.coding.script.CodeLine;
import ru.openhousing.utils.ItemBuilder;
import ru.openhousing.utils.AnvilGUIHelper;

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
    }
    
    /**
     * Закрытие GUI
     */
    public void close() {
        player.closeInventory();
    }
    
    /**
     * Обновление инвентаря
     */
    public void updateInventory() {
        try {
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
                    setupScriptMenu();
                    break;
                case BLOCK_EDIT:
                    setupBlockEdit();
                    break;
            }
            
            addNavigationItems();
            
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
        inventory.setItem(28, new ItemBuilder(Material.PAPER)
            .name("§bПоделиться кодом")
            .lore(Arrays.asList(
                "§7Поделитесь своим кодом с другими игроками",
                "",
                "§eНажмите, чтобы создать ссылку"
            ))
            .build());
        
        // Импорт кода
        inventory.setItem(30, new ItemBuilder(Material.WRITTEN_BOOK)
            .name("§9Импорт кода")
            .lore(Arrays.asList(
                "§7Импортируйте код от другого игрока",
                "",
                "§eНажмите, чтобы ввести код"
            ))
            .build());
        
        // Очистить код
        inventory.setItem(32, new ItemBuilder(Material.BARRIER)
            .name("§cОчистить код")
            .lore(Arrays.asList(
                "§7Удалить все блоки из кода",
                "",
                "§c⚠ Это действие нельзя отменить!",
                "",
                "§eНажмите, чтобы очистить"
            ))
            .build());
        
        // Запуск кода
        inventory.setItem(33, new ItemBuilder(Material.EMERALD_BLOCK)
            .name("§aЗапустить код")
            .lore(Arrays.asList(
                "§7Выполнить созданный код",
                "§7немедленно",
                "",
                "§aНажмите для запуска"
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
     * Настройка просмотра кода по строкам
     */
    private void setupScriptView() {
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
            lore.add("§eShift+Клик - добавить блок");
            
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
     * Обработка клика по слоту
     */
    public void handleClick(int slot, boolean isRightClick, boolean isShiftClick) {
        ItemStack item = inventory.getItem(slot);
        if (item == null || item.getType() == Material.AIR) {
            plugin.getLogger().info("Click on empty slot: " + slot);
            return;
        }
        
        plugin.getLogger().info("Handling click in CodeEditorGUI: slot=" + slot + ", mode=" + mode + ", item=" + item.getType());
        
        switch (mode) {
            case MAIN:
                plugin.getLogger().info("Handling main menu click");
                handleMainMenuClick(slot);
                break;
            case CATEGORIES:
                plugin.getLogger().info("Handling categories click");
                handleCategoriesClick(slot);
                break;
            case BLOCKS:
                plugin.getLogger().info("Handling blocks click");
                handleBlocksClick(slot, isRightClick);
                break;
            case SCRIPT:
                plugin.getLogger().info("Handling script click");
                handleScriptClick(slot, isRightClick);
                break;
            case BLOCK_EDIT:
                plugin.getLogger().info("Handling block edit click");
                handleBlockEditClick(slot);
                break;
        }
        
        // Общие навигационные элементы
        handleNavigationClick(slot);
    }
    
    private void handleMainMenuClick(int slot) {
        plugin.getLogger().info("Main menu click: slot=" + slot);
        
        switch (slot) {
            case 10: // Просмотр кода
                plugin.getLogger().info("Switching to SCRIPT mode");
                mode = EditorMode.SCRIPT;
                page = 0;
                updateInventory();
                break;
            case 12: // Добавить блок
                plugin.getLogger().info("Switching to CATEGORIES mode");
                mode = EditorMode.CATEGORIES;
                updateInventory();
                break;
            case 14: // Выполнить код
                plugin.getLogger().info("Executing script");
                plugin.getCodeManager().executeScript(player);
                player.sendMessage("§aКод выполнен!");
                break;
            case 16: // Поиск блоков
                openBlockSearch();
                break;
            case 28: // Настройки
                openScriptSettings();
                break;
            case 32: // Очистить код
                plugin.getLogger().info("Clearing script");
                script.clear();
                player.sendMessage("§cКод очищен!");
                updateInventory();
                break;
            case 33: // Запустить код
                plugin.getLogger().info("Running script immediately");
                plugin.getCodeManager().executeScript(player);
                player.sendMessage("§aКод запущен!");
                break;
            default:
                plugin.getLogger().info("Unhandled main menu slot: " + slot);
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
        try {
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
                        // Добавить блок напрямую в первую доступную строку
                        addBlockToScript(blockType);
                    }
                }
            }
        } catch (Exception e) {
            plugin.getLogger().severe("Error in handleBlocksClick: " + e.getMessage());
            e.printStackTrace();
            player.sendMessage("§cОшибка при добавлении блока!");
        }
    }
    
    /**
     * Добавление блока в скрипт
     */
    private void addBlockToScript(BlockType blockType) {
        try {
            // Создаем блок
            CodeBlock block = createBlockInstance(blockType);
            if (block == null) {
                player.sendMessage("§cНе удалось создать блок типа: " + blockType.getDisplayName());
                return;
            }
            
            // Если нет строк, создаем первую
            if (script.getLines().isEmpty()) {
                script.createLine("Строка 1");
            }
            
            // Добавляем в первую строку
            CodeLine firstLine = script.getLines().get(0);
            script.addBlockToLine(firstLine.getLineNumber(), block);
            
            player.sendMessage("§aБлок добавлен: §f" + blockType.getDisplayName());
            
            // Возвращаемся к просмотру скрипта
            mode = EditorMode.SCRIPT;
            page = 0;
            updateInventory();
            
        } catch (Exception e) {
            plugin.getLogger().severe("Error adding block to script: " + e.getMessage());
            e.printStackTrace();
            player.sendMessage("§cОшибка при добавлении блока в скрипт!");
        }
    }
    
    private void handleScriptClick(int slot, boolean isRightClick) {
        // Обработка кликов по строкам
        if (slot == 1) { // Добавить новую строку
            String lineName = "Строка " + (script.getLines().size() + 1);
            script.createLine(lineName);
            player.sendMessage("§aСоздана новая строка: " + lineName);
            updateInventory();
            return;
        }
        
        // Проверяем клик по строке (слоты 18-44)
        if (slot >= 18 && slot <= 44) {
            int lineIndex = -1;
            
            // Вычисляем индекс строки из слота
            if (slot >= 18 && slot <= 25) {
                lineIndex = slot - 18;
            } else if (slot >= 27 && slot <= 34) {
                lineIndex = 8 + (slot - 27);
            } else if (slot >= 36 && slot <= 43) {
                lineIndex = 16 + (slot - 36);
            }
            
            if (lineIndex >= 0) {
                int actualIndex = page * 21 + lineIndex; // 21 строк на страницу
                List<CodeLine> lines = script.getLines();
            
                if (actualIndex < lines.size()) {
                    CodeLine selectedLine = lines.get(actualIndex);
                    
                    if (isRightClick) {
                        // Открыть настройки строки
                        player.closeInventory();
                        LineSettingsGUI settingsGUI = new LineSettingsGUI(plugin, player, script, selectedLine);
                        settingsGUI.open();
                    } else {
                        // Просмотр блоков в строке
                        player.closeInventory();
                        LineBlocksGUI lineBlocksGUI = new LineBlocksGUI(plugin, player, script, selectedLine);
                        lineBlocksGUI.open();
                    }
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
                break;
            case 42: // Удалить
                if (selectedBlock != null) {
                    // Удалить блок - найти его строку и удалить
                    boolean removed = false;
                    for (CodeLine line : script.getLines()) {
                        if (script.removeBlockFromLine(line.getLineNumber(), selectedBlock)) {
                            removed = true;
                            break;
                        }
                    }
                    if (removed) {
                        player.sendMessage("§cБлок удален!");
                    } else {
                        player.sendMessage("§cНе удалось найти блок для удаления!");
                    }
                    mode = EditorMode.SCRIPT;
                    updateInventory();
                }
                break;
        }
    }
    
    /**
     * Настройка просмотра кода
     */
    private void setupScriptMenu() {
        setupScriptView(); // Перенаправляем на новый метод
    }

    /**
     * Открытие селектора переменных
     */
    private void openVariableSelector() {
        try {
            // Закрываем текущий GUI перед открытием нового
            player.closeInventory();
            
            // Небольшая задержка для корректного закрытия
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                try {
                    // Предполагается, что VariableSelectorGUI существует
                    // VariableSelectorGUI variableSelector = new VariableSelectorGUI(plugin, player, script);
                    // variableSelector.open();
                    player.sendMessage("§eОткрытие выбора переменных...");
                } catch (Exception e) {
                    plugin.getLogger().severe("Error opening variable selector: " + e.getMessage());
                    e.printStackTrace();
                    player.sendMessage("§cОшибка открытия выбора переменных!");
                    // Возвращаемся к редактору кода
                    Bukkit.getScheduler().runTaskLater(plugin, this::open, 1L);
                }
            }, 1L);
        } catch (Exception e) {
            plugin.getLogger().severe("Error in openVariableSelector: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void handleNavigationClick(int slot) {
        try {
            switch (slot) {
                case 0: // Назад
                    switch (mode) {
                        case CATEGORIES:
                            mode = EditorMode.MAIN;
                            break;
                        case BLOCKS:
                            mode = EditorMode.CATEGORIES;
                            break;
                        case SCRIPT:
                            mode = EditorMode.MAIN;
                            break;
                        case BLOCK_EDIT:
                            mode = EditorMode.SCRIPT;
                            break;
                        default:
                            return;
                    }
                    page = 0;
                    updateInventory();
                    break;
                case 17: // Закрыть
                    player.closeInventory();
                    break;
                case 8: // Переменные
                    openVariableSelector();
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
                case 46: // Категории
                    mode = EditorMode.CATEGORIES;
                    page = 0;
                    updateInventory();
                    break;
                case 47: // Скрипт
                    mode = EditorMode.SCRIPT;
                    page = 0;
                    updateInventory();
                    break;
                case 48: // Редактирование блока
                    mode = EditorMode.MAIN;
                    page = 0;
                    updateInventory();
                    break;
            }
        } catch (Exception e) {
            plugin.getLogger().severe("Error in navigation click: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Открытие селектора строк для добавления блока
     */
    private void openLineSelectorForBlock(BlockType blockType) {
        try {
            // Закрываем текущий GUI перед открытием нового
            player.closeInventory();
            
            // Небольшая задержка для корректного закрытия
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                try {
                    LineSelectorGUI lineSelector = new LineSelectorGUI(plugin, player, script, blockType);
                    lineSelector.open();
                } catch (Exception e) {
                    plugin.getLogger().severe("Error opening line selector: " + e.getMessage());
                    e.printStackTrace();
                    player.sendMessage("§cОшибка открытия выбора строки!");
                    // Возвращаемся к редактору кода
                    Bukkit.getScheduler().runTaskLater(plugin, () -> {
                        plugin.getCodeManager().openCodeEditor(player);
                    }, 1L);
                }
            }, 1L);
        } catch (Exception e) {
            plugin.getLogger().severe("Error in openLineSelectorForBlock: " + e.getMessage());
            e.printStackTrace();
            player.sendMessage("§cОшибка открытия выбора строки!");
        }
    }
    
    private CodeBlock createBlockInstance(BlockType blockType) {
        switch (blockType) {
            // События
            case PLAYER_EVENT:
                return new ru.openhousing.coding.blocks.events.PlayerEventBlock();
            case ENTITY_EVENT:
                return new ru.openhousing.coding.blocks.events.EntityEventBlock();
            case WORLD_EVENT:
                return new ru.openhousing.coding.blocks.events.WorldEventBlock();
            
            // Условия
            case IF_PLAYER:
                return new ru.openhousing.coding.blocks.conditions.IfPlayerBlock();
            case IF_ENTITY:
                return new ru.openhousing.coding.blocks.conditions.IfEntityBlock();
            case IF_VARIABLE:
                return new ru.openhousing.coding.blocks.conditions.IfVariableBlock();
            
            // Действия
            case PLAYER_ACTION:
                return new ru.openhousing.coding.blocks.actions.PlayerActionBlock();
            case ENTITY_ACTION:
                return new ru.openhousing.coding.blocks.actions.EntityActionBlock();
            case WORLD_ACTION:
                return new ru.openhousing.coding.blocks.actions.WorldActionBlock();
            case VARIABLE_ACTION:
                return new ru.openhousing.coding.blocks.variables.VariableActionBlock();
            
            // Функции
            case FUNCTION:
                return new ru.openhousing.coding.blocks.functions.FunctionBlock();
            case CALL_FUNCTION:
                return new ru.openhousing.coding.blocks.functions.CallFunctionBlock();
            
                                    // Управление
                        case REPEAT:
                            return new ru.openhousing.coding.blocks.control.RepeatBlock();
                        case ELSE:
                            return new ru.openhousing.coding.blocks.control.ElseBlock();
                                    case TARGET:
                return new ru.openhousing.coding.blocks.control.TargetBlock();
            
            // Математика и утилиты
            case MATH:
                return new ru.openhousing.coding.blocks.math.MathBlock();
            case TEXT_OPERATION:
                return new ru.openhousing.coding.blocks.text.TextOperationBlock();
            case INVENTORY_ACTION:
                return new ru.openhousing.coding.blocks.inventory.InventoryActionBlock();
            case ITEM_CHECK:
                return new ru.openhousing.coding.blocks.inventory.ItemCheckBlock();
            
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
    
    /**
     * Открыть поиск блоков
     */
    private void openBlockSearch() {
        player.closeInventory();
        AnvilGUIHelper.openTextInput(plugin, player, "Поиск блоков", "", (searchTerm) -> {
            // Создаем список найденных блоков
            List<BlockType> foundBlocks = new ArrayList<>();
            
            for (BlockType blockType : BlockType.values()) {
                if (blockType.getDisplayName().toLowerCase().contains(searchTerm.toLowerCase()) ||
                    blockType.name().toLowerCase().contains(searchTerm.toLowerCase()) ||
                    blockType.getDescription().toLowerCase().contains(searchTerm.toLowerCase())) {
                    foundBlocks.add(blockType);
                }
            }
            
            if (foundBlocks.isEmpty()) {
                player.sendMessage("§cПо запросу '" + searchTerm + "' ничего не найдено");
                this.open();
                return;
            }
            
            // Показываем результаты поиска в GUI
            showSearchResults(foundBlocks, searchTerm);
        });
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
    private void showSearchResults(List<BlockType> foundBlocks, String searchTerm) {
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
    
    /**
     * Открыть настройки кода
     */
    private void openScriptSettings() {
        player.sendMessage("§6=== Настройки кода ===");
        player.sendMessage("§7Статус: " + (script.isEnabled() ? "§aВключен" : "§cВыключен"));
        
        CodeScript.ScriptStats stats = script.getStats();
        player.sendMessage("§7Блоков: §f" + stats.getTotalBlocks());
        player.sendMessage("§7Строк: §f" + script.getLines().size());
        player.sendMessage("§7Состояние: " + (stats.hasErrors() ? "§cЕсть ошибки" : "§aВсе в порядке"));
        
        this.open();
    }
    

    
    @Override
    public Inventory getInventory() {
        return inventory;
    }
}

