package ru.openhousing.gui;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import ru.openhousing.OpenHousing;
import ru.openhousing.coding.gui.helpers.AnvilGUIHelper;
import ru.openhousing.housing.House;
import ru.openhousing.utils.ItemBuilder;
import ru.openhousing.utils.MessageUtil;

import java.util.Arrays;
import java.util.UUID;

/**
 * GUI для настроек дома
 */
public class HouseSettingsGUI implements Listener {
    
    private final OpenHousing plugin;
    private final Player player;
    private final House house;
    private final Inventory inventory;
    
    public HouseSettingsGUI(OpenHousing plugin, Player player, House house) {
        this.plugin = plugin;
        this.player = player;
        this.house = house;
        this.inventory = Bukkit.createInventory(null, 45, "§6Настройки дома");
        
        setupGUI();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }
    
    /**
     * Открытие GUI
     */
    public void open() {
        player.openInventory(inventory);
    }
    
    /**
     * Настройка GUI
     */
    private void setupGUI() {
        inventory.clear();
        
        // Заголовок
        inventory.setItem(4, new ItemBuilder(Material.WHITE_BANNER)
            .name("§6Настройки дома")
            .lore(Arrays.asList(
                "§7Владелец: §e" + house.getOwner(),
                "§7Статус: " + (house.isPublic() ? "§aПубличный" : "§cПриватный"),
                "§7Разрешенных игроков: §e" + house.getAllowedPlayers().size(),
                "§7Заблокированных игроков: §e" + house.getBannedPlayers().size(),
                "",
                "§7Настройте параметры дома ниже"
            ))
            .build());
        
        // Публичность дома
        inventory.setItem(19, new ItemBuilder(house.isPublic() ? Material.LIME_DYE : Material.GRAY_DYE)
            .name(house.isPublic() ? "§aДом публичный" : "§cДом приватный")
            .lore(Arrays.asList(
                "§7Публичные дома доступны всем игрокам",
                "§7Приватные дома - только разрешенным",
                "",
                "§eКлик для переключения"
            ))
            .build());
        
        // Управление разрешенными игроками
        inventory.setItem(20, new ItemBuilder(Material.PLAYER_HEAD)
            .name("§6Разрешенные игроки")
            .lore(Arrays.asList(
                "§7Игроки с доступом к дому:",
                house.getAllowedPlayers().isEmpty() ? "§7Нет разрешенных игроков" : "",
                "",
                "§eЛевый клик - добавить игрока",
                "§eПравый клик - просмотреть список"
            ))
            .build());
        
        // Управление заблокированными игроками
        inventory.setItem(21, new ItemBuilder(Material.BARRIER)
            .name("§cЗаблокированные игроки")
            .lore(Arrays.asList(
                "§7Игроки без доступа к дому:",
                house.getBannedPlayers().isEmpty() ? "§7Нет заблокированных игроков" : "",
                "",
                "§eЛевый клик - заблокировать игрока",
                "§eПравый клик - просмотреть список"
            ))
            .build());
        
        // Настройки дома
        inventory.setItem(23, new ItemBuilder(Material.WRITABLE_BOOK)
            .name("§6Описание дома")
            .lore(Arrays.asList(
                "§7Текущее описание:",
                house.getDescription().isEmpty() ? "§7Нет описания" : "§f" + house.getDescription(),
                "",
                "§eКлик для изменения"
            ))
            .build());
        
        inventory.setItem(24, new ItemBuilder(Material.COMPASS)
            .name("§6Точка спавна дома")
            .lore(Arrays.asList(
                "§7Установить точку телепортации",
                "§7при входе в дом",
                "",
                "§eКлик для установки на текущую позицию"
            ))
            .build());
        
        // Дополнительные настройки
        inventory.setItem(29, new ItemBuilder(Material.REDSTONE)
            .name("§6Настройки безопасности")
            .lore(Arrays.asList(
                "§7Настройки защиты дома",
                "§7и разрешений",
                "",
                "§eКлик для открытия"
            ))
            .build());
        
        inventory.setItem(30, new ItemBuilder(Material.CLOCK)
            .name("§6Временные настройки")
            .lore(Arrays.asList(
                "§7Настройки времени и погоды",
                "§7в доме",
                "",
                "§eКлик для открытия"
            ))
            .build());
        
        inventory.setItem(31, new ItemBuilder(Material.MUSIC_DISC_BLOCKS)
            .name("§6Звуковые настройки")
            .lore(Arrays.asList(
                "§7Настройки звуков и музыки",
                "§7в доме",
                "",
                "§eКлик для открытия"
            ))
            .build());
        
        // Управляющие кнопки
        inventory.setItem(38, new ItemBuilder(Material.LIME_CONCRETE)
            .name("§aСохранить")
            .lore("§7Сохранить все изменения")
            .build());
        
        inventory.setItem(42, new ItemBuilder(Material.RED_CONCRETE)
            .name("§cЗакрыть")
            .lore("§7Закрыть меню настроек")
            .build());
        
        // Опасные действия
        inventory.setItem(40, new ItemBuilder(Material.TNT)
            .name("§cУдалить дом")
            .lore(Arrays.asList(
                "§7Полностью удалить дом",
                "",
                "§c§lВНИМАНИЕ: Действие необратимо!",
                "§eКлик для подтверждения"
            ))
            .build());
    }
    
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        String title = null;
        try {
            title = event.getView().getTitle();
        } catch (NoSuchMethodError e) {
            return; // Пропускаем если не можем получить title
        }
        
        if (title == null || !title.equals("§6Настройки дома")) {
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
        
        // Проверяем права доступа
        if (!house.getOwner().equals(player.getName()) && !player.hasPermission("openhousing.admin")) {
            MessageUtil.send(player, "&cУ вас нет прав для изменения настроек этого дома!");
            return;
        }
        
        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null || clickedItem.getType() == Material.AIR) {
            return;
        }
        
        int slot = event.getSlot();
        boolean isRightClick = event.getClick().isRightClick();
        
        handleClick(slot, isRightClick);
    }
    
    /**
     * Обработка кликов
     */
    private void handleClick(int slot, boolean isRightClick) {
        switch (slot) {
            case 19: // Публичность
                house.setPublic(!house.isPublic());
                MessageUtil.send(player, "&aДом теперь " + (house.isPublic() ? "публичный" : "приватный"));
                setupGUI();
                break;
                
            case 20: // Разрешенные игроки
                if (isRightClick) {
                    showAllowedPlayers();
                } else {
                    addAllowedPlayer();
                }
                break;
                
            case 21: // Заблокированные игроки
                if (isRightClick) {
                    showBannedPlayers();
                } else {
                    banPlayer();
                }
                break;
                
            case 23: // Описание
                changeDescription();
                break;
                
            case 24: // Точка спавна
                house.setSpawnLocation(player.getLocation());
                MessageUtil.send(player, "&aТочка спавна дома установлена на вашу текущую позицию");
                break;
                
            case 29: // Настройки безопасности
                openSecuritySettings();
                break;
                
            case 30: // Временные настройки
                openTimeSettings();
                break;
                
            case 31: // Звуковые настройки
                openSoundSettings();
                break;
                
            case 38: // Сохранить
                saveHouse();
                break;
                
            case 40: // Удалить дом
                confirmDeleteHouse();
                break;
                
            case 42: // Закрыть
                player.closeInventory();
                break;
        }
    }
    
    /**
     * Добавление разрешенного игрока
     */
    private void addAllowedPlayer() {
        player.closeInventory();
        new AnvilGUIHelper(plugin, player, "Введите имя игрока", (playerName) -> {
            house.addAllowedPlayer(playerName);
            MessageUtil.send(player, "&aИгрок &e" + playerName + " &aдобавлен в разрешенные");
            this.open();
        }).open();
    }
    
    /**
     * Блокировка игрока
     */
    private void banPlayer() {
        player.closeInventory();
        new AnvilGUIHelper(plugin, player, "Введите имя игрока", (playerName) -> {
            Player targetPlayer = Bukkit.getPlayer(playerName);
            if (targetPlayer != null) {
                house.banPlayer(targetPlayer.getUniqueId());
                MessageUtil.send(player, "&cИгрок &e" + playerName + " &cзаблокирован");
            } else {
                MessageUtil.send(player, "&cИгрок не найден!");
            }
            this.open();
        }).open();
    }
    
    /**
     * Изменение описания
     */
    private void changeDescription() {
        player.closeInventory();
        new AnvilGUIHelper(plugin, player, "Введите описание дома", (description) -> {
            house.setDescription(description);
            MessageUtil.send(player, "&aОписание дома изменено");
            this.open();
        }).open();
    }
    
    /**
     * Показать разрешенных игроков
     */
    private void showAllowedPlayers() {
        if (house.getAllowedPlayers().isEmpty()) {
            MessageUtil.send(player, "&7Нет разрешенных игроков");
            return;
        }
        
        MessageUtil.send(player, "&6&l=== Разрешенные игроки ===");
        for (UUID allowedPlayerId : house.getAllowedPlayers()) {
            Player allowedPlayer = Bukkit.getPlayer(allowedPlayerId);
            String playerName = allowedPlayer != null ? allowedPlayer.getName() : "Неизвестный игрок";
            MessageUtil.send(player, "&a• " + playerName);
        }
    }
    
    /**
     * Показать заблокированных игроков
     */
    private void showBannedPlayers() {
        if (house.getBannedPlayers().isEmpty()) {
            MessageUtil.send(player, "&7Нет заблокированных игроков");
            return;
        }
        
        MessageUtil.send(player, "&c&l=== Заблокированные игроки ===");
        for (UUID bannedPlayerId : house.getBannedPlayers()) {
            Player bannedPlayer = Bukkit.getPlayer(bannedPlayerId);
            String playerName = bannedPlayer != null ? bannedPlayer.getName() : "Неизвестный игрок";
            MessageUtil.send(player, "&c• " + playerName);
        }
    }
    
    /**
     * Настройки безопасности
     */
    private void openSecuritySettings() {
        new SecuritySettingsGUI(plugin, player, house).open();
    }
    
    /**
     * Настройки времени
     */
    private void openTimeSettings() {
        MessageUtil.send(player, "&7Настройки времени будут добавлены в следующем обновлении");
    }
    
    /**
     * Настройки звука
     */
    private void openSoundSettings() {
        MessageUtil.send(player, "&7Настройки звука будут добавлены в следующем обновлении");
    }
    
    /**
     * Сохранение дома
     */
    private void saveHouse() {
        plugin.getDatabaseManager().saveHouse(house);
        MessageUtil.send(player, "&aНастройки дома сохранены!");
        player.closeInventory();
    }
    
    /**
     * Подтверждение удаления дома
     */
    private void confirmDeleteHouse() {
        player.closeInventory();
        MessageUtil.send(player, 
            "&c&l=== ПОДТВЕРЖДЕНИЕ УДАЛЕНИЯ ===",
            "&cВы действительно хотите удалить дом?",
            "&cЭто действие необратимо!",
            "",
            "&eНапишите в чат: &cДА УДАЛИТЬ &eдля подтверждения",
            "&7Или любое другое сообщение для отмены"
        );
        
        // Регистрируем временный слушатель чата
        ru.openhousing.listeners.ChatListener.registerTemporaryInput(player, (input) -> {
            if ("ДА УДАЛИТЬ".equalsIgnoreCase(input.trim())) {
                if (plugin.getHousingManager().deleteHouse(house, player)) {
                    MessageUtil.send(player, "&cДом успешно удален!");
                } else {
                    MessageUtil.send(player, "&cОшибка удаления дома");
                }
            } else {
                MessageUtil.send(player, "&7Удаление дома отменено");
            }
        });
    }
    
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        String title = null;
        try {
            title = event.getView().getTitle();
        } catch (NoSuchMethodError e) {
            return; // Пропускаем если не можем получить title
        }
        
        if (title != null && title.equals("§6Настройки дома") && 
            event.getPlayer().equals(player)) {
            // Отменяем регистрацию листенера
            InventoryClickEvent.getHandlerList().unregister(this);
            InventoryCloseEvent.getHandlerList().unregister(this);
        }
    }
}
