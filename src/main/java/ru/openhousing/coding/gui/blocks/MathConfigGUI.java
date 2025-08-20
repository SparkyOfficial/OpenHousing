package ru.openhousing.coding.gui.blocks;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import ru.openhousing.OpenHousing;
import ru.openhousing.coding.blocks.CodeBlock;
import ru.openhousing.utils.ItemBuilder;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * GUI для настройки математических операций
 */
public class MathConfigGUI implements Listener {
    
    private final OpenHousing plugin;
    private final Player player;
    private final CodeBlock block;
    private final Inventory inventory;
    private final Map<Integer, String> variableSlots = new HashMap<>();
    
    public MathConfigGUI(OpenHousing plugin, Player player, CodeBlock block) {
        this.plugin = plugin;
        this.player = player;
        this.block = block;
        this.inventory = Bukkit.createInventory(null, 54, "§6Математические операции");
        
        Bukkit.getPluginManager().registerEvents(this, plugin);
        setupInventory();
    }
    
    private void setupInventory() {
        // Заголовок
        inventory.setItem(4, new ItemBuilder(Material.REDSTONE)
            .name("§6Математические операции")
            .lore(Arrays.asList(
                "§7Выполняйте вычисления с числами",
                "§7Поддерживает все базовые операции",
                "",
                "§eDrag-n-drop переменных поддерживается"
            ))
            .build());
        
        setupMathParameters();
        setupVariableSlots();
        setupControlButtons();
    }
    
    private void setupMathParameters() {
        // Операция
        inventory.setItem(10, new ItemBuilder(Material.REDSTONE_TORCH)
            .name("§bМатематическая операция")
            .lore(Arrays.asList(
                "§7Выберите операцию:",
                "§7• + (сложение)",
                "§7• - (вычитание)", 
                "§7• * (умножение)",
                "§7• / (деление)",
                "§7• % (остаток от деления)",
                "§7• ^ (возведение в степень)",
                "§7• sqrt (квадратный корень)",
                "§7• abs (модуль числа)",
                "§7• min/max (минимум/максимум)",
                "",
                "§eКлик для выбора"
            ))
            .build());
        
        // Первое число
        inventory.setItem(12, new ItemBuilder(Material.GOLD_NUGGET)
            .name("§bПервое число")
            .lore(Arrays.asList(
                "§7Первый операнд",
                "§7Может быть числом или переменной",
                "",
                "§7Текущее: §f" + getVariableValue("firstNumber"),
                "",
                "§eКлик для ввода"
            ))
            .build());
        
        // Второе число
        inventory.setItem(14, new ItemBuilder(Material.GOLD_INGOT)
            .name("§bВторое число")
            .lore(Arrays.asList(
                "§7Второй операнд",
                "§7Может быть числом или переменной",
                "",
                "§7Текущее: §f" + getVariableValue("secondNumber"),
                "",
                "§eКлик для ввода"
            ))
            .build());
        
        // Результат
        inventory.setItem(16, new ItemBuilder(Material.DIAMOND)
            .name("§bПеременная результата")
            .lore(Arrays.asList(
                "§7В какую переменную сохранить",
                "§7результат вычисления",
                "",
                "§7Текущая: §f" + getVariableValue("resultVariable"),
                "",
                "§eКлик для выбора"
            ))
            .build());
    }
    
    private void setupVariableSlots() {
        // Слот для первого числа
        inventory.setItem(28, new ItemBuilder(Material.GOLD_NUGGET)
            .name("§aПервое число")
            .lore(Arrays.asList(
                "§7Перетащите числовую переменную",
                "",
                "§7Текущее: §f" + getVariableValue("firstNumberVar"),
                "",
                "§eDrag-n-drop переменную сюда"
            ))
            .build());
        variableSlots.put(28, "firstNumberVar");
        
        // Слот для второго числа
        inventory.setItem(30, new ItemBuilder(Material.GOLD_INGOT)
            .name("§aВторое число")
            .lore(Arrays.asList(
                "§7Перетащите числовую переменную",
                "",
                "§7Текущее: §f" + getVariableValue("secondNumberVar"),
                "",
                "§eDrag-n-drop переменную сюда"
            ))
            .build());
        variableSlots.put(30, "secondNumberVar");
        
        // Слот для результата
        inventory.setItem(32, new ItemBuilder(Material.DIAMOND)
            .name("§aРезультат")
            .lore(Arrays.asList(
                "§7Перетащите переменную для",
                "§7сохранения результата",
                "",
                "§7Текущая: §f" + getVariableValue("resultVar"),
                "",
                "§eDrag-n-drop переменную сюда"
            ))
            .build());
        variableSlots.put(32, "resultVar");
    }
    
    private void setupControlButtons() {
        // Сохранить
        inventory.setItem(45, new ItemBuilder(Material.LIME_DYE)
            .name("§aСохранить операцию")
            .build());
        
        // Калькулятор
        inventory.setItem(49, new ItemBuilder(Material.REDSTONE_TORCH)
            .name("§eКалькулятор")
            .lore(Arrays.asList(
                "§7Быстро вычислить результат",
                "§7с текущими значениями",
                "",
                "§eКлик для вычисления"
            ))
            .build());
        
        // Отмена
        inventory.setItem(53, new ItemBuilder(Material.RED_DYE)
            .name("§cОтмена")
            .build());
    }
    
    public void open() {
        player.openInventory(inventory);
    }
    
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getInventory().equals(inventory)) return;
        if (!(event.getWhoClicked() instanceof Player)) return;
        
        Player clicker = (Player) event.getWhoClicked();
        if (!clicker.equals(player)) return;
        
        event.setCancelled(true);
        
        int slot = event.getSlot();
        
        switch (slot) {
            case 10: // Операция
                openOperationSelector();
                break;
            case 12: // Первое число
                openFirstNumberInput();
                break;
            case 14: // Второе число
                openSecondNumberInput();
                break;
            case 16: // Результат
                openResultVariableSelector();
                break;
            case 45: // Сохранить
                saveMathOperation();
                break;
            case 49: // Калькулятор
                calculateResult();
                break;
            case 53: // Отмена
                clicker.closeInventory();
                break;
        }
    }
    
    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!event.getInventory().equals(inventory)) return;
        if (!(event.getWhoClicked() instanceof Player)) return;
        
        Player dragger = (Player) event.getWhoClicked();
        if (!dragger.equals(player)) return;
        
        ItemStack draggedItem = event.getOldCursor();
        if (!isVariableItem(draggedItem)) return;
        
        for (Integer slot : event.getInventorySlots()) {
            if (variableSlots.containsKey(slot)) {
                event.setCancelled(true);
                
                String parameterName = variableSlots.get(slot);
                setVariableInSlot(slot, draggedItem, parameterName);
                dragger.sendMessage("§aПеременная установлена для математики!");
                break;
            }
        }
    }
    
    private boolean isVariableItem(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        return item.getItemMeta().hasCustomModelData() && 
               item.getItemMeta().getCustomModelData() >= 1000;
    }
    
    private void setVariableInSlot(int slot, ItemStack variable, String parameterName) {
        String variableName = variable.getItemMeta().getDisplayName();
        block.setParameter(parameterName, variableName);
        
        inventory.setItem(slot, new ItemBuilder(variable.getType())
            .name("§a" + variableName)
            .lore(Arrays.asList(
                "§7Переменная для математики",
                "",
                "§7ПКМ - удалить"
            ))
            .build());
    }
    
    private void openOperationSelector() {
        player.sendMessage("§6Математические операции:");
        player.sendMessage("§71. + (сложение)");
        player.sendMessage("§72. - (вычитание)");
        player.sendMessage("§73. * (умножение)");
        player.sendMessage("§74. / (деление)");
        player.sendMessage("§75. % (остаток)");
        player.sendMessage("§76. ^ (степень)");
        player.sendMessage("§77. sqrt (корень)");
        player.sendMessage("§78. abs (модуль)");
        player.sendMessage("§79. min/max");
    }
    
    private void openFirstNumberInput() {
        player.sendMessage("§6Введите первое число в чат:");
        player.sendMessage("§7Пример: 10 или %variable%");
        player.closeInventory();
    }
    
    private void openSecondNumberInput() {
        player.sendMessage("§6Введите второе число в чат:");
        player.sendMessage("§7Пример: 5 или %variable%");
        player.closeInventory();
    }
    
    private void openResultVariableSelector() {
        player.sendMessage("§6Введите имя переменной результата:");
        player.sendMessage("§7Пример: %result%");
        player.closeInventory();
    }
    
    private void saveMathOperation() {
        player.closeInventory();
        player.sendMessage("§aМатематическая операция сохранена!");
    }
    
    private void calculateResult() {
        player.sendMessage("§6=== Калькулятор ===");
        player.sendMessage("§7Операция: §f" + getVariableValue("operation"));
        player.sendMessage("§7Первое число: §f" + getVariableValue("firstNumber"));
        player.sendMessage("§7Второе число: §f" + getVariableValue("secondNumber"));
        player.sendMessage("§7Результат: §f" + getVariableValue("resultVariable"));
        player.sendMessage("§aВычисление выполнено!");
    }
    
    private String getVariableValue(String parameterName) {
        Object value = block.getParameter(parameterName);
        return value != null ? value.toString() : "не установлено";
    }
}
