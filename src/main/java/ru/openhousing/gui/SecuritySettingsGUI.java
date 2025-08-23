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
import java.util.UUID;

/**
 * GUI для настройки безопасности дома
 */
public class SecuritySettingsGUI implements Listener {
    
    private final OpenHousing plugin;
    private final Player player;
    private final House house;
    private final Inventory inventory;
    
    public SecuritySettingsGUI(OpenHousing plugin, Player player, House house) {
        this.plugin = plugin;
        this.player = player;
        this.house = house;
        this.inventory = Bukkit.createInventory(null, 54, "§6Настройки безопасности");
        
        setupGUI();
        registerListener();
    }
    
    /**
     * Настройка GUI
     */
    private void setupGUI() {
        // Заголовок
        inventory.setItem(4, new ItemBuilder(Material.SHIELD)
            .name("§6§lНастройки безопасности")
            .lore(Arrays.asList(
                "§7Настройте параметры безопасности",
                "§7вашего дома",
                "",
                "§eДом: §f" + house.getName()
            ))
            .build());
        
        // Публичный/Приватный дом
        boolean isPublic = house.isPublic();
        inventory.setItem(19, new ItemBuilder(isPublic ? Material.GREEN_CONCRETE : Material.RED_CONCRETE)
            .name("§eПубличный дом")
            .lore(Arrays.asList(
                "§7Текущее состояние: " + (isPublic ? "§aВключен" : "§cВыключен"),
                "",
                "§7Если включен, любой игрок может",
                "§7посетить ваш дом",
                "",
                "§eКликните для изменения"
            ))
            .build());
        
        // Разрешить PvP
        Object pvpObj = house.getSetting("pvp_allowed");
        boolean allowPvP = pvpObj instanceof Boolean ? (Boolean) pvpObj : false;
        inventory.setItem(21, new ItemBuilder(allowPvP ? Material.DIAMOND_SWORD : Material.SHIELD)
            .name("§eРазрешить PvP")
            .lore(Arrays.asList(
                "§7Текущее состояние: " + (allowPvP ? "§aРазрешено" : "§cЗапрещено"),
                "",
                "§7Если разрешено, игроки могут",
                "§7атаковать друг друга в доме",
                "",
                "§eКликните для изменения"
            ))
            .build());
        
        // Разрешить взрывы
        Object explosionsObj = house.getSetting("explosions_allowed");
        boolean allowExplosions = explosionsObj instanceof Boolean ? (Boolean) explosionsObj : false;
        inventory.setItem(23, new ItemBuilder(allowExplosions ? Material.TNT : Material.BARRIER)
            .name("§eРазрешить взрывы")
            .lore(Arrays.asList(
                "§7Текущее состояние: " + (allowExplosions ? "§aРазрешено" : "§cЗапрещено"),
                "",
                "§7Если разрешено, взрывы могут",
                "§7разрушать блоки в доме",
                "",
                "§eКликните для изменения"
            ))
            .build());
        
        // Разрешить порталы
        Object portalsObj = house.getSetting("portals_allowed");
        boolean allowPortals = portalsObj instanceof Boolean ? (Boolean) portalsObj : false;
        inventory.setItem(25, new ItemBuilder(allowPortals ? Material.OBSIDIAN : Material.BARRIER)
            .name("§eРазрешить порталы")
            .lore(Arrays.asList(
                "§7Текущее состояние: " + (allowPortals ? "§aРазрешено" : "§cЗапрещено"),
                "",
                "§7Если разрешено, можно создавать",
                "§7порталы в доме",
                "",
                "§eКликните для изменения"
            ))
            .build());
        
        // Максимальное количество игроков
        Object maxPlayersObj = house.getSetting("max_players");
        int maxPlayers = maxPlayersObj instanceof Integer ? (Integer) maxPlayersObj : 10;
        inventory.setItem(37, new ItemBuilder(Material.PLAYER_HEAD)
            .name("§eМаксимум игроков")
            .lore(Arrays.asList(
                "§7Текущее значение: §f" + maxPlayers,
                "",
                "§7Максимальное количество игроков",
                "§7которые могут находиться в доме",
                "",
                "§eКликните для изменения"
            ))
            .build());
        
        // Автоматическое закрытие
        Object autoCloseObj = house.getSetting("auto_close");
        boolean autoClose = autoCloseObj instanceof Boolean ? (Boolean) autoCloseObj : true;
        inventory.setItem(39, new ItemBuilder(autoClose ? Material.CLOCK : Material.BARRIER)
            .name("§eАвтозакрытие")
            .lore(Arrays.asList(
                "§7Текущее состояние: " + (autoClose ? "§aВключено" : "§cВыключено"),
                "",
                "§7Если включено, дом автоматически",
                "§7закрывается при выходе владельца",
                "",
                "§eКликните для изменения"
            ))
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
        if (!event.getView().getTitle().equals("§6Настройки безопасности") || 
            !event.getWhoClicked().equals(player)) {
            return;
        }
        
        event.setCancelled(true);
        
        int slot = event.getRawSlot();
        
        switch (slot) {
            case 19: // Публичный дом
                togglePublic();
                break;
                
            case 21: // PvP
                togglePvP();
                break;
                
            case 23: // Взрывы
                toggleExplosions();
                break;
                
            case 25: // Порталы
                togglePortals();
                break;
                
            case 37: // Максимум игроков
                openMaxPlayersInput();
                break;
                
            case 39: // Автозакрытие
                toggleAutoClose();
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
     * Переключение публичного доступа
     */
    private void togglePublic() {
        boolean newValue = !house.isPublic();
        house.setPublic(newValue);
        setupGUI();
        MessageUtil.send(player, "§aПубличный доступ " + (newValue ? "включен" : "выключен"));
    }
    
    /**
     * Переключение PvP
     */
    private void togglePvP() {
        Object pvpObj = house.getSetting("pvp_allowed");
        boolean currentValue = pvpObj instanceof Boolean ? (Boolean) pvpObj : false;
        boolean newValue = !currentValue;
        house.setSetting("pvp_allowed", newValue);
        setupGUI();
        MessageUtil.send(player, "§aPvP " + (newValue ? "разрешено" : "запрещено"));
    }
    
    /**
     * Переключение взрывов
     */
    private void toggleExplosions() {
        Object explosionsObj = house.getSetting("explosions_allowed");
        boolean currentValue = explosionsObj instanceof Boolean ? (Boolean) explosionsObj : false;
        boolean newValue = !currentValue;
        house.setSetting("explosions_allowed", newValue);
        setupGUI();
        MessageUtil.send(player, "§aВзрывы " + (newValue ? "разрешены" : "запрещены"));
    }
    
    /**
     * Переключение порталов
     */
    private void togglePortals() {
        Object portalsObj = house.getSetting("portals_allowed");
        boolean currentValue = portalsObj instanceof Boolean ? (Boolean) portalsObj : false;
        boolean newValue = !currentValue;
        house.setSetting("portals_allowed", newValue);
        setupGUI();
        MessageUtil.send(player, "§aПорталы " + (newValue ? "разрешены" : "запрещены"));
    }
    
    /**
     * Открытие ввода максимального количества игроков
     */
    private void openMaxPlayersInput() {
        MessageUtil.send(player, 
            "§eВведите максимальное количество игроков:",
            "§7(1-50)"
        );
        
        // Регистрируем временный слушатель чата
        ru.openhousing.listeners.ChatListener.registerTemporaryInput(player, (input) -> {
            try {
                int maxPlayers = Integer.parseInt(input.trim());
                if (maxPlayers >= 1 && maxPlayers <= 50) {
                    house.setSetting("max_players", maxPlayers);
                    setupGUI();
                    open();
                    MessageUtil.send(player, "§aМаксимум игроков установлен: §f" + maxPlayers);
                } else {
                    MessageUtil.send(player, "§cКоличество должно быть от 1 до 50");
                    open();
                }
            } catch (NumberFormatException e) {
                MessageUtil.send(player, "§cВведите корректное число");
                open();
            }
        });
    }
    
    /**
     * Переключение автозакрытия
     */
    private void toggleAutoClose() {
        Object autoCloseObj = house.getSetting("auto_close");
        boolean currentValue = autoCloseObj instanceof Boolean ? (Boolean) autoCloseObj : true;
        boolean newValue = !currentValue;
        house.setSetting("auto_close", newValue);
        setupGUI();
        MessageUtil.send(player, "§aАвтозакрытие " + (newValue ? "включено" : "выключено"));
    }
    
    /**
     * Сохранение и закрытие
     */
    private void saveAndClose() {
        plugin.getDatabaseManager().saveHouse(house);
        MessageUtil.send(player, "§aНастройки безопасности сохранены!");
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
        house.setPublic(false);
        house.setSetting("pvp_allowed", false);
        house.setSetting("explosions_allowed", false);
        house.setSetting("portals_allowed", false);
        house.setSetting("max_players", 10);
        house.setSetting("auto_close", true);
        
        setupGUI();
        MessageUtil.send(player, "§aНастройки сброшены к значениям по умолчанию");
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
