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
import ru.openhousing.coding.blocks.CodeBlock;
import ru.openhousing.coding.script.CodeLine;
import ru.openhousing.coding.script.CodeScript;
import ru.openhousing.utils.ItemBuilder;

import java.util.Arrays;
import java.util.List;

/**
 * Простой просмотр блоков в строке
 */
public class LineBlocksGUI implements Listener {

    private final OpenHousing plugin;
    private final Player player;
    private final CodeScript script;
    private final CodeLine line;
    private final Inventory inventory;

    public LineBlocksGUI(OpenHousing plugin, Player player, CodeScript script, CodeLine line) {
        this.plugin = plugin;
        this.player = player;
        this.script = script;
        this.line = line;
        this.inventory = Bukkit.createInventory(null, 54, "§6Блоки строки: " + line.getName());
        
        // Регистрируем listener
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        
        setupGUI();
    }

    private void setupGUI() {
        inventory.clear();

        // Информация о строке
        inventory.setItem(4, new ItemBuilder(Material.BOOK)
            .name("§6Строка #" + line.getLineNumber())
            .lore(Arrays.asList(
                "§7Название: §e" + line.getName(),
                "§7Блоков: §e" + line.getBlockCount(),
                "§7Состояние: " + (line.isEnabled() ? "§aВключена" : "§cВыключена")
            ))
            .build());

        // Отображаем блоки
        List<CodeBlock> blocks = line.getBlocks();
        for (int i = 0; i < Math.min(blocks.size(), 28); i++) {
            CodeBlock block = blocks.get(i);
            
            ItemStack item = new ItemBuilder(block.getType().getMaterial())
                .name("§e" + block.getType().getDisplayName())
                .lore(Arrays.asList(
                    "§7Тип: §f" + block.getType().name(),
                    "§7Описание: §f" + block.getType().getDescription(),
                    "",
                    "§eЛКМ - редактировать",
                    "§cПКМ - удалить"
                ))
                .build();
                
            // Размещаем блоки в центральной области (слоты 10-37)
            int slot = 10 + i;
            if (slot > 16) slot += 2; // Пропускаем края
            if (slot > 25) slot += 2;
            if (slot > 34) slot += 2;
            if (slot < 44) {
                inventory.setItem(slot, item);
            }
        }

        // Кнопки управления
        inventory.setItem(48, new ItemBuilder(Material.LIME_DYE)
            .name("§aДобавить блок")
            .lore("§7Открыть выбор блоков")
            .build());

        inventory.setItem(49, new ItemBuilder(Material.ARROW)
            .name("§7Назад к редактору")
            .build());

        inventory.setItem(50, new ItemBuilder(Material.BARRIER)
            .name("§cЗакрыть")
            .build());
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
        
        if (title == null || !title.startsWith("§6Блоки строки:")) return;
        if (!(event.getWhoClicked() instanceof Player)) return;
        if (!event.getWhoClicked().getUniqueId().equals(player.getUniqueId())) return;
        
        event.setCancelled(true);

        int slot = event.getSlot();
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) return;

        // Кнопки управления
        if (slot == 49) {
            // Назад к редактору
            player.closeInventory();
            plugin.getCodeManager().openCodeEditor(player);
            return;
        }
        
        if (slot == 50) {
            // Закрыть
            player.closeInventory();
            return;
        }
        
        if (slot == 48) {
            // Добавить блок - открываем редактор в режиме добавления
            player.closeInventory();
            plugin.getCodeManager().openCodeEditor(player);
            player.sendMessage("§eИспользуйте редактор для добавления блоков в строку");
            return;
        }

        // Клик по блоку
        int blockIndex = getBlockIndex(slot);
        if (blockIndex >= 0 && blockIndex < line.getBlocks().size()) {
            CodeBlock block = line.getBlocks().get(blockIndex);
            
            if (event.isRightClick()) {
                // Удаление блока
                if (script.removeBlockFromLine(line.getLineNumber(), block)) {
                    player.sendMessage("§cБлок удален из строки");
                    setupGUI(); // Обновляем GUI
                }
            } else {
                // Редактирование блока - открываем BlockConfigGUI
                player.closeInventory();
                new BlockConfigGUI(plugin, player, block, (updatedBlock) -> {
                    // Обновляем блок в строке
                    line.replaceBlock(blockIndex, updatedBlock);
                    player.sendMessage("§aБлок обновлен!");
                    
                    // Возвращаемся к просмотру блоков
                    new LineBlocksGUI(plugin, player, script, line).open();
                }).open();
            }
        }
    }

    private int getBlockIndex(int slot) {
        // Преобразуем слот обратно в индекс блока
        if (slot >= 10 && slot <= 16) return slot - 10;
        if (slot >= 19 && slot <= 25) return slot - 19 + 7;
        if (slot >= 28 && slot <= 34) return slot - 28 + 14;
        if (slot >= 37 && slot <= 43) return slot - 37 + 21;
        return -1;
    }
}
