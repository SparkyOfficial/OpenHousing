package ru.openhousing.coding.gui.blocks;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import ru.openhousing.OpenHousing;
import ru.openhousing.coding.blocks.control.TargetBlock;
import ru.openhousing.coding.blocks.control.TargetBlock.TargetType;
import ru.openhousing.coding.gui.BaseBlockConfigGUI;

import java.util.Arrays;

/**
 * GUI для настройки TargetBlock (выбора цели)
 */
public class TargetConfigGUI extends BaseBlockConfigGUI {
    
    // Слоты GUI
    private static final int TARGET_TYPE_SLOT = 10;
    private static final int TARGET_VALUE_SLOT = 12;
    private static final int RADIUS_SLOT = 14;
    private static final int INFO_SLOT = 16;
    
    public TargetConfigGUI(OpenHousing plugin, Player player, TargetBlock block, Runnable onSaveCallback) {
        super(plugin, player, block, onSaveCallback);
    }
    
    @Override
    public void setupInventory() {
        this.inventory = Bukkit.createInventory(null, 36, "§8Настройка цели");
        
        // Заполняем фон
        for (int i = 0; i < inventory.getSize(); i++) {
            inventory.setItem(i, new ItemStack(Material.GRAY_STAINED_GLASS_PANE));
        }
        
        // Заголовок
        ItemStack headerItem = new ItemStack(Material.TARGET);
        ItemMeta headerMeta = headerItem.getItemMeta();
        headerMeta.setDisplayName("§6§lВыбор цели");
        headerMeta.setLore(Arrays.asList(
            "§7Настройте параметры цели",
            "§7для последующих действий"
        ));
        headerItem.setItemMeta(headerMeta);
        inventory.setItem(4, headerItem);
        
        // Тип цели
        TargetType currentType = (TargetType) block.getParameter("targetType");
        ItemStack typeItem = new ItemStack(Material.TARGET);
        ItemMeta typeMeta = typeItem.getItemMeta();
        typeMeta.setDisplayName("§eТип цели");
        typeMeta.setLore(Arrays.asList(
            "§7Текущий: §f" + (currentType != null ? currentType.getDisplayName() : "Не выбран"),
            "",
            "§7Кликните для выбора типа",
            "§7цели"
        ));
        typeItem.setItemMeta(typeMeta);
        inventory.setItem(TARGET_TYPE_SLOT, typeItem);
        
        // Значение цели
        String currentValue = (String) block.getParameter("targetValue");
        ItemStack valueItem = new ItemStack(Material.PAPER);
        ItemMeta valueMeta = valueItem.getItemMeta();
        valueMeta.setDisplayName("§eЗначение цели");
        valueMeta.setLore(Arrays.asList(
            "§7Текущее: §f" + (currentValue != null ? currentValue : "Не указано"),
            "",
            "§7Для SPECIFIC_PLAYER: имя игрока",
            "§7Для VARIABLE_TARGET: имя переменной",
            "",
            "§7Кликните для изменения"
        ));
        valueItem.setItemMeta(valueMeta);
        inventory.setItem(TARGET_VALUE_SLOT, valueItem);
        
        // Радиус поиска
        String currentRadius = (String) block.getParameter("radius");
        ItemStack radiusItem = new ItemStack(Material.COMPASS);
        ItemMeta radiusMeta = radiusItem.getItemMeta();
        radiusMeta.setDisplayName("§aРадиус поиска");
        radiusMeta.setLore(Arrays.asList(
            "§7Текущий: §f" + (currentRadius != null ? currentRadius : "10.0"),
            "",
            "§7Радиус для поиска ближайших",
            "§7игроков/существ",
            "",
            "§7Кликните для изменения"
        ));
        radiusItem.setItemMeta(radiusMeta);
        inventory.setItem(RADIUS_SLOT, radiusItem);
        
        // Информация о типах целей
        ItemStack infoItem = new ItemStack(Material.BOOK);
        ItemMeta infoMeta = infoItem.getItemMeta();
        infoMeta.setDisplayName("§bТипы целей");
        infoMeta.setLore(Arrays.asList(
            "§7Доступные типы целей:",
            "",
            "§7• CURRENT_PLAYER - текущий игрок",
            "§7• ALL_PLAYERS - все игроки",
            "§7• PLAYERS_IN_HOUSE - игроки в доме",
            "§7• NEAREST_PLAYER - ближайший игрок",
            "§7• RANDOM_PLAYER - случайный игрок",
            "§7• NEAREST_ENTITY - ближайшее существо",
            "§7• ALL_ENTITIES - все существа",
            "§7• SPECIFIC_PLAYER - конкретный игрок",
            "§7• VARIABLE_TARGET - цель из переменной"
        ));
        infoItem.setItemMeta(infoMeta);
        inventory.setItem(INFO_SLOT, infoItem);
        
        // Настраиваем навигационные элементы
        setupNavigationItems();
    }
    
    @Override
    protected void handleSpecificClick(int slot, boolean isRightClick, boolean isShiftClick) {
        switch (slot) {
            case TARGET_TYPE_SLOT:
                openTargetTypeSelector();
                break;
                
            case TARGET_VALUE_SLOT:
                openTargetValueInput();
                break;
                
            case RADIUS_SLOT:
                openRadiusInput();
                break;
        }
    }
    
    @Override
    protected String getParameterPrompt(String parameterName) {
        return switch (parameterName) {
            case "targetType" -> "Выберите тип цели";
            case "targetValue" -> "Введите значение цели";
            case "radius" -> "Введите радиус поиска (1-100)";
            default -> "Введите значение для параметра " + parameterName;
        };
    }
    
    /**
     * Открытие селектора типа цели
     */
    private void openTargetTypeSelector() {
        Inventory selector = Bukkit.createInventory(null, 54, "§8Выберите тип цели");
        
        // Заполняем фон
        for (int i = 0; i < selector.getSize(); i++) {
            selector.setItem(i, new ItemStack(Material.GRAY_STAINED_GLASS_PANE));
        }
        
        // Заголовок
        ItemStack headerItem = new ItemStack(Material.TARGET);
        ItemMeta headerMeta = headerItem.getItemMeta();
        headerMeta.setDisplayName("§6§lТип цели");
        headerMeta.setLore(Arrays.asList("§7Выберите тип цели"));
        headerItem.setItemMeta(headerMeta);
        selector.setItem(4, headerItem);
        
        // Типы целей
        int slot = 19;
        for (TargetType type : TargetType.values()) {
            if (slot >= 53) break;
            
            ItemStack typeItem = new ItemStack(getMaterialForTargetType(type));
            ItemMeta typeMeta = typeItem.getItemMeta();
            typeMeta.setDisplayName("§e" + type.getDisplayName());
            typeMeta.setLore(Arrays.asList(
                "§7" + type.getDescription(),
                "",
                "§7Кликните для выбора"
            ));
            typeItem.setItemMeta(typeMeta);
            selector.setItem(slot, typeItem);
            
            slot++;
        }
        
        // Кнопка назад
        ItemStack backItem = new ItemStack(Material.ARROW);
        ItemMeta backMeta = backItem.getItemMeta();
        backMeta.setDisplayName("§cНазад");
        backMeta.setLore(Arrays.asList("§7Вернуться к настройкам"));
        backItem.setItemMeta(backMeta);
        selector.setItem(49, backItem);
        
        player.openInventory(selector);
    }
    
    /**
     * Получение материала для типа цели
     */
    private Material getMaterialForTargetType(TargetType type) {
        return switch (type) {
            case CURRENT_PLAYER -> Material.PLAYER_HEAD;
            case ALL_PLAYERS -> Material.PLAYER_HEAD;
            case PLAYERS_IN_HOUSE -> Material.OAK_DOOR;
            case NEAREST_PLAYER -> Material.COMPASS;
            case RANDOM_PLAYER -> Material.EMERALD;
            case NEAREST_ENTITY -> Material.ENDER_EYE;
            case ALL_ENTITIES -> Material.ENDER_EYE;
            case SPECIFIC_PLAYER -> Material.NAME_TAG;
            case VARIABLE_TARGET -> Material.BOOK;
        };
    }
    
    /**
     * Открытие ввода значения цели
     */
    private void openTargetValueInput() {
        TargetType currentType = (TargetType) block.getParameter("targetType");
        if (currentType == null) {
            player.sendMessage("§cСначала выберите тип цели!");
            return;
        }
        
        if (currentType == TargetType.SPECIFIC_PLAYER) {
            player.sendMessage("§eВведите имя игрока:");
            player.sendMessage("§7Используйте команду: /openhousing set " + block.getId() + " targetValue <имя>");
        } else if (currentType == TargetType.VARIABLE_TARGET) {
            player.sendMessage("§eВведите имя переменной:");
            player.sendMessage("§7Используйте команду: /openhousing set " + block.getId() + " targetValue <переменная>");
        } else {
            player.sendMessage("§eДля этого типа цели значение не требуется");
        }
    }
    
    /**
     * Открытие ввода радиуса
     */
    private void openRadiusInput() {
        player.sendMessage("§eВведите радиус поиска (1-100):");
        player.sendMessage("§7Используйте команду: /openhousing set " + block.getId() + " radius <число>");
    }
}
