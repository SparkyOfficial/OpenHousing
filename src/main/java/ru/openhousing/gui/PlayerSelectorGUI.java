package ru.openhousing.gui;

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

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

/**
 * GUI для выбора игроков
 */
public class PlayerSelectorGUI implements Listener {
    
    private final OpenHousing plugin;
    private final Player player;
    private final Consumer<Player> onPlayerSelect;
    private final Consumer<Void> onCancel;
    private final Inventory inventory;
    private final List<Player> onlinePlayers;
    private int currentPage = 0;
    private static final int PLAYERS_PER_PAGE = 45;
    
    public PlayerSelectorGUI(OpenHousing plugin, Player player, Consumer<Player> onPlayerSelect) {
        this(plugin, player, onPlayerSelect, null);
    }
    
    public PlayerSelectorGUI(OpenHousing plugin, Player player, Consumer<Player> onPlayerSelect, Consumer<Void> onCancel) {
        this.plugin = plugin;
        this.player = player;
        this.onPlayerSelect = onPlayerSelect;
        this.onCancel = onCancel;
        this.onlinePlayers = Arrays.asList(Bukkit.getOnlinePlayers().toArray(new Player[0]));
        this.inventory = Bukkit.createInventory(null, 54, "§6Выбор игрока");
        
        setupGUI();
        registerListener();
    }
    
    /**
     * Настройка GUI
     */
    private void setupGUI() {
        // Заголовок
        inventory.setItem(4, new ItemBuilder(Material.PLAYER_HEAD)
            .name("§6§lВыбор игрока")
            .lore(Arrays.asList(
                "§7Выберите игрока из списка",
                "§7онлайн игроков",
                "",
                "§eВсего игроков: §f" + onlinePlayers.size()
            ))
            .build());
        
        // Отображение игроков
        int startIndex = currentPage * PLAYERS_PER_PAGE;
        int endIndex = Math.min(startIndex + PLAYERS_PER_PAGE, onlinePlayers.size());
        
        for (int i = 0; i < endIndex - startIndex; i++) {
            Player targetPlayer = onlinePlayers.get(startIndex + i);
            inventory.setItem(i, new ItemBuilder(Material.PLAYER_HEAD)
                .name("§e" + targetPlayer.getName())
                .lore(Arrays.asList(
                    "§7Кликните для выбора",
                    "",
                    "§7Статус: §aОнлайн",
                    "§7Уровень: §f" + targetPlayer.getLevel(),
                    "§7Здоровье: §f" + (int)targetPlayer.getHealth() + "/" + (int)targetPlayer.getMaxHealth()
                ))
                .build());
        }
        
        // Кнопки навигации
        if (currentPage > 0) {
            inventory.setItem(45, new ItemBuilder(Material.ARROW)
                .name("§eПредыдущая страница")
                .lore(Arrays.asList("§7Страница " + (currentPage - 1)))
                .build());
        }
        
        if (endIndex < onlinePlayers.size()) {
            inventory.setItem(53, new ItemBuilder(Material.ARROW)
                .name("§eСледующая страница")
                .lore(Arrays.asList("§7Страница " + (currentPage + 1)))
                .build());
        }
        
        // Информация о странице
        inventory.setItem(49, new ItemBuilder(Material.BOOK)
            .name("§eСтраница " + (currentPage + 1))
            .lore(Arrays.asList(
                "§7Показано: §f" + (endIndex - startIndex) + " игроков",
                "§7Всего: §f" + onlinePlayers.size() + " игроков"
            ))
            .build());
        
        // Кнопки управления
        inventory.setItem(47, new ItemBuilder(Material.RED_CONCRETE)
            .name("§c§lОтмена")
            .lore(Arrays.asList("§7Отменить выбор"))
            .build());
        
        // Поиск игрока
        inventory.setItem(51, new ItemBuilder(Material.COMPASS)
            .name("§eПоиск игрока")
            .lore(Arrays.asList(
                "§7Найти игрока по имени",
                "§7(введите в чат)"
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
        if (!event.getView().getTitle().equals("§6Выбор игрока") || 
            !event.getWhoClicked().equals(player)) {
            return;
        }
        
        event.setCancelled(true);
        
        int slot = event.getRawSlot();
        
        // Выбор игрока
        if (slot < PLAYERS_PER_PAGE) {
            int startIndex = currentPage * PLAYERS_PER_PAGE;
            int playerIndex = startIndex + slot;
            
            if (playerIndex < onlinePlayers.size()) {
                Player selectedPlayer = onlinePlayers.get(playerIndex);
                onPlayerSelect.accept(selectedPlayer);
                close();
                return;
            }
        }
        
        // Навигация
        switch (slot) {
            case 45: // Предыдущая страница
                if (currentPage > 0) {
                    currentPage--;
                    setupGUI();
                }
                break;
                
            case 53: // Следующая страница
                int maxPage = (onlinePlayers.size() - 1) / PLAYERS_PER_PAGE;
                if (currentPage < maxPage) {
                    currentPage++;
                    setupGUI();
                }
                break;
                
            case 47: // Отмена
                if (onCancel != null) {
                    onCancel.accept(null);
                }
                close();
                break;
                
            case 51: // Поиск игрока
                openPlayerSearch();
                break;
        }
    }
    
    /**
     * Открытие поиска игрока
     */
    private void openPlayerSearch() {
        MessageUtil.send(player, 
            "§eВведите имя игрока для поиска:",
            "§7(или часть имени)"
        );
        
        // Регистрируем временный слушатель чата
        ru.openhousing.listeners.ChatListener.registerTemporaryInput(player, (input) -> {
            String searchQuery = input.trim().toLowerCase();
            
            if (searchQuery.isEmpty()) {
                MessageUtil.send(player, "§cПоисковый запрос не может быть пустым");
                open();
                return;
            }
            
            // Ищем игроков по имени
            List<Player> foundPlayers = onlinePlayers.stream()
                .filter(p -> p.getName().toLowerCase().contains(searchQuery))
                .toList();
            
            if (foundPlayers.isEmpty()) {
                MessageUtil.send(player, "§cИгроки с именем '" + searchQuery + "' не найдены");
                open();
            } else if (foundPlayers.size() == 1) {
                // Если найден только один игрок, выбираем его
                onPlayerSelect.accept(foundPlayers.get(0));
                close();
            } else {
                // Если найдено несколько игроков, показываем их
                MessageUtil.send(player, "§aНайдено игроков: §f" + foundPlayers.size());
                for (Player foundPlayer : foundPlayers) {
                    MessageUtil.send(player, "§e• " + foundPlayer.getName());
                }
                MessageUtil.send(player, "§7Уточните поиск или выберите из списка выше");
                open();
            }
        });
    }
    
    /**
     * Закрытие GUI
     */
    private void close() {
        player.closeInventory();
        InventoryClickEvent.getHandlerList().unregister(this);
    }
}
