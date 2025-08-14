package ru.openhousing.coding.gui;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import ru.openhousing.OpenHousing;
import ru.openhousing.coding.blocks.BlockType;
import ru.openhousing.coding.blocks.CodeBlock;
import ru.openhousing.coding.script.CodeLine;
import ru.openhousing.coding.script.CodeScript;
import ru.openhousing.utils.ItemBuilder;
import ru.openhousing.utils.MessageUtil;

import java.util.*;

/**
 * GUI для выбора строки при добавлении блока
 */
public class LineSelectorGUI implements Listener {
    
    private final OpenHousing plugin;
    private final Player player;
    private final CodeScript script;
    private final BlockType blockTypeToAdd;
    private final Inventory inventory;
    private int page = 0;
    private static final int MAX_LINES_PER_PAGE = 28;
    
    public LineSelectorGUI(OpenHousing plugin, Player player, CodeScript script, BlockType blockTypeToAdd) {
        this.plugin = plugin;
        this.player = player;
        this.script = script;
        this.blockTypeToAdd = blockTypeToAdd;
        this.inventory = Bukkit.createInventory(null, 54, "§6Выбор строки для блока");
        
        setupGUI();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }
    
    /**
     * Открытие GUI
     */
    public void open() {
        player.openInventory(inventory);
    }
    
    /**
     * Настройка GUI
     */
    private void setupGUI() {
        inventory.clear();
        
        // Заголовок
        inventory.setItem(4, new ItemBuilder(blockTypeToAdd.getMaterial())
            .name("§6Добавление блока: " + blockTypeToAdd.getDisplayName())
            .lore(Arrays.asList(
                "§7" + blockTypeToAdd.getDescription(),
                "",
                "§eВыберите строку для добавления блока"
            ))
            .build());
        
        // Получаем строки скрипта
        List<CodeLine> lines = new ArrayList<>();
        for (int i = 1; i <= 50; i++) { // максимум 50 строк
            CodeLine line = script.getLine(i);
            if (line != null) {
                lines.add(line);
            }
        }
        
        // Кнопка создания новой строки
        inventory.setItem(8, new ItemBuilder(Material.LIME_CONCRETE)
            .name("§a+ Создать новую строку")
            .lore(Arrays.asList(
                "§7Создать новую строку и добавить",
                "§7блок в неё",
                "",
                "§eНажмите для создания"
            ))
            .build());
        
        // Отображение строк
        displayLines(lines);
        
        // Навигация
        setupNavigation(lines.size());
        
        // Кнопка отмены
        inventory.setItem(49, new ItemBuilder(Material.BARRIER)
            .name("§cОтмена")
            .lore("§7Закрыть меню без добавления блока")
            .build());
    }
    
    /**
     * Отображение строк
     */
    private void displayLines(List<CodeLine> lines) {
        int startIndex = page * MAX_LINES_PER_PAGE;
        int endIndex = Math.min(startIndex + MAX_LINES_PER_PAGE, lines.size());
        
        int slot = 18; // Начинаем с третьей строки
        for (int i = startIndex; i < endIndex; i++) {
            CodeLine line = lines.get(i);
            
            // Выбираем материал в зависимости от состояния строки
            Material material = line.isEnabled() ? Material.PAPER : Material.GRAY_DYE;
            if (line.isEmpty()) {
                material = Material.WHITE_DYE;
            } else if (line.getBlockCount() > 10) {
                material = Material.ORANGE_DYE;
            }
            
            List<String> lore = new ArrayList<>();
            lore.add("§7Номер: §e" + line.getLineNumber());
            lore.add("§7Блоков: §e" + line.getBlockCount());
            lore.add("§7Состояние: " + (line.isEnabled() ? "§aВключена" : "§cВыключена"));
            
            if (!line.getDescription().isEmpty()) {
                lore.add("§7Описание: §f" + line.getDescription());
            }
            
            lore.add("");
            
            if (line.isEmpty()) {
                lore.add("§7Строка пуста");
            } else {
                lore.add("§7Блоки в строке:");
                List<CodeBlock> blocks = line.getBlocks();
                for (int j = 0; j < Math.min(blocks.size(), 3); j++) {
                    CodeBlock block = blocks.get(j);
                    lore.add("§8  " + (j + 1) + ". §f" + block.getType().getDisplayName());
                }
                if (blocks.size() > 3) {
                    lore.add("§8  ... и еще " + (blocks.size() - 3) + " блоков");
                }
            }
            
            lore.add("");
            lore.add("§eЛевый клик - добавить блок в эту строку");
            lore.add("§eПравый клик - настройки строки");
            
            inventory.setItem(slot, new ItemBuilder(material)
                .name("§6" + line.getName())
                .lore(lore)
                .build());
            
            slot++;
            if (slot == 26) slot = 27; // Переход на следующую строку
            if (slot == 35) slot = 36;
            if (slot >= 44) break;
        }
    }
    
    /**
     * Настройка навигации
     */
    private void setupNavigation(int totalLines) {
        int totalPages = (int) Math.ceil((double) totalLines / MAX_LINES_PER_PAGE);
        
        // Предыдущая страница
        if (page > 0) {
            inventory.setItem(45, new ItemBuilder(Material.ARROW)
                .name("§7◀ Предыдущая страница")
                .lore("§7Страница " + page + " из " + totalPages)
                .build());
        }
        
        // Следующая страница
        if (page < totalPages - 1) {
            inventory.setItem(53, new ItemBuilder(Material.ARROW)
                .name("§7Следующая страница ▶")
                .lore("§7Страница " + (page + 2) + " из " + totalPages)
                .build());
        }
        
        // Информация о странице
        inventory.setItem(49, new ItemBuilder(Material.BOOK)
            .name("§6Страница " + (page + 1) + " из " + Math.max(1, totalPages))
            .lore(Arrays.asList(
                "§7Всего строк: §e" + totalLines,
                "§7На странице: §e" + Math.min(MAX_LINES_PER_PAGE, totalLines - page * MAX_LINES_PER_PAGE)
            ))
            .build());
    }
    
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getView().getTitle().equals("§6Выбор строки для блока")) {
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
        
        // Обработка кликов
        switch (slot) {
            case 8: // Создать новую строку
                createNewLineAndAddBlock();
                break;
                
            case 45: // Предыдущая страница
                if (page > 0) {
                    page--;
                    setupGUI();
                }
                break;
                
            case 53: // Следующая страница
                page++;
                setupGUI();
                break;
                
            case 49: // Отмена
                player.closeInventory();
                break;
                
            default:
                // Клик по строке
                if (slot >= 18 && slot < 45) {
                    handleLineClick(slot, event.isRightClick());
                }
                break;
        }
    }
    
    /**
     * Обработка клика по строке
     */
    private void handleLineClick(int slot, boolean isRightClick) {
        // Вычисляем индекс строки
        int relativeSlot = slot - 18;
        if (relativeSlot >= 9) relativeSlot -= 9; // Учитываем пропуски
        if (relativeSlot >= 18) relativeSlot -= 9;
        
        int lineIndex = page * MAX_LINES_PER_PAGE + relativeSlot;
        
        // Получаем список строк
        List<CodeLine> lines = new ArrayList<>();
        for (int i = 1; i <= 50; i++) {
            CodeLine line = script.getLine(i);
            if (line != null) {
                lines.add(line);
            }
        }
        
        if (lineIndex >= 0 && lineIndex < lines.size()) {
            CodeLine line = lines.get(lineIndex);
            
            if (isRightClick) {
                // Настройки строки
                openLineSettings(line);
            } else {
                // Добавить блок в строку
                addBlockToLine(line);
            }
        }
    }
    
    /**
     * Создание новой строки и добавление блока
     */
    private void createNewLineAndAddBlock() {
        try {
            CodeLine newLine = script.createLine();
            addBlockToLine(newLine);
        } catch (Exception e) {
            MessageUtil.send(player, "&cОшибка создания строки: " + e.getMessage());
        }
    }
    
    /**
     * Добавление блока в строку
     */
    private void addBlockToLine(CodeLine line) {
        try {
            // Создаем экземпляр блока
            CodeBlock block = createBlockInstance(blockTypeToAdd);
            if (block != null) {
                line.addBlock(block);
                player.closeInventory();
                MessageUtil.send(player, "&aБлок §e" + blockTypeToAdd.getDisplayName() + 
                    " &aдобавлен в строку §e" + line.getName());
                
                // TODO: Открыть конфигурацию блока когда будет реализовано
                // BlockConfigGUI configGUI = new BlockConfigGUI(plugin, player, block);
                // configGUI.open();
            } else {
                MessageUtil.send(player, "&cОшибка создания блока!");
            }
        } catch (Exception e) {
            MessageUtil.send(player, "&cОшибка добавления блока: " + e.getMessage());
        }
    }
    
    /**
     * Открытие настроек строки
     */
    private void openLineSettings(CodeLine line) {
        // TODO: Создать GUI для настроек строки
        MessageUtil.send(player, "&eНастройки строки будут добавлены в следующем обновлении!");
    }
    
    /**
     * Создание экземпляра блока
     */
    private CodeBlock createBlockInstance(BlockType blockType) {
        try {
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
                
                // Математика и текст
                case MATH:
                    return new ru.openhousing.coding.blocks.math.MathBlock();
                case TEXT_OPERATION:
                    return new ru.openhousing.coding.blocks.text.TextOperationBlock();
                
                // Инвентарь
                case INVENTORY_ACTION:
                    return new ru.openhousing.coding.blocks.inventory.InventoryActionBlock();
                case ITEM_CHECK:
                    return new ru.openhousing.coding.blocks.inventory.ItemCheckBlock();
                
                default:
                    return null;
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Ошибка создания блока " + blockType + ": " + e.getMessage());
            return null;
        }
    }
    
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getView().getTitle().equals("§6Выбор строки для блока") && 
            event.getPlayer().equals(player)) {
            // Отменяем регистрацию листенера
            InventoryClickEvent.getHandlerList().unregister(this);
            InventoryCloseEvent.getHandlerList().unregister(this);
        }
    }
}
