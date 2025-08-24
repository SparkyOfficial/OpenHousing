package ru.openhousing.coding.gui;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
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
import ru.openhousing.coding.gui.VariablesViewerGUI;
import ru.openhousing.coding.gui.ExecutionLogViewerGUI;
import ru.openhousing.coding.gui.StatsViewerGUI;

import java.util.Arrays;

/**
 * GUI для отладки кода и просмотра переменных
 */
public class DebugGUI implements IBlockConfigurationGUI {
    
    private final OpenHousing plugin;
    private final Player player;
    private final ExecutionContext context;
    private Inventory inventory;
    
    // Слоты GUI
    private static final int DEBUG_MODE_SLOT = 10;
    private static final int VARIABLES_SLOT = 12;
    private static final int EXECUTION_LOG_SLOT = 14;
    private static final int STATS_SLOT = 16;
    private static final int REFRESH_SLOT = 22;
    private static final int CLOSE_SLOT = 26;
    
    public DebugGUI(OpenHousing plugin, Player player, ExecutionContext context) {
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
        return null; // DebugGUI не привязан к конкретному блоку
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
        this.inventory = Bukkit.createInventory(null, 36, "§8Отладка кода");
        
        // Заполняем фон
        for (int i = 0; i < inventory.getSize(); i++) {
            inventory.setItem(i, new ItemStack(Material.GRAY_STAINED_GLASS_PANE));
        }
        
        // Заголовок
        ItemStack headerItem = new ItemStack(Material.BOOK);
        ItemMeta headerMeta = headerItem.getItemMeta();
        headerMeta.setDisplayName("§6§lОтладка кода");
        headerMeta.setLore(Arrays.asList(
            "§7Просмотр переменных и логов",
            "§7выполнения кода"
        ));
        headerItem.setItemMeta(headerMeta);
        inventory.setItem(4, headerItem);
        
        // Режим отладки
        ItemStack debugItem = new ItemStack(context.isDebugMode() ? Material.LIME_DYE : Material.RED_DYE);
        ItemMeta debugMeta = debugItem.getItemMeta();
        debugMeta.setDisplayName("§eРежим отладки");
        debugMeta.setLore(Arrays.asList(
            "§7Текущий: " + (context.isDebugMode() ? "§aВключен" : "§cВыключен"),
            "",
            "§7Кликните для переключения",
            "§7режима отладки"
        ));
        debugItem.setItemMeta(debugMeta);
        inventory.setItem(DEBUG_MODE_SLOT, debugItem);
        
        // Переменные
        ItemStack varsItem = new ItemStack(Material.PAPER);
        ItemMeta varsMeta = varsItem.getItemMeta();
        varsMeta.setDisplayName("§aПеременные");
        varsMeta.setLore(Arrays.asList(
            "§7Локальные: §f" + context.getLocalVariables().size(),
            "§7Глобальные: §f" + context.getGlobalVariables().size(),
            "§7Системные: §f" + context.getSystemVariables().size(),
            "",
            "§7Кликните для просмотра",
            "§7всех переменных"
        ));
        varsItem.setItemMeta(varsMeta);
        inventory.setItem(VARIABLES_SLOT, varsItem);
        
        // Лог выполнения
        ItemStack logItem = new ItemStack(Material.BOOK);
        ItemMeta logMeta = logItem.getItemMeta();
        logMeta.setDisplayName("§bЛог выполнения");
        logMeta.setLore(Arrays.asList(
            "§7Записей: §f" + context.getExecutionLog().size(),
            "§7Глубина: §f" + context.getExecutionDepth(),
            "",
            "§7Кликните для просмотра",
            "§7лога выполнения"
        ));
        logItem.setItemMeta(logMeta);
        inventory.setItem(EXECUTION_LOG_SLOT, logItem);
        
        // Статистика
        ItemStack statsItem = new ItemStack(Material.CLOCK);
        ItemMeta statsMeta = statsItem.getItemMeta();
        statsMeta.setDisplayName("§dСтатистика");
        statsMeta.setLore(Arrays.asList(
            "§7Время выполнения: §f" + context.getExecutionStats().getExecutionTime() + "ms",
            "§7Макс. глубина: §f" + context.getExecutionStats().getMaxDepth(),
            "§7Всего переменных: §f" + context.getExecutionStats().getTotalVarCount(),
            "",
            "§7Кликните для подробной",
            "§7статистики"
        ));
        statsItem.setItemMeta(statsMeta);
        inventory.setItem(STATS_SLOT, statsItem);
        
        // Кнопка обновления
        ItemStack refreshItem = new ItemStack(Material.EMERALD);
        ItemMeta refreshMeta = refreshItem.getItemMeta();
        refreshMeta.setDisplayName("§aОбновить");
        refreshMeta.setLore(Arrays.asList("§7Обновить данные"));
        refreshItem.setItemMeta(refreshMeta);
        inventory.setItem(REFRESH_SLOT, refreshItem);
        
        // Кнопка закрытия
        ItemStack closeItem = new ItemStack(Material.BARRIER);
        ItemMeta closeMeta = closeItem.getItemMeta();
        closeMeta.setDisplayName("§cЗакрыть");
        closeMeta.setLore(Arrays.asList("§7Закрыть отладчик"));
        closeItem.setItemMeta(closeMeta);
        inventory.setItem(CLOSE_SLOT, closeItem);
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
            case DEBUG_MODE_SLOT:
                toggleDebugMode();
                break;
            case VARIABLES_SLOT:
                openVariablesViewer();
                break;
            case EXECUTION_LOG_SLOT:
                openExecutionLogViewer();
                break;
            case STATS_SLOT:
                openStatsViewer();
                break;
            case REFRESH_SLOT:
                refreshData();
                break;
            case CLOSE_SLOT:
                close();
                break;
        }
    }
    
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (isValidInventory(event.getInventory())) {
            close();
        }
    }
    
    private void toggleDebugMode() {
        boolean newMode = !context.isDebugMode();
        context.setDebugMode(newMode);
        
        player.sendMessage("§aРежим отладки " + (newMode ? "включен" : "выключен"));
        refreshData();
    }
    
    private void openVariablesViewer() {
        close();
        new VariablesViewerGUI(plugin, player, context).open();
    }
    
    private void openExecutionLogViewer() {
        close();
        new ExecutionLogViewerGUI(plugin, player, context).open();
    }
    
    private void openStatsViewer() {
        close();
        new StatsViewerGUI(plugin, player, context).open();
    }
    
    private void refreshData() {
        setupInventory();
        player.openInventory(inventory);
        player.sendMessage("§aДанные обновлены");
    }
    
    /**
     * Получить инвентарь GUI
     */
    public Inventory getInventory() {
        return inventory;
    }
    
    /**
     * Обновить инвентарь
     */
    public void updateInventory() {
        setupInventory();
        if (player.getOpenInventory().getTopInventory().equals(inventory)) {
            player.updateInventory();
        }
    }
    

}
