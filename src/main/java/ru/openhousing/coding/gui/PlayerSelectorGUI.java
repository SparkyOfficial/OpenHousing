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
import ru.openhousing.utils.ItemBuilder;
import ru.openhousing.utils.MessageUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

/**
 * GUI для выбора игрока
 */
public class PlayerSelectorGUI implements Listener {
    
    private final OpenHousing plugin;
    private final Player player;
    private final Inventory inventory;
    private final Consumer<String> onPlayerSelected;
    private int page = 0;
    
    public PlayerSelectorGUI(OpenHousing plugin, Player player, Consumer<String> onPlayerSelected) {
        this.plugin = plugin;
        this.player = player;
        this.onPlayerSelected = onPlayerSelected;
        this.inventory = Bukkit.createInventory(null, 54, "§6Выбор игрока");
        
        setupGUI();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }
    
    public void open() {
        player.openInventory(inventory);
    }
    
    private void setupGUI() {
        inventory.clear();
        
        // Специальные селекторы
        inventory.setItem(4, new ItemBuilder(Material.EMERALD)
            .name("§6@p - Ближайший игрок")
            .lore(Arrays.asList(
                "§7Выбирает ближайшего игрока",
                "§7к выполняющему скрипт",
                "",
                "§eКлик для выбора"
            ))
            .build());
            
        inventory.setItem(2, new ItemBuilder(Material.DIAMOND)
            .name("§6@a - Все игроки")
            .lore(Arrays.asList(
                "§7Выбирает всех игроков",
                "§7на сервере",
                "",
                "§eКлик для выбора"
            ))
            .build());
            
        inventory.setItem(6, new ItemBuilder(Material.GOLD_INGOT)
            .name("§6@r - Случайный игрок")
            .lore(Arrays.asList(
                "§7Выбирает случайного игрока",
                "§7из онлайн игроков",
                "",
                "§eКлик для выбора"
            ))
            .build());
            
        inventory.setItem(8, new ItemBuilder(Material.IRON_INGOT)
            .name("§6@s - Себя")
            .lore(Arrays.asList(
                "§7Выбирает игрока,",
                "§7который выполняет скрипт",
                "",
                "§eКлик для выбора"
            ))
            .build());
        
        // Онлайн игроки
        List<Player> onlinePlayers = new ArrayList<>(Bukkit.getOnlinePlayers());
        int maxPlayers = 28; // 4 ряда по 7 игроков
        int startIndex = page * maxPlayers;
        int endIndex = Math.min(startIndex + maxPlayers, onlinePlayers.size());
        
        int slot = 18; // Начинаем с третьего ряда
        for (int i = startIndex; i < endIndex; i++) {
            Player onlinePlayer = onlinePlayers.get(i);
            
            ItemStack playerItem = new ItemBuilder(Material.PLAYER_HEAD)
                .name("§e" + onlinePlayer.getName())
                .lore(Arrays.asList(
                    "§7Уровень: §f" + onlinePlayer.getLevel(),
                    "§7Мир: §f" + onlinePlayer.getWorld().getName(),
                    "§7Игровой режим: §f" + onlinePlayer.getGameMode().name(),
                    "",
                    "§eКлик для выбора"
                ))
                .build();
            
            // Попытка установить скин игрока
            try {
                ItemBuilder.setPlayerHead(playerItem, onlinePlayer.getName());
            } catch (Exception e) {
                // Если не удалось установить скин, оставляем обычную голову
            }
            
            inventory.setItem(slot, playerItem);
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
        
        if (endIndex < onlinePlayers.size()) {
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
    
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getView().getTitle().equals("§6Выбор игрока")) {
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
        
        // Обработка кликов
        if (slot == 2) { // @a
            selectPlayer("@a");
        } else if (slot == 4) { // @p
            selectPlayer("@p");
        } else if (slot == 6) { // @r
            selectPlayer("@r");
        } else if (slot == 8) { // @s
            selectPlayer("@s");
        } else if (slot == 45 && page > 0) { // Предыдущая страница
            page--;
            setupGUI();
        } else if (slot == 53) { // Следующая страница
            List<Player> onlinePlayers = new ArrayList<>(Bukkit.getOnlinePlayers());
            if ((page + 1) * 28 < onlinePlayers.size()) {
                page++;
                setupGUI();
            }
        } else if (slot == 49) { // Отмена
            player.closeInventory();
            MessageUtil.send(player, "§7Выбор игрока отменен");
        } else if (slot >= 18 && slot < 45) { // Клик по игроку
            // Вычисляем индекс игрока
            int relativeSlot = slot - 18;
            if (relativeSlot >= 9) relativeSlot -= 9;
            if (relativeSlot >= 18) relativeSlot -= 9;
            
            int playerIndex = page * 28 + relativeSlot;
            List<Player> onlinePlayers = new ArrayList<>(Bukkit.getOnlinePlayers());
            
            if (playerIndex >= 0 && playerIndex < onlinePlayers.size()) {
                Player selectedPlayer = onlinePlayers.get(playerIndex);
                selectPlayer(selectedPlayer.getName());
            }
        }
    }
    
    private void selectPlayer(String playerName) {
        player.closeInventory();
        MessageUtil.send(player, "§aВыбран игрок: §e" + playerName);
        
        if (onPlayerSelected != null) {
            onPlayerSelected.accept(playerName);
        }
        
        // Отменяем регистрацию листенера
        InventoryClickEvent.getHandlerList().unregister(this);
    }
}
