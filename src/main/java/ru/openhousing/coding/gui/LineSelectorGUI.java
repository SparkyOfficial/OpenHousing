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
        if (blockTypeToAdd != null) {
            inventory.setItem(4, new ItemBuilder(blockTypeToAdd.getMaterial())
                .name("§6Добавление блока: " + blockTypeToAdd.getDisplayName())
                .lore(Arrays.asList(
                    "§7" + blockTypeToAdd.getDescription(),
                    "",
                    "§eВыберите строку для добавления блока"
                ))
                .build());
        } else {
            inventory.setItem(4, new ItemBuilder(Material.PAPER)
                .name("§6Управление строками")
                .lore(Arrays.asList(
                    "§7Просмотр и редактирование",
                    "§7строк вашего кода",
                    "",
                    "§eВыберите строку для настройки"
                ))
                .build());
        }
        
        // Получаем строки кода
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
        String title = null;
        try {
            title = event.getView().getTitle();
        } catch (NoSuchMethodError e) {
            return; // Пропускаем если не можем получить title
        }
        
        if (title == null || !title.equals("§6Выбор строки для блока")) {
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
                
                // Открываем конфигурацию блока
                try {
                    // Создаем временный CodeEditorGUI для совместимости
                    ru.openhousing.coding.gui.CodeEditorGUI tempEditor = 
                        new ru.openhousing.coding.gui.CodeEditorGUI(plugin, player, script);
                    
                    BlockConfigGUI configGUI = new BlockConfigGUI(plugin, player, block, tempEditor);
                    configGUI.open();
                } catch (Exception e) {
                    MessageUtil.send(player, "&cОшибка открытия настроек блока: " + e.getMessage());
                }
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
        player.closeInventory();
        LineSettingsGUI settingsGUI = new LineSettingsGUI(plugin, player, script, line);
        settingsGUI.open();
    }
    
    /**
     * Создание экземпляра блока
     */
    private CodeBlock createBlockInstance(BlockType blockType) {
        try {
            switch (blockType) {
                // События игрока
                case PLAYER_JOIN:
                case PLAYER_QUIT:
                case PLAYER_CHAT:
                case PLAYER_COMMAND:
                case PLAYER_MOVE:
                case PLAYER_TELEPORT:
                case PLAYER_DEATH:
                case PLAYER_RESPAWN:
                case PLAYER_DAMAGE:
                case PLAYER_HEAL:
                case PLAYER_FOOD_CHANGE:
                case PLAYER_EXP_CHANGE:
                case PLAYER_LEVEL_UP:
                case PLAYER_INVENTORY_CLICK:
                case PLAYER_ITEM_DROP:
                case PLAYER_ITEM_PICKUP:
                case PLAYER_ITEM_CONSUME:
                case PLAYER_ITEM_BREAK:
                case PLAYER_BLOCK_BREAK:
                case PLAYER_BLOCK_PLACE:
                case PLAYER_INTERACT:
                case PLAYER_INTERACT_ENTITY:
                case PLAYER_FISH:
                case PLAYER_ENCHANT:
                case PLAYER_CRAFT:
                case PLAYER_SMELT:
                case PLAYER_TRADE:
                case PLAYER_SNEAK:
                case PLAYER_EVENT:
                    return new ru.openhousing.coding.blocks.events.PlayerEventBlock();
                
                // События мира
                case WORLD_WEATHER_CHANGE:
                case WORLD_TIME_CHANGE:
                case WORLD_CHUNK_LOAD:
                case WORLD_CHUNK_UNLOAD:
                case WORLD_STRUCTURE_GROW:
                case WORLD_EXPLOSION:
                case WORLD_PORTAL_CREATE:
                case WORLD_EVENT:
                    return new ru.openhousing.coding.blocks.events.WorldEventBlock();
                
                // События существ
                case ENTITY_SPAWN:
                case ENTITY_DEATH:
                case ENTITY_DAMAGE:
                case ENTITY_TARGET:
                case ENTITY_TAME:
                case ENTITY_BREED:
                case ENTITY_EXPLODE:
                case ENTITY_INTERACT:
                case ENTITY_MOUNT:
                case ENTITY_DISMOUNT:
                case ENTITY_LEASH:
                case ENTITY_UNLEASH:
                case ENTITY_SHEAR:
                case ENTITY_MILK:
                case ENTITY_TRANSFORM:
                case ENTITY_EVENT:
                    return new ru.openhousing.coding.blocks.events.EntityEventBlock();
            
                // Условия
                case IF_PLAYER:
                case IF_PLAYER_ONLINE:
                case IF_PLAYER_PERMISSION:
                case IF_PLAYER_GAMEMODE:
                case IF_PLAYER_WORLD:
                case IF_PLAYER_FLYING:
                case IF_PLAYER_SNEAKING:
                case IF_PLAYER_BLOCKING:
                case IF_PLAYER_ITEM:
                    return new ru.openhousing.coding.blocks.conditions.IfPlayerBlock();
                case IF_ENTITY:
                    return new ru.openhousing.coding.blocks.conditions.IfEntityBlock();
                case IF_VARIABLE:
                case IF_VARIABLE_EQUALS:
                case IF_VARIABLE_GREATER:
                case IF_VARIABLE_LESS:
                case IF_VARIABLE_CONTAINS:
                case IF_VARIABLE_EXISTS:
                case IF_VARIABLE_SAVED:
                case IF_VARIABLE_TYPE:
                    return new ru.openhousing.coding.blocks.conditions.IfVariableBlock();
            
                // Действия игрока
                case PLAYER_SEND_MESSAGE:
                case PLAYER_SEND_TITLE:
                case PLAYER_SEND_ACTIONBAR:
                case PLAYER_TELEPORT_ACTION:
                case PLAYER_GIVE_ITEM:
                case PLAYER_REMOVE_ITEM:
                case PLAYER_CLEAR_INVENTORY:
                case PLAYER_SET_HEALTH:
                case PLAYER_SET_FOOD:
                case PLAYER_SET_EXP:
                case PLAYER_GIVE_EFFECT:
                case PLAYER_REMOVE_EFFECT:
                case PLAYER_PLAY_SOUND:
                case PLAYER_STOP_SOUND:
                case PLAYER_SPAWN_PARTICLE:
                case PLAYER_SET_GAMEMODE:
                case PLAYER_KICK:
                case PLAYER_BAN:
                case PLAYER_OP:
                case PLAYER_DEOP:
                case PLAYER_WHITELIST_ADD:
                case PLAYER_WHITELIST_REMOVE:
                case PLAYER_SET_DISPLAY_NAME:
                case PLAYER_RESET_DISPLAY_NAME:
                case PLAYER_SEND_PLUGIN_MESSAGE:
                case PLAYER_ACTION:
                    return new ru.openhousing.coding.blocks.actions.PlayerActionBlock();
                
                // Действия переменных
                case VAR_SET:
                case VAR_ADD:
                case VAR_SUBTRACT:
                case VAR_MULTIPLY:
                case VAR_DIVIDE:
                case VAR_APPEND_TEXT:
                case VAR_REPLACE_TEXT:
                case VAR_UPPERCASE:
                case VAR_LOWERCASE:
                case VAR_REVERSE:
                case VAR_LENGTH:
                case VAR_SUBSTRING:
                case VAR_RANDOM_NUMBER:
                case VAR_ROUND:
                case VAR_ABS:
                case VAR_MIN:
                case VAR_MAX:
                case VAR_SAVE:
                case VAR_DELETE:
                case VAR_COPY:
                    return new ru.openhousing.coding.blocks.variables.VariableActionBlock();
                
                // Действия мира
                case GAME_SET_TIME:
                case GAME_SET_WEATHER:
                case GAME_SET_DIFFICULTY:
                case GAME_BROADCAST:
                case GAME_EXECUTE_COMMAND:
                case GAME_STOP_SERVER:
                case GAME_RESTART_SERVER:
                case GAME_SAVE_WORLD:
                case GAME_LOAD_WORLD:
                case GAME_CREATE_EXPLOSION:
                case GAME_SPAWN_ENTITY:
                case GAME_REMOVE_ENTITY:
                case GAME_SET_BLOCK:
                case GAME_BREAK_BLOCK:
                case GAME_SEND_PACKET:
                case WORLD_ACTION:
                    return new ru.openhousing.coding.blocks.actions.WorldActionBlock();
                
                case ENTITY_ACTION:
                    return new ru.openhousing.coding.blocks.actions.EntityActionBlock();
            
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
        } catch (Exception e) {
            plugin.getLogger().warning("Ошибка создания блока " + blockType + ": " + e.getMessage());
            return null;
        }
    }
    
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        String title = null;
        try {
            title = event.getView().getTitle();
        } catch (NoSuchMethodError e) {
            return; // Пропускаем если не можем получить title
        }
        
        if (title != null && title.equals("§6Выбор строки для блока") && 
            event.getPlayer().equals(player)) {
            // Отменяем регистрацию листенера
            InventoryClickEvent.getHandlerList().unregister(this);
            InventoryCloseEvent.getHandlerList().unregister(this);
        }
    }
}
