package ru.openhousing.coding.gui.blocks;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import ru.openhousing.OpenHousing;
import ru.openhousing.coding.blocks.math.MathBlock;
import ru.openhousing.coding.blocks.math.MathBlock.MathOperation;
import ru.openhousing.coding.constants.BlockParams;
import ru.openhousing.coding.gui.BaseBlockConfigGUI;

import java.util.Arrays;

/**
 * GUI для настройки MathBlock (математических операций)
 */
public class MathConfigGUI extends BaseBlockConfigGUI {
    
    // Слоты GUI
    private static final int OPERATION_SLOT = 10;
    private static final int OPERAND1_SLOT = 12;
    private static final int OPERAND2_SLOT = 14;
    private static final int RESULT_VARIABLE_SLOT = 16;
    private static final int INFO_SLOT = 20;
    
    public MathConfigGUI(OpenHousing plugin, Player player, MathBlock block, Runnable onSaveCallback) {
        super(plugin, player, block, onSaveCallback);
    }
    
    @Override
    public void setupInventory() {
        this.inventory = Bukkit.createInventory(null, 36, "§8Настройка математической операции");
        
        // Заполняем фон
        for (int i = 0; i < inventory.getSize(); i++) {
            inventory.setItem(i, new ItemStack(Material.GRAY_STAINED_GLASS_PANE));
        }
        
        // Заголовок
        ItemStack headerItem = new ItemStack(Material.REDSTONE);
        ItemMeta headerMeta = headerItem.getItemMeta();
        headerMeta.setDisplayName("§6§lМатематическая операция");
        headerMeta.setLore(Arrays.asList(
            "§7Настройте параметры",
            "§7математической операции"
        ));
        headerItem.setItemMeta(headerMeta);
        inventory.setItem(4, headerItem);
        
        // Операция
        MathOperation currentOperation = (MathOperation) block.getParameter("operation");
        ItemStack operationItem = new ItemStack(Material.REPEATER);
        ItemMeta operationMeta = operationItem.getItemMeta();
        operationMeta.setDisplayName("§eОперация");
        operationMeta.setLore(Arrays.asList(
            "§7Текущая: §f" + (currentOperation != null ? currentOperation.getDisplayName() : "Не выбрана"),
            "",
            "§7Кликните для выбора",
            "§7математической операции"
        ));
        operationItem.setItemMeta(operationMeta);
        inventory.setItem(OPERATION_SLOT, operationItem);
        
        // Первый операнд
        String currentOperand1 = (String) block.getParameter("operand1");
        ItemStack operand1Item = new ItemStack(Material.PAPER);
        ItemMeta operand1Meta = operand1Item.getItemMeta();
        operand1Meta.setDisplayName("§eПервый операнд");
        operand1Meta.setLore(Arrays.asList(
            "§7Текущий: §f" + (currentOperand1 != null ? currentOperand1 : "Не указан"),
            "",
            "§7Число, переменная или выражение",
            "§7Кликните для изменения"
        ));
        operand1Item.setItemMeta(operand1Meta);
        inventory.setItem(OPERAND1_SLOT, operand1Item);
        
        // Второй операнд (для бинарных операций)
        String currentOperand2 = (String) block.getParameter("operand2");
        ItemStack operand2Item = new ItemStack(Material.PAPER);
        ItemMeta operand2Meta = operand2Item.getItemMeta();
        operand2Meta.setDisplayName("§eВторой операнд");
        operand2Meta.setLore(Arrays.asList(
            "§7Текущий: §f" + (currentOperand2 != null ? currentOperand2 : "Не указан"),
            "",
            "§7Требуется для бинарных операций",
            "§7Кликните для изменения"
        ));
        operand2Item.setItemMeta(operand2Meta);
        inventory.setItem(OPERAND2_SLOT, operand2Item);
        
        // Переменная для результата
        String currentResultVar = (String) block.getParameter("resultVariable");
        ItemStack resultVarItem = new ItemStack(Material.BOOK);
        ItemMeta resultVarMeta = resultVarItem.getItemMeta();
        resultVarMeta.setDisplayName("§aПеременная результата");
        resultVarMeta.setLore(Arrays.asList(
            "§7Текущая: §f" + (currentResultVar != null ? currentResultVar : "result"),
            "",
            "§7Куда сохранить результат",
            "§7Кликните для изменения"
        ));
        resultVarItem.setItemMeta(resultVarMeta);
        inventory.setItem(RESULT_VARIABLE_SLOT, resultVarItem);
        
        // Информация об операциях
        ItemStack infoItem = new ItemStack(Material.BOOK);
        ItemMeta infoMeta = infoItem.getItemMeta();
        infoMeta.setDisplayName("§bДоступные операции");
        infoMeta.setLore(Arrays.asList(
            "§7Математические операции:",
            "",
            "§7• ADD (+) - сложение",
            "§7• SUBTRACT (-) - вычитание",
            "§7• MULTIPLY (*) - умножение",
            "§7• DIVIDE (/) - деление",
            "§7• MODULO (%) - остаток",
            "§7• POWER (^) - степень",
            "§7• SQUARE_ROOT (√) - корень",
            "§7• ABSOLUTE (|x|) - модуль",
            "§7• ROUND - округление",
            "§7• FLOOR - округление вниз",
            "§7• CEIL - округление вверх",
            "§7• MIN - минимум",
            "§7• MAX - максимум",
            "§7• RANDOM - случайное число"
        ));
        infoItem.setItemMeta(infoMeta);
        inventory.setItem(INFO_SLOT, infoItem);
        
        // Настраиваем навигационные элементы
        setupNavigationItems();
    }
    
    @Override
    protected void handleSpecificClick(int slot, boolean isRightClick, boolean isShiftClick) {
        switch (slot) {
            case OPERATION_SLOT:
                openOperationSelector();
                break;
                
            case OPERAND1_SLOT:
                openOperand1Input();
                break;
                
            case OPERAND2_SLOT:
                openOperand2Input();
                break;
                
            case RESULT_VARIABLE_SLOT:
                openResultVariableInput();
                break;
        }
    }
    
    @Override
    protected String getParameterPrompt(String parameterName) {
        return switch (parameterName) {
            case "operation" -> "Выберите математическую операцию";
            case "operand1" -> "Введите первый операнд";
            case "operand2" -> "Введите второй операнд";
            case "resultVariable" -> "Введите имя переменной для результата";
            default -> "Введите значение для параметра " + parameterName;
        };
    }
    
    /**
     * Открытие селектора операции
     */
    private void openOperationSelector() {
        Inventory selector = Bukkit.createInventory(null, 54, "§8Выберите операцию");
        
        // Заполняем фон
        for (int i = 0; i < selector.getSize(); i++) {
            selector.setItem(i, new ItemStack(Material.GRAY_STAINED_GLASS_PANE));
        }
        
        // Заголовок
        ItemStack headerItem = new ItemStack(Material.REDSTONE);
        ItemMeta headerMeta = headerItem.getItemMeta();
        headerMeta.setDisplayName("§6§lМатематическая операция");
        headerMeta.setLore(Arrays.asList("§7Выберите операцию"));
        headerItem.setItemMeta(headerMeta);
        selector.setItem(4, headerItem);
        
        // Операции
        int slot = 19;
        for (MathOperation operation : MathOperation.values()) {
            if (slot >= 53) break;
            
            ItemStack operationItem = new ItemStack(getMaterialForOperation(operation));
            ItemMeta operationMeta = operationItem.getItemMeta();
            operationMeta.setDisplayName("§e" + operation.getDisplayName());
            operationMeta.setLore(Arrays.asList(
                "§7" + operation.getDescription(),
                "§7Символ: §f" + operation.getSymbol(),
                "",
                "§7Кликните для выбора"
            ));
            operationItem.setItemMeta(operationMeta);
            selector.setItem(slot, operationItem);
            
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
     * Получение материала для операции
     */
    private Material getMaterialForOperation(MathOperation operation) {
        return switch (operation) {
            case ADD -> Material.GREEN_WOOL;
            case SUBTRACT -> Material.RED_WOOL;
            case MULTIPLY -> Material.YELLOW_WOOL;
            case DIVIDE -> Material.BLUE_WOOL;
            case MODULO -> Material.PURPLE_WOOL;
            case POWER -> Material.ORANGE_WOOL;
            case SQUARE_ROOT -> Material.LIME_WOOL;
            case ABSOLUTE -> Material.WHITE_WOOL;
            case ROUND -> Material.CYAN_WOOL;
            case FLOOR -> Material.GRAY_WOOL;
            case CEIL -> Material.LIGHT_GRAY_WOOL;
            case MIN -> Material.LIGHT_BLUE_WOOL;
            case MAX -> Material.PINK_WOOL;
            case RANDOM -> Material.MAGENTA_WOOL;
        };
    }
    
    /**
     * Открытие ввода первого операнда
     */
    private void openOperand1Input() {
        player.sendMessage("§eВведите первый операнд:");
        player.sendMessage("§7Может быть число, переменная или выражение");
        player.sendMessage("§7Используйте команду: /openhousing set " + block.getId() + " operand1 <значение>");
    }
    
    /**
     * Открытие ввода второго операнда
     */
    private void openOperand2Input() {
        MathOperation currentOperation = (MathOperation) block.getParameter("operation");
        if (currentOperation == null) {
            player.sendMessage("§cСначала выберите операцию!");
            return;
        }
        
        if (isUnaryOperation(currentOperation)) {
            player.sendMessage("§eДля операции " + currentOperation.getDisplayName() + " второй операнд не требуется");
            return;
        }
        
        player.sendMessage("§eВведите второй операнд:");
        player.sendMessage("§7Может быть число, переменная или выражение");
        player.sendMessage("§7Используйте команду: /openhousing set " + block.getId() + " operand2 <значение>");
    }
    
    /**
     * Открытие ввода переменной результата
     */
    private void openResultVariableInput() {
        player.sendMessage("§eВведите имя переменной для результата:");
        player.sendMessage("§7Используйте команду: /openhousing set " + block.getId() + " resultVariable <имя>");
    }
    
    /**
     * Проверка, является ли операция унарной
     */
    private boolean isUnaryOperation(MathOperation operation) {
        return operation == MathOperation.SQUARE_ROOT ||
               operation == MathOperation.ABSOLUTE ||
               operation == MathOperation.ROUND ||
               operation == MathOperation.FLOOR ||
               operation == MathOperation.CEIL;
    }
}
