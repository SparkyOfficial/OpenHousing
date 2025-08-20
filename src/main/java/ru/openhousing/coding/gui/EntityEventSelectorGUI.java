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
import ru.openhousing.coding.blocks.events.EntityEventBlock;
import ru.openhousing.utils.ItemBuilder;

import java.util.Arrays;
import java.util.function.Consumer;

/**
 * GUI для выбора типа события сущности
 */
public class EntityEventSelectorGUI implements Listener {
    
    private final OpenHousing plugin;
    private final Player player;
    private final Consumer<EntityEventBlock.EntityEventType> onSelect;
    private final Inventory inventory;
    
    public EntityEventSelectorGUI(OpenHousing plugin, Player player, Consumer<EntityEventBlock.EntityEventType> onSelect) {
        this.plugin = plugin;
        this.player = player;
        this.onSelect = onSelect;
        this.inventory = Bukkit.createInventory(null, 54, "§6Выбор события сущности");
        
        // Регистрируем listener
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        
        setupGUI();
    }
    
    private void setupGUI() {
        inventory.clear();
        
        // События сущностей
        EntityEventBlock.EntityEventType[] events = EntityEventBlock.EntityEventType.values();
        
        int slot = 9; // Начинаем с второй строки
        for (EntityEventBlock.EntityEventType eventType : events) {
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
    
    private Material getEventMaterial(EntityEventBlock.EntityEventType eventType) {
        return switch (eventType) {
            case SPAWN -> Material.EGG;
            case DEATH -> Material.BONE;
            case DAMAGE -> Material.IRON_SWORD;
            case DAMAGE_ENTITY -> Material.DIAMOND_SWORD;
            case TARGET -> Material.BOW;
            case INTERACT -> Material.CARROT_ON_A_STICK;
            case TAME -> Material.BONE;
            case BREED -> Material.WHEAT;
            case EXPLODE -> Material.TNT;
            case TRANSFORM -> Material.FERMENTED_SPIDER_EYE;
            case TELEPORT -> Material.CHORUS_FRUIT;
            case PICKUP_ITEM -> Material.HOPPER;
            case DROP_ITEM -> Material.DROPPER;
            case CHANGE_BLOCK -> Material.GRASS_BLOCK;
            case ENTER_PORTAL -> Material.OBSIDIAN;
            case REGAIN_HEALTH -> Material.GOLDEN_APPLE;
            case POTION_EFFECT -> Material.POTION;
            case COMBUSTION -> Material.FIRE_CHARGE;
            case FREEZE -> Material.ICE;
            case MOUNT -> Material.SADDLE;
            case DISMOUNT -> Material.LEATHER;
            default -> Material.EGG;
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
        
        if (title == null || !title.equals("§6Выбор события сущности")) return;
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
        EntityEventBlock.EntityEventType selectedEvent = getEventBySlot(slot);
        if (selectedEvent != null) {
            player.closeInventory();
            if (onSelect != null) {
                onSelect.accept(selectedEvent);
            }
        }
    }
    
    private EntityEventBlock.EntityEventType getEventBySlot(int slot) {
        EntityEventBlock.EntityEventType[] events = EntityEventBlock.EntityEventType.values();
        
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
