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
 * Уникальное GUI для настройки действий с существами
 */
public class EntityActionConfigGUI implements Listener {
    
    private final OpenHousing plugin;
    private final Player player;
    private final CodeBlock block;
    private final Inventory inventory;
    private final Map<Integer, String> variableSlots = new HashMap<>();
    
    public EntityActionConfigGUI(OpenHousing plugin, Player player, CodeBlock block) {
        this.plugin = plugin;
        this.player = player;
        this.block = block;
        this.inventory = Bukkit.createInventory(null, 54, "§6Действие с существом");
        
        Bukkit.getPluginManager().registerEvents(this, plugin);
        setupInventory();
    }
    
    private void setupInventory() {
        // Заголовок
        inventory.setItem(4, new ItemBuilder(Material.ZOMBIE_HEAD)
            .name("§6Действие с существом")
            .lore(Arrays.asList(
                "§7Настройте действие для существ",
                "§7Поддерживает все типы мобов",
                "",
                "§eDrag-n-drop переменных поддерживается"
            ))
            .build());
        
        setupEntityParameters();
        setupVariableSlots();
        setupControlButtons();
    }
    
    private void setupEntityParameters() {
        // Тип действия с существом
        inventory.setItem(10, new ItemBuilder(Material.BONE)
            .name("§bТип действия")
            .lore(Arrays.asList(
                "§7Что делать с существом:",
                "§7• Убить существо",
                "§7• Исцелить существо", 
                "§7• Приручить существо",
                "§7• Изменить тип существа",
                "§7• Телепортировать существо",
                "§7• Дать эффект существу",
                "",
                "§eКлик для выбора"
            ))
            .build());
        
        // Цель - какое существо
        inventory.setItem(12, new ItemBuilder(Material.LEAD)
            .name("§bЦелевое существо")
            .lore(Arrays.asList(
                "§7Какое существо выбрать:",
                "§7• %entity% - из события",
                "§7• %victim% - жертва",
                "§7• %damager% - атакующий",
                "§7• Все существа типа",
                "§7• Существа в радиусе",
                "",
                "§eКлик для выбора"
            ))
            .build());
        
        // Параметры действия
        inventory.setItem(14, new ItemBuilder(Material.CHICKEN_SPAWN_EGG)
            .name("§bПараметры")
            .lore(Arrays.asList(
                "§7Дополнительные настройки:",
                "§7• Радиус действия",
                "§7• Тип существа",
                "§7• Сила эффекта",
                "",
                "§eКлик для настройки"
            ))
            .build());
    }
    
    private void setupVariableSlots() {
        // Слот для существа
        inventory.setItem(28, new ItemBuilder(Material.ZOMBIE_SPAWN_EGG)
            .name("§aПеременная существа")
            .lore(Arrays.asList(
                "§7Перетащите переменную с именем существа",
                "",
                "§7Текущее: §f" + getVariableValue("entityVariable"),
                "",
                "§eDrag-n-drop переменную сюда"
            ))
            .build());
        variableSlots.put(28, "entityVariable");
        
        // Слот для параметра действия
        inventory.setItem(30, new ItemBuilder(Material.REDSTONE)
            .name("§aПараметр действия")
            .lore(Arrays.asList(
                "§7Перетащите переменную с параметром",
                "§7(количество здоровья, тип моба и т.д.)",
                "",
                "§7Текущее: §f" + getVariableValue("actionParameter"),
                "",
                "§eDrag-n-drop переменную сюда"
            ))
            .build());
        variableSlots.put(30, "actionParameter");
        
        // Слот для местоположения
        inventory.setItem(32, new ItemBuilder(Material.COMPASS)
            .name("§aМестоположение")
            .lore(Arrays.asList(
                "§7Перетащите переменную местоположения",
                "§7для телепортации существа",
                "",
                "§7Текущее: §f" + getVariableValue("locationVariable"),
                "",
                "§eDrag-n-drop переменную сюда"
            ))
            .build());
        variableSlots.put(32, "locationVariable");
        
        // Слот для эффекта
        inventory.setItem(34, new ItemBuilder(Material.POTION)
            .name("§aЭффект зелья")
            .lore(Arrays.asList(
                "§7Перетащите переменную эффекта зелья",
                "§7для применения к существу",
                "",
                "§7Текущее: §f" + getVariableValue("potionEffect"),
                "",
                "§eDrag-n-drop переменную сюда"
            ))
            .build());
        variableSlots.put(34, "potionEffect");
    }
    
    private void setupControlButtons() {
        // Сохранить
        inventory.setItem(45, new ItemBuilder(Material.LIME_DYE)
            .name("§aСохранить действие")
            .build());
        
        // Тест действия
        inventory.setItem(49, new ItemBuilder(Material.REDSTONE_TORCH)
            .name("§eТестировать действие")
            .lore(Arrays.asList(
                "§7Проверить действие с текущими",
                "§7параметрами на ближайшем существе",
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
            case 10: // Тип действия
                openEntityActionSelector();
                break;
            case 12: // Цель
                openEntityTargetSelector();
                break;
            case 14: // Параметры
                openEntityParametersSelector();
                break;
            case 45: // Сохранить
                saveEntityAction();
                break;
            case 49: // Тест
                testEntityAction();
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
                dragger.sendMessage("§aПеременная установлена для существа!");
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
                "§7Переменная для существа",
                "",
                "§7ПКМ - удалить"
            ))
            .build());
    }
    
    private void openEntityActionSelector() {
        player.sendMessage("§6Действия с существами:");
        player.sendMessage("§71. kill - убить существо");
        player.sendMessage("§72. heal - исцелить существо");
        player.sendMessage("§73. tame - приручить существо");
        player.sendMessage("§74. transform - изменить тип");
        player.sendMessage("§75. teleport - телепортировать");
        player.sendMessage("§76. give_effect - дать эффект");
        player.sendMessage("§77. remove - удалить существо");
        player.sendMessage("§78. spawn - заспавнить существо");
    }
    
    private void openEntityTargetSelector() {
        player.sendMessage("§6Выбор существа:");
        player.sendMessage("§71. %entity% - из события");
        player.sendMessage("§72. %victim% - жертва");
        player.sendMessage("§73. %damager% - атакующий");
        player.sendMessage("§74. all:[тип] - все существа типа");
        player.sendMessage("§75. radius:[число] - в радиусе");
    }
    
    private void openEntityParametersSelector() {
        player.sendMessage("§6Параметры действия:");
        player.sendMessage("§7health:[число] - количество здоровья");
        player.sendMessage("§7type:[тип] - тип существа");
        player.sendMessage("§7effect:[эффект] - эффект зелья");
        player.sendMessage("§7radius:[число] - радиус действия");
    }
    
    private void saveEntityAction() {
        player.closeInventory();
        player.sendMessage("§aДействие с существом сохранено!");
    }
    
    private void testEntityAction() {
        player.sendMessage("§6=== Тест действия с существом ===");
        player.sendMessage("§7Действие: §f" + getVariableValue("actionType"));
        player.sendMessage("§7Цель: §f" + getVariableValue("entityTarget"));
        player.sendMessage("§aТест выполнен на ближайшем существе!");
    }
}
