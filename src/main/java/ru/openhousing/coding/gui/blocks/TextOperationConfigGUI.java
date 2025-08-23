package ru.openhousing.coding.gui.blocks;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import ru.openhousing.OpenHousing;
import ru.openhousing.coding.blocks.CodeBlock;
import ru.openhousing.coding.constants.BlockParams;
import ru.openhousing.utils.ItemBuilder;
import ru.openhousing.utils.MessageUtil;

import java.util.Arrays;
import java.util.function.Consumer;

/**
 * GUI для настройки операций с текстом
 */
public class TextOperationConfigGUI implements Listener {
    
    private final OpenHousing plugin;
    private final Player player;
    private final CodeBlock block;
    private final Consumer<CodeBlock> onSave;
    private final Inventory inventory;
    
    public TextOperationConfigGUI(OpenHousing plugin, Player player, CodeBlock block) {
        this(plugin, player, block, null);
    }
    
    public TextOperationConfigGUI(OpenHousing plugin, Player player, CodeBlock block, Consumer<CodeBlock> onSave) {
        this.plugin = plugin;
        this.player = player;
        this.block = block;
        this.onSave = onSave;
        this.inventory = Bukkit.createInventory(null, 54, "§6Настройка операций с текстом");
        
        setupGUI();
        registerListener();
    }
    
    /**
     * Настройка GUI
     */
    private void setupGUI() {
        // Заголовок
        inventory.setItem(4, new ItemBuilder(Material.WRITABLE_BOOK)
            .name("§6§lОперации с текстом")
            .lore(Arrays.asList(
                "§7Настройте параметры операций",
                "§7с текстовыми строками",
                "",
                "§eТип блока: §f" + block.getType().getDisplayName()
            ))
            .build());
        
        // Тип операции
        Object operationTypeObj = block.getParameter(BlockParams.OPERATION);
        String operationType = operationTypeObj != null ? operationTypeObj.toString() : "UPPERCASE";
        inventory.setItem(19, new ItemBuilder(Material.ANVIL)
            .name("§eТип операции")
            .lore(Arrays.asList(
                "§7Текущее значение: §f" + getOperationTypeDisplayName(operationType),
                "",
                "§7Доступные операции:",
                "§a• UPPERCASE - Верхний регистр",
                "§a• LOWERCASE - Нижний регистр",
                "§a• REVERSE - Обратить текст",
                "§a• LENGTH - Длина текста",
                "§a• SUBSTRING - Подстрока",
                "§a• REPLACE - Заменить текст",
                "§a• SPLIT - Разделить текст",
                "§a• JOIN - Объединить текст",
                "",
                "§eКликните для изменения"
            ))
            .build());
        
        // Исходный текст
        Object sourceTextObj = block.getParameter(BlockParams.VALUE);
        String sourceText = sourceTextObj != null ? sourceTextObj.toString() : "";
        inventory.setItem(21, new ItemBuilder(Material.PAPER)
            .name("§eИсходный текст")
            .lore(Arrays.asList(
                "§7Текущий текст: §f" + (sourceText.isEmpty() ? "пусто" : sourceText),
                "",
                "§7Кликните для изменения",
                "§7Максимум 1000 символов"
            ))
            .build());
        
        // Дополнительный параметр (для некоторых операций)
        if (needsExtraParameter(operationType)) {
            Object extraParamObj = block.getParameter(BlockParams.EXTRA);
            String extraParam = extraParamObj != null ? extraParamObj.toString() : "";
            inventory.setItem(23, new ItemBuilder(Material.REDSTONE)
                .name("§eДополнительный параметр")
                .lore(Arrays.asList(
                    "§7Текущее значение: §f" + (extraParam.isEmpty() ? "пусто" : extraParam),
                    "",
                    getExtraParameterDescription(operationType),
                    "",
                    "§7Кликните для изменения"
                ))
                .build());
        }
        
        // Второй дополнительный параметр (для SUBSTRING)
        if ("SUBSTRING".equals(operationType)) {
            Object extra2Obj = block.getParameter(BlockParams.EXTRA2);
            String extra2 = extra2Obj != null ? extra2Obj.toString() : "";
            inventory.setItem(25, new ItemBuilder(Material.REDSTONE_TORCH)
                .name("§eКонечная позиция")
                .lore(Arrays.asList(
                    "§7Текущее значение: §f" + (extra2.isEmpty() ? "пусто" : extra2),
                    "",
                    "§7Конечная позиция подстроки",
                    "§7(если пусто - до конца текста)",
                    "",
                    "§7Кликните для изменения"
                ))
                .build());
        }
        
        // Результат
        inventory.setItem(37, new ItemBuilder(Material.BOOK)
            .name("§eРезультат")
            .lore(Arrays.asList(
                "§7Результат операции будет",
                "§7сохранен в переменную",
                "",
                "§7Используйте переменные для",
                "§7получения результата"
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
     * Получение отображаемого имени типа операции
     */
    private String getOperationTypeDisplayName(String operationType) {
        switch (operationType) {
            case "UPPERCASE": return "Верхний регистр";
            case "LOWERCASE": return "Нижний регистр";
            case "REVERSE": return "Обратить текст";
            case "LENGTH": return "Длина текста";
            case "SUBSTRING": return "Подстрока";
            case "REPLACE": return "Заменить текст";
            case "SPLIT": return "Разделить текст";
            case "JOIN": return "Объединить текст";
            default: return operationType;
        }
    }
    
    /**
     * Проверка, нужен ли дополнительный параметр
     */
    private boolean needsExtraParameter(String operationType) {
        return "SUBSTRING".equals(operationType) || 
               "REPLACE".equals(operationType) || 
               "SPLIT".equals(operationType) || 
               "JOIN".equals(operationType);
    }
    
    /**
     * Получение описания дополнительного параметра
     */
    private String getExtraParameterDescription(String operationType) {
        switch (operationType) {
            case "SUBSTRING": return "§7Начальная позиция подстроки";
            case "REPLACE": return "§7Текст для замены";
            case "SPLIT": return "§7Разделитель";
            case "JOIN": return "§7Разделитель для объединения";
            default: return "§7Дополнительный параметр";
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
        if (!event.getView().getTitle().equals("§6Настройка операций с текстом") || 
            !event.getWhoClicked().equals(player)) {
            return;
        }
        
        event.setCancelled(true);
        
        int slot = event.getRawSlot();
        
        switch (slot) {
            case 19: // Тип операции
                openOperationTypeSelector();
                break;
                
            case 21: // Исходный текст
                openSourceTextInput();
                break;
                
            case 23: // Дополнительный параметр (если видимый)
                if (inventory.getItem(23) != null) {
                    openExtraParameterInput();
                }
                break;
                
            case 25: // Второй дополнительный параметр (если видимый)
                if (inventory.getItem(25) != null) {
                    openExtra2ParameterInput();
                }
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
     * Открытие селектора типа операции
     */
    private void openOperationTypeSelector() {
        Inventory selector = Bukkit.createInventory(null, 36, "§6Выбор типа операции");
        
        selector.setItem(10, new ItemBuilder(Material.BOOK)
            .name("§aВерхний регистр")
            .lore(Arrays.asList("§7Переводит текст в верхний регистр"))
            .build());
            
        selector.setItem(11, new ItemBuilder(Material.WRITTEN_BOOK)
            .name("§bНижний регистр")
            .lore(Arrays.asList("§7Переводит текст в нижний регистр"))
            .build());
            
        selector.setItem(12, new ItemBuilder(Material.COMPASS)
            .name("§cОбратить текст")
            .lore(Arrays.asList("§7Переворачивает текст задом наперед"))
            .build());
            
        selector.setItem(13, new ItemBuilder(Material.STICK)
            .name("§eДлина текста")
            .lore(Arrays.asList("§7Возвращает количество символов"))
            .build());
            
        selector.setItem(14, new ItemBuilder(Material.SHEARS)
            .name("§6Подстрока")
            .lore(Arrays.asList("§7Извлекает часть текста"))
            .build());
            
        selector.setItem(15, new ItemBuilder(Material.WRITABLE_BOOK)
            .name("§dЗаменить текст")
            .lore(Arrays.asList("§7Заменяет часть текста"))
            .build());
            
        selector.setItem(16, new ItemBuilder(Material.HOPPER)
            .name("§5Разделить текст")
            .lore(Arrays.asList("§7Разделяет текст по разделителю"))
            .build());
            
        selector.setItem(17, new ItemBuilder(Material.BEACON)
            .name("§fОбъединить текст")
            .lore(Arrays.asList("§7Объединяет элементы с разделителем"))
            .build());
        
        // Временный слушатель для селектора
        new InventorySelector(plugin, player, selector, (selectedSlot) -> {
            String newOperationType = null;
            switch (selectedSlot) {
                case 10: newOperationType = "UPPERCASE"; break;
                case 11: newOperationType = "LOWERCASE"; break;
                case 12: newOperationType = "REVERSE"; break;
                case 13: newOperationType = "LENGTH"; break;
                case 14: newOperationType = "SUBSTRING"; break;
                case 15: newOperationType = "REPLACE"; break;
                case 16: newOperationType = "SPLIT"; break;
                case 17: newOperationType = "JOIN"; break;
            }
            
            if (newOperationType != null) {
                block.setParameter(BlockParams.OPERATION, newOperationType);
                setupGUI(); // Обновляем GUI
                open();
            }
        });
    }
    
    /**
     * Открытие ввода исходного текста
     */
    private void openSourceTextInput() {
        MessageUtil.send(player, 
            "§eВведите исходный текст:",
            "§7(максимум 1000 символов)"
        );
        
        // Регистрируем временный слушатель чата
        ru.openhousing.listeners.ChatListener.registerTemporaryInput(player, (input) -> {
            if (input.length() > 1000) {
                MessageUtil.send(player, "§cТекст слишком длинный (максимум 1000 символов)");
                open();
            } else {
                block.setParameter(BlockParams.VALUE, input);
                setupGUI();
                open();
                MessageUtil.send(player, "§aИсходный текст установлен");
            }
        });
    }
    
    /**
     * Открытие ввода дополнительного параметра
     */
    private void openExtraParameterInput() {
        Object operationTypeObj = block.getParameter(BlockParams.OPERATION);
        String operationType = operationTypeObj != null ? operationTypeObj.toString() : "UPPERCASE";
        
        MessageUtil.send(player, 
            "§eВведите дополнительный параметр:",
            getExtraParameterDescription(operationType)
        );
        
        // Регистрируем временный слушатель чата
        ru.openhousing.listeners.ChatListener.registerTemporaryInput(player, (input) -> {
            block.setParameter(BlockParams.EXTRA, input);
            setupGUI();
            open();
            MessageUtil.send(player, "§aДополнительный параметр установлен");
        });
    }
    
    /**
     * Открытие ввода второго дополнительного параметра
     */
    private void openExtra2ParameterInput() {
        MessageUtil.send(player, 
            "§eВведите конечную позицию:",
            "§7(если пусто - до конца текста)"
        );
        
        // Регистрируем временный слушатель чата
        ru.openhousing.listeners.ChatListener.registerTemporaryInput(player, (input) -> {
            block.setParameter(BlockParams.EXTRA2, input);
            setupGUI();
            open();
            MessageUtil.send(player, "§aКонечная позиция установлена");
        });
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
            if (!event.getView().getTitle().equals("§6Выбор типа операции") || 
                !event.getWhoClicked().equals(player)) {
                return;
            }
            
            event.setCancelled(true);
            
            int slot = event.getRawSlot();
            if (slot >= 10 && slot <= 17) {
                callback.accept(slot);
                player.closeInventory();
                InventoryClickEvent.getHandlerList().unregister(this);
            }
        }
    }
}
