package ru.openhousing.coding.gui;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import ru.openhousing.OpenHousing;
import ru.openhousing.coding.values.Value;
import ru.openhousing.coding.values.ValueType;
import ru.openhousing.utils.ItemBuilder;
import ru.openhousing.utils.MessageUtil;

import java.util.Arrays;
import java.util.function.Consumer;

/**
 * GUI для выбора и настройки значений
 */
public class ValueSelectorGUI {
    
    private final OpenHousing plugin;
    private final Player player;
    private final String title;
    private final Consumer<Value> callback;
    private Inventory inventory;
    private Value currentValue;
    
    public ValueSelectorGUI(OpenHousing plugin, Player player, String title, Value currentValue, Consumer<Value> callback) {
        this.plugin = plugin;
        this.player = player;
        this.title = title;
        this.currentValue = currentValue;
        this.callback = callback;
        this.inventory = Bukkit.createInventory(null, 54, "§6Выбор значения: " + title);
    }
    
    /**
     * Открытие GUI
     */
    public void open() {
        setupInventory();
        player.openInventory(inventory);
    }
    
    /**
     * Настройка интерфейса
     */
    private void setupInventory() {
        inventory.clear();
        
        // Заполнение фона
        ItemStack filler = new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE)
            .name(" ")
            .build();
        
        for (int i = 0; i < inventory.getSize(); i++) {
            inventory.setItem(i, filler);
        }
        
        // Заголовок
        ItemStack titleItem = new ItemBuilder(Material.NAME_TAG)
            .name("§6" + title)
            .lore(Arrays.asList(
                "§7Выберите тип значения",
                "§7для настройки параметра",
                "",
                currentValue != null ? "§7Текущее: §f" + currentValue.getDisplayValue() : "§7Не установлено"
            ))
            .build();
        inventory.setItem(4, titleItem);
        
        // Типы значений
        setupValueTypes();
        
        // Кнопки управления
        setupControlButtons();
    }
    
    /**
     * Настройка типов значений
     */
    private void setupValueTypes() {
        ValueType[] types = ValueType.values();
        int startSlot = 19; // Начинаем с третьего ряда
        
        for (int i = 0; i < types.length && i < 7; i++) {
            ValueType type = types[i];
            
            ItemStack typeItem = new ItemBuilder(type.getIcon())
                .name("§e" + type.getDisplayName())
                .lore(Arrays.asList(
                    "§7" + type.getDescription(),
                    "",
                    "§7Клик для выбора"
                ))
                .build();
            
            inventory.setItem(startSlot + i, typeItem);
        }
        
        // Второй ряд для остальных типов
        if (types.length > 7) {
            for (int i = 7; i < types.length && i < 14; i++) {
                ValueType type = types[i];
                
                ItemStack typeItem = new ItemBuilder(type.getIcon())
                    .name("§e" + type.getDisplayName())
                    .lore(Arrays.asList(
                        "§7" + type.getDescription(),
                        "",
                        "§7Клик для выбора"
                    ))
                    .build();
                
                inventory.setItem(startSlot + 9 + (i - 7), typeItem);
            }
        }
    }
    
    /**
     * Кнопки управления
     */
    private void setupControlButtons() {
        // Текущее значение (если есть)
        if (currentValue != null) {
            ItemStack current = new ItemBuilder(Material.WRITABLE_BOOK)
                .name("§aТекущее значение")
                .lore(Arrays.asList(
                    "§7Тип: §f" + currentValue.getType().getDisplayName(),
                    "§7Значение: §f" + currentValue.getDisplayValue(),
                    "",
                    "§7Клик для редактирования"
                ))
                .build();
            inventory.setItem(48, current);
        }
        
        // Удалить значение
        ItemStack remove = new ItemBuilder(Material.BARRIER)
            .name("§cУдалить значение")
            .lore(Arrays.asList(
                "§7Очистить параметр"
            ))
            .build();
        inventory.setItem(49, remove);
        
        // Отмена
        ItemStack cancel = new ItemBuilder(Material.ARROW)
            .name("§7Назад")
            .lore(Arrays.asList(
                "§7Вернуться без изменений"
            ))
            .build();
        inventory.setItem(50, cancel);
    }
    
    /**
     * Обработка кликов
     */
    public void handleClick(int slot) {
        if (slot == 48 && currentValue != null) {
            // Редактирование текущего значения
            openValueEditor(currentValue.getType(), currentValue.getRawValue());
        } else if (slot == 49) {
            // Удаление значения
            callback.accept(null);
            player.closeInventory();
            MessageUtil.send(player, "&cЗначение удалено");
        } else if (slot == 50) {
            // Отмена
            player.closeInventory();
        } else {
            // Проверка типов значений
            ValueType selectedType = getValueTypeBySlot(slot);
            if (selectedType != null) {
                openValueEditor(selectedType, "");
            }
        }
    }
    
    /**
     * Получение типа значения по слоту
     */
    private ValueType getValueTypeBySlot(int slot) {
        ValueType[] types = ValueType.values();
        
        // Первый ряд (слоты 19-25)
        if (slot >= 19 && slot <= 25) {
            int index = slot - 19;
            return index < types.length ? types[index] : null;
        }
        
        // Второй ряд (слоты 28-34)
        if (slot >= 28 && slot <= 34) {
            int index = (slot - 28) + 7;
            return index < types.length ? types[index] : null;
        }
        
        return null;
    }
    
    /**
     * Открытие редактора значения
     */
    private void openValueEditor(ValueType type, String initialValue) {
        player.closeInventory();
        
        // В будущем здесь будет полноценный редактор
        // Пока что показываем простое сообщение
        MessageUtil.send(player, "&eВведите значение для типа: &f" + type.getDisplayName());
        MessageUtil.send(player, "&7Пример: " + getExampleValue(type));
        
        // Создаем значение по умолчанию
        Value defaultValue = Value.create(type, getExampleValue(type));
        callback.accept(defaultValue);
    }
    
    /**
     * Получение примера значения для типа
     */
    private String getExampleValue(ValueType type) {
        switch (type) {
            case TEXT:
                return "Привет, %player%!";
            case NUMBER:
                return "10";
            case VARIABLE:
                return "%player_health%";
            case LOCATION:
                return "world,0,64,0,0,0";
            case POTION_EFFECT:
                return "SPEED,600,0,false,true";
            case PARTICLE:
                return "FLAME,10,0.5,0.5,0.5,0.1";
            case ITEM:
                return "DIAMOND,1,&bСпециальный алмаз";
            case SOUND:
                return "ENTITY_EXPERIENCE_ORB_PICKUP,1.0,1.0";
            default:
                return "";
        }
    }
    
    public Inventory getInventory() {
        return inventory;
    }
    
    public Player getPlayer() {
        return player;
    }
}
