package ru.openhousing.coding.gui.blocks;

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
import ru.openhousing.coding.constants.BlockParams;
import ru.openhousing.coding.values.ValueType;
import ru.openhousing.utils.ItemBuilder;
import ru.openhousing.utils.MessageUtil;

import java.util.Arrays;
import java.util.function.Consumer;

/**
 * GUI для настройки действий с инвентарем
 */
public class InventoryActionConfigGUI implements Listener {
    
    private final OpenHousing plugin;
    private final Player player;
    private final CodeBlock block;
    private final Consumer<CodeBlock> onSave;
    private final Inventory inventory;
    
    public InventoryActionConfigGUI(OpenHousing plugin, Player player, CodeBlock block) {
        this(plugin, player, block, null);
    }
    
    public InventoryActionConfigGUI(OpenHousing plugin, Player player, CodeBlock block, Consumer<CodeBlock> onSave) {
        this.plugin = plugin;
        this.player = player;
        this.block = block;
        this.onSave = onSave;
        this.inventory = Bukkit.createInventory(null, 54, "§6Настройка действий инвентаря");
        
        setupGUI();
        registerListener();
    }
    
    /**
     * Настройка GUI
     */
    private void setupGUI() {
        // Заголовок
        inventory.setItem(4, new ItemBuilder(Material.CHEST)
            .name("§6§lДействия с инвентарем")
            .lore(Arrays.asList(
                "§7Настройте параметры действия",
                "§7с инвентарем игрока",
                "",
                "§eТип блока: §f" + block.getType().getDisplayName()
            ))
            .build());
        
        // Тип действия инвентаря
        Object actionTypeObj = block.getParameter(BlockParams.ACTION_TYPE);
        String actionType = actionTypeObj != null ? actionTypeObj.toString() : "GIVE_ITEM";
        inventory.setItem(19, new ItemBuilder(Material.ANVIL)
            .name("§eТип действия")
            .lore(Arrays.asList(
                "§7Текущее значение: §f" + getActionTypeDisplayName(actionType),
                "",
                "§7Доступные типы:",
                "§a• GIVE_ITEM - Дать предмет",
                "§a• REMOVE_ITEM - Забрать предмет", 
                "§a• CLEAR_INVENTORY - Очистить инвентарь",
                "§a• SET_SLOT - Установить в слот",
                "§a• SWAP_SLOTS - Поменять слоты",
                "",
                "§eКликните для изменения"
            ))
            .build());
        
        // Предмет
        Object itemTypeObj = block.getParameter(BlockParams.MATERIAL);
        String itemType = itemTypeObj != null ? itemTypeObj.toString() : "DIAMOND";
        inventory.setItem(21, new ItemBuilder(Material.valueOf(itemType))
            .name("§eПредмет")
            .lore(Arrays.asList(
                "§7Текущий предмет: §f" + itemType,
                "",
                "§7Кликните для выбора",
                "§7другого предмета"
            ))
            .build());
        
        // Количество
        Object amountObj = block.getParameter(BlockParams.AMOUNT);
        String amount = amountObj != null ? amountObj.toString() : "1";
        inventory.setItem(23, new ItemBuilder(Material.REDSTONE)
            .name("§eКоличество")
            .lore(Arrays.asList(
                "§7Текущее количество: §f" + amount,
                "",
                "§7Кликните для изменения",
                "§7(1-64 для предметов, 1-2304 для блоков)"
            ))
            .build());
        
        // Слот (для SET_SLOT и SWAP_SLOTS)
        if ("SET_SLOT".equals(actionType) || "SWAP_SLOTS".equals(actionType)) {
            Object slotObj = block.getParameter(BlockParams.INVENTORY_SLOT);
            String slot = slotObj != null ? slotObj.toString() : "0";
            inventory.setItem(25, new ItemBuilder(Material.HOPPER)
                .name("§eСлот")
                .lore(Arrays.asList(
                    "§7Текущий слот: §f" + slot,
                    "",
                    "§7Кликните для изменения",
                    "§7(0-35 для инвентаря, 36-44 для брони)"
                ))
                .build());
        }
        
        // Целевой игрок
        Object targetPlayerObj = block.getParameter(BlockParams.PLAYER_NAME);
        String targetPlayer = targetPlayerObj != null ? targetPlayerObj.toString() : "%player%";
        inventory.setItem(37, new ItemBuilder(Material.PLAYER_HEAD)
            .name("§eЦелевой игрок")
            .lore(Arrays.asList(
                "§7Текущий игрок: §f" + targetPlayer,
                "",
                "§7Кликните для изменения",
                "§7Используйте %player% для себя"
            ))
            .build());
        
        // Кнопки управления
        inventory.setItem(45, new ItemBuilder(Material.LIME_CONCRETE)
            .name("§a§lСохранить")
            .lore(Arrays.asList(
                "§7Сохранить настройки",
                "§7и вернуться к редактору"
            ))
            .build());
        
        inventory.setItem(47, new ItemBuilder(Material.RED_CONCRETE)
            .name("§c§lОтмена")
            .lore(Arrays.asList(
                "§7Отменить изменения",
                "§7и вернуться к редактору"
            ))
            .build());
        
        inventory.setItem(49, new ItemBuilder(Material.BARRIER)
            .name("§4§lУдалить блок")
            .lore(Arrays.asList(
                "§7Удалить этот блок",
                "§7из строки"
            ))
            .build());
    }
    
    /**
     * Получение отображаемого имени типа действия
     */
    private String getActionTypeDisplayName(String actionType) {
        switch (actionType) {
            case "GIVE_ITEM": return "Дать предмет";
            case "REMOVE_ITEM": return "Забрать предмет";
            case "CLEAR_INVENTORY": return "Очистить инвентарь";
            case "SET_SLOT": return "Установить в слот";
            case "SWAP_SLOTS": return "Поменять слоты";
            default: return actionType;
        }
    }
    
    /**
     * Регистрация слушателя
     */
    private void registerListener() {
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }
    
    /**
     * Открытие GUI
     */
    public void open() {
        player.openInventory(inventory);
    }
    
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getView().getTitle().equals("§6Настройка действий инвентаря") || 
            !event.getWhoClicked().equals(player)) {
            return;
        }
        
        event.setCancelled(true);
        
        int slot = event.getRawSlot();
        
        switch (slot) {
            case 19: // Тип действия
                openActionTypeSelector();
                break;
                
            case 21: // Предмет
                openItemSelector();
                break;
                
            case 23: // Количество
                openAmountInput();
                break;
                
            case 25: // Слот (если видимый)
                if (inventory.getItem(25) != null) {
                    openSlotInput();
                }
                break;
                
            case 37: // Целевой игрок
                openPlayerSelector();
                break;
                
            case 45: // Сохранить
                saveAndClose();
                break;
                
            case 47: // Отмена
                cancelAndClose();
                break;
                
            case 49: // Удалить блок
                deleteBlock();
                break;
        }
    }
    
    /**
     * Открытие селектора типа действия
     */
    private void openActionTypeSelector() {
        Inventory selector = Bukkit.createInventory(null, 27, "§6Выбор типа действия");
        
        selector.setItem(10, new ItemBuilder(Material.CHEST)
            .name("§aДать предмет")
            .lore(Arrays.asList("§7Добавляет предмет в инвентарь"))
            .build());
            
        selector.setItem(11, new ItemBuilder(Material.HOPPER)
            .name("§cЗабрать предмет")
            .lore(Arrays.asList("§7Удаляет предмет из инвентаря"))
            .build());
            
        selector.setItem(12, new ItemBuilder(Material.BARRIER)
            .name("§4Очистить инвентарь")
            .lore(Arrays.asList("§7Полностью очищает инвентарь"))
            .build());
            
        selector.setItem(13, new ItemBuilder(Material.ANVIL)
            .name("§eУстановить в слот")
            .lore(Arrays.asList("§7Устанавливает предмет в конкретный слот"))
            .build());
            
        selector.setItem(14, new ItemBuilder(Material.PISTON)
            .name("§6Поменять слоты")
            .lore(Arrays.asList("§7Меняет местами два слота"))
            .build());
        
        // Временный слушатель для селектора
        new InventorySelector(plugin, player, selector, (selectedSlot) -> {
            String newActionType = null;
            switch (selectedSlot) {
                case 10: newActionType = "GIVE_ITEM"; break;
                case 11: newActionType = "REMOVE_ITEM"; break;
                case 12: newActionType = "CLEAR_INVENTORY"; break;
                case 13: newActionType = "SET_SLOT"; break;
                case 14: newActionType = "SWAP_SLOTS"; break;
            }
            
            if (newActionType != null) {
                block.setParameter(BlockParams.ACTION_TYPE, newActionType);
                setupGUI(); // Обновляем GUI
                open();
            }
        });
    }
    
    /**
     * Открытие селектора предметов
     */
    private void openItemSelector() {
        // TODO: Создать полноценный селектор предметов
        MessageUtil.send(player, "§7Селектор предметов будет добавлен в следующем обновлении");
    }
    
    /**
     * Открытие ввода количества
     */
    private void openAmountInput() {
        MessageUtil.send(player, 
            "§eВведите количество предметов:",
            "§7(1-64 для предметов, 1-2304 для блоков)"
        );
        
        // Регистрируем временный слушатель чата
        ru.openhousing.listeners.ChatListener.registerTemporaryInput(player, (input) -> {
            try {
                int amount = Integer.parseInt(input.trim());
                if (amount >= 1 && amount <= 2304) {
                    block.setParameter(BlockParams.AMOUNT, String.valueOf(amount));
                    setupGUI();
                    open();
                    MessageUtil.send(player, "§aКоличество установлено: §f" + amount);
                } else {
                    MessageUtil.send(player, "§cКоличество должно быть от 1 до 2304");
                    open();
                }
            } catch (NumberFormatException e) {
                MessageUtil.send(player, "§cВведите корректное число");
                open();
            }
        });
    }
    
    /**
     * Открытие ввода слота
     */
    private void openSlotInput() {
        MessageUtil.send(player, 
            "§eВведите номер слота:",
            "§7(0-35 для инвентаря, 36-44 для брони)"
        );
        
        // Регистрируем временный слушатель чата
        ru.openhousing.listeners.ChatListener.registerTemporaryInput(player, (input) -> {
            try {
                int slot = Integer.parseInt(input.trim());
                if (slot >= 0 && slot <= 44) {
                    block.setParameter(BlockParams.INVENTORY_SLOT, String.valueOf(slot));
                    setupGUI();
                    open();
                    MessageUtil.send(player, "§aСлот установлен: §f" + slot);
                } else {
                    MessageUtil.send(player, "§cСлот должен быть от 0 до 44");
                    open();
                }
            } catch (NumberFormatException e) {
                MessageUtil.send(player, "§cВведите корректное число");
                open();
            }
        });
    }
    
    /**
     * Открытие селектора игроков
     */
    private void openPlayerSelector() {
        // TODO: Создать полноценный селектор игроков
        MessageUtil.send(player, "§7Селектор игроков будет добавлен в следующем обновлении");
    }
    
    /**
     * Сохранение и закрытие
     */
    private void saveAndClose() {
        if (onSave != null) {
            onSave.accept(block);
        }
        MessageUtil.send(player, "§aНастройки сохранены!");
        close();
    }
    
    /**
     * Отмена и закрытие
     */
    private void cancelAndClose() {
        MessageUtil.send(player, "§7Изменения отменены");
        close();
    }
    
    /**
     * Удаление блока
     */
    private void deleteBlock() {
        // TODO: Удаление блока из строки
        MessageUtil.send(player, "§cБлок удален из строки");
        close();
    }
    
    /**
     * Закрытие GUI
     */
    private void close() {
        player.closeInventory();
        InventoryClickEvent.getHandlerList().unregister(this);
    }
    
    /**
     * Вспомогательный класс для селекторов
     */
    private static class InventorySelector implements Listener {
        private final OpenHousing plugin;
        private final Player player;
        private final Inventory inventory;
        private final Consumer<Integer> callback;
        
        public InventorySelector(OpenHousing plugin, Player player, Inventory inventory, Consumer<Integer> callback) {
            this.plugin = plugin;
            this.player = player;
            this.inventory = inventory;
            this.callback = callback;
            
            Bukkit.getPluginManager().registerEvents(this, plugin);
            player.openInventory(inventory);
        }
        
        @EventHandler
        public void onInventoryClick(InventoryClickEvent event) {
            if (!event.getView().getTitle().equals("§6Выбор типа действия") || 
                !event.getWhoClicked().equals(player)) {
                return;
            }
            
            event.setCancelled(true);
            
            int slot = event.getRawSlot();
            if (slot >= 10 && slot <= 14) {
                callback.accept(slot);
                player.closeInventory();
                InventoryClickEvent.getHandlerList().unregister(this);
            }
        }
    }
}
