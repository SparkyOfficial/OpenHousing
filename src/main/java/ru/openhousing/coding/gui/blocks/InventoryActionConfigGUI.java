package ru.openhousing.coding.gui.blocks;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import ru.openhousing.OpenHousing;
import ru.openhousing.coding.blocks.CodeBlock;
import ru.openhousing.coding.blocks.inventory.InventoryActionBlock;
import ru.openhousing.coding.constants.BlockParams;
import ru.openhousing.utils.ItemBuilder;
import ru.openhousing.utils.MessageUtil;

import java.util.Arrays;
import java.util.List;

/**
 * GUI для настройки блоков действий с инвентарем
 */
public class InventoryActionConfigGUI {
    
    private final OpenHousing plugin;
    private final Player player;
    private final CodeBlock block;
    private Inventory inventory;
    
    // Действия инвентаря
    private static final List<InventoryActionBlock.InventoryAction> ACTIONS = Arrays.asList(
        InventoryActionBlock.InventoryAction.values()
    );
    
    public InventoryActionConfigGUI(OpenHousing plugin, Player player, CodeBlock block) {
        this.plugin = plugin;
        this.player = player;
        this.block = block;
        setupGUI();
    }
    
    /**
     * Настройка GUI
     */
    private void setupGUI() {
        inventory = Bukkit.createInventory(null, 54, "§8Настройка блока инвентаря");
        
        // Заполняем фон
        ItemStack background = new ItemBuilder(Material.BLACK_STAINED_GLASS_PANE)
            .name(" ")
            .build();
        
        for (int i = 0; i < inventory.getSize(); i++) {
            inventory.setItem(i, background);
        }
        
        // Заголовок
        inventory.setItem(4, new ItemBuilder(Material.CHEST)
            .name("§6§lДействия с инвентарем")
            .lore(
                "§7Настройте параметры блока",
                "§7для работы с инвентарем игрока"
            )
            .build());
        
        // Выбор действия
        setupActionSelector();
        
        // Параметры предмета
        setupItemParameters();
        
        // Параметры количества и слота
        setupAmountAndSlotParameters();
        
        // Кнопки управления
        setupControlButtons();
        
        // Информация о текущих настройках
        updateInfoDisplay();
    }
    
    /**
     * Настройка выбора действия
     */
    private void setupActionSelector() {
        int startSlot = 19;
        for (int i = 0; i < ACTIONS.size(); i++) {
            InventoryActionBlock.InventoryAction action = ACTIONS.get(i);
            int slot = startSlot + i;
            
            Material icon = getActionIcon(action);
            String currentAction = (String) block.getParameter(BlockParams.ACTION_TYPE);
            boolean isSelected = action.name().equals(currentAction);
            
            ItemStack item = new ItemBuilder(icon)
                .name("§e" + action.getDisplayName())
                .lore(
                    "§7" + action.getDescription(),
                    "",
                    isSelected ? "§a✓ Выбрано" : "§7Клик для выбора"
                )
                .build();
            
            inventory.setItem(slot, item);
        }
    }
    
    /**
     * Получение иконки для действия
     */
    private Material getActionIcon(InventoryActionBlock.InventoryAction action) {
        return switch (action) {
            case GIVE_ITEM -> Material.DIAMOND;
            case TAKE_ITEM -> Material.REDSTONE;
            case CLEAR_INVENTORY -> Material.BARRIER;
            case CLEAR_HOTBAR -> Material.GRAY_DYE;
            case SET_ITEM -> Material.ITEM_FRAME;
            case CHECK_SPACE -> Material.MAP;
            case COUNT_ITEMS -> Material.BOOK;
        };
    }
    
    /**
     * Настройка параметров предмета
     */
    private void setupItemParameters() {
        // Выбор материала
        String currentItem = (String) block.getParameter(BlockParams.MATERIAL);
        if (currentItem == null) currentItem = "DIAMOND";
        
        inventory.setItem(28, new ItemBuilder(Material.NAME_TAG)
            .name("§aМатериал предмета")
            .lore(
                "§7Текущий: §e" + currentItem,
                "",
                "§7Клик для изменения",
                "§7(введите в чат)"
            )
            .build());
        
        // Популярные материалы
        Material[] popularMaterials = {
            Material.DIAMOND, Material.EMERALD, Material.GOLD_INGOT, Material.IRON_INGOT,
            Material.COAL, Material.REDSTONE, Material.LAPIS_LAZULI, Material.QUARTZ
        };
        
        int startSlot = 37;
        for (int i = 0; i < popularMaterials.length; i++) {
            Material material = popularMaterials[i];
            boolean isSelected = material.name().equals(currentItem);
            
            ItemStack item = new ItemBuilder(material)
                .name("§e" + material.name())
                .lore(
                    isSelected ? "§a✓ Выбрано" : "§7Клик для выбора"
                )
                .build();
            
            inventory.setItem(startSlot + i, item);
        }
    }
    
    /**
     * Настройка параметров количества и слота
     */
    private void setupAmountAndSlotParameters() {
        // Количество
        String currentAmount = (String) block.getParameter(BlockParams.AMOUNT);
        if (currentAmount == null) currentAmount = "1";
        
        inventory.setItem(30, new ItemBuilder(Material.PAPER)
            .name("§aКоличество")
            .lore(
                "§7Текущее: §e" + currentAmount,
                "",
                "§7Клик для изменения",
                "§7(введите в чат)"
            )
            .build());
        
        // Быстрый выбор количества
        int[] quickAmounts = {1, 5, 10, 16, 32, 64};
        int startSlot = 46;
        for (int i = 0; i < quickAmounts.length; i++) {
            int amount = quickAmounts[i];
            boolean isSelected = String.valueOf(amount).equals(currentAmount);
            
            ItemStack item = new ItemBuilder(Material.PAPER)
                .name("§e" + amount)
                .lore(
                    isSelected ? "§a✓ Выбрано" : "§7Клик для выбора"
                )
                .build();
            
            inventory.setItem(startSlot + i, item);
        }
        
        // Слот (только для SET_ITEM)
        String currentAction = (String) block.getParameter(BlockParams.ACTION_TYPE);
        if (currentAction != null && currentAction.equals("SET_ITEM")) {
            String currentSlot = (String) block.getParameter(BlockParams.INVENTORY_SLOT);
            if (currentSlot == null) currentSlot = "0";
            
            inventory.setItem(32, new ItemBuilder(Material.HOPPER)
                .name("§aСлот инвентаря")
                .lore(
                    "§7Текущий: §e" + currentSlot,
                    "",
                    "§7Клик для изменения",
                    "§7(введите в чат)"
                )
                .build());
        }
    }
    
    /**
     * Настройка кнопок управления
     */
    private void setupControlButtons() {
        // Кнопка сохранения
        inventory.setItem(49, new ItemBuilder(Material.LIME_CONCRETE)
            .name("§a§lСохранить")
            .lore(
                "§7Сохранить настройки",
                "§7и закрыть GUI"
            )
            .build());
        
        // Кнопка отмены
        inventory.setItem(51, new ItemBuilder(Material.RED_CONCRETE)
            .name("§c§lОтмена")
            .lore(
                "§7Отменить изменения",
                "§7и закрыть GUI"
            )
            .build());
        
        // Кнопка сброса
        inventory.setItem(47, new ItemBuilder(Material.ORANGE_CONCRETE)
            .name("§6§lСброс")
            .lore(
                "§7Сбросить к значениям",
                "§7по умолчанию"
            )
            .build());
    }
    
    /**
     * Обновление информационного дисплея
     */
    private void updateInfoDisplay() {
        String action = (String) block.getParameter(BlockParams.ACTION_TYPE);
        String item = (String) block.getParameter(BlockParams.MATERIAL);
        String amount = (String) block.getParameter(BlockParams.AMOUNT);
        String slot = (String) block.getParameter(BlockParams.INVENTORY_SLOT);
        
        if (action == null) action = "GIVE_ITEM";
        if (item == null) item = "DIAMOND";
        if (amount == null) amount = "1";
        if (slot == null) slot = "0";
        
        inventory.setItem(22, new ItemBuilder(Material.BOOK)
            .name("§6§lТекущие настройки")
            .lore(
                "§7Действие: §e" + action,
                "§7Предмет: §e" + item,
                "§7Количество: §e" + amount,
                action.equals("SET_ITEM") ? "§7Слот: §e" + slot : "",
                "",
                "§7Предварительный просмотр:",
                getActionPreview(action, item, amount, slot)
            )
            .build());
    }
    
    /**
     * Получение предварительного просмотра действия
     */
    private String getActionPreview(String action, String item, String amount, String slot) {
        return switch (action) {
            case "GIVE_ITEM" -> "§aДать " + amount + "x " + item;
            case "TAKE_ITEM" -> "§cЗабрать " + amount + "x " + item;
            case "CLEAR_INVENTORY" -> "§cОчистить весь инвентарь";
            case "CLEAR_HOTBAR" -> "§cОчистить панель быстрого доступа";
            case "SET_ITEM" -> "§eУстановить " + amount + "x " + item + " в слот " + slot;
            case "CHECK_SPACE" -> "§bПроверить свободное место";
            case "COUNT_ITEMS" -> "§bПодсчитать " + item;
            default -> "§7Неизвестное действие";
        };
    }
    
    /**
     * Обработка клика по GUI
     */
    public void handleClick(int slot, boolean isRightClick) {
        if (slot < 0 || slot >= inventory.getSize()) return;
        
        ItemStack clicked = inventory.getItem(slot);
        if (clicked == null || clicked.getType() == Material.BLACK_STAINED_GLASS_PANE) return;
        
        // Выбор действия
        if (slot >= 19 && slot <= 25) {
            int actionIndex = slot - 19;
            if (actionIndex < ACTIONS.size()) {
                InventoryActionBlock.InventoryAction action = ACTIONS.get(actionIndex);
                block.setParameter(BlockParams.ACTION_TYPE, action.name());
                setupGUI(); // Обновляем GUI
                MessageUtil.send(player, "§aВыбрано действие: " + action.getDisplayName());
            }
            return;
        }
        
        // Выбор материала
        if (slot >= 37 && slot <= 44) {
            Material material = clicked.getType();
            block.setParameter(BlockParams.MATERIAL, material.name());
            setupGUI();
            MessageUtil.send(player, "§aВыбран материал: " + material.name());
            return;
        }
        
        // Быстрый выбор количества
        if (slot >= 46 && slot <= 51) {
            String amount = clicked.getItemMeta().getDisplayName().replace("§e", "");
            block.setParameter(BlockParams.AMOUNT, amount);
            setupGUI();
            MessageUtil.send(player, "§aУстановлено количество: " + amount);
            return;
        }
        
        // Кнопки управления
        switch (slot) {
            case 28: // Материал
                requestMaterialInput();
                break;
            case 30: // Количество
                requestAmountInput();
                break;
            case 32: // Слот
                if (isActionSetItem()) {
                    requestSlotInput();
                }
                break;
            case 47: // Сброс
                resetToDefaults();
                break;
            case 49: // Сохранить
                saveAndClose();
                break;
            case 51: // Отмена
                cancelAndClose();
                break;
        }
    }
    
    /**
     * Проверка, является ли действие SET_ITEM
     */
    private boolean isActionSetItem() {
        String action = (String) block.getParameter(BlockParams.ACTION_TYPE);
        return "SET_ITEM".equals(action);
    }
    
    /**
     * Запрос ввода материала
     */
    private void requestMaterialInput() {
        MessageUtil.send(player, "§aВведите название материала в чат:");
        MessageUtil.send(player, "§7Примеры: DIAMOND, EMERALD, GOLD_INGOT");
        
        plugin.getChatListener().registerTemporaryInput(player, input -> {
            try {
                Material.valueOf(input.toUpperCase());
                block.setParameter(BlockParams.MATERIAL, input.toUpperCase());
                setupGUI();
                MessageUtil.send(player, "§aМатериал установлен: " + input.toUpperCase());
            } catch (IllegalArgumentException e) {
                MessageUtil.send(player, "§cНеверный материал! Попробуйте еще раз.");
                open();
            }
        });
        
        close();
    }
    
    /**
     * Запрос ввода количества
     */
    private void requestAmountInput() {
        MessageUtil.send(player, "§aВведите количество в чат:");
        MessageUtil.send(player, "§7(число от 1 до 64)");
        
        plugin.getChatListener().registerTemporaryInput(player, input -> {
            try {
                int amount = Integer.parseInt(input);
                if (amount >= 1 && amount <= 64) {
                    block.setParameter(BlockParams.AMOUNT, String.valueOf(amount));
                    setupGUI();
                    MessageUtil.send(player, "§aКоличество установлено: " + amount);
                } else {
                    MessageUtil.send(player, "§cКоличество должно быть от 1 до 64!");
                    open();
                }
            } catch (NumberFormatException e) {
                MessageUtil.send(player, "§cНеверное число! Попробуйте еще раз.");
                open();
            }
        });
        
        close();
    }
    
    /**
     * Запрос ввода слота
     */
    private void requestSlotInput() {
        MessageUtil.send(player, "§aВведите номер слота в чат:");
        MessageUtil.send(player, "§7(0-35, где 0-8 - панель быстрого доступа)");
        
        plugin.getChatListener().registerTemporaryInput(player, input -> {
            try {
                int slot = Integer.parseInt(input);
                if (slot >= 0 && slot <= 35) {
                    block.setParameter(BlockParams.INVENTORY_SLOT, String.valueOf(slot));
                    setupGUI();
                    MessageUtil.send(player, "§aСлот установлен: " + slot);
                } else {
                    MessageUtil.send(player, "§cСлот должен быть от 0 до 35!");
                    open();
                }
            } catch (NumberFormatException e) {
                MessageUtil.send(player, "§cНеверное число! Попробуйте еще раз.");
                open();
            }
        });
        
        close();
    }
    
    /**
     * Сброс к значениям по умолчанию
     */
    private void resetToDefaults() {
        block.setParameter(BlockParams.ACTION_TYPE, "GIVE_ITEM");
        block.setParameter(BlockParams.MATERIAL, "DIAMOND");
        block.setParameter(BlockParams.AMOUNT, "1");
        block.setParameter(BlockParams.INVENTORY_SLOT, "0");
        
        setupGUI();
        MessageUtil.send(player, "§aНастройки сброшены к значениям по умолчанию");
    }
    
    /**
     * Сохранение и закрытие
     */
    private void saveAndClose() {
        plugin.getDatabaseManager().saveCodeScriptAsync(
            plugin.getCodeManager().getScript(player),
            () -> MessageUtil.send(player, "§aНастройки сохранены!")
        );
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
     * Открытие GUI
     */
    public void open() {
        player.openInventory(inventory);
        // Простой тест - отправляем сообщение
        MessageUtil.send(player, "§aОткрыт GUI для настройки блока инвентаря");
        MessageUtil.send(player, "§7Выберите действие и настройте параметры");
    }
    
    /**
     * Закрытие GUI
     */
    public void close() {
        player.closeInventory();
    }
}
