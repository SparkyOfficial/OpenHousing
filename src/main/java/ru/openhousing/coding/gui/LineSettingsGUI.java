package ru.openhousing.coding.gui;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import ru.openhousing.OpenHousing;
import ru.openhousing.coding.script.CodeLine;
import ru.openhousing.coding.script.CodeScript;
import ru.openhousing.coding.gui.helpers.AnvilGUIHelper;
import ru.openhousing.utils.ItemBuilder;
import ru.openhousing.utils.MessageUtil;

import java.util.Arrays;

/**
 * GUI для настройки строк кода
 */
public class LineSettingsGUI implements Listener {
    
    private final OpenHousing plugin;
    private final Player player;
    private final CodeScript script;
    private final CodeLine line;
    private final Inventory inventory;
    
    public LineSettingsGUI(OpenHousing plugin, Player player, CodeScript script, CodeLine line) {
        this.plugin = plugin;
        this.player = player;
        this.script = script;
        this.line = line;
        this.inventory = Bukkit.createInventory(null, 45, "§6Настройки строки: " + line.getName());
        
        setupGUI();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }
    
    /**
     * Открытие GUI
     */
    public void open() {
        player.openInventory(inventory);
    }
    
    /**
     * Настройка GUI
     */
    private void setupGUI() {
        inventory.clear();
        
        // Заголовок
        inventory.setItem(4, new ItemBuilder(Material.PAPER)
            .name("§6Строка #" + line.getLineNumber())
            .lore(Arrays.asList(
                "§7Имя: §e" + line.getName(),
                "§7Блоков: §e" + line.getBlockCount(),
                "§7Состояние: " + (line.isEnabled() ? "§aВключена" : "§cВыключена"),
                "",
                "§7Настройте параметры строки ниже"
            ))
            .build());
        
        // Настройка имени
        inventory.setItem(19, new ItemBuilder(Material.NAME_TAG)
            .name("§6Изменить имя")
            .lore(Arrays.asList(
                "§7Текущее имя: §e" + line.getName(),
                "",
                "§eЛевый клик - изменить имя",
                "§eПравый клик - сбросить к умолчанию"
            ))
            .build());
        
        // Настройка описания
        inventory.setItem(20, new ItemBuilder(Material.WRITABLE_BOOK)
            .name("§6Описание строки")
            .lore(Arrays.asList(
                "§7Описание: §e" + (line.getDescription().isEmpty() ? "Нет" : line.getDescription()),
                "",
                "§eКлик для изменения"
            ))
            .build());
        
        // Включение/выключение
        inventory.setItem(21, new ItemBuilder(line.isEnabled() ? Material.LIME_DYE : Material.GRAY_DYE)
            .name(line.isEnabled() ? "§aСтрока включена" : "§cСтрока выключена")
            .lore(Arrays.asList(
                "§7Включенные строки выполняются",
                "§7при запуске кода",
                "",
                "§eКлик для переключения"
            ))
            .build());
        
        // Действия со строкой
        inventory.setItem(23, new ItemBuilder(Material.SHEARS)
            .name("§6Очистить строку")
            .lore(Arrays.asList(
                "§7Удалить все блоки из строки",
                "",
                "§cБез возможности отмены!",
                "§eКлик для очистки"
            ))
            .build());
        
        inventory.setItem(24, new ItemBuilder(Material.PAPER)
            .name("§6Дублировать строку")
            .lore(Arrays.asList(
                "§7Создать копию этой строки",
                "§7со всеми блоками",
                "",
                "§eКлик для дублирования"
            ))
            .build());
        
        inventory.setItem(25, new ItemBuilder(Material.BARRIER)
            .name("§cУдалить строку")
            .lore(Arrays.asList(
                "§7Удалить эту строку навсегда",
                "",
                "§cБез возможности отмены!",
                "§eКлик для удаления"
            ))
            .build());
        
        // Метаданные строки
        inventory.setItem(31, new ItemBuilder(Material.KNOWLEDGE_BOOK)
            .name("§6Метаданные")
            .lore(Arrays.asList(
                "§7Дополнительные параметры строки",
                "§7для продвинутых пользователей",
                "",
                "§eКлик для просмотра"
            ))
            .build());
        
        // Управление порядком
        inventory.setItem(37, new ItemBuilder(Material.ARROW)
            .name("§6Переместить вверх")
            .lore(Arrays.asList(
                "§7Изменить порядок выполнения",
                "§7строк в скрипте",
                "",
                "§eКлик для перемещения"
            ))
            .build());
        
        inventory.setItem(43, new ItemBuilder(Material.ARROW)
            .name("§6Переместить вниз")
            .lore(Arrays.asList(
                "§7Изменить порядок выполнения",
                "§7строк в скрипте",
                "",
                "§eКлик для перемещения"
            ))
            .build());
        
        // Кнопки управления
        inventory.setItem(40, new ItemBuilder(Material.LIME_CONCRETE)
            .name("§aСохранить")
            .lore("§7Сохранить изменения")
            .build());
        
        inventory.setItem(44, new ItemBuilder(Material.RED_CONCRETE)
            .name("§cОтмена")
            .lore("§7Закрыть без сохранения")
            .build());
    }
    
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getView().getTitle().startsWith("§6Настройки строки:")) {
            return;
        }
        
        event.setCancelled(true);
        
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }
        
        Player clicker = (Player) event.getWhoClicked();
        if (!clicker.equals(player)) {
            return;
        }
        
        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null || clickedItem.getType() == Material.AIR) {
            return;
        }
        
        int slot = event.getSlot();
        boolean isRightClick = event.getClick().isRightClick();
        
        handleClick(slot, isRightClick);
    }
    
    /**
     * Обработка кликов
     */
    private void handleClick(int slot, boolean isRightClick) {
        switch (slot) {
            case 19: // Изменить имя
                if (isRightClick) {
                    // Сброс к умолчанию
                    line.setName("Строка " + line.getLineNumber());
                    MessageUtil.send(player, "&aИмя строки сброшено к умолчанию");
                    setupGUI();
                } else {
                    // Изменить имя
                    player.closeInventory();
                    new AnvilGUIHelper(plugin, player, "Введите новое имя строки", (newName) -> {
                        line.setName(newName);
                        MessageUtil.send(player, "&aИмя строки изменено на: &e" + newName);
                        this.open(); // Возвращаемся к настройкам
                    }).open();
                }
                break;
                
            case 20: // Описание
                player.closeInventory();
                new AnvilGUIHelper(plugin, player, "Введите описание строки", (newDescription) -> {
                    line.setDescription(newDescription);
                    MessageUtil.send(player, "&aОписание строки изменено");
                    this.open(); // Возвращаемся к настройкам
                }).open();
                break;
                
            case 21: // Включение/выключение
                line.setEnabled(!line.isEnabled());
                MessageUtil.send(player, "&aСтрока " + (line.isEnabled() ? "включена" : "выключена"));
                setupGUI();
                break;
                
            case 23: // Очистить строку
                line.clearBlocks();
                MessageUtil.send(player, "&aСтрока очищена");
                setupGUI();
                break;
                
            case 24: // Дублировать строку
                try {
                    CodeLine duplicate = line.clone();
                    duplicate.setName(line.getName() + " (копия)");
                    
                    // Добавляем дублированную строку после текущей
                    script.insertLine(line.getLineNumber() + 1, duplicate);
                    
                    MessageUtil.send(player, "&aСтрока дублирована и добавлена под номером " + (line.getLineNumber() + 1));
                    setupGUI(); // Обновляем GUI
                } catch (Exception e) {
                    MessageUtil.send(player, "&cОшибка дублирования строки: " + e.getMessage());
                }
                break;
                
            case 25: // Удалить строку
                if (script.removeLine(line.getLineNumber())) {
                    MessageUtil.send(player, "&cСтрока удалена");
                    player.closeInventory();
                } else {
                    MessageUtil.send(player, "&cОшибка удаления строки");
                }
                break;
                
            case 31: // Метаданные
                showMetadata();
                break;
                
            case 37: // Переместить вверх
                if (line.getLineNumber() > 1) {
                    if (script.moveLineUp(line.getLineNumber())) {
                        MessageUtil.send(player, "&aСтрока перемещена вверх");
                        setupGUI(); // Обновляем GUI
                    } else {
                        MessageUtil.send(player, "&cОшибка перемещения строки");
                    }
                } else {
                    MessageUtil.send(player, "&eСтрока уже находится в самом верху");
                }
                break;
                
            case 43: // Переместить вниз
                if (line.getLineNumber() < script.getLineCount()) {
                    if (script.moveLineDown(line.getLineNumber())) {
                        MessageUtil.send(player, "&aСтрока перемещена вниз");
                        setupGUI(); // Обновляем GUI
                    } else {
                        MessageUtil.send(player, "&cОшибка перемещения строки");
                    }
                } else {
                    MessageUtil.send(player, "&eСтрока уже находится в самом низу");
                }
                break;
                
            case 40: // Сохранить
                saveAndClose();
                break;
                
            case 44: // Отмена
                player.closeInventory();
                MessageUtil.send(player, "&7Изменения не сохранены");
                break;
        }
    }
    
    /**
     * Показать метаданные
     */
    private void showMetadata() {
        MessageUtil.send(player,
            "&6&l=== Метаданные строки ===",
            "&eНомер: &f" + line.getLineNumber(),
            "&eИмя: &f" + line.getName(),
            "&eОписание: &f" + (line.getDescription().isEmpty() ? "Нет" : line.getDescription()),
            "&eБлоков: &f" + line.getBlockCount(),
            "&eСостояние: " + (line.isEnabled() ? "&aВключена" : "&cВыключена"),
            "&eПустая: " + (line.isEmpty() ? "&aДа" : "&cНет"),
            "&eВалидная: " + (line.isValid() ? "&aДа" : "&cНет")
        );
    }
    
    /**
     * Сохранение и закрытие
     */
    private void saveAndClose() {
        // Сохраняем код
        plugin.getDatabaseManager().saveCodeScript(script);
        MessageUtil.send(player, "&aНастройки строки сохранены!");
        player.closeInventory();
    }
    
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getView().getTitle().startsWith("§6Настройки строки:") && 
            event.getPlayer().equals(player)) {
            // Отменяем регистрацию листенера
            InventoryClickEvent.getHandlerList().unregister(this);
            InventoryCloseEvent.getHandlerList().unregister(this);
        }
    }
}
