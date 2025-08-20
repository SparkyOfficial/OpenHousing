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
import ru.openhousing.coding.blocks.actions.EntityActionBlock;
import ru.openhousing.utils.ItemBuilder;

import java.util.Arrays;
import java.util.function.Consumer;

/**
 * GUI для выбора типа действия сущности
 */
public class EntityActionSelectorGUI implements Listener {
    
    private final OpenHousing plugin;
    private final Player player;
    private final Consumer<EntityActionBlock.EntityActionType> onSelect;
    private Inventory inventory;
    
    public EntityActionSelectorGUI(OpenHousing plugin, Player player, Consumer<EntityActionBlock.EntityActionType> onSelect) {
        this.plugin = plugin;
        this.player = player;
        this.onSelect = onSelect;
        this.inventory = Bukkit.createInventory(null, 54, "§6Выбор действия сущности");
        
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }
    
    public void open() {
        setupInventory();
        player.openInventory(inventory);
    }
    
    private void setupInventory() {
        inventory.clear();
        
        EntityActionBlock.EntityActionType[] actions = EntityActionBlock.EntityActionType.values();
        
        for (int i = 0; i < actions.length && i < 45; i++) {
            EntityActionBlock.EntityActionType action = actions[i];
            Material material = getActionMaterial(action);
            
            ItemStack item = new ItemBuilder(material)
                .name("§e" + action.getDisplayName())
                .lore(Arrays.asList(
                    "§7" + action.getDescription(),
                    "",
                    "§7Клик для выбора"
                ))
                .build();
                
            inventory.setItem(i, item);
        }
        
        // Кнопка отмены
        inventory.setItem(49, new ItemBuilder(Material.REDSTONE_BLOCK)
            .name("§cОтмена")
            .lore("§7Вернуться назад")
            .build());
    }
    
    private Material getActionMaterial(EntityActionBlock.EntityActionType action) {
        switch (action) {
            case SPAWN: return Material.EGG;
            case REMOVE: return Material.BARRIER;
            case KILL: return Material.IRON_SWORD;
            case DAMAGE: return Material.WOODEN_SWORD;
            case HEAL: return Material.GOLDEN_APPLE;
            case SET_HEALTH: return Material.RED_DYE;
            case SET_MAX_HEALTH: return Material.REDSTONE;
            case TELEPORT: return Material.ENDER_PEARL;
            case TELEPORT_TO_PLAYER: return Material.ENDER_EYE;
            case SET_VELOCITY: return Material.FEATHER;
            case PUSH: return Material.PISTON;
            case LAUNCH: return Material.FIREWORK_ROCKET;
            case SET_FIRE: return Material.FLINT_AND_STEEL;
            case EXTINGUISH: return Material.WATER_BUCKET;
            case SET_NAME: return Material.NAME_TAG;
            case SET_NAME_VISIBLE: return Material.PAPER;
            case SET_GLOWING: return Material.GLOWSTONE_DUST;
            case SET_INVISIBLE: return Material.POTION;
            case SET_SILENT: return Material.MUSIC_DISC_11;
            case SET_GRAVITY: return Material.ANVIL;
            case SET_INVULNERABLE: return Material.TOTEM_OF_UNDYING;
            case ADD_POTION_EFFECT: return Material.BREWING_STAND;
            case REMOVE_POTION_EFFECT: return Material.MILK_BUCKET;
            case CLEAR_EFFECTS: return Material.BUCKET;
            case SET_TARGET: return Material.TARGET;
            case CLEAR_TARGET: return Material.WHITE_WOOL;
            case SET_ANGRY: return Material.TNT;
            case SET_PEACEFUL: return Material.WHITE_DYE;
            case TAME: return Material.BONE;
            case UNTAME: return Material.STICK;
            case SIT: return Material.SADDLE;
            case STAND: return Material.LEAD;
            case SET_BABY: return Material.EGG;
            case SET_ADULT: return Material.WHEAT;
            case SET_AGE: return Material.CLOCK;
            case BREED: return Material.WHEAT_SEEDS;
            case SHEAR: return Material.SHEARS;
            case MILK: return Material.MILK_BUCKET;
            case LEASH: return Material.LEAD;
            case UNLEASH: return Material.STRING;
            case MOUNT_PLAYER: return Material.MINECART;
            case DISMOUNT_PLAYER: return Material.RAIL;
            case EXPLODE: return Material.TNT;
            case LIGHTNING_STRIKE: return Material.TRIDENT;
            case PLAY_SOUND: return Material.NOTE_BLOCK;
            case SPAWN_PARTICLES: return Material.FIREWORK_STAR;
            case DROP_ITEM: return Material.DROPPER;
            case GIVE_ITEM: return Material.CHEST;
            case SET_EQUIPMENT: return Material.DIAMOND_CHESTPLATE;
            case FOLLOW_PLAYER: return Material.COMPASS;
            case STOP_FOLLOWING: return Material.BARRIER;
            case FACE_PLAYER: return Material.PLAYER_HEAD;
            case FACE_LOCATION: return Material.COMPASS;
            case SET_AI: return Material.REDSTONE_TORCH;
            case SET_COLLIDABLE: return Material.SLIME_BLOCK;
            case FREEZE: return Material.ICE;
            case UNFREEZE: return Material.MAGMA_BLOCK;
            default: return Material.STONE;
        }
    }
    
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        String title = null;
        try {
            title = event.getView().getTitle();
        } catch (NoSuchMethodError e) {
            return; // Пропускаем если не можем получить title
        }
        
        if (title == null || !title.equals("§6Выбор действия сущности")) return;
        if (!(event.getWhoClicked() instanceof Player)) return;
        
        Player clicker = (Player) event.getWhoClicked();
        if (!clicker.equals(player)) return;
        
        event.setCancelled(true);
        
        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null || clickedItem.getType() == Material.AIR) return;
        
        int slot = event.getSlot();
        
        if (slot == 49) {
            // Отмена
            player.closeInventory();
            return;
        }
        
        if (slot < EntityActionBlock.EntityActionType.values().length) {
            EntityActionBlock.EntityActionType selectedAction = EntityActionBlock.EntityActionType.values()[slot];
            player.closeInventory();
            
            if (onSelect != null) {
                onSelect.accept(selectedAction);
            }
        }
    }
}
