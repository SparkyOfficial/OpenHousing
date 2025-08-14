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
import ru.openhousing.utils.ItemBuilder;
import ru.openhousing.utils.MessageUtil;

import java.util.Arrays;
import java.util.function.Consumer;

/**
 * GUI для выбора локации
 */
public class LocationSelectorGUI implements Listener {
    
    private final OpenHousing plugin;
    private final Player player;
    private final Inventory inventory;
    private final Consumer<String> onLocationSelected;
    
    public LocationSelectorGUI(OpenHousing plugin, Player player, Consumer<String> onLocationSelected) {
        this.plugin = plugin;
        this.player = player;
        this.onLocationSelected = onLocationSelected;
        this.inventory = Bukkit.createInventory(null, 45, "§6Выбор локации");
        
        setupGUI();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }
    
    public void open() {
        player.openInventory(inventory);
    }
    
    private void setupGUI() {
        inventory.clear();
        
        // Текущая позиция игрока
        Location loc = player.getLocation();
        inventory.setItem(13, new ItemBuilder(Material.COMPASS)
            .name("§6Текущая позиция")
            .lore(Arrays.asList(
                "§7Мир: §f" + loc.getWorld().getName(),
                "§7X: §f" + String.format("%.1f", loc.getX()),
                "§7Y: §f" + String.format("%.1f", loc.getY()),
                "§7Z: §f" + String.format("%.1f", loc.getZ()),
                "",
                "§eКлик для выбора"
            ))
            .build());
        
        // Спавн мира
        Location worldSpawn = player.getWorld().getSpawnLocation();
        inventory.setItem(10, new ItemBuilder(Material.BEACON)
            .name("§6Спавн мира")
            .lore(Arrays.asList(
                "§7Точка спавна текущего мира",
                "§7X: §f" + worldSpawn.getBlockX(),
                "§7Y: §f" + worldSpawn.getBlockY(),
                "§7Z: §f" + worldSpawn.getBlockZ(),
                "",
                "§eКлик для выбора"
            ))
            .build());
        
        // Кровать игрока
        Location bedSpawn = player.getBedSpawnLocation();
        if (bedSpawn != null) {
            inventory.setItem(12, new ItemBuilder(Material.RED_BED)
                .name("§6Кровать")
                .lore(Arrays.asList(
                    "§7Точка спавна в кровати",
                    "§7Мир: §f" + bedSpawn.getWorld().getName(),
                    "§7X: §f" + bedSpawn.getBlockX(),
                    "§7Y: §f" + bedSpawn.getBlockY(),
                    "§7Z: §f" + bedSpawn.getBlockZ(),
                    "",
                    "§eКлик для выбора"
                ))
                .build());
        } else {
            inventory.setItem(12, new ItemBuilder(Material.GRAY_BED)
                .name("§7Кровать не установлена")
                .lore("§7У вас нет точки спавна в кровати")
                .build());
        }
        
        // Последняя смерть
        Location deathLoc = player.getLastDeathLocation();
        if (deathLoc != null) {
            inventory.setItem(14, new ItemBuilder(Material.SKELETON_SKULL)
                .name("§cМесто смерти")
                .lore(Arrays.asList(
                    "§7Место последней смерти",
                    "§7Мир: §f" + deathLoc.getWorld().getName(),
                    "§7X: §f" + deathLoc.getBlockX(),
                    "§7Y: §f" + deathLoc.getBlockY(),
                    "§7Z: §f" + deathLoc.getBlockZ(),
                    "",
                    "§eКлик для выбора"
                ))
                .build());
        } else {
            inventory.setItem(14, new ItemBuilder(Material.BONE)
                .name("§7Смертей не было")
                .lore("§7У вас нет записи о смерти")
                .build());
        }
        
        // Относительные позиции
        inventory.setItem(19, new ItemBuilder(Material.EMERALD)
            .name("§6~ ~ ~ (Относительно)")
            .lore(Arrays.asList(
                "§7Позиция относительно",
                "§7выполняющего команду",
                "",
                "§eКлик для выбора"
            ))
            .build());
        
        // Ручной ввод координат
        inventory.setItem(21, new ItemBuilder(Material.WRITABLE_BOOK)
            .name("§6Ввести координаты")
            .lore(Arrays.asList(
                "§7Введите координаты вручную",
                "§7в формате: x y z",
                "",
                "§eКлик для ввода"
            ))
            .build());
        
        // Выбор блока взглядом
        inventory.setItem(23, new ItemBuilder(Material.SPYGLASS)
            .name("§6Блок перед собой")
            .lore(Arrays.asList(
                "§7Блок, на который",
                "§7вы смотрите",
                "",
                "§eКлик для выбора"
            ))
            .build());
        
        // Случайная позиция
        inventory.setItem(25, new ItemBuilder(Material.ENDER_PEARL)
            .name("§6Случайная позиция")
            .lore(Arrays.asList(
                "§7Случайная позиция",
                "§7в радиусе 100 блоков",
                "",
                "§eКлик для выбора"
            ))
            .build());
        
        // Кнопка закрытия
        inventory.setItem(40, new ItemBuilder(Material.BARRIER)
            .name("§cОтмена")
            .lore("§7Закрыть без выбора")
            .build());
    }
    
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getView().getTitle().equals("§6Выбор локации")) {
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
        
        switch (slot) {
            case 10: // Спавн мира
                Location worldSpawn = player.getWorld().getSpawnLocation();
                selectLocation(formatLocation(worldSpawn));
                break;
                
            case 12: // Кровать
                Location bedSpawn = player.getBedSpawnLocation();
                if (bedSpawn != null) {
                    selectLocation(formatLocation(bedSpawn));
                } else {
                    MessageUtil.send(player, "§cУ вас нет установленной кровати!");
                }
                break;
                
            case 13: // Текущая позиция
                Location current = player.getLocation();
                selectLocation(formatLocation(current));
                break;
                
            case 14: // Место смерти
                Location deathLoc = player.getLastDeathLocation();
                if (deathLoc != null) {
                    selectLocation(formatLocation(deathLoc));
                } else {
                    MessageUtil.send(player, "§cУ вас нет записи о смерти!");
                }
                break;
                
            case 19: // Относительная позиция
                selectLocation("~ ~ ~");
                break;
                
            case 21: // Ручной ввод
                player.closeInventory();
                MessageUtil.send(player, "§6Введите координаты в формате: x y z");
                MessageUtil.send(player, "§7Пример: 100 64 -50");
                // TODO: Система ввода координат через чат
                break;
                
            case 23: // Блок перед собой
                Location targetBlock = player.getTargetBlock(null, 100).getLocation();
                selectLocation(formatLocation(targetBlock));
                break;
                
            case 25: // Случайная позиция
                Location randomLoc = generateRandomLocation(player.getLocation());
                selectLocation(formatLocation(randomLoc));
                break;
                
            case 40: // Отмена
                player.closeInventory();
                MessageUtil.send(player, "§7Выбор локации отменен");
                break;
        }
    }
    
    private String formatLocation(Location loc) {
        return String.format("%.1f %.1f %.1f", loc.getX(), loc.getY(), loc.getZ());
    }
    
    private Location generateRandomLocation(Location center) {
        double x = center.getX() + (Math.random() - 0.5) * 200; // ±100 блоков
        double z = center.getZ() + (Math.random() - 0.5) * 200;
        double y = center.getWorld().getHighestBlockYAt((int)x, (int)z) + 1;
        
        return new Location(center.getWorld(), x, y, z);
    }
    
    private void selectLocation(String location) {
        player.closeInventory();
        MessageUtil.send(player, "§aВыбрана локация: §e" + location);
        
        if (onLocationSelected != null) {
            onLocationSelected.accept(location);
        }
        
        // Отменяем регистрацию листенера
        InventoryClickEvent.getHandlerList().unregister(this);
    }
}
