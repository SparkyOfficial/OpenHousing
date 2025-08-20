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
 * Уникальное GUI для настройки действий с переменными
 */
public class VariableActionConfigGUI implements Listener {
    
    private final OpenHousing plugin;
    private final Player player;
    private final CodeBlock block;
    private final Inventory inventory;
    private final Map<Integer, String> variableSlots = new HashMap<>();
    
    public VariableActionConfigGUI(OpenHousing plugin, Player player, CodeBlock block) {
        this.plugin = plugin;
        this.player = player;
        this.block = block;
        this.inventory = Bukkit.createInventory(null, 54, "§6Действие с переменной");
        
        Bukkit.getPluginManager().registerEvents(this, plugin);
        setupInventory();
    }
    
    private void setupInventory() {
        // Заголовок
        inventory.setItem(4, new ItemBuilder(Material.CHEST)
            .name("§6Действие с переменной")
            .lore(Arrays.asList(
                "§7Настройте операции с переменными",
                "§7Поддерживает все типы переменных",
                "",
                "§eПоддерживает drag-n-drop"
            ))
            .build());
        
        setupVariableOperations();
        setupVariableSlots();
        setupControlButtons();
    }
    
    private void setupVariableOperations() {
        // Тип операции
        inventory.setItem(10, new ItemBuilder(Material.REDSTONE)
            .name("§bТип операции")
            .lore(Arrays.asList(
                "§7Что делать с переменной:",
                "§7• Установить (=)",
                "§7• Прибавить (+)",
                "§7• Отнять (-)",
                "§7• Умножить (*)",
                "§7• Разделить (/)",
                "§7• Добавить текст",
                "",
                "§eКлик для выбора"
            ))
            .build());
        
        // Целевая переменная
        inventory.setItem(12, new ItemBuilder(Material.CHEST)
            .name("§bЦелевая переменная")
            .lore(Arrays.asList(
                "§7Какую переменную изменить:",
                "§7• Существующая переменная",
                "§7• Новая переменная",
                "§7• Динамическая с плейсхолдером",
                "",
                "§eКлик для выбора"
            ))
            .build());
        
        // Значение для операции
        inventory.setItem(14, new ItemBuilder(Material.PAPER)
            .name("§bЗначение")
            .lore(Arrays.asList(
                "§7Значение для операции:",
                "§7• Фиксированное число/текст",
                "§7• Другая переменная",
                "§7• Результат функции",
                "",
                "§eКлик для настройки"
            ))
            .build());
    }
    
    private void setupVariableSlots() {
        // Слот целевой переменной
        inventory.setItem(28, new ItemBuilder(Material.ENDER_CHEST)
            .name("§aЦелевая переменная")
            .lore(Arrays.asList(
                "§7Перетащите переменную которую нужно изменить",
                "",
                "§7Текущая: §f" + getVariableValue("targetVariable"),
                "",
                "§eDrag-n-drop переменную сюда"
            ))
            .build());
        variableSlots.put(28, "targetVariable");
        
        // Операция
        inventory.setItem(30, new ItemBuilder(Material.REDSTONE_TORCH)
            .name("§eОперация")
            .lore(Arrays.asList(
                "§7Текущая операция: §f" + getVariableValue("operation"),
                "",
                "§eКлик для изменения"
            ))
            .build());
        
        // Слот значения
        inventory.setItem(32, new ItemBuilder(Material.WRITABLE_BOOK)
            .name("§aЗначение операции")
            .lore(Arrays.asList(
                "§7Перетащите переменную или установите значение",
                "",
                "§7Текущее: §f" + getVariableValue("operationValue"),
                "",
                "§eDrag-n-drop или клик для настройки"
            ))
            .build());
        variableSlots.put(32, "operationValue");
        
        // Дополнительные настройки
        inventory.setItem(34, new ItemBuilder(Material.COMPARATOR)
            .name("§6Дополнительно")
            .lore(Arrays.asList(
                "§7Дополнительные параметры:",
                "§7• Сохранить навсегда (СОХРАНЕНО)",
                "§7• Область видимости (глобальная/локальная)",
                "§7• Условия выполнения",
                "",
                "§eКлик для настройки"
            ))
            .build());
    }
    
    private void setupControlButtons() {
        // Сохранить
        inventory.setItem(45, new ItemBuilder(Material.LIME_DYE)
            .name("§aСохранить действие")
            .build());
        
        // Предварительный просмотр
        inventory.setItem(49, new ItemBuilder(Material.SPYGLASS)
            .name("§eПредварительный просмотр")
            .lore(Arrays.asList(
                "§7Посмотреть результат операции",
                "§7с текущими переменными",
                "",
                "§eКлик для просмотра"
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
            case 10: // Тип операции
                openOperationSelector();
                break;
            case 12: // Целевая переменная
                openTargetVariableSelector();
                break;
            case 14: // Значение
                openValueSelector();
                break;
            case 30: // Операция
                cycleOperation();
                break;
            case 34: // Дополнительно
                openAdditionalSettings();
                break;
            case 45: // Сохранить
                saveVariableAction();
                break;
            case 49: // Предварительный просмотр
                showPreview();
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
                dragger.sendMessage("§aПеременная установлена!");
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
                "§7Установленная переменная",
                "",
                "§7ПКМ - удалить"
            ))
            .build());
    }
    
    private void openOperationSelector() {
        player.sendMessage("§6Операции с переменными:");
        player.sendMessage("§71. set - установить значение");
        player.sendMessage("§72. add - прибавить");
        player.sendMessage("§73. subtract - отнять");
        player.sendMessage("§74. multiply - умножить");
        player.sendMessage("§75. divide - разделить");
        player.sendMessage("§76. append - добавить текст");
    }
    
    private void openTargetVariableSelector() {
        player.sendMessage("§6Выберите переменную для изменения:");
        player.sendMessage("§7Напишите имя переменной в чат");
        player.sendMessage("§7Или перетащите существующую переменную");
    }
    
    private void openValueSelector() {
        player.sendMessage("§6Установите значение:");
        player.sendMessage("§7Напишите значение в чат");
        player.sendMessage("§7Или перетащите переменную");
    }
    
    private void cycleOperation() {
        String[] operations = {"set", "add", "subtract", "multiply", "divide", "append"};
        String current = (String) block.getParameter("operation");
        
        int currentIndex = 0;
        for (int i = 0; i < operations.length; i++) {
            if (operations[i].equals(current)) {
                currentIndex = i;
                break;
            }
        }
        
        int nextIndex = (currentIndex + 1) % operations.length;
        block.setParameter("operation", operations[nextIndex]);
        
        setupInventory();
        player.sendMessage("§aОперация изменена на: §f" + operations[nextIndex]);
    }
    
    private void openAdditionalSettings() {
        player.sendMessage("§6Дополнительные настройки:");
        player.sendMessage("§7save:true - сохранить переменную навсегда");
        player.sendMessage("§7scope:global - глобальная область видимости");
        player.sendMessage("§7condition:[условие] - условие выполнения");
    }
    
    private void saveVariableAction() {
        player.closeInventory();
        player.sendMessage("§aДействие с переменной сохранено!");
    }
    
    private void showPreview() {
        player.sendMessage("§6=== Предварительный просмотр ===");
        player.sendMessage("§7Операция: §f" + getVariableValue("operation"));
        player.sendMessage("§7Переменная: §f" + getVariableValue("targetVariable"));
        player.sendMessage("§7Значение: §f" + getVariableValue("operationValue"));
        
        String operation = (String) block.getParameter("operation");
        if ("add".equals(operation)) {
            player.sendMessage("§aРезультат: Переменная будет увеличена");
        } else if ("set".equals(operation)) {
            player.sendMessage("§aРезультат: Переменная получит новое значение");
        }
    }
}
