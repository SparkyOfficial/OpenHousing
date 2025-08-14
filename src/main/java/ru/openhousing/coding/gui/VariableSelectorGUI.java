package ru.openhousing.coding.gui;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import ru.openhousing.OpenHousing;
import ru.openhousing.coding.script.CodeScript;
import ru.openhousing.utils.ItemBuilder;
import ru.openhousing.utils.MessageUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

/**
 * GUI для выбора переменной
 */
public class VariableSelectorGUI implements Listener {
    
    private final OpenHousing plugin;
    private final Player player;
    private final CodeScript script;
    private final Inventory inventory;
    private final Consumer<String> onVariableSelected;
    private int page = 0;
    
    public VariableSelectorGUI(OpenHousing plugin, Player player, CodeScript script, Consumer<String> onVariableSelected) {
        this.plugin = plugin;
        this.player = player;
        this.script = script;
        this.onVariableSelected = onVariableSelected;
        this.inventory = Bukkit.createInventory(null, 54, "§6Выбор переменной");
        
        setupGUI();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }
    
    public void open() {
        player.openInventory(inventory);
    }
    
    private void setupGUI() {
        inventory.clear();
        
        // Стандартные системные переменные
        inventory.setItem(1, new ItemBuilder(Material.CLOCK)
            .name("§6{time}")
            .lore(Arrays.asList(
                "§7Текущее время в мире",
                "§7в тиках",
                "",
                "§eКлик для выбора"
            ))
            .build());
            
        inventory.setItem(2, new ItemBuilder(Material.COMPASS)
            .name("§6{player}")
            .lore(Arrays.asList(
                "§7Имя текущего игрока",
                "",
                "§eКлик для выбора"
            ))
            .build());
            
        inventory.setItem(3, new ItemBuilder(Material.MAP)
            .name("§6{world}")
            .lore(Arrays.asList(
                "§7Название мира",
                "",
                "§eКлик для выбора"
            ))
            .build());
            
        inventory.setItem(4, new ItemBuilder(Material.EXPERIENCE_BOTTLE)
            .name("§6{level}")
            .lore(Arrays.asList(
                "§7Уровень игрока",
                "",
                "§eКлик для выбора"
            ))
            .build());
            
        inventory.setItem(5, new ItemBuilder(Material.GOLDEN_APPLE)
            .name("§6{health}")
            .lore(Arrays.asList(
                "§7Здоровье игрока",
                "",
                "§eКлик для выбора"
            ))
            .build());
            
        inventory.setItem(6, new ItemBuilder(Material.COOKED_BEEF)
            .name("§6{food}")
            .lore(Arrays.asList(
                "§7Уровень голода игрока",
                "",
                "§eКлик для выбора"
            ))
            .build());
            
        inventory.setItem(7, new ItemBuilder(Material.EMERALD)
            .name("§6{money}")
            .lore(Arrays.asList(
                "§7Деньги игрока",
                "§7(если установлен Vault)",
                "",
                "§eКлик для выбора"
            ))
            .build());
        
        // Кнопка создания новой переменной
        inventory.setItem(8, new ItemBuilder(Material.LIME_DYE)
            .name("§aСоздать переменную")
            .lore(Arrays.asList(
                "§7Создать новую переменную",
                "§7для скрипта",
                "",
                "§eКлик для создания"
            ))
            .build());
        
        // Пользовательские переменные из скрипта
        Set<String> userVariables = script.getUsedVariables();
        List<String> variableList = new ArrayList<>(userVariables);
        
        int maxVars = 28; // 4 ряда по 7 переменных
        int startIndex = page * maxVars;
        int endIndex = Math.min(startIndex + maxVars, variableList.size());
        
        int slot = 18; // Начинаем с третьего ряда
        for (int i = startIndex; i < endIndex; i++) {
            String variable = variableList.get(i);
            
            // Определяем тип переменной по имени
            Material varMaterial = getVariableMaterial(variable);
            
            ItemStack varItem = new ItemBuilder(varMaterial)
                .name("§e{" + variable + "}")
                .lore(Arrays.asList(
                    "§7Пользовательская переменная",
                    "§7Тип: §f" + getVariableType(variable),
                    "§7Использований: §f" + getVariableUsageCount(variable),
                    "",
                    "§eЛевый клик - выбрать",
                    "§eПравый клик - переименовать"
                ))
                .build();
            
            inventory.setItem(slot, varItem);
            slot++;
            
            // Пропускаем границы инвентаря
            if (slot == 26) slot = 27;
            if (slot == 35) slot = 36;
            if (slot == 44) slot = 45;
        }
        
        // Навигация
        if (page > 0) {
            inventory.setItem(45, new ItemBuilder(Material.ARROW)
                .name("§7Предыдущая страница")
                .build());
        }
        
        if (endIndex < variableList.size()) {
            inventory.setItem(53, new ItemBuilder(Material.ARROW)
                .name("§7Следующая страница")
                .build());
        }
        
        // Кнопка закрытия
        inventory.setItem(49, new ItemBuilder(Material.BARRIER)
            .name("§cОтмена")
            .lore("§7Закрыть без выбора")
            .build());
    }
    
    private Material getVariableMaterial(String variable) {
        String varLower = variable.toLowerCase();
        if (varLower.contains("count") || varLower.contains("number") || varLower.contains("amount")) {
            return Material.PAPER;
        } else if (varLower.contains("name") || varLower.contains("text") || varLower.contains("message")) {
            return Material.WRITABLE_BOOK;
        } else if (varLower.contains("location") || varLower.contains("pos")) {
            return Material.COMPASS;
        } else if (varLower.contains("item")) {
            return Material.CHEST;
        } else {
            return Material.BOOK;
        }
    }
    
    private String getVariableType(String variable) {
        String varLower = variable.toLowerCase();
        if (varLower.contains("count") || varLower.contains("number") || varLower.contains("amount")) {
            return "Число";
        } else if (varLower.contains("name") || varLower.contains("text") || varLower.contains("message")) {
            return "Текст";
        } else if (varLower.contains("location") || varLower.contains("pos")) {
            return "Локация";
        } else if (varLower.contains("item")) {
            return "Предмет";
        } else {
            return "Смешанный";
        }
    }
    
    private int getVariableUsageCount(String variable) {
        // TODO: Реализовать подсчет использований переменной в скрипте
        return 1;
    }
    
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getView().getTitle().equals("§6Выбор переменной")) {
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
        boolean isRightClick = event.getClick().isRightClick();
        
        // Системные переменные
        if (slot == 1) {
            selectVariable("time");
        } else if (slot == 2) {
            selectVariable("player");
        } else if (slot == 3) {
            selectVariable("world");
        } else if (slot == 4) {
            selectVariable("level");
        } else if (slot == 5) {
            selectVariable("health");
        } else if (slot == 6) {
            selectVariable("food");
        } else if (slot == 7) {
            selectVariable("money");
        } else if (slot == 8) {
            // Создать новую переменную
            player.closeInventory();
            MessageUtil.send(player, "§6Введите имя новой переменной в чат:");
            // TODO: Система ввода через чат
        } else if (slot == 45 && page > 0) {
            page--;
            setupGUI();
        } else if (slot == 53) {
            Set<String> userVariables = script.getUsedVariables();
            if ((page + 1) * 28 < userVariables.size()) {
                page++;
                setupGUI();
            }
        } else if (slot == 49) {
            player.closeInventory();
            MessageUtil.send(player, "§7Выбор переменной отменен");
        } else if (slot >= 18 && slot < 45) {
            // Клик по пользовательской переменной
            Set<String> userVariables = script.getUsedVariables();
            List<String> variableList = new ArrayList<>(userVariables);
            
            int relativeSlot = slot - 18;
            if (relativeSlot >= 9) relativeSlot -= 9;
            if (relativeSlot >= 18) relativeSlot -= 9;
            
            int varIndex = page * 28 + relativeSlot;
            
            if (varIndex >= 0 && varIndex < variableList.size()) {
                String variable = variableList.get(varIndex);
                
                if (isRightClick) {
                    // Переименование переменной
                    player.closeInventory();
                    MessageUtil.send(player, "§6Введите новое имя для переменной '" + variable + "':");
                    // TODO: Система переименования переменной
                } else {
                    // Выбор переменной
                    selectVariable(variable);
                }
            }
        }
    }
    
    private void selectVariable(String variableName) {
        player.closeInventory();
        MessageUtil.send(player, "§aВыбрана переменная: §e{" + variableName + "}");
        
        if (onVariableSelected != null) {
            onVariableSelected.accept("{" + variableName + "}");
        }
        
        // Отменяем регистрацию листенера
        InventoryClickEvent.getHandlerList().unregister(this);
    }
}
