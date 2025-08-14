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
import ru.openhousing.coding.blocks.events.PlayerEventBlock;
import ru.openhousing.utils.ItemBuilder;

import java.util.Arrays;
import java.util.function.Consumer;

/**
 * GUI для выбора типа события игрока
 */
public class PlayerEventSelectorGUI implements Listener {
    
    private final OpenHousing plugin;
    private final Player player;
    private final Consumer<PlayerEventBlock.PlayerEventType> onSelect;
    private final Inventory inventory;
    
    public PlayerEventSelectorGUI(OpenHousing plugin, Player player, Consumer<PlayerEventBlock.PlayerEventType> onSelect) {
        this.plugin = plugin;
        this.player = player;
        this.onSelect = onSelect;
        this.inventory = Bukkit.createInventory(null, 54, "§6Выбор события игрока");
        
        // Регистрируем listener
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        
        setupGUI();
    }
    
    private void setupGUI() {
        inventory.clear();
        
        // Основные события
        PlayerEventBlock.PlayerEventType[] events = PlayerEventBlock.PlayerEventType.values();
        
        int slot = 9; // Начинаем с второй строки
        for (PlayerEventBlock.PlayerEventType eventType : events) {
            if (slot >= 45) break; // Не выходим за пределы инвентаря
            
            Material material = getEventMaterial(eventType);
            
            ItemStack item = new ItemBuilder(material)
                .name("§e" + eventType.getDisplayName())
                .lore(Arrays.asList(
                    "§7" + eventType.getDescription(),
                    "",
                    "§aКлик для выбора"
                ))
                .build();
                
            inventory.setItem(slot, item);
            slot++;
            
            // Пропускаем края инвентаря
            if ((slot + 1) % 9 == 0) slot += 2;
        }
        
        // Кнопка закрытия
        inventory.setItem(49, new ItemBuilder(Material.BARRIER)
            .name("§cОтмена")
            .lore("§7Закрыть без выбора")
            .build());
    }
    
    private Material getEventMaterial(PlayerEventBlock.PlayerEventType eventType) {
        return switch (eventType) {
            case JOIN -> Material.EMERALD;
            case QUIT -> Material.REDSTONE;
            case CHAT -> Material.PAPER;
            case MOVE -> Material.LEATHER_BOOTS;
            case INTERACT -> Material.STICK;
            case INTERACT_ENTITY -> Material.LEAD;
            case DAMAGE -> Material.IRON_SWORD;
            case DEATH -> Material.SKELETON_SKULL;
            case RESPAWN -> Material.TOTEM_OF_UNDYING;
            case DROP_ITEM -> Material.DROPPER;
            case PICKUP_ITEM -> Material.HOPPER;
            case INVENTORY_CLICK -> Material.CHEST;
            case COMMAND -> Material.COMMAND_BLOCK;
            case TELEPORT -> Material.ENDER_PEARL;
            case WORLD_CHANGE -> Material.NETHER_PORTAL;
            case SNEAK -> Material.LEATHER_LEGGINGS;
            case JUMP -> Material.RABBIT_FOOT;
            case LEFT_CLICK -> Material.WOODEN_SWORD;
            case RIGHT_CLICK -> Material.BLAZE_ROD;
            case BREAK_BLOCK -> Material.DIAMOND_PICKAXE;
            case PLACE_BLOCK -> Material.COBBLESTONE;
            case CHANGE_GAMEMODE -> Material.COMMAND_BLOCK_MINECART;
            case LEVEL_CHANGE -> Material.EXPERIENCE_BOTTLE;
            case FOOD_CHANGE -> Material.BREAD;
            case ITEM_CONSUME -> Material.GOLDEN_APPLE;
            case FISH -> Material.FISHING_ROD;
            case ENCHANT_ITEM -> Material.ENCHANTING_TABLE;
            case ANVIL_USE -> Material.ANVIL;
            case CRAFT_ITEM -> Material.CRAFTING_TABLE;
            case PORTAL_USE -> Material.END_PORTAL_FRAME;
            default -> Material.STONE;
        };
    }
    
    public void open() {
        player.openInventory(inventory);
    }
    
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getView().getTitle().equals("§6Выбор события игрока")) return;
        if (!(event.getWhoClicked() instanceof Player)) return;
        if (!event.getWhoClicked().getUniqueId().equals(player.getUniqueId())) return;
        
        event.setCancelled(true);
        
        int slot = event.getSlot();
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) return;
        
        // Кнопка отмены
        if (slot == 49) {
            player.closeInventory();
            return;
        }
        
        // Определяем выбранное событие по слоту
        PlayerEventBlock.PlayerEventType selectedEvent = getEventBySlot(slot);
        if (selectedEvent != null) {
            player.closeInventory();
            if (onSelect != null) {
                onSelect.accept(selectedEvent);
            }
        }
    }
    
    private PlayerEventBlock.PlayerEventType getEventBySlot(int slot) {
        PlayerEventBlock.PlayerEventType[] events = PlayerEventBlock.PlayerEventType.values();
        
        // Преобразуем слот обратно в индекс события
        int eventIndex = -1;
        int currentSlot = 9;
        
        for (int i = 0; i < events.length; i++) {
            if (currentSlot == slot) {
                eventIndex = i;
                break;
            }
            currentSlot++;
            if ((currentSlot + 1) % 9 == 0) currentSlot += 2;
        }
        
        return (eventIndex >= 0 && eventIndex < events.length) ? events[eventIndex] : null;
    }
}
