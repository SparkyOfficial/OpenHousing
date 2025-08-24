package ru.openhousing.coding.gui;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import ru.openhousing.OpenHousing;
import ru.openhousing.coding.blocks.CodeBlock;
import ru.openhousing.coding.blocks.CodeBlock.ExecutionContext;

import java.util.*;

/**
 * GUI для просмотра лога выполнения кода
 */
public class ExecutionLogViewerGUI implements IBlockConfigurationGUI {
    
    private final OpenHousing plugin;
    private final Player player;
    private final ExecutionContext context;
    private Inventory inventory;
    private int currentPage = 0;
    private static final int ITEMS_PER_PAGE = 21; // 3 ряда по 7 слотов
    
    // Слоты GUI
    private static final int BACK_SLOT = 26;
    private static final int PREV_PAGE_SLOT = 18;
    private static final int NEXT_PAGE_SLOT = 8;
    private static final int CLEAR_SLOT = 22;
    private static final int FILTER_SLOT = 4;
    
    public ExecutionLogViewerGUI(OpenHousing plugin, Player player, ExecutionContext context) {
        this.plugin = plugin;
        this.player = player;
        this.context = context;
    }
    
    @Override
    public void open() {
        setupInventory();
        player.openInventory(inventory);
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }
    
    @Override
    public CodeBlock getBlock() {
        return null;
    }
    
    @Override
    public void close() {
        HandlerList.unregisterAll(this);
    }
    
    @Override
    public boolean isValidInventory(Inventory inventory) {
        return this.inventory != null && this.inventory.equals(inventory);
    }
    
    @Override
    public void setupInventory() {
        this.inventory = Bukkit.createInventory(null, 36, "§8Лог выполнения");
        
        // Заполняем фон
        for (int i = 0; i < inventory.getSize(); i++) {
            inventory.setItem(i, new ItemStack(Material.GRAY_STAINED_GLASS_PANE));
        }
        
        // Заголовок
        ItemStack headerItem = new ItemStack(Material.BOOK);
        ItemMeta headerMeta = headerItem.getItemMeta();
        headerMeta.setDisplayName("§6§lЛог выполнения");
        headerMeta.setLore(Arrays.asList(
            "§7Просмотр истории выполнения",
            "§7кода и отладочной информации"
        ));
        headerItem.setItemMeta(headerMeta);
        inventory.setItem(FILTER_SLOT, headerItem);
        
        // Кнопка очистки
        ItemStack clearItem = new ItemStack(Material.BARRIER);
        ItemMeta clearMeta = clearItem.getItemMeta();
        clearMeta.setDisplayName("§cОчистить лог");
        clearMeta.setLore(Arrays.asList(
            "§7Очистить весь лог",
            "§7выполнения"
        ));
        clearItem.setItemMeta(clearMeta);
        inventory.setItem(CLEAR_SLOT, clearItem);
        
        // Навигация
        setupNavigation();
        
        // Записи лога
        displayLogEntries();
    }
    
    private void setupNavigation() {
        // Кнопка "Назад"
        ItemStack backItem = new ItemStack(Material.ARROW);
        ItemMeta backMeta = backItem.getItemMeta();
        backMeta.setDisplayName("§e← Назад");
        backItem.setItemMeta(backMeta);
        inventory.setItem(BACK_SLOT, backItem);
        
        // Кнопка "Предыдущая страница"
        if (currentPage > 0) {
            ItemStack prevItem = new ItemStack(Material.ARROW);
            ItemMeta prevMeta = prevItem.getItemMeta();
            prevMeta.setDisplayName("§e← Предыдущая");
            prevItem.setItemMeta(prevMeta);
            inventory.setItem(PREV_PAGE_SLOT, prevItem);
        }
        
        // Кнопка "Следующая страница"
        int totalPages = getTotalPages();
        if (currentPage < totalPages - 1) {
            ItemStack nextItem = new ItemStack(Material.ARROW);
            ItemMeta nextMeta = nextItem.getItemMeta();
            nextMeta.setDisplayName("§eСледующая →");
            nextItem.setItemMeta(nextMeta);
            inventory.setItem(NEXT_PAGE_SLOT, nextItem);
        }
        
        // Информация о странице
        ItemStack pageInfo = new ItemStack(Material.CLOCK);
        ItemMeta pageMeta = pageInfo.getItemMeta();
        pageMeta.setDisplayName("§bСтраница " + (currentPage + 1) + " из " + totalPages);
        pageMeta.setLore(Arrays.asList(
            "§7Всего записей: " + getTotalLogCount(),
            "§7Текущая глубина: " + context.getExecutionDepth(),
            "§7Время выполнения: " + context.getExecutionStats().getExecutionTime() + "ms"
        ));
        pageInfo.setItemMeta(pageMeta);
        inventory.setItem(13, pageInfo);
    }
    
    private void displayLogEntries() {
        List<String> logEntries = context.getExecutionLog();
        int startIndex = currentPage * ITEMS_PER_PAGE;
        int endIndex = Math.min(startIndex + ITEMS_PER_PAGE, logEntries.size());
        
        int slot = 9; // Начинаем с 9-го слота (второй ряд)
        for (int i = startIndex; i < endIndex; i++) {
            String entry = logEntries.get(i);
            inventory.setItem(slot, createLogEntryItem(entry, i));
            slot++;
            
            // Переходим на следующий ряд каждые 7 слотов
            if ((slot - 9) % 7 == 0) {
                slot += 2; // Пропускаем 2 слота для навигации
            }
        }
    }
    
    private ItemStack createLogEntryItem(String entry, int index) {
        Material material = getMaterialForLogEntry(entry);
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        
        meta.setDisplayName("§7#" + (index + 1));
        
        List<String> lore = new ArrayList<>();
        
        // Разбиваем длинную запись на строки
        String[] lines = entry.split("\n");
        for (String line : lines) {
            if (line.length() > 30) {
                lore.add("§7" + line.substring(0, 27) + "...");
            } else {
                lore.add("§7" + line);
            }
        }
        
        // Добавляем информацию о времени
        long currentTime = System.currentTimeMillis();
        long entryTime = context.getStartTime() + (index * 100); // Примерное время
        lore.add("");
        lore.add("§8Время: " + formatTime(entryTime));
        
        meta.setLore(lore);
        item.setItemMeta(meta);
        
        return item;
    }
    
    private Material getMaterialForLogEntry(String entry) {
        String lowerEntry = entry.toLowerCase();
        
        if (lowerEntry.contains("ошибка") || lowerEntry.contains("error")) {
            return Material.REDSTONE;
        } else if (lowerEntry.contains("предупреждение") || lowerEntry.contains("warning")) {
            return Material.YELLOW_DYE;
        } else if (lowerEntry.contains("блок") || lowerEntry.contains("block")) {
            return Material.COMMAND_BLOCK;
        } else if (lowerEntry.contains("переменная") || lowerEntry.contains("variable")) {
            return Material.PAPER;
        } else if (lowerEntry.contains("событие") || lowerEntry.contains("event")) {
            return Material.REDSTONE_TORCH;
        } else if (lowerEntry.contains("выполнение") || lowerEntry.contains("execution")) {
            return Material.EMERALD;
        } else {
            return Material.BOOK;
        }
    }
    
    private String formatTime(long timestamp) {
        long diff = System.currentTimeMillis() - timestamp;
        if (diff < 1000) {
            return diff + "ms назад";
        } else if (diff < 60000) {
            return (diff / 1000) + "s назад";
        } else {
            return (diff / 60000) + "m назад";
        }
    }
    
    private int getTotalPages() {
        int totalLogs = getTotalLogCount();
        return (int) Math.ceil((double) totalLogs / ITEMS_PER_PAGE);
    }
    
    private int getTotalLogCount() {
        return context.getExecutionLog().size();
    }
    
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!isValidInventory(event.getInventory())) return;
        event.setCancelled(true);
        
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player clicker = (Player) event.getWhoClicked();
        if (!clicker.equals(player)) return;
        
        int slot = event.getRawSlot();
        
        switch (slot) {
            case BACK_SLOT:
                close();
                new DebugGUI(plugin, player, context).open();
                break;
            case PREV_PAGE_SLOT:
                if (currentPage > 0) {
                    currentPage--;
                    setupInventory();
                    player.openInventory(inventory);
                }
                break;
            case NEXT_PAGE_SLOT:
                int totalPages = getTotalPages();
                if (currentPage < totalPages - 1) {
                    currentPage++;
                    setupInventory();
                    player.openInventory(inventory);
                }
                break;
            case CLEAR_SLOT:
                clearExecutionLog();
                break;
            default:
                // Проверяем, не кликнули ли по записи лога
                if (slot >= 9 && slot < 36 && (slot - 9) % 9 < 7) {
                    handleLogEntryClick(slot);
                }
                break;
        }
    }
    
    private void handleLogEntryClick(int slot) {
        List<String> logEntries = context.getExecutionLog();
        int startIndex = currentPage * ITEMS_PER_PAGE;
        int slotIndex = (slot - 9) + startIndex;
        
        if (slotIndex < logEntries.size()) {
            String entry = logEntries.get(slotIndex);
            player.sendMessage("§eЗапись лога #" + (slotIndex + 1) + ":");
            player.sendMessage("§7" + entry);
        }
    }
    
    private void clearExecutionLog() {
        int count = context.getExecutionLog().size();
        context.clearExecutionLog();
        player.sendMessage("§aОчищен лог выполнения (" + count + " записей)");
        setupInventory();
        player.openInventory(inventory);
    }
    
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (isValidInventory(event.getInventory())) {
            close();
        }
    }
    
    /**
     * Получить инвентарь
     */
    public Inventory getInventory() {
        return inventory;
    }
}
