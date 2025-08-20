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
 * Уникальное GUI для настройки игровых действий
 */
public class GameActionConfigGUI implements Listener {
    
    private final OpenHousing plugin;
    private final Player player;
    private final CodeBlock block;
    private final Inventory inventory;
    private final Map<Integer, String> variableSlots = new HashMap<>();
    
    public GameActionConfigGUI(OpenHousing plugin, Player player, CodeBlock block) {
        this.plugin = plugin;
        this.player = player;
        this.block = block;
        this.inventory = Bukkit.createInventory(null, 54, "§6Игровое действие");
        
        Bukkit.getPluginManager().registerEvents(this, plugin);
        setupInventory();
    }
    
    private void setupInventory() {
        // Заголовок
        inventory.setItem(4, new ItemBuilder(Material.EMERALD)
            .name("§6Игровое действие")
            .lore(Arrays.asList(
                "§7Настройте действие для сервера/мира",
                "§7Глобальные команды и изменения",
                "",
                "§eDrag-n-drop переменных поддерживается"
            ))
            .build());
        
        setupGameParameters();
        setupVariableSlots();
        setupControlButtons();
    }
    
    private void setupGameParameters() {
        // Тип игрового действия
        inventory.setItem(10, new ItemBuilder(Material.COMMAND_BLOCK)
            .name("§bТип действия")
            .lore(Arrays.asList(
                "§7Что делать в игре:",
                "§7• Установить время/погоду",
                "§7• Выполнить команду",
                "§7• Объявление всем",
                "§7• Сохранить/загрузить мир",
                "§7• Управление сервером",
                "",
                "§eКлик для выбора"
            ))
            .build());
        
        // Область действия
        inventory.setItem(12, new ItemBuilder(Material.BEACON)
            .name("§bОбласть действия")
            .lore(Arrays.asList(
                "§7Где применить действие:",
                "§7• Текущий мир",
                "§7• Все миры",
                "§7• Весь сервер",
                "§7• Конкретный мир",
                "",
                "§eКлик для выбора"
            ))
            .build());
        
        // Параметры выполнения
        inventory.setItem(14, new ItemBuilder(Material.CLOCK)
            .name("§bПараметры выполнения")
            .lore(Arrays.asList(
                "§7Как выполнить:",
                "§7• Немедленно",
                "§7• С задержкой",
                "§7• Повторно",
                "§7• По расписанию",
                "",
                "§eКлик для настройки"
            ))
            .build());
    }
    
    private void setupVariableSlots() {
        // Слот для команды/значения
        inventory.setItem(28, new ItemBuilder(Material.COMMAND_BLOCK_MINECART)
            .name("§aКоманда/Значение")
            .lore(Arrays.asList(
                "§7Перетащите переменную с командой",
                "§7или значением для установки",
                "",
                "§7Текущее: §f" + getVariableValue("commandValue"),
                "",
                "§eDrag-n-drop переменную сюда"
            ))
            .build());
        variableSlots.put(28, "commandValue");
        
        // Слот для мира
        inventory.setItem(30, new ItemBuilder(Material.GRASS_BLOCK)
            .name("§aМир")
            .lore(Arrays.asList(
                "§7Перетащите переменную с именем мира",
                "",
                "§7Текущее: §f" + getVariableValue("worldName"),
                "",
                "§eDrag-n-drop переменную сюда"
            ))
            .build());
        variableSlots.put(30, "worldName");
        
        // Слот для времени/задержки
        inventory.setItem(32, new ItemBuilder(Material.CLOCK)
            .name("§aВремя/Задержка")
            .lore(Arrays.asList(
                "§7Перетащите числовую переменную",
                "§7с временем или задержкой",
                "",
                "§7Текущее: §f" + getVariableValue("timeDelay"),
                "",
                "§eDrag-n-drop переменную сюда"
            ))
            .build());
        variableSlots.put(32, "timeDelay");
        
        // Слот для дополнительных параметров
        inventory.setItem(34, new ItemBuilder(Material.REDSTONE)
            .name("§aДополнительные параметры")
            .lore(Arrays.asList(
                "§7Перетащите переменную с параметрами",
                "",
                "§7Текущее: §f" + getVariableValue("extraParams"),
                "",
                "§eDrag-n-drop переменную сюда"
            ))
            .build());
        variableSlots.put(34, "extraParams");
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
                "§7Посмотреть что произойдет",
                "§7при выполнении действия",
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
            case 10: // Тип действия
                openGameActionSelector();
                break;
            case 12: // Область
                openScopeSelector();
                break;
            case 14: // Параметры
                openExecutionParametersSelector();
                break;
            case 45: // Сохранить
                saveGameAction();
                break;
            case 49: // Предварительный просмотр
                showGamePreview();
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
                dragger.sendMessage("§aПеременная установлена для игрового действия!");
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
                "§7Переменная для игрового действия",
                "",
                "§7ПКМ - удалить"
            ))
            .build());
    }
    
    private void openGameActionSelector() {
        player.sendMessage("§6Игровые действия:");
        player.sendMessage("§71. set_time - установить время");
        player.sendMessage("§72. set_weather - установить погоду");
        player.sendMessage("§73. broadcast - объявление всем");
        player.sendMessage("§74. execute_command - выполнить команду");
        player.sendMessage("§75. save_world - сохранить мир");
        player.sendMessage("§76. create_explosion - создать взрыв");
    }
    
    private void openScopeSelector() {
        player.sendMessage("§6Область действия:");
        player.sendMessage("§71. current_world - текущий мир");
        player.sendMessage("§72. all_worlds - все миры");
        player.sendMessage("§73. server - весь сервер");
        player.sendMessage("§74. world:[имя] - конкретный мир");
    }
    
    private void openExecutionParametersSelector() {
        player.sendMessage("§6Параметры выполнения:");
        player.sendMessage("§7delay:[тики] - задержка");
        player.sendMessage("§7repeat:[раз] - повторения");
        player.sendMessage("§7schedule:[время] - по расписанию");
    }
    
    private void saveGameAction() {
        player.closeInventory();
        player.sendMessage("§aИгровое действие сохранено!");
    }
    
    private void showGamePreview() {
        player.sendMessage("§6=== Предварительный просмотр игрового действия ===");
        player.sendMessage("§7Действие: §f" + getVariableValue("actionType"));
        player.sendMessage("§7Область: §f" + getVariableValue("scope"));
        player.sendMessage("§7Параметры: §f" + getVariableValue("executionParams"));
        player.sendMessage("§aДействие будет применено согласно настройкам!");
    }
}
