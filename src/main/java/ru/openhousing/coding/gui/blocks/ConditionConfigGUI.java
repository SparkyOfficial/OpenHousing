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
import ru.openhousing.coding.variables.VariableType;
import ru.openhousing.utils.ItemBuilder;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Уникальное GUI для настройки условий с поддержкой переменных
 */
public class ConditionConfigGUI implements Listener {
    
    private final OpenHousing plugin;
    private final Player player;
    private final CodeBlock block;
    private final Inventory inventory;
    private final Map<Integer, String> variableSlots = new HashMap<>();
    
    public ConditionConfigGUI(OpenHousing plugin, Player player, CodeBlock block) {
        this.plugin = plugin;
        this.player = player;
        this.block = block;
        this.inventory = Bukkit.createInventory(null, 54, "§6Настройка условия");
        
        Bukkit.getPluginManager().registerEvents(this, plugin);
        setupInventory();
    }
    
    private void setupInventory() {
        // Заголовок
        inventory.setItem(4, new ItemBuilder(Material.COMPARATOR)
            .name("§6Условие")
            .lore(Arrays.asList(
                "§7Настройте параметры условия",
                "§7Перетащите переменные для сравнения",
                "",
                "§eПоддерживает drag-n-drop переменных"
            ))
            .build());
        
        setupConditionParameters();
        setupVariableSlots();
        setupControlButtons();
    }
    
    private void setupConditionParameters() {
        // Тип условия
        inventory.setItem(10, new ItemBuilder(Material.BOOK)
            .name("§bТип условия")
            .lore(Arrays.asList(
                "§7Выберите что проверять:",
                "§7• Равно (=)",
                "§7• Не равно (≠)",
                "§7• Больше (>)",
                "§7• Меньше (<)",
                "§7• Содержит текст",
                "§7• Игрок онлайн",
                "",
                "§eКлик для выбора"
            ))
            .build());
        
        // Левая часть сравнения
        inventory.setItem(12, new ItemBuilder(Material.PAPER)
            .name("§bЛевая часть")
            .lore(Arrays.asList(
                "§7Что сравниваем:",
                "§7• Переменная",
                "§7• Плейсхолдер (%player%, %victim%)",
                "§7• Фиксированное значение",
                "",
                "§eКлик для настройки"
            ))
            .build());
        
        // Правая часть сравнения
        inventory.setItem(14, new ItemBuilder(Material.WRITABLE_BOOK)
            .name("§bПравая часть")
            .lore(Arrays.asList(
                "§7С чем сравниваем:",
                "§7• Переменная",
                "§7• Плейсхолдер",
                "§7• Фиксированное значение",
                "",
                "§eКлик для настройки"
            ))
            .build());
    }
    
    private void setupVariableSlots() {
        // Слот для левой переменной
        inventory.setItem(28, new ItemBuilder(Material.LIME_STAINED_GLASS)
            .name("§aЛевая переменная")
            .lore(Arrays.asList(
                "§7Перетащите переменную для сравнения",
                "",
                "§7Текущее: §f" + getVariableValue("leftVariable"),
                "",
                "§eDrag-n-drop переменную сюда"
            ))
            .build());
        variableSlots.put(28, "leftVariable");
        
        // Оператор сравнения
        inventory.setItem(30, new ItemBuilder(Material.COMPARATOR)
            .name("§eОператор")
            .lore(Arrays.asList(
                "§7Текущий оператор: §f" + getVariableValue("operator"),
                "",
                "§eКлик для изменения"
            ))
            .build());
        
        // Слот для правой переменной
        inventory.setItem(32, new ItemBuilder(Material.RED_STAINED_GLASS)
            .name("§cПравая переменная")
            .lore(Arrays.asList(
                "§7Перетащите переменную для сравнения",
                "",
                "§7Текущее: §f" + getVariableValue("rightVariable"),
                "",
                "§eDrag-n-drop переменную сюда"
            ))
            .build());
        variableSlots.put(32, "rightVariable");
        
        // Результат условия
        inventory.setItem(34, new ItemBuilder(Material.REDSTONE_LAMP)
            .name("§6Результат")
            .lore(Arrays.asList(
                "§7Что произойдет при выполнении:",
                "§7• true - условие выполнено",
                "§7• false - условие не выполнено",
                "",
                "§7Инвертировать: §f" + (block.getParameter("invert") != null ? "Да" : "Нет"),
                "",
                "§eКлик для инвертирования"
            ))
            .build());
    }
    
    private void setupControlButtons() {
        // Сохранить
        inventory.setItem(45, new ItemBuilder(Material.LIME_DYE)
            .name("§aСохранить условие")
            .build());
        
        // Тест условия
        inventory.setItem(49, new ItemBuilder(Material.REDSTONE_TORCH)
            .name("§eТестировать условие")
            .lore(Arrays.asList(
                "§7Проверить условие с текущими",
                "§7параметрами и переменными",
                "",
                "§eКлик для теста"
            ))
            .build());
        
        // Отмена
        inventory.setItem(53, new ItemBuilder(Material.RED_DYE)
            .name("§cОтмена")
            .build());
    }
    
    private String getVariableValue(String parameterName) {
        Object value = block.getParameter(parameterName);
        return value != null ? value.toString() : "не установлено";
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
            case 10: // Тип условия
                openConditionTypeSelector();
                break;
            case 30: // Оператор
                cycleOperator();
                break;
            case 34: // Инвертировать
                toggleInvert();
                break;
            case 45: // Сохранить
                saveCondition();
                break;
            case 49: // Тест
                testCondition();
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
                
                VariableType variableType = getVariableTypeFromItem(draggedItem);
                String parameterName = variableSlots.get(slot);
                
                setVariableInSlot(slot, draggedItem, parameterName);
                dragger.sendMessage("§aПеременная установлена для сравнения!");
                break;
            }
        }
    }
    
    private boolean isVariableItem(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        return item.getItemMeta().hasCustomModelData() && 
               item.getItemMeta().getCustomModelData() >= 1000;
    }
    
    private VariableType getVariableTypeFromItem(ItemStack item) {
        if (!item.hasItemMeta() || !item.getItemMeta().hasCustomModelData()) {
            return null;
        }
        
        int modelData = item.getItemMeta().getCustomModelData();
        int typeIndex = modelData - 1000;
        
        VariableType[] types = VariableType.values();
        if (typeIndex >= 0 && typeIndex < types.length) {
            return types[typeIndex];
        }
        
        return null;
    }
    
    private void setVariableInSlot(int slot, ItemStack variable, String parameterName) {
        String variableName = variable.getItemMeta().getDisplayName();
        block.setParameter(parameterName, variableName);
        
        inventory.setItem(slot, new ItemBuilder(variable.getType())
            .name("§a" + variableName)
            .lore(Arrays.asList(
                "§7Переменная для сравнения",
                "§7Тип: " + getVariableTypeFromItem(variable).getDisplayName(),
                "",
                "§7ПКМ - удалить"
            ))
            .build());
    }
    
    private void openConditionTypeSelector() {
        player.sendMessage("§6Операторы сравнения:");
        player.sendMessage("§71. = (равно)");
        player.sendMessage("§72. != (не равно)");
        player.sendMessage("§73. > (больше)");
        player.sendMessage("§74. < (меньше)");
        player.sendMessage("§75. contains (содержит)");
    }
    
    private void cycleOperator() {
        String[] operators = {"=", "!=", ">", "<", ">=", "<=", "contains"};
        String current = (String) block.getParameter("operator");
        
        int currentIndex = 0;
        for (int i = 0; i < operators.length; i++) {
            if (operators[i].equals(current)) {
                currentIndex = i;
                break;
            }
        }
        
        int nextIndex = (currentIndex + 1) % operators.length;
        block.setParameter("operator", operators[nextIndex]);
        
        setupInventory(); // Обновляем GUI
        player.sendMessage("§aОператор изменен на: §f" + operators[nextIndex]);
    }
    
    private void toggleInvert() {
        boolean current = block.getParameter("invert") != null;
        block.setParameter("invert", current ? null : true);
        
        setupInventory(); // Обновляем GUI
        player.sendMessage("§aИнвертирование: " + (!current ? "§aВключено" : "§cВыключено"));
    }
    
    private void saveCondition() {
        player.closeInventory();
        player.sendMessage("§aУсловие настроено и сохранено!");
    }
    
    private void testCondition() {
        player.sendMessage("§6=== Тест условия ===");
        
        try {
            // Симулируем выполнение условия
            CodeBlock.ExecutionContext context = new CodeBlock.ExecutionContext(player);
            CodeBlock.ExecutionResult result = block.execute(context);
            
            if (result.isSuccess()) {
                player.sendMessage("§aУсловие: §fВЫПОЛНЕНО (true)");
            } else {
                player.sendMessage("§cУсловие: §fНЕ ВЫПОЛНЕНО (false)");
            }
            
            if (result.getMessage() != null) {
                player.sendMessage("§7Детали: " + result.getMessage());
            }
        } catch (Exception e) {
            player.sendMessage("§cОшибка при тестировании: " + e.getMessage());
        }
    }
}
