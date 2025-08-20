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
import ru.openhousing.coding.blocks.actions.PlayerActionBlock;
import ru.openhousing.utils.ItemBuilder;

import java.util.Arrays;
import java.util.function.Consumer;

/**
 * GUI для выбора типа действия игрока
 */
public class PlayerActionSelectorGUI implements Listener {
    
    private final OpenHousing plugin;
    private final Player player;
    private final Consumer<PlayerActionBlock.PlayerActionType> onSelect;
    private final Inventory inventory;
    
    public PlayerActionSelectorGUI(OpenHousing plugin, Player player, Consumer<PlayerActionBlock.PlayerActionType> onSelect) {
        this.plugin = plugin;
        this.player = player;
        this.onSelect = onSelect;
        this.inventory = Bukkit.createInventory(null, 54, "§6Выбор действия игрока");
        
        // Регистрируем listener
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        
        setupGUI();
    }
    
    private void setupGUI() {
        inventory.clear();
        
        // Действия игрока
        PlayerActionBlock.PlayerActionType[] actions = PlayerActionBlock.PlayerActionType.values();
        
        int slot = 9; // Начинаем с второй строки
        for (PlayerActionBlock.PlayerActionType actionType : actions) {
            if (slot >= 45) break; // Не выходим за пределы инвентаря
            
            Material material = getActionMaterial(actionType);
            
            ItemStack item = new ItemBuilder(material)
                .name("§e" + actionType.getDisplayName())
                .lore(Arrays.asList(
                    "§7" + actionType.getDescription(),
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
    
    private Material getActionMaterial(PlayerActionBlock.PlayerActionType actionType) {
        return switch (actionType) {
            case SEND_MESSAGE -> Material.PAPER;
            case TELEPORT -> Material.ENDER_PEARL;
            case GIVE_ITEM -> Material.CHEST;
            case TAKE_ITEM -> Material.HOPPER;
            case SET_HEALTH -> Material.GOLDEN_APPLE;
            case SET_FOOD -> Material.BREAD;
            case SET_LEVEL -> Material.EXPERIENCE_BOTTLE;
            case ADD_EXPERIENCE -> Material.EMERALD;
            case SET_GAMEMODE -> Material.COMMAND_BLOCK;
            case PLAY_SOUND -> Material.NOTE_BLOCK;
            case SHOW_TITLE -> Material.BOOK;
            case SEND_ACTIONBAR -> Material.OAK_SIGN;
            case KICK_PLAYER -> Material.BARRIER;
            case ADD_POTION_EFFECT -> Material.POTION;
            case REMOVE_POTION_EFFECT -> Material.MILK_BUCKET;
            case CLEAR_INVENTORY -> Material.LAVA_BUCKET;
            case OPEN_INVENTORY -> Material.ENDER_CHEST;
            case CLOSE_INVENTORY -> Material.IRON_DOOR;
            case SET_FLY -> Material.ELYTRA;
            case SPAWN_PARTICLE -> Material.FIREWORK_ROCKET;
            case STRIKE_LIGHTNING -> Material.LIGHTNING_ROD;
            case SET_FIRE -> Material.FIRE_CHARGE;
            case EXTINGUISH -> Material.WATER_BUCKET;
            case HIDE_PLAYER -> Material.POTION;
            case SHOW_PLAYER -> Material.GLASS_BOTTLE;
            default -> Material.REDSTONE;
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
        
        if (title == null || !title.equals("§6Выбор действия игрока")) return;
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
        
        // Определяем выбранное действие по слоту
        PlayerActionBlock.PlayerActionType selectedAction = getActionBySlot(slot);
        if (selectedAction != null) {
            player.closeInventory();
            if (onSelect != null) {
                onSelect.accept(selectedAction);
            }
        }
    }
    
    private PlayerActionBlock.PlayerActionType getActionBySlot(int slot) {
        PlayerActionBlock.PlayerActionType[] actions = PlayerActionBlock.PlayerActionType.values();
        
        // Преобразуем слот обратно в индекс действия
        int actionIndex = -1;
        int currentSlot = 9;
        
        for (int i = 0; i < actions.length; i++) {
            if (currentSlot == slot) {
                actionIndex = i;
                break;
            }
            currentSlot++;
            if ((currentSlot + 1) % 9 == 0) currentSlot += 2;
        }
        
        return (actionIndex >= 0 && actionIndex < actions.length) ? actions[actionIndex] : null;
    }
}
