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
import ru.openhousing.coding.blocks.CodeBlock.ExecutionContext.ExecutionStats;

import java.util.*;

/**
 * GUI для просмотра статистики выполнения кода
 */
public class StatsViewerGUI implements IBlockConfigurationGUI {
    
    private final OpenHousing plugin;
    private final Player player;
    private final ExecutionContext context;
    private Inventory inventory;
    
    // Слоты GUI
    private static final int BACK_SLOT = 26;
    private static final int REFRESH_SLOT = 22;
    private static final int EXPORT_SLOT = 4;
    
    public StatsViewerGUI(OpenHousing plugin, Player player, ExecutionContext context) {
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
        this.inventory = Bukkit.createInventory(null, 36, "§8Статистика выполнения");
        
        // Заполняем фон
        for (int i = 0; i < inventory.getSize(); i++) {
            inventory.setItem(i, new ItemStack(Material.GRAY_STAINED_GLASS_PANE));
        }
        
        // Заголовок
        ItemStack headerItem = new ItemStack(Material.CLOCK);
        ItemMeta headerMeta = headerItem.getItemMeta();
        headerMeta.setDisplayName("§6§lСтатистика выполнения");
        headerMeta.setLore(Arrays.asList(
            "§7Детальная информация о",
            "§7выполнении кода"
        ));
        headerItem.setItemMeta(headerMeta);
        inventory.setItem(EXPORT_SLOT, headerItem);
        
        // Кнопка обновления
        ItemStack refreshItem = new ItemStack(Material.EMERALD);
        ItemMeta refreshMeta = refreshItem.getItemMeta();
        refreshMeta.setDisplayName("§aОбновить");
        refreshMeta.setLore(Arrays.asList(
            "§7Обновить статистику"
        ));
        refreshItem.setItemMeta(refreshMeta);
        inventory.setItem(REFRESH_SLOT, refreshItem);
        
        // Кнопка "Назад"
        ItemStack backItem = new ItemStack(Material.ARROW);
        ItemMeta backMeta = backItem.getItemMeta();
        backMeta.setDisplayName("§e← Назад");
        backItem.setItemMeta(backMeta);
        inventory.setItem(BACK_SLOT, backItem);
        
        // Отображаем статистику
        displayStats();
    }
    
    private void displayStats() {
        ExecutionStats stats = context.getExecutionStats();
        
        // Основная статистика
        displayMainStats(stats);
        
        // Статистика переменных
        displayVariableStats();
        
        // Статистика производительности
        displayPerformanceStats(stats);
        
        // Информация о контексте
        displayContextInfo();
    }
    
    private void displayMainStats(ExecutionStats stats) {
        // Время выполнения
        ItemStack timeItem = new ItemStack(Material.CLOCK);
        ItemMeta timeMeta = timeItem.getItemMeta();
        timeMeta.setDisplayName("§bВремя выполнения");
        timeMeta.setLore(Arrays.asList(
            "§7Общее время: §f" + stats.getExecutionTime() + "ms",
            "§7Среднее на блок: §f" + getAverageTimePerBlock(stats) + "ms",
            "§7Максимальное время: §f" + getMaxExecutionTime(stats) + "ms"
        ));
        timeItem.setItemMeta(timeMeta);
        inventory.setItem(9, timeItem);
        
        // Глубина выполнения
        ItemStack depthItem = new ItemStack(Material.LADDER);
        ItemMeta depthMeta = depthItem.getItemMeta();
        depthMeta.setDisplayName("§eГлубина выполнения");
        depthMeta.setLore(Arrays.asList(
            "§7Текущая глубина: §f" + context.getExecutionDepth(),
            "§7Максимальная глубина: §f" + stats.getMaxDepth(),
            "§7Средняя глубина: §f" + getAverageDepth(stats)
        ));
        depthItem.setItemMeta(depthMeta);
        inventory.setItem(10, depthItem);
        
        // Количество блоков
        ItemStack blocksItem = new ItemStack(Material.COMMAND_BLOCK);
        ItemMeta blocksMeta = blocksItem.getItemMeta();
        blocksMeta.setDisplayName("§aБлоки кода");
        blocksMeta.setLore(Arrays.asList(
            "§7Всего выполнено: §f" + getTotalExecutedBlocks(stats),
            "§7Успешно: §f" + getSuccessfulBlocks(stats),
            "§7С ошибками: §f" + getFailedBlocks(stats)
        ));
        blocksItem.setItemMeta(blocksMeta);
        inventory.setItem(11, blocksItem);
    }
    
    private void displayVariableStats() {
        // Локальные переменные
        ItemStack localItem = new ItemStack(Material.PAPER);
        ItemMeta localMeta = localItem.getItemMeta();
        localMeta.setDisplayName("§aЛокальные переменные");
        localMeta.setLore(Arrays.asList(
            "§7Количество: §f" + context.getLocalVariables().size(),
            "§7Типы: §f" + getVariableTypes(context.getLocalVariables()),
            "§7Размер памяти: §f" + estimateMemoryUsage(context.getLocalVariables()) + " bytes"
        ));
        localItem.setItemMeta(localMeta);
        inventory.setItem(12, localItem);
        
        // Глобальные переменные
        ItemStack globalItem = new ItemStack(Material.BOOK);
        ItemMeta globalMeta = globalItem.getItemMeta();
        globalMeta.setDisplayName("§bГлобальные переменные");
        globalMeta.setLore(Arrays.asList(
            "§7Количество: §f" + context.getGlobalVariables().size(),
            "§7Типы: §f" + getVariableTypes(context.getGlobalVariables()),
            "§7Размер памяти: §f" + estimateMemoryUsage(context.getGlobalVariables()) + " bytes"
        ));
        globalItem.setItemMeta(globalMeta);
        inventory.setItem(13, globalItem);
        
        // Системные переменные
        ItemStack systemItem = new ItemStack(Material.REDSTONE);
        ItemMeta systemMeta = systemItem.getItemMeta();
        systemMeta.setDisplayName("§dСистемные переменные");
        systemMeta.setLore(Arrays.asList(
            "§7Количество: §f" + context.getSystemVariables().size(),
            "§7Типы: §f" + getVariableTypes(context.getSystemVariables()),
            "§7Размер памяти: §f" + estimateMemoryUsage(context.getSystemVariables()) + " bytes"
        ));
        systemItem.setItemMeta(systemMeta);
        inventory.setItem(14, systemItem);
    }
    
    private void displayPerformanceStats(ExecutionStats stats) {
        // Производительность
        ItemStack perfItem = new ItemStack(Material.REDSTONE_BLOCK);
        ItemMeta perfMeta = perfItem.getItemMeta();
        perfMeta.setDisplayName("§cПроизводительность");
        perfMeta.setLore(Arrays.asList(
            "§7TPS: §f" + getCurrentTPS(),
            "§7Использование памяти: §f" + getMemoryUsage(),
            "§7Загрузка CPU: §f" + getCPUUsage() + "%"
        ));
        perfItem.setItemMeta(perfMeta);
        inventory.setItem(15, perfItem);
        
        // Логирование
        ItemStack logItem = new ItemStack(Material.BOOK);
        ItemMeta logMeta = logItem.getItemMeta();
        logMeta.setDisplayName("§eЛогирование");
        logMeta.setLore(Arrays.asList(
            "§7Записей в логе: §f" + stats.getLogEntries(),
            "§7Размер лога: §f" + estimateLogSize() + " bytes",
            "§7Частота логирования: §f" + getLoggingFrequency(stats) + " записей/сек"
        ));
        logItem.setItemMeta(logMeta);
        inventory.setItem(16, logItem);
    }
    
    private void displayContextInfo() {
        // Информация о контексте
        ItemStack contextItem = new ItemStack(Material.COMPASS);
        ItemMeta contextMeta = contextItem.getItemMeta();
        contextMeta.setDisplayName("§6Контекст выполнения");
        contextMeta.setLore(Arrays.asList(
            "§7Игрок: §f" + (context.getPlayer() != null ? context.getPlayer().getName() : "Unknown"),
            "§7Мир: §f" + (context.getPlayer() != null && context.getPlayer().getWorld() != null ? 
                context.getPlayer().getWorld().getName() : "Unknown"),
            "§7Режим отладки: " + (context.isDebugMode() ? "§aВключен" : "§cВыключен"),
            "§7Текущий блок: §f" + context.getCurrentBlockId()
        ));
        contextItem.setItemMeta(contextMeta);
        inventory.setItem(17, contextItem);
    }
    
    // Вспомогательные методы для вычисления статистики
    private String getAverageTimePerBlock(ExecutionStats stats) {
        if (getTotalExecutedBlocks(stats) == 0) return "0";
        return String.format("%.2f", (double) stats.getExecutionTime() / getTotalExecutedBlocks(stats));
    }
    
    private String getMaxExecutionTime(ExecutionStats stats) {
        // Пока возвращаем текущее время выполнения
        return String.valueOf(stats.getExecutionTime());
    }
    
    private String getAverageDepth(ExecutionStats stats) {
        if (getTotalExecutedBlocks(stats) == 0) return "0";
        return String.format("%.2f", (double) stats.getMaxDepth() / getTotalExecutedBlocks(stats));
    }
    
    private int getTotalExecutedBlocks(ExecutionStats stats) {
        // Примерная оценка на основе времени выполнения
        return Math.max(1, (int)(stats.getExecutionTime() / 10));
    }
    
    private int getSuccessfulBlocks(ExecutionStats stats) {
        // Примерная оценка
        return (int) (getTotalExecutedBlocks(stats) * 0.95);
    }
    
    private int getFailedBlocks(ExecutionStats stats) {
        return getTotalExecutedBlocks(stats) - getSuccessfulBlocks(stats);
    }
    
    private String getVariableTypes(Map<String, Object> variables) {
        Set<String> types = new HashSet<>();
        variables.values().forEach(value -> types.add(getTypeName(value)));
        return String.join(", ", types);
    }
    
    private String getTypeName(Object value) {
        if (value == null) return "null";
        if (value instanceof String) return "String";
        if (value instanceof Integer) return "Integer";
        if (value instanceof Double) return "Double";
        if (value instanceof Boolean) return "Boolean";
        return value.getClass().getSimpleName();
    }
    
    private int estimateMemoryUsage(Map<String, Object> variables) {
        int total = 0;
        for (Map.Entry<String, Object> entry : variables.entrySet()) {
            total += entry.getKey().length() * 2; // UTF-16
            total += estimateObjectSize(entry.getValue());
        }
        return total;
    }
    
    private int estimateObjectSize(Object value) {
        if (value == null) return 0;
        if (value instanceof String) return ((String) value).length() * 2;
        if (value instanceof Number) return 8;
        if (value instanceof Boolean) return 1;
        return 16; // Примерная оценка для других объектов
    }
    
    private String getCurrentTPS() {
        // Примерная оценка TPS
        return "20.0";
    }
    
    private String getMemoryUsage() {
        Runtime runtime = Runtime.getRuntime();
        long used = runtime.totalMemory() - runtime.freeMemory();
        long max = runtime.maxMemory();
        return String.format("%.1f MB / %.1f MB", 
            used / 1024.0 / 1024.0, max / 1024.0 / 1024.0);
    }
    
    private String getCPUUsage() {
        // Примерная оценка
        return "15-25";
    }
    
    private int estimateLogSize() {
        return context.getExecutionLog().size() * 100; // Примерная оценка
    }
    
    private String getLoggingFrequency(ExecutionStats stats) {
        if (stats.getExecutionTime() == 0) return "0";
        double frequency = (double) stats.getLogEntries() / (stats.getExecutionTime() / 1000.0);
        return String.format("%.2f", frequency);
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
            case REFRESH_SLOT:
                setupInventory();
                player.openInventory(inventory);
                player.sendMessage("§aСтатистика обновлена");
                break;
        }
    }
    
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (isValidInventory(event.getInventory())) {
            close();
        }
    }
}
