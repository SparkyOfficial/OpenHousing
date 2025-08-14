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
import ru.openhousing.coding.values.ValueType;
import ru.openhousing.utils.ItemBuilder;
import ru.openhousing.utils.AnvilGUIHelper;

import java.util.Arrays;
import java.util.function.Consumer;

/**
 * Универсальный GUI для выбора значений
 */
public class ValueSelectorGUI implements Listener {

    private final OpenHousing plugin;
    private final Player player;
    private final String title;
    private final ValueType[] allowedTypes;
    private final Consumer<Object> callback;
    private final Inventory inventory;

    public ValueSelectorGUI(OpenHousing plugin, Player player, String title, ValueType[] allowedTypes, Consumer<Object> callback) {
        this.plugin = plugin;
        this.player = player;
        this.title = title;
        this.allowedTypes = allowedTypes;
        this.callback = callback;
        this.inventory = Bukkit.createInventory(null, 54, "§6" + title);

        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        setupGUI();
    }

    private void setupGUI() {
        inventory.clear();

        // Заголовок
        inventory.setItem(4, new ItemBuilder(Material.BOOK)
            .name("§6" + title)
            .lore("§7Выберите тип значения")
            .build());

        int slot = 10;
        for (ValueType type : allowedTypes) {
            Material material = getValueTypeMaterial(type);
            
            inventory.setItem(slot, new ItemBuilder(material)
                .name("§e" + type.getDisplayName())
                .lore(Arrays.asList(
                    "§7" + type.getDescription(),
                    "",
                    "§eНажмите для выбора"
                ))
                .build());
            
            slot++;
            if (slot == 17) slot = 19;
            if (slot == 26) slot = 28;
        }

        // Кнопки управления
        inventory.setItem(49, new ItemBuilder(Material.ARROW)
            .name("§7Назад")
            .build());

        inventory.setItem(50, new ItemBuilder(Material.BARRIER)
            .name("§cОтмена")
            .build());
    }

    private Material getValueTypeMaterial(ValueType type) {
        return switch (type) {
            case TEXT -> Material.PAPER;
            case NUMBER -> Material.GOLD_NUGGET;
            case VARIABLE -> Material.CHEST;
            case LOCATION -> Material.COMPASS;
            case ITEM -> Material.DIAMOND;
            case SOUND -> Material.NOTE_BLOCK;
            case PARTICLE -> Material.BLAZE_POWDER;
            case POTION_EFFECT -> Material.POTION;
            default -> Material.STONE;
        };
    }

    public void open() {
        player.openInventory(inventory);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getView().getTitle().equals("§6" + title)) return;
        if (!(event.getWhoClicked() instanceof Player)) return;
        if (!event.getWhoClicked().getUniqueId().equals(player.getUniqueId())) return;

        event.setCancelled(true);

        int slot = event.getSlot();
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) return;

        // Кнопки управления
        if (slot == 49) {
            // Назад - пока просто закрываем
            player.closeInventory();
            return;
        }

        if (slot == 50) {
            // Отмена
            player.closeInventory();
            return;
        }

        // Выбор типа значения
        ValueType selectedType = getValueTypeFromSlot(slot);
        if (selectedType != null) {
            player.closeInventory();
            openValueInput(selectedType);
        }
    }

    private ValueType getValueTypeFromSlot(int slot) {
        int index = -1;
        if (slot >= 10 && slot <= 16) index = slot - 10;
        else if (slot >= 19 && slot <= 25) index = slot - 19 + 7;
        else if (slot >= 28 && slot <= 34) index = slot - 28 + 14;

        if (index >= 0 && index < allowedTypes.length) {
            return allowedTypes[index];
        }
        return null;
    }

    private void openValueInput(ValueType type) {
        switch (type) {
            case TEXT:
                AnvilGUIHelper.openTextInput(plugin, player, "Введите текст", "", (text) -> {
                    callback.accept(text);
                });
                break;

            case NUMBER:
                AnvilGUIHelper.openNumberInput(plugin, player, "Введите число", "0", (number) -> {
                    callback.accept(number);
                });
                break;

            case VARIABLE:
                openVariableSelector();
                break;

            case LOCATION:
                openLocationSelector();
                break;

            case ITEM:
                openItemSelector();
                break;

            case SOUND:
                openSoundSelector();
                break;

            case PARTICLE:
                openParticleSelector();
                break;

            case POTION_EFFECT:
                openPotionEffectSelector();
                break;

            default:
                player.sendMessage("§cТип значения не поддерживается");
                break;
        }
    }

    private void openVariableSelector() {
        // Простой выбор переменных
        player.sendMessage("§eВыберите тип переменной:");
        player.sendMessage("§7- §eglobal:<имя> §7- глобальная переменная");
        player.sendMessage("§7- §elocal:<имя> §7- локальная переменная");
        player.sendMessage("§7- §esystem:<имя> §7- системная переменная");
        player.sendMessage("§7Введите в чат имя переменной:");
    }

    private void openLocationSelector() {
        // Простой выбор локаций
        player.sendMessage("§eВыберите локацию:");
        player.sendMessage("§7- §ecurrent §7- текущая позиция");
        player.sendMessage("§7- §espawn §7- точка спавна мира");
        player.sendMessage("§7- §ebed §7- кровать игрока");
        player.sendMessage("§7- §ex,y,z §7- координаты (например: 100,64,200)");
        player.sendMessage("§7Введите в чат тип локации:");
    }

    private void openItemSelector() {
        // Предмет в руке или по ID
        ItemStack inHand = player.getInventory().getItemInMainHand();
        if (inHand != null && inHand.getType() != Material.AIR) {
            callback.accept(inHand);
            player.sendMessage("§aВыбран предмет из руки: " + inHand.getType().name());
        } else {
            player.sendMessage("§cВозьмите предмет в руку или введите ID материала в чат:");
        }
    }

    private void openSoundSelector() {
        player.sendMessage("§eВведите название звука в чат:");
        player.sendMessage("§7Примеры: ENTITY_PLAYER_LEVELUP, BLOCK_NOTE_BLOCK_PLING");
    }

    private void openParticleSelector() {
        player.sendMessage("§eВведите название частицы в чат:");
        player.sendMessage("§7Примеры: FLAME, HEART, EXPLOSION_LARGE");
    }

    private void openPotionEffectSelector() {
        player.sendMessage("§eВведите эффект зелья в чат:");
        player.sendMessage("§7Примеры: SPEED, STRENGTH, REGENERATION");
    }
}