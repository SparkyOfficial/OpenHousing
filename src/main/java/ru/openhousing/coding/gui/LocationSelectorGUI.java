package ru.openhousing.coding.gui;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import ru.openhousing.OpenHousing;
import ru.openhousing.coding.values.LocationValue;
import ru.openhousing.utils.AnvilGUIHelper;
import ru.openhousing.utils.ItemBuilder;

import java.util.Arrays;
import java.util.function.Consumer;

/**
 * Профессиональный GUI для выбора локаций
 */
public class LocationSelectorGUI implements Listener {

    private final OpenHousing plugin;
    private final Player player;
    private final Consumer<LocationValue> callback;
    private final Inventory inventory;

    public LocationSelectorGUI(OpenHousing plugin, Player player, Consumer<LocationValue> callback) {
        this.plugin = plugin;
        this.player = player;
        this.callback = callback;
        this.inventory = Bukkit.createInventory(null, 45, "§6Выбор локации");

        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        setupGUI();
    }

    private void setupGUI() {
        inventory.clear();

        // Заголовок
        inventory.setItem(4, new ItemBuilder(Material.COMPASS)
            .name("§6Выбор локации")
            .lore("§7Выберите способ указания координат")
            .build());

        // Быстрые локации
        inventory.setItem(10, new ItemBuilder(Material.ENDER_PEARL)
            .name("§eТекущая позиция")
            .lore(Arrays.asList(
                "§7Использовать текущие координаты",
                "§7игрока, выполняющего код",
                "",
                "§7X: §f" + String.format("%.1f", player.getLocation().getX()),
                "§7Y: §f" + String.format("%.1f", player.getLocation().getY()),
                "§7Z: §f" + String.format("%.1f", player.getLocation().getZ()),
                "",
                "§eКлик для выбора"
            ))
            .build());

        inventory.setItem(11, new ItemBuilder(Material.RED_BED)
            .name("§eКровать игрока")
            .lore(Arrays.asList(
                "§7Позиция кровати/точки спавна",
                "§7игрока",
                "",
                "§eКлик для выбора"
            ))
            .build());

        inventory.setItem(12, new ItemBuilder(Material.BEACON)
            .name("§eСпавн мира")
            .lore(Arrays.asList(
                "§7Точка спавна текущего мира",
                "",
                "§eКлик для выбора"
            ))
            .build());

        inventory.setItem(13, new ItemBuilder(Material.ENDER_EYE)
            .name("§eВзгляд игрока")
            .lore(Arrays.asList(
                "§7Точка, на которую смотрит",
                "§7игрок (до 100 блоков)",
                "",
                "§eКлик для выбора"
            ))
            .build());

        // Ввод координат
        inventory.setItem(19, new ItemBuilder(Material.PAPER)
            .name("§6Ввести координаты")
            .lore(Arrays.asList(
                "§7Ввести точные координаты",
                "§7в формате: X Y Z",
                "§7Пример: 100 64 -50",
                "",
                "§eКлик для ввода"
            ))
            .build());

        inventory.setItem(20, new ItemBuilder(Material.WRITABLE_BOOK)
            .name("§6Относительные координаты")
            .lore(Arrays.asList(
                "§7Координаты относительно игрока",
                "§7Формат: ~X ~Y ~Z",
                "§7Пример: ~10 ~0 ~-5",
                "",
                "§eКлик для ввода"
            ))
            .build());

        // Переменные
        inventory.setItem(21, new ItemBuilder(Material.CHEST)
            .name("§dИз переменной")
            .lore(Arrays.asList(
                "§7Использовать локацию",
                "§7сохраненную в переменной",
                "",
                "§eКлик для выбора"
            ))
            .build());

        // Специальные локации
        inventory.setItem(28, new ItemBuilder(Material.OBSIDIAN)
            .name("§5Портал в Нижний мир")
            .lore(Arrays.asList(
                "§7Найти ближайший портал",
                "§7в Нижний мир",
                "",
                "§eКлик для выбора"
            ))
            .build());

        inventory.setItem(29, new ItemBuilder(Material.END_PORTAL_FRAME)
            .name("§5Портал в Край")
            .lore(Arrays.asList(
                "§7Найти портал в Край",
                "",
                "§eКлик для выбора"
            ))
            .build());

        inventory.setItem(30, new ItemBuilder(Material.STRUCTURE_BLOCK)
            .name("§5Ближайшая структура")
            .lore(Arrays.asList(
                "§7Найти ближайшую структуру",
                "§7(деревня, храм, и т.д.)",
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
        if (!event.getView().getTitle().equals("§6Выбор локации")) return;
        if (!(event.getWhoClicked() instanceof Player)) return;
        if (!event.getWhoClicked().getUniqueId().equals(player.getUniqueId())) return;

        event.setCancelled(true);

        int slot = event.getSlot();
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) return;

        switch (slot) {
            case 10: // Текущая позиция
                player.closeInventory();
                callback.accept(new LocationValue(player.getLocation()));
                break;

            case 11: // Кровать игрока
                player.closeInventory();
                Location bedLocation = player.getBedSpawnLocation();
                if (bedLocation != null) {
                    callback.accept(new LocationValue(bedLocation));
                } else {
                    callback.accept(new LocationValue("bed"));
                }
                break;

            case 12: // Спавн мира
                player.closeInventory();
                callback.accept(new LocationValue(player.getWorld().getSpawnLocation()));
                break;

            case 13: // Взгляд игрока
                player.closeInventory();
                Location target = player.getTargetBlock(null, 100).getLocation();
                callback.accept(new LocationValue(target));
                break;

            case 19: // Ввести координаты
                player.closeInventory();
                AnvilGUIHelper.openTextInput(plugin, player, "Введите координаты (X Y Z)", "", (coords) -> {
                    try {
                        String[] parts = coords.split(" ");
                        if (parts.length == 3) {
                            double x = Double.parseDouble(parts[0]);
                            double y = Double.parseDouble(parts[1]);
                            double z = Double.parseDouble(parts[2]);
                            Location loc = new Location(player.getWorld(), x, y, z);
                            callback.accept(new LocationValue(loc));
                        } else {
                            player.sendMessage("§cНеверный формат! Используйте: X Y Z");
                        }
                    } catch (NumberFormatException e) {
                        player.sendMessage("§cНеверный формат координат!");
                    }
                });
                break;

            case 20: // Относительные координаты
                player.closeInventory();
                AnvilGUIHelper.openTextInput(plugin, player, "Относительные координаты (~X ~Y ~Z)", "~0 ~0 ~0", (coords) -> {
                    if (coords.matches("^~-?\\d+(\\.\\d+)? ~-?\\d+(\\.\\d+)? ~-?\\d+(\\.\\d+)?$")) {
                        callback.accept(new LocationValue(coords));
                    } else {
                        player.sendMessage("§cНеверный формат! Используйте: ~X ~Y ~Z");
                    }
                });
                break;

            case 21: // Из переменной
                player.closeInventory();
                AnvilGUIHelper.openTextInput(plugin, player, "Имя переменной с локацией", "", (varName) -> {
                    callback.accept(new LocationValue("${" + varName + "}"));
                });
                break;

            case 28: // Портал в Нижний мир
                player.closeInventory();
                callback.accept(new LocationValue("nether_portal"));
                break;

            case 29: // Портал в Край
                player.closeInventory();
                callback.accept(new LocationValue("end_portal"));
                break;

            case 30: // Ближайшая структура
                player.closeInventory();
                openStructureSelector();
                break;

            case 40: // Назад
                player.closeInventory();
                break;

            case 44: // Отмена
                player.closeInventory();
                break;
        }
    }

    private void openStructureSelector() {
        AnvilGUIHelper.openTextInput(plugin, player, "Тип структуры", "village", (structureType) -> {
            callback.accept(new LocationValue("structure:" + structureType));
        });
    }
}