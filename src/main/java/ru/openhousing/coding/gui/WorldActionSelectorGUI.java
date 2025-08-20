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
import ru.openhousing.coding.blocks.actions.WorldActionBlock;
import ru.openhousing.utils.ItemBuilder;

import java.util.Arrays;
import java.util.function.Consumer;

/**
 * GUI для выбора типа действия мира
 */
public class WorldActionSelectorGUI implements Listener {
    
    private final OpenHousing plugin;
    private final Player player;
    private final Consumer<WorldActionBlock.WorldActionType> onSelect;
    private Inventory inventory;
    
    public WorldActionSelectorGUI(OpenHousing plugin, Player player, Consumer<WorldActionBlock.WorldActionType> onSelect) {
        this.plugin = plugin;
        this.player = player;
        this.onSelect = onSelect;
        this.inventory = Bukkit.createInventory(null, 54, "§6Выбор действия мира");
        
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }
    
    public void open() {
        setupInventory();
        player.openInventory(inventory);
    }
    
    private void setupInventory() {
        inventory.clear();
        
        WorldActionBlock.WorldActionType[] actions = WorldActionBlock.WorldActionType.values();
        
        for (int i = 0; i < actions.length && i < 45; i++) {
            WorldActionBlock.WorldActionType action = actions[i];
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
    
    private Material getActionMaterial(WorldActionBlock.WorldActionType action) {
        switch (action) {
            case SET_BLOCK: return Material.STONE;
            case BREAK_BLOCK: return Material.DIAMOND_PICKAXE;
            case FILL_AREA: return Material.BUCKET;
            case REPLACE_BLOCKS: return Material.PISTON;
            case SET_TIME: return Material.CLOCK;
            case ADD_TIME: return Material.REDSTONE;
            case SET_WEATHER: return Material.WATER_BUCKET;
            case SET_STORM: return Material.LIGHTNING_ROD;
            case STRIKE_LIGHTNING: return Material.TRIDENT;
            case PLAY_SOUND: return Material.NOTE_BLOCK;
            case PLAY_SOUND_ALL: return Material.JUKEBOX;
            case STOP_SOUND: return Material.BARRIER;
            case SPAWN_PARTICLE: return Material.FIREWORK_STAR;
            case SPAWN_PARTICLE_LINE: return Material.STICK;
            case SPAWN_PARTICLE_CIRCLE: return Material.COMPASS;
            case SPAWN_PARTICLE_SPHERE: return Material.ENDER_PEARL;
            case CREATE_EXPLOSION: return Material.TNT;
            case SEND_MESSAGE: return Material.PAPER;
            case SEND_MESSAGE_ALL: return Material.BOOK;
            case SEND_TITLE: return Material.ENCHANTED_BOOK;
            case SEND_ACTIONBAR: return Material.NAME_TAG;
            case TELEPORT_PLAYER: return Material.ENDER_PEARL;
            case HEAL_PLAYER: return Material.GOLDEN_APPLE;
            case FEED_PLAYER: return Material.BREAD;
            case GIVE_ITEM: return Material.CHEST;
            case TAKE_ITEM: return Material.HOPPER;
            case CLEAR_INVENTORY: return Material.SHULKER_BOX;
            case SET_GAMEMODE: return Material.COMMAND_BLOCK;
            case SET_FLY: return Material.FEATHER;
            case SET_GOD_MODE: return Material.TOTEM_OF_UNDYING;
            case KICK_PLAYER: return Material.IRON_DOOR;
            case BAN_PLAYER: return Material.BARRIER;
            case RUN_COMMAND: return Material.COMMAND_BLOCK;
            case RUN_COMMAND_CONSOLE: return Material.REPEATING_COMMAND_BLOCK;
            case SET_SPAWN: return Material.WHITE_BED;
            case LOAD_CHUNK: return Material.MAP;
            case UNLOAD_CHUNK: return Material.PAPER;
            case SAVE_WORLD: return Material.WRITABLE_BOOK;
            case SET_DIFFICULTY: return Material.IRON_SWORD;
            case SET_GAME_RULE: return Material.LECTERN;
            case CREATE_FIREWORK: return Material.FIREWORK_ROCKET;
            case FREEZE_PLAYER: return Material.ICE;
            case UNFREEZE_PLAYER: return Material.MAGMA_BLOCK;
            case SET_WALKSPEED: return Material.LEATHER_BOOTS;
            case SET_FLYSPEED: return Material.ELYTRA;
            case PUSH_PLAYER: return Material.PISTON;
            case LAUNCH_PLAYER: return Material.SLIME_BLOCK;
            case HIDE_PLAYER: return Material.POTION;
            case SHOW_PLAYER: return Material.GLASS_BOTTLE;
            case SET_EXPERIENCE: return Material.EXPERIENCE_BOTTLE;
            case GIVE_EXPERIENCE: return Material.EMERALD;
            case TAKE_EXPERIENCE: return Material.COAL;
            case WAIT: return Material.CLOCK;
            case SEND_TO_SERVER: return Material.NETHER_PORTAL;
            default: return Material.STONE;
        }
    }
    
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getView().getTitle().equals("§6Выбор действия мира")) return;
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
        
        if (slot < WorldActionBlock.WorldActionType.values().length) {
            WorldActionBlock.WorldActionType selectedAction = WorldActionBlock.WorldActionType.values()[slot];
            player.closeInventory();
            
            if (onSelect != null) {
                onSelect.accept(selectedAction);
            }
        }
    }
}
