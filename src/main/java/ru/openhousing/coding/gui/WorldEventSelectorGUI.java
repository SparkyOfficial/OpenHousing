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
import ru.openhousing.coding.blocks.events.WorldEventBlock;
import ru.openhousing.utils.ItemBuilder;

import java.util.Arrays;
import java.util.function.Consumer;

/**
 * GUI для выбора типа события мира
 */
public class WorldEventSelectorGUI implements Listener {
    
    private final OpenHousing plugin;
    private final Player player;
    private final Consumer<WorldEventBlock.WorldEventType> onSelect;
    private final Inventory inventory;
    
    public WorldEventSelectorGUI(OpenHousing plugin, Player player, Consumer<WorldEventBlock.WorldEventType> onSelect) {
        this.plugin = plugin;
        this.player = player;
        this.onSelect = onSelect;
        this.inventory = Bukkit.createInventory(null, 54, "§6Выбор события мира");
        
        // Регистрируем listener
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        
        setupGUI();
    }
    
    private void setupGUI() {
        inventory.clear();
        
        // События мира
        WorldEventBlock.WorldEventType[] events = WorldEventBlock.WorldEventType.values();
        
        int slot = 9; // Начинаем с второй строки
        for (WorldEventBlock.WorldEventType eventType : events) {
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
    
    private Material getEventMaterial(WorldEventBlock.WorldEventType eventType) {
        return switch (eventType) {
            case BLOCK_BREAK -> Material.DIAMOND_PICKAXE;
            case BLOCK_PLACE -> Material.COBBLESTONE;
            case BLOCK_BURN -> Material.FIRE_CHARGE;
            case BLOCK_EXPLODE -> Material.TNT;
            case BLOCK_FADE -> Material.ICE;
            case BLOCK_FORM -> Material.WATER_BUCKET;
            case BLOCK_GROW -> Material.BONE_MEAL;
            case LEAVES_DECAY -> Material.OAK_LEAVES;
            case REDSTONE_CHANGE -> Material.REDSTONE;
            case PISTON_EXTEND -> Material.PISTON;
            case PISTON_RETRACT -> Material.STICKY_PISTON;
            case WEATHER_CHANGE -> Material.SUNFLOWER;
            case THUNDER_CHANGE -> Material.LIGHTNING_ROD;
            case LIGHTNING_STRIKE -> Material.TRIDENT;
            case TIME_SKIP -> Material.CLOCK;
            case WORLD_LOAD -> Material.GRASS_BLOCK;
            case WORLD_UNLOAD -> Material.BARRIER;
            case WORLD_SAVE -> Material.WRITABLE_BOOK;
            case STRUCTURE_GROW -> Material.OAK_SAPLING;
            case CHUNK_LOAD -> Material.MAP;
            case CHUNK_UNLOAD -> Material.PAPER;
            case EXPLOSION -> Material.GUNPOWDER;
            case WATER_LEVEL_CHANGE -> Material.WATER_BUCKET;
            case LAVA_LEVEL_CHANGE -> Material.LAVA_BUCKET;
            case DROPPER_DROP -> Material.DROPPER;
            case HOPPER_MOVE -> Material.HOPPER;
            default -> Material.GRASS_BLOCK;
        };
    }
    
    public void open() {
        player.openInventory(inventory);
    }
    
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        String title = null;
        try {
            title = event.getView().getTitle();
        } catch (NoSuchMethodError e) {
            return; // Пропускаем если не можем получить title
        }
        
        if (title == null || !title.equals("§6Выбор события мира")) return;
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
        WorldEventBlock.WorldEventType selectedEvent = getEventBySlot(slot);
        if (selectedEvent != null) {
            player.closeInventory();
            if (onSelect != null) {
                onSelect.accept(selectedEvent);
            }
        }
    }
    
    private WorldEventBlock.WorldEventType getEventBySlot(int slot) {
        WorldEventBlock.WorldEventType[] events = WorldEventBlock.WorldEventType.values();
        
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
