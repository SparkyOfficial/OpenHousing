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
import ru.openhousing.coding.variables.VariableType;
import ru.openhousing.utils.ItemBuilder;

import java.util.Arrays;
import java.util.function.Consumer;

/**
 * GUI для получения переменных (железный слиток в 9 слоте)
 * Согласно википедии: "Переменную можно получить нажав по железному слитку"
 */
public class VariableToolbarGUI implements Listener {
    
    private final OpenHousing plugin;
    private final Player player;
    private final Consumer<VariableType> onVariableSelected;
    private final Inventory inventory;
    
    public VariableToolbarGUI(OpenHousing plugin, Player player, Consumer<VariableType> onVariableSelected) {
        this.plugin = plugin;
        this.player = player;
        this.onVariableSelected = onVariableSelected;
        this.inventory = Bukkit.createInventory(null, 27, "§6Получить переменную");
        
        Bukkit.getPluginManager().registerEvents(this, plugin);
        setupInventory();
    }
    
    /**
     * Настройка содержимого инвентаря
     */
    private void setupInventory() {
        // Заголовок
        inventory.setItem(4, new ItemBuilder(Material.IRON_INGOT)
            .name("§6Переменные")
            .lore(Arrays.asList(
                "§7Выберите тип переменной",
                "§7для использования в коде",
                "",
                "§7Название можно изменить,",
                "§7взяв в руку и написав в чат"
            ))
            .build());
        
        // Все типы переменных
        int slot = 10;
        for (VariableType type : VariableType.values()) {
            inventory.setItem(slot, new ItemBuilder(type.getMaterial())
                .name("§b" + type.getDisplayName())
                .lore(Arrays.asList(
                    "§7" + type.getDescription(),
                    "",
                    getVariableSpecialInfo(type),
                    "",
                    "§eНажмите для получения"
                ))
                .build());
            slot++;
            
            // Переход на следующий ряд
            if (slot == 17) slot = 19;
        }
        
        // Кнопка закрытия
        inventory.setItem(22, new ItemBuilder(Material.BARRIER)
            .name("§cЗакрыть")
            .lore("§7Закрыть окно переменных")
            .build());
    }
    
    /**
     * Получение специальной информации о переменной
     */
    private String getVariableSpecialInfo(VariableType type) {
        switch (type) {
            case TEXT:
                return "§7Для названий предметов/сущностей, сообщений";
            case NUMBER:
                return "§7Включая Pi и e. Обязательно для численных функций";
            case LOCATION:
                return "§7ПКМ по пустоте - записать позицию, ПКМ по блоку - координаты блока";
            case DYNAMIC:
                return "§7Shift+ПКМ - сохранить навсегда (добавит 'СОХРАНЕНО')";
            case GAME_VALUE:
                return "§73 раздела: Сущности, События, Игра";
            case POTION_EFFECT:
                return "§7Настройка в чате: '4:30' (время), '3' (сила)";
            case PARTICLE_EFFECT:
                return "§7Выбор эффекта для визуальных действий";
            default:
                return "§7Переменная для использования в коде";
        }
    }
    
    /**
     * Открытие GUI
     */
    public void open() {
        player.openInventory(inventory);
    }
    
    /**
     * Обработка кликов в инвентаре
     */
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getInventory().equals(inventory)) return;
        if (!(event.getWhoClicked() instanceof Player)) return;
        
        Player clicker = (Player) event.getWhoClicked();
        if (!clicker.equals(player)) return;
        
        event.setCancelled(true);
        
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) return;
        
        int slot = event.getSlot();
        
        // Закрытие
        if (slot == 22) {
            clicker.closeInventory();
            return;
        }
        
        // Выбор переменной
        VariableType selectedType = getVariableTypeFromSlot(slot);
        if (selectedType != null) {
            clicker.closeInventory();
            
            if (onVariableSelected != null) {
                onVariableSelected.accept(selectedType);
            }
            
            // Даём переменную игроку в инвентарь
            giveVariableToPlayer(clicker, selectedType);
        }
    }
    
    /**
     * Получение типа переменной по слоту
     */
    private VariableType getVariableTypeFromSlot(int slot) {
        VariableType[] types = VariableType.values();
        
        switch (slot) {
            case 10: return types[0]; // TEXT
            case 11: return types[1]; // NUMBER  
            case 12: return types[2]; // LOCATION
            case 13: return types[3]; // DYNAMIC
            case 14: return types[4]; // GAME_VALUE
            case 15: return types[5]; // POTION_EFFECT
            case 16: return types[6]; // PARTICLE_EFFECT
            default: return null;
        }
    }
    
    /**
     * Выдача переменной игроку в инвентарь
     */
    private void giveVariableToPlayer(Player player, VariableType type) {
        ItemStack variable = new ItemBuilder(type.getMaterial())
            .name("§b" + type.getDisplayName())
            .lore(Arrays.asList(
                "§7" + type.getDescription(),
                "",
                "§7Возьмите в руку и напишите в чат",
                "§7для изменения названия",
                "",
                getVariableUsageHint(type)
            ))
            .build();
            
        // Добавляем NBT метку для распознавания как переменной
        variable.getItemMeta().setCustomModelData(type.ordinal() + 1000);
        
        player.getInventory().addItem(variable);
        player.sendMessage("§aПолучена переменная: §f" + type.getDisplayName());
        
        // Подсказка об использовании
        if (type == VariableType.DYNAMIC) {
            player.sendMessage("§7Совет: Используйте Shift+ПКМ для сохранения навсегда");
        } else if (type == VariableType.LOCATION) {
            player.sendMessage("§7Совет: ПКМ по пустоте для записи позиции");
        }
    }
    
    /**
     * Подсказка по использованию переменной
     */
    private String getVariableUsageHint(VariableType type) {
        switch (type) {
            case DYNAMIC:
                return "§eShift+ПКМ - сохранить переменную навсегда";
            case LOCATION:
                return "§eПКМ - записать координаты, Shift+ЛКМ - телепорт";
            case POTION_EFFECT:
                return "§eВ чате: '4:30' (время), '3' (сила зелья)";
            default:
                return "§eИспользуйте в блоках кода для хранения данных";
        }
    }
}
