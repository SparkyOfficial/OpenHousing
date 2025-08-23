package ru.openhousing.gui;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import ru.openhousing.OpenHousing;
import ru.openhousing.housing.House;
import ru.openhousing.utils.ItemBuilder;
import ru.openhousing.utils.MessageUtil;

import java.util.Arrays;

/**
 * GUI для настройки времени в доме
 */
public class TimeSettingsGUI implements Listener {
    
    private final OpenHousing plugin;
    private final Player player;
    private final House house;
    private final Inventory inventory;
    
    public TimeSettingsGUI(OpenHousing plugin, Player player, House house) {
        this.plugin = plugin;
        this.player = player;
        this.house = house;
        this.inventory = Bukkit.createInventory(null, 54, "§6Настройки времени");
        
        setupGUI();
        registerListener();
    }
    
    /**
     * Настройка GUI
     */
    private void setupGUI() {
        // Заголовок
        inventory.setItem(4, new ItemBuilder(Material.CLOCK)
            .name("§6§lНастройки времени")
            .lore(Arrays.asList(
                "§7Настройте параметры времени",
                "§7в вашем доме",
                "",
                "§eДом: §f" + house.getName()
            ))
            .build());
        
        // Фиксированное время
        Object fixedTimeObj = house.getSetting("fixed_time");
        boolean fixedTime = fixedTimeObj instanceof Boolean ? (Boolean) fixedTimeObj : false;
        inventory.setItem(19, new ItemBuilder(fixedTime ? Material.CLOCK : Material.BARRIER)
            .name("§eФиксированное время")
            .lore(Arrays.asList(
                "§7Текущее состояние: " + (fixedTime ? "§aВключено" : "§cВыключено"),
                "",
                "§7Если включено, время в доме",
                "§7не будет меняться",
                "",
                "§eКликните для изменения"
            ))
            .build());
        
        // Время дня
        Object timeObj = house.getSetting("time");
        int time = timeObj instanceof Integer ? (Integer) timeObj : 6000; // Полдень по умолчанию
        inventory.setItem(21, new ItemBuilder(Material.SUNFLOWER)
            .name("§eВремя дня")
            .lore(Arrays.asList(
                "§7Текущее время: §f" + getTimeDisplay(time),
                "",
                "§7Установите желаемое время",
                "§7в доме (0-24000)",
                "",
                "§eКликните для изменения"
            ))
            .build());
        
        // Цикл дня и ночи
        Object dayCycleObj = house.getSetting("day_cycle");
        boolean dayCycle = dayCycleObj instanceof Boolean ? (Boolean) dayCycleObj : false;
        inventory.setItem(23, new ItemBuilder(dayCycle ? Material.SUNFLOWER : Material.BARRIER)
            .name("§eЦикл дня и ночи")
            .lore(Arrays.asList(
                "§7Текущее состояние: " + (dayCycle ? "§aВключен" : "§cВыключен"),
                "",
                "§7Если включен, время будет",
                "§7меняться автоматически",
                "",
                "§eКликните для изменения"
            ))
            .build());
        
        // Скорость времени
        Object timeSpeedObj = house.getSetting("time_speed");
        int timeSpeed = timeSpeedObj instanceof Integer ? (Integer) timeSpeedObj : 1;
        inventory.setItem(25, new ItemBuilder(Material.REDSTONE)
            .name("§eСкорость времени")
            .lore(Arrays.asList(
                "§7Текущая скорость: §f" + timeSpeed + "x",
                "",
                "§7Множитель скорости времени",
                "§7(1x = нормальная, 2x = в 2 раза быстрее)",
                "",
                "§eКликните для изменения"
            ))
            .build());
        
        // Быстрый выбор времени
        inventory.setItem(37, new ItemBuilder(Material.YELLOW_CONCRETE)
            .name("§eРассвет (6:00)")
            .lore(Arrays.asList("§7Установить время рассвета"))
            .build());
            
        inventory.setItem(38, new ItemBuilder(Material.ORANGE_CONCRETE)
            .name("§6Полдень (12:00)")
            .lore(Arrays.asList("§7Установить полдень"))
            .build());
            
        inventory.setItem(39, new ItemBuilder(Material.RED_CONCRETE)
            .name("§cЗакат (18:00)")
            .lore(Arrays.asList("§7Установить время заката"))
            .build());
            
        inventory.setItem(40, new ItemBuilder(Material.BLUE_CONCRETE)
            .name("§9Полночь (0:00)")
            .lore(Arrays.asList("§7Установить полночь"))
            .build());
        
        // Кнопки управления
        inventory.setItem(45, new ItemBuilder(Material.LIME_CONCRETE)
            .name("§a§lСохранить")
            .lore(Arrays.asList(
                "§7Сохранить настройки",
                "§7и вернуться к настройкам дома"
            ))
            .build());
        
        inventory.setItem(47, new ItemBuilder(Material.RED_CONCRETE)
            .name("§c§lОтмена")
            .lore(Arrays.asList(
                "§7Отменить изменения",
                "§7и вернуться к настройкам дома"
            ))
            .build());
        
        inventory.setItem(49, new ItemBuilder(Material.BARRIER)
            .name("§4§lСбросить")
            .lore(Arrays.asList(
                "§7Сбросить все настройки",
                "§7к значениям по умолчанию"
            ))
            .build());
    }
    
    /**
     * Получение отображаемого времени
     */
    private String getTimeDisplay(int time) {
        int hours = (time / 1000 + 6) % 24;
        int minutes = (time % 1000) * 60 / 1000;
        return String.format("%02d:%02d (%d)", hours, minutes, time);
    }
    
    /**
     * Регистрация слушателя
     */
    private void registerListener() {
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }
    
    /**
     * Открытие GUI
     */
    public void open() {
        player.openInventory(inventory);
    }
    
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getView().getTitle().equals("§6Настройки времени") || 
            !event.getWhoClicked().equals(player)) {
            return;
        }
        
        event.setCancelled(true);
        
        int slot = event.getRawSlot();
        
        switch (slot) {
            case 19: // Фиксированное время
                toggleFixedTime();
                break;
                
            case 21: // Время дня
                openTimeInput();
                break;
                
            case 23: // Цикл дня и ночи
                toggleDayCycle();
                break;
                
            case 25: // Скорость времени
                openTimeSpeedInput();
                break;
                
            case 37: // Рассвет
                setTime(1000); // 6:00
                break;
                
            case 38: // Полдень
                setTime(6000); // 12:00
                break;
                
            case 39: // Закат
                setTime(12000); // 18:00
                break;
                
            case 40: // Полночь
                setTime(18000); // 0:00
                break;
                
            case 45: // Сохранить
                saveAndClose();
                break;
                
            case 47: // Отмена
                cancelAndClose();
                break;
                
            case 49: // Сбросить
                resetToDefaults();
                break;
        }
    }
    
    /**
     * Переключение фиксированного времени
     */
    private void toggleFixedTime() {
        Object fixedTimeObj = house.getSetting("fixed_time");
        boolean currentValue = fixedTimeObj instanceof Boolean ? (Boolean) fixedTimeObj : false;
        boolean newValue = !currentValue;
        house.setSetting("fixed_time", newValue);
        setupGUI();
        MessageUtil.send(player, "§aФиксированное время " + (newValue ? "включено" : "выключено"));
    }
    
    /**
     * Открытие ввода времени
     */
    private void openTimeInput() {
        MessageUtil.send(player, 
            "§eВведите время (0-24000):",
            "§7Где 0 = полночь, 6000 = полдень"
        );
        
        // Регистрируем временный слушатель чата
        ru.openhousing.listeners.ChatListener.registerTemporaryInput(player, (input) -> {
            try {
                int time = Integer.parseInt(input.trim());
                if (time >= 0 && time <= 24000) {
                    setTime(time);
                } else {
                    MessageUtil.send(player, "§cВремя должно быть от 0 до 24000");
                    open();
                }
            } catch (NumberFormatException e) {
                MessageUtil.send(player, "§cВведите корректное число");
                open();
            }
        });
    }
    
    /**
     * Установка времени
     */
    private void setTime(int time) {
        house.setSetting("time", time);
        setupGUI();
        MessageUtil.send(player, "§aВремя установлено: §f" + getTimeDisplay(time));
    }
    
    /**
     * Переключение цикла дня и ночи
     */
    private void toggleDayCycle() {
        Object dayCycleObj = house.getSetting("day_cycle");
        boolean currentValue = dayCycleObj instanceof Boolean ? (Boolean) dayCycleObj : false;
        boolean newValue = !currentValue;
        house.setSetting("day_cycle", newValue);
        setupGUI();
        MessageUtil.send(player, "§aЦикл дня и ночи " + (newValue ? "включен" : "выключен"));
    }
    
    /**
     * Открытие ввода скорости времени
     */
    private void openTimeSpeedInput() {
        MessageUtil.send(player, 
            "§eВведите скорость времени:",
            "§7(1-10, где 1 = нормальная скорость)"
        );
        
        // Регистрируем временный слушатель чата
        ru.openhousing.listeners.ChatListener.registerTemporaryInput(player, (input) -> {
            try {
                int speed = Integer.parseInt(input.trim());
                if (speed >= 1 && speed <= 10) {
                    house.setSetting("time_speed", speed);
                    setupGUI();
                    open();
                    MessageUtil.send(player, "§aСкорость времени установлена: §f" + speed + "x");
                } else {
                    MessageUtil.send(player, "§cСкорость должна быть от 1 до 10");
                    open();
                }
            } catch (NumberFormatException e) {
                MessageUtil.send(player, "§cВведите корректное число");
                open();
            }
        });
    }
    
    /**
     * Сохранение и закрытие
     */
    private void saveAndClose() {
        plugin.getDatabaseManager().saveHouse(house);
        MessageUtil.send(player, "§aНастройки времени сохранены!");
        close();
    }
    
    /**
     * Отмена и закрытие
     */
    private void cancelAndClose() {
        MessageUtil.send(player, "§7Изменения отменены");
        close();
    }
    
    /**
     * Сброс к значениям по умолчанию
     */
    private void resetToDefaults() {
        house.setSetting("fixed_time", false);
        house.setSetting("time", 6000); // Полдень
        house.setSetting("day_cycle", false);
        house.setSetting("time_speed", 1);
        
        setupGUI();
        MessageUtil.send(player, "§aНастройки времени сброшены к значениям по умолчанию");
    }
    
    /**
     * Закрытие GUI
     */
    private void close() {
        player.closeInventory();
        InventoryClickEvent.getHandlerList().unregister(this);
        
        // Возвращаемся к настройкам дома
        new HouseSettingsGUI(plugin, player, house).open();
    }
}
