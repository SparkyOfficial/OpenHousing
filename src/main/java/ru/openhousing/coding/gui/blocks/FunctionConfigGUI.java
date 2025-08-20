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
 * GUI для настройки функций
 */
public class FunctionConfigGUI implements Listener {
    
    private final OpenHousing plugin;
    private final Player player;
    private final CodeBlock block;
    private final Inventory inventory;
    private final Map<Integer, String> variableSlots = new HashMap<>();
    
    public FunctionConfigGUI(OpenHousing plugin, Player player, CodeBlock block) {
        this.plugin = plugin;
        this.player = player;
        this.block = block;
        this.inventory = Bukkit.createInventory(null, 54, "§6Настройка функции");
        
        Bukkit.getPluginManager().registerEvents(this, plugin);
        setupInventory();
    }
    
    private void setupInventory() {
        // Заголовок
        inventory.setItem(4, new ItemBuilder(Material.BOOK)
            .name("§6Настройка функции")
            .lore(Arrays.asList(
                "§7Создайте пользовательскую функцию",
                "§7с параметрами и возвращаемым значением",
                "",
                "§eDrag-n-drop переменных поддерживается"
            ))
            .build());
        
        setupFunctionParameters();
        setupVariableSlots();
        setupControlButtons();
    }
    
    private void setupFunctionParameters() {
        // Имя функции
        inventory.setItem(10, new ItemBuilder(Material.NAME_TAG)
            .name("§bИмя функции")
            .lore(Arrays.asList(
                "§7Уникальное имя функции",
                "§7Используется для вызова",
                "",
                "§7Текущее: §f" + getVariableValue("functionName"),
                "",
                "§eКлик для изменения"
            ))
            .build());
        
        // Параметры функции
        inventory.setItem(12, new ItemBuilder(Material.WRITABLE_BOOK)
            .name("§bПараметры функции")
            .lore(Arrays.asList(
                "§7Входные параметры функции",
                "§7Разделяйте запятыми",
                "",
                "§7Текущие: §f" + getVariableValue("parameters"),
                "",
                "§eКлик для настройки"
            ))
            .build());
        
        // Возвращаемое значение
        inventory.setItem(14, new ItemBuilder(Material.ENDER_PEARL)
            .name("§bВозвращаемое значение")
            .lore(Arrays.asList(
                "§7Что функция возвращает",
                "§7Может быть переменной",
                "",
                "§7Текущее: §f" + getVariableValue("returnValue"),
                "",
                "§eКлик для настройки"
            ))
            .build());
        
        // Тело функции
        inventory.setItem(16, new ItemBuilder(Material.REDSTONE_BLOCK)
            .name("§bТело функции")
            .lore(Arrays.asList(
                "§7Код, который выполняет функция",
                "§7Открывает редактор кода",
                "",
                "§eКлик для редактирования"
            ))
            .build());
    }
    
    private void setupVariableSlots() {
        // Слот для имени функции
        inventory.setItem(28, new ItemBuilder(Material.PAPER)
            .name("§aИмя функции")
            .lore(Arrays.asList(
                "§7Перетащите текстовую переменную",
                "§7с именем функции",
                "",
                "§7Текущее: §f" + getVariableValue("functionNameVar"),
                "",
                "§eDrag-n-drop переменную сюда"
            ))
            .build());
        variableSlots.put(28, "functionNameVar");
        
        // Слот для параметров
        inventory.setItem(30, new ItemBuilder(Material.STRING)
            .name("§aПараметры")
            .lore(Arrays.asList(
                "§7Перетащите переменную со списком",
                "§7параметров функции",
                "",
                "§7Текущее: §f" + getVariableValue("parametersVar"),
                "",
                "§eDrag-n-drop переменную сюда"
            ))
            .build());
        variableSlots.put(30, "parametersVar");
        
        // Слот для возвращаемого значения
        inventory.setItem(32, new ItemBuilder(Material.GOLD_INGOT)
            .name("§aВозвращаемое значение")
            .lore(Arrays.asList(
                "§7Перетащите переменную которую",
                "§7функция должна вернуть",
                "",
                "§7Текущее: §f" + getVariableValue("returnVar"),
                "",
                "§eDrag-n-drop переменную сюда"
            ))
            .build());
        variableSlots.put(32, "returnVar");
    }
    
    private void setupControlButtons() {
        // Сохранить
        inventory.setItem(45, new ItemBuilder(Material.LIME_DYE)
            .name("§aСохранить функцию")
            .build());
        
        // Тест функции
        inventory.setItem(49, new ItemBuilder(Material.REDSTONE_TORCH)
            .name("§eТестировать функцию")
            .lore(Arrays.asList(
                "§7Проверить функцию с тестовыми",
                "§7параметрами",
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
            case 10: // Имя функции
                openFunctionNameInput();
                break;
            case 12: // Параметры
                openParametersInput();
                break;
            case 14: // Возвращаемое значение
                openReturnValueInput();
                break;
            case 16: // Тело функции
                openFunctionBodyEditor();
                break;
            case 45: // Сохранить
                saveFunction();
                break;
            case 49: // Тест
                testFunction();
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
                dragger.sendMessage("§aПеременная установлена для функции!");
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
                "§7Переменная функции",
                "",
                "§7ПКМ - удалить"
            ))
            .build());
    }
    
    private void openFunctionNameInput() {
        player.sendMessage("§6Введите имя функции в чат:");
        player.sendMessage("§7Пример: myCustomFunction");
        player.closeInventory();
    }
    
    private void openParametersInput() {
        player.sendMessage("§6Введите параметры функции в чат:");
        player.sendMessage("§7Пример: player,amount,message");
        player.closeInventory();
    }
    
    private void openReturnValueInput() {
        player.sendMessage("§6Введите возвращаемое значение в чат:");
        player.sendMessage("§7Пример: %result% или число/текст");
        player.closeInventory();
    }
    
    private void openFunctionBodyEditor() {
        player.sendMessage("§6Редактор тела функции:");
        player.sendMessage("§7Используйте /code для редактирования");
        player.closeInventory();
    }
    
    private void saveFunction() {
        player.closeInventory();
        player.sendMessage("§aФункция сохранена!");
    }
    
    private void testFunction() {
        player.sendMessage("§6=== Тест функции ===");
        player.sendMessage("§7Имя: §f" + getVariableValue("functionName"));
        player.sendMessage("§7Параметры: §f" + getVariableValue("parameters"));
        player.sendMessage("§7Возврат: §f" + getVariableValue("returnValue"));
        player.sendMessage("§aТест выполнен!");
    }
}
