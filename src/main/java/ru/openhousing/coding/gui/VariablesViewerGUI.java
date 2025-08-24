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
 * GUI для просмотра переменных в контексте выполнения
 */
public class VariablesViewerGUI implements IBlockConfigurationGUI {
    
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
    
    public VariablesViewerGUI(OpenHousing plugin, Player player, ExecutionContext context) {
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
        this.inventory = Bukkit.createInventory(null, 36, "§8Переменные");
        
        // Заполняем фон
        for (int i = 0; i < inventory.getSize(); i++) {
            inventory.setItem(i, new ItemStack(Material.GRAY_STAINED_GLASS_PANE));
        }
        
        // Заголовок
        ItemStack headerItem = new ItemStack(Material.PAPER);
        ItemMeta headerMeta = headerItem.getItemMeta();
        headerMeta.setDisplayName("§6§lПеременные");
        headerMeta.setLore(Arrays.asList(
            "§7Просмотр всех переменных",
            "§7в контексте выполнения"
        ));
        headerItem.setItemMeta(headerMeta);
        inventory.setItem(4, headerItem);
        
        // Кнопка очистки
        ItemStack clearItem = new ItemStack(Material.BARRIER);
        ItemMeta clearMeta = clearItem.getItemMeta();
        clearMeta.setDisplayName("§cОчистить локальные");
        clearMeta.setLore(Arrays.asList(
            "§7Очистить все локальные",
            "§7переменные"
        ));
        clearItem.setItemMeta(clearMeta);
        inventory.setItem(CLEAR_SLOT, clearItem);
        
        // Навигация
        setupNavigation();
        
        // Переменные
        displayVariables();
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
        ItemStack pageInfo = new ItemStack(Material.BOOK);
        ItemMeta pageMeta = pageInfo.getItemMeta();
        pageMeta.setDisplayName("§bСтраница " + (currentPage + 1) + " из " + totalPages);
        pageMeta.setLore(Arrays.asList(
            "§7Всего переменных: " + getTotalVariableCount()
        ));
        pageInfo.setItemMeta(pageMeta);
        inventory.setItem(13, pageInfo);
    }
    
    private void displayVariables() {
        List<VariableInfo> allVariables = getAllVariables();
        int startIndex = currentPage * ITEMS_PER_PAGE;
        int endIndex = Math.min(startIndex + ITEMS_PER_PAGE, allVariables.size());
        
        int slot = 9; // Начинаем с 9-го слота (второй ряд)
        for (int i = startIndex; i < endIndex; i++) {
            VariableInfo varInfo = allVariables.get(i);
            inventory.setItem(slot, createVariableItem(varInfo));
            slot++;
            
            // Переходим на следующий ряд каждые 7 слотов
            if ((slot - 9) % 7 == 0) {
                slot += 2; // Пропускаем 2 слота для навигации
            }
        }
    }
    
    private ItemStack createVariableItem(VariableInfo varInfo) {
        Material material = getMaterialForType(varInfo.type);
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        
        meta.setDisplayName("§a" + varInfo.name);
        
        List<String> lore = new ArrayList<>();
        lore.add("§7Значение: §f" + formatValue(varInfo.value));
        lore.add("§7Тип: §e" + varInfo.type);
        lore.add("§7Область: " + getScopeColor(varInfo.scope) + varInfo.scope);
        
        if (varInfo.scope.equals("Локальная")) {
            lore.add("");
            lore.add("§7Кликните для удаления");
        }
        
        meta.setLore(lore);
        item.setItemMeta(meta);
        
        return item;
    }
    
    private Material getMaterialForType(String type) {
        switch (type) {
            case "String": return Material.PAPER;
            case "Integer": return Material.IRON_INGOT;
            case "Double": return Material.GOLD_INGOT;
            case "Boolean": return Material.LEVER;
            case "Location": return Material.COMPASS;
            case "Player": return Material.PLAYER_HEAD;
            case "Entity": return Material.ZOMBIE_HEAD;
            case "ItemStack": return Material.DIAMOND;
            default: return Material.BOOK;
        }
    }
    
    private String getScopeColor(String scope) {
        switch (scope) {
            case "Локальная": return "§a";
            case "Глобальная": return "§b";
            case "Системная": return "§d";
            default: return "§7";
        }
    }
    
    private String formatValue(Object value) {
        if (value == null) return "null";
        if (value instanceof String) {
            String str = (String) value;
            return str.length() > 20 ? str.substring(0, 17) + "..." : str;
        }
        return value.toString();
    }
    
    private List<VariableInfo> getAllVariables() {
        List<VariableInfo> variables = new ArrayList<>();
        
        // Локальные переменные
        context.getLocalVariables().forEach((name, value) -> 
            variables.add(new VariableInfo(name, value, getTypeName(value), "Локальная")));
        
        // Глобальные переменные
        context.getGlobalVariables().forEach((name, value) -> 
            variables.add(new VariableInfo(name, value, getTypeName(value), "Глобальная")));
        
        // Системные переменные
        context.getSystemVariables().forEach((name, value) -> 
            variables.add(new VariableInfo(name, value, getTypeName(value), "Системная")));
        
        // Сортируем по имени
        variables.sort(Comparator.comparing(v -> v.name));
        
        return variables;
    }
    
    private String getTypeName(Object value) {
        if (value == null) return "null";
        if (value instanceof String) return "String";
        if (value instanceof Integer) return "Integer";
        if (value instanceof Double) return "Double";
        if (value instanceof Boolean) return "Boolean";
        if (value instanceof org.bukkit.Location) return "Location";
        if (value instanceof org.bukkit.entity.Player) return "Player";
        if (value instanceof org.bukkit.entity.Entity) return "Entity";
        if (value instanceof org.bukkit.inventory.ItemStack) return "ItemStack";
        return value.getClass().getSimpleName();
    }
    
    private int getTotalPages() {
        int totalVars = getTotalVariableCount();
        return (int) Math.ceil((double) totalVars / ITEMS_PER_PAGE);
    }
    
    private int getTotalVariableCount() {
        return context.getLocalVariables().size() + 
               context.getGlobalVariables().size() + 
               context.getSystemVariables().size();
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
                clearLocalVariables();
                break;
            default:
                // Проверяем, не кликнули ли по переменной
                if (slot >= 9 && slot < 36 && (slot - 9) % 9 < 7) {
                    handleVariableClick(slot);
                }
                break;
        }
    }
    
    private void handleVariableClick(int slot) {
        List<VariableInfo> allVariables = getAllVariables();
        int startIndex = currentPage * ITEMS_PER_PAGE;
        int slotIndex = (slot - 9) + startIndex;
        
        if (slotIndex < allVariables.size()) {
            VariableInfo varInfo = allVariables.get(slotIndex);
            if (varInfo.scope.equals("Локальная")) {
                context.removeLocalVariable(varInfo.name);
                player.sendMessage("§aПеременная '" + varInfo.name + "' удалена");
                setupInventory();
                player.openInventory(inventory);
            } else {
                player.sendMessage("§eПеременная '" + varInfo.name + "' (" + varInfo.scope + ") не может быть удалена");
            }
        }
    }
    
    private void clearLocalVariables() {
        int count = context.getLocalVariables().size();
        context.getLocalVariables().clear();
        player.sendMessage("§aОчищено " + count + " локальных переменных");
        setupInventory();
        player.openInventory(inventory);
    }
    
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (isValidInventory(event.getInventory())) {
            close();
        }
    }
    
    private static class VariableInfo {
        final String name;
        final Object value;
        final String type;
        final String scope;
        
        VariableInfo(String name, Object value, String type, String scope) {
            this.name = name;
            this.value = value;
            this.type = type;
            this.scope = scope;
        }
    }
    
    /**
     * Получить инвентарь
     */
    public Inventory getInventory() {
        return inventory;
    }
}
