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
import ru.openhousing.utils.AnvilGUIHelper;
import ru.openhousing.utils.ItemBuilder;

import java.util.Arrays;
import java.util.function.Consumer;

/**
 * Специализированный GUI для выбора игроков
 */
public class PlayerSelectorGUI implements Listener {

    private final OpenHousing plugin;
    private final Player player;
    private final Consumer<String> callback;
    private final Inventory inventory;

    public PlayerSelectorGUI(OpenHousing plugin, Player player, Consumer<String> callback) {
        this.plugin = plugin;
        this.player = player;
        this.callback = callback;
        this.inventory = Bukkit.createInventory(null, 45, "§6Выбор игрока");

        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        setupGUI();
    }

    private void setupGUI() {
        inventory.clear();

        // Заголовок
        inventory.setItem(4, new ItemBuilder(Material.PLAYER_HEAD)
            .name("§6Выбор игрока")
            .lore("§7Выберите цель для действия")
            .build());

        // Селекторы игроков
        inventory.setItem(10, new ItemBuilder(Material.COMPASS)
            .name("§e@p - Ближайший игрок")
            .lore(Arrays.asList(
                "§7Выбрать ближайшего игрока",
                "§7к месту выполнения",
                "",
                "§eКлик для выбора"
            ))
            .build());

        inventory.setItem(11, new ItemBuilder(Material.ENDER_EYE)
            .name("§e@s - Сам игрок")
            .lore(Arrays.asList(
                "§7Выбрать игрока, который",
                "§7запустил скрипт",
                "",
                "§eКлик для выбора"
            ))
            .build());

        inventory.setItem(12, new ItemBuilder(Material.FIREWORK_STAR)
            .name("§e@r - Случайный игрок")
            .lore(Arrays.asList(
                "§7Выбрать случайного игрока",
                "§7из онлайна",
                "",
                "§eКлик для выбора"
            ))
            .build());

        inventory.setItem(13, new ItemBuilder(Material.BEACON)
            .name("§e@a - Все игроки")
            .lore(Arrays.asList(
                "§7Применить действие ко",
                "§7всем онлайн игрокам",
                "",
                "§eКлик для выбора"
            ))
            .build());

        // Онлайн игроки
        inventory.setItem(19, new ItemBuilder(Material.EMERALD)
            .name("§aОнлайн игроки")
            .lore(Arrays.asList(
                "§7Выбрать конкретного",
                "§7игрока из онлайна",
                "",
                "§eКлик для просмотра"
            ))
            .build());

        // Ввод имени
        inventory.setItem(20, new ItemBuilder(Material.NAME_TAG)
            .name("§6Ввести имя игрока")
            .lore(Arrays.asList(
                "§7Ввести имя игрока",
                "§7вручную",
                "",
                "§eКлик для ввода"
            ))
            .build());

        // Переменная
        inventory.setItem(21, new ItemBuilder(Material.CHEST)
            .name("§dИз переменной")
            .lore(Arrays.asList(
                "§7Использовать значение",
                "§7из переменной",
                "",
                "§eКлик для выбора"
            ))
            .build());

        // Кнопки управления
        inventory.setItem(40, new ItemBuilder(Material.ARROW)
            .name("§7Назад")
            .build());

        inventory.setItem(44, new ItemBuilder(Material.BARRIER)
            .name("§cОтмена")
            .build());
    }

    public void open() {
        player.openInventory(inventory);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getView().getTitle().equals("§6Выбор игрока")) return;
        if (!(event.getWhoClicked() instanceof Player)) return;
        if (!event.getWhoClicked().getUniqueId().equals(player.getUniqueId())) return;

        event.setCancelled(true);

        int slot = event.getSlot();
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) return;

        switch (slot) {
            case 10: // @p
                player.closeInventory();
                callback.accept("@p");
                break;

            case 11: // @s
                player.closeInventory();
                callback.accept("@s");
                break;

            case 12: // @r
                player.closeInventory();
                callback.accept("@r");
                break;

            case 13: // @a
                player.closeInventory();
                callback.accept("@a");
                break;

            case 19: // Онлайн игроки
                showOnlinePlayers();
                break;

            case 20: // Ввод имени
                player.closeInventory();
                AnvilGUIHelper.openTextInput(plugin, player, "Введите имя игрока", "", callback);
                break;

            case 21: // Переменная
                player.closeInventory();
                AnvilGUIHelper.openTextInput(plugin, player, "Введите имя переменной (например: player_name)", "", (varName) -> {
                    callback.accept("${" + varName + "}");
                });
                break;

            case 40: // Назад
                player.closeInventory();
                break;

            case 44: // Отмена
                player.closeInventory();
                break;
        }
    }

    private void showOnlinePlayers() {
        // Создаем новый инвентарь для онлайн игроков
        Inventory onlineInventory = Bukkit.createInventory(null, 54, "§6Онлайн игроки");

        int slot = 10;
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            if (slot >= 44) break;

            onlineInventory.setItem(slot, new ItemBuilder(Material.PLAYER_HEAD)
                .name("§e" + onlinePlayer.getName())
                .lore(Arrays.asList(
                    "§7Уровень: §f" + onlinePlayer.getLevel(),
                    "§7Мир: §f" + onlinePlayer.getWorld().getName(),
                    "",
                    "§eКлик для выбора"
                ))
                .playerHead(onlinePlayer.getName())
                .build());

            slot++;
            if (slot == 17) slot = 19;
            if (slot == 26) slot = 28;
            if (slot == 35) slot = 37;
        }

        // Кнопка назад
        onlineInventory.setItem(49, new ItemBuilder(Material.ARROW)
            .name("§7Назад к выбору")
            .build());

        player.openInventory(onlineInventory);
    }
}