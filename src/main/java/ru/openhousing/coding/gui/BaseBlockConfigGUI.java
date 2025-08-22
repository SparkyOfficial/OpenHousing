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
import ru.openhousing.coding.values.Value;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Базовый класс для конфигурационных GUI блоков
 */
public abstract class BaseBlockConfigGUI implements IBlockConfigurationGUI {
    
    protected final OpenHousing plugin;
    protected final Player player;
    protected final CodeBlock block;
    protected final Runnable onSaveCallback;
    protected Inventory inventory;
    
    // Слоты для переменных (ключ - слот, значение - имя параметра)
    protected final Map<Integer, String> variableSlots = new HashMap<>();
    
    // Навигационные слоты
    protected static final int SAVE_SLOT = 53;
    protected static final int CANCEL_SLOT = 45;
    protected static final int BACK_SLOT = 49;
    
    public BaseBlockConfigGUI(OpenHousing plugin, Player player, CodeBlock block, Runnable onSaveCallback) {
        this.plugin = plugin;
        this.player = player;
        this.block = block;
        this.onSaveCallback = onSaveCallback;
    }
    
    @Override
    public void open() {
        setupInventory();
        player.openInventory(inventory);
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }
    
    @Override
    public CodeBlock getBlock() {
        return block;
    }
    
    @Override
    public void close() {
        HandlerList.unregisterAll(this);
        if (onSaveCallback != null) {
            onSaveCallback.run();
        }
    }
    
    @Override
    public boolean isValidInventory(Inventory inventory) {
        return this.inventory != null && this.inventory.equals(inventory);
    }
    
    /**
     * Создать базовые навигационные элементы
     */
    protected void setupNavigationItems() {
        // Кнопка сохранения
        ItemStack saveItem = new ItemStack(Material.EMERALD);
        ItemMeta saveMeta = saveItem.getItemMeta();
        saveMeta.setDisplayName(ChatColor.GREEN + "Сохранить");
        saveMeta.setLore(Arrays.asList(ChatColor.GRAY + "Сохранить изменения и закрыть"));
        saveItem.setItemMeta(saveMeta);
        inventory.setItem(SAVE_SLOT, saveItem);
        
        // Кнопка отмены
        ItemStack cancelItem = new ItemStack(Material.BARRIER);
        ItemMeta cancelMeta = cancelItem.getItemMeta();
        cancelMeta.setDisplayName(ChatColor.RED + "Отмена");
        cancelMeta.setLore(Arrays.asList(ChatColor.GRAY + "Закрыть без сохранения"));
        cancelItem.setItemMeta(cancelMeta);
        inventory.setItem(CANCEL_SLOT, cancelItem);
        
        // Кнопка назад
        ItemStack backItem = new ItemStack(Material.ARROW);
        ItemMeta backMeta = backItem.getItemMeta();
        backMeta.setDisplayName(ChatColor.YELLOW + "Назад");
        backMeta.setLore(Arrays.asList(ChatColor.GRAY + "Вернуться к основному меню"));
        backItem.setItemMeta(backMeta);
        inventory.setItem(BACK_SLOT, backItem);
    }
    
    /**
     * Создать слот для переменной
     */
    protected void setupVariableSlot(int slot, String parameterName, String displayName, String description) {
        variableSlots.put(slot, parameterName);
        
        Object currentValue = block.getParameter(parameterName);
        ItemStack item = new ItemStack(Material.PAPER);
        ItemMeta meta = item.getItemMeta();
        
        meta.setDisplayName(ChatColor.AQUA + displayName);
        
        if (currentValue != null) {
            meta.setLore(Arrays.asList(
                ChatColor.GRAY + description,
                ChatColor.GREEN + "Текущее значение: " + ChatColor.WHITE + currentValue.toString(),
                ChatColor.YELLOW + "ЛКМ - изменить значение",
                ChatColor.YELLOW + "ПКМ - выбрать переменную"
            ));
        } else {
            meta.setLore(Arrays.asList(
                ChatColor.GRAY + description,
                ChatColor.RED + "Значение не установлено",
                ChatColor.YELLOW + "ЛКМ - ввести значение",
                ChatColor.YELLOW + "ПКМ - выбрать переменную"
            ));
        }
        
        item.setItemMeta(meta);
        inventory.setItem(slot, item);
    }
    
    /**
     * Обработка клика по навигационным элементам
     */
    protected boolean handleNavigationClick(int slot) {
        switch (slot) {
            case SAVE_SLOT:
                close();
                return true;
            case CANCEL_SLOT:
                HandlerList.unregisterAll(this);
                player.closeInventory();
                return true;
            case BACK_SLOT:
                HandlerList.unregisterAll(this);
                new BlockConfigGUI(plugin, player, block, (updatedBlock) -> {
                    onSaveCallback.run();
                }).open();
                return true;
        }
        return false;
    }
    
    /**
     * Обработка клика по слоту переменной
     */
    protected boolean handleVariableSlotClick(int slot, boolean isRightClick) {
        String parameterName = variableSlots.get(slot);
        if (parameterName == null) return false;
        
        if (isRightClick) {
            // Открыть селектор переменных
            HandlerList.unregisterAll(this);
            new VariableSelectorGUI(plugin, player, (variable) -> {
                block.setParameter(parameterName, variable.getRawValue());
                open(); // Переоткрыть GUI с обновленными данными
            }, false).open();
        } else {
            // Открыть ввод текста
            openTextInput(parameterName, getParameterPrompt(parameterName));
        }
        return true;
    }
    
    /**
     * Получить подсказку для параметра
     */
    protected abstract String getParameterPrompt(String parameterName);
    
    /**
     * Открыть ввод текста для параметра
     */
    protected void openTextInput(String parameterName, String prompt) {
        HandlerList.unregisterAll(this);
        player.closeInventory();
        
        Object currentValue = block.getParameter(parameterName);
        String defaultText = currentValue != null ? currentValue.toString() : "";
        
        ru.openhousing.utils.AnvilGUIHelper.openTextInput(plugin, player, prompt, defaultText, (input) -> {
            if (input != null && !input.trim().isEmpty()) {
                block.setParameter(parameterName, input.trim());
            }
            Bukkit.getScheduler().runTask(plugin, this::open);
        });
    }
    
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!isValidInventory(event.getInventory()) || !event.getWhoClicked().equals(player)) {
            return;
        }
        
        event.setCancelled(true);
        
        int slot = event.getSlot();
        boolean isRightClick = event.isRightClick();
        
        // Обработка навигационных кликов
        if (handleNavigationClick(slot)) {
            return;
        }
        
        // Обработка кликов по слотам переменных
        if (handleVariableSlotClick(slot, isRightClick)) {
            return;
        }
        
        // Делегировать специфическую обработку подклассам
        handleSpecificClick(slot, isRightClick, event.isShiftClick());
    }
    
    /**
     * Обработка специфических кликов для конкретного типа блока
     */
    protected abstract void handleSpecificClick(int slot, boolean isRightClick, boolean isShiftClick);
    
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (isValidInventory(event.getInventory()) && event.getPlayer().equals(player)) {
            HandlerList.unregisterAll(this);
        }
    }
}
