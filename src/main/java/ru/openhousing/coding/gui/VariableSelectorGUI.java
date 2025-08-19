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
import ru.openhousing.coding.values.VariableValue;
import ru.openhousing.utils.AnvilGUIHelper;
import ru.openhousing.utils.ItemBuilder;

import java.util.Arrays;
import java.util.function.Consumer;

/**
 * Профессиональный GUI для работы с переменными
 */
public class VariableSelectorGUI implements Listener {

    private final OpenHousing plugin;
    private final Player player;
    private final Consumer<VariableValue> callback;
    private final Inventory inventory;
    private final boolean isForSetting; // true для установки, false для получения

    public VariableSelectorGUI(OpenHousing plugin, Player player, Consumer<VariableValue> callback, boolean isForSetting) {
        this.plugin = plugin;
        this.player = player;
        this.callback = callback;
        this.isForSetting = isForSetting;
        this.inventory = Bukkit.createInventory(null, 45, isForSetting ? "§6Установить переменную" : "§6Выбор переменной");

        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        setupGUI();
    }

    private void setupGUI() {
        inventory.clear();

        // Заголовок
        inventory.setItem(4, new ItemBuilder(Material.CHEST)
            .name(isForSetting ? "§6Установить переменную" : "§6Выбор переменной")
            .lore(Arrays.asList(
                "§7Выберите тип и область видимости",
                "§7переменной для " + (isForSetting ? "установки" : "получения"),
                "",
                "§eГлобальные §7- доступны везде",
                "§eЛокальные §7- только в этом коде",
                "§eСистемные §7- встроенные значения"
            ))
            .build());

        // Глобальные переменные
        inventory.setItem(10, new ItemBuilder(Material.ENDER_CHEST)
            .name("§eГлобальная переменная")
            .lore(Arrays.asList(
                "§7Переменная, доступная во всех",
                "§7коде и сессиях игрока",
                "",
                "§7Префикс: §eglobal:",
                "§7Пример: §eglobal:player_score",
                "",
                "§eКлик для ввода имени"
            ))
            .build());

        inventory.setItem(11, new ItemBuilder(Material.SHULKER_BOX)
            .name("§eЛокальная переменная")
            .lore(Arrays.asList(
                "§7Переменная, доступная только",
                "§7в текущем коде",
                "",
                "§7Префикс: §elocal:",
                "§7Пример: §elocal:temp_value",
                "",
                "§eКлик для ввода имени"
            ))
            .build());

        inventory.setItem(12, new ItemBuilder(Material.COMMAND_BLOCK)
            .name("§eСистемная переменная")
            .lore(Arrays.asList(
                "§7Встроенные переменные системы",
                "§7(только для чтения)",
                "",
                "§7Префикс: §esystem:",
                "§7Примеры: §esystem:time, system:weather",
                "",
                "§eКлик для выбора"
            ))
            .build());

        // Быстрые переменные
        inventory.setItem(19, new ItemBuilder(Material.PLAYER_HEAD)
            .name("§aИгровые переменные")
            .lore(Arrays.asList(
                "§7Часто используемые переменные",
                "§7связанные с игроками",
                "",
                "§eКлик для просмотра списка"
            ))
            .build());

        inventory.setItem(20, new ItemBuilder(Material.CLOCK)
            .name("§aВременные переменные")
            .lore(Arrays.asList(
                "§7Переменные времени и даты",
                "",
                "§eКлик для просмотра списка"
            ))
            .build());

        inventory.setItem(21, new ItemBuilder(Material.COMPASS)
            .name("§aМировые переменные")
            .lore(Arrays.asList(
                "§7Переменные мира и локаций",
                "",
                "§eКлик для просмотра списка"
            ))
            .build());

        // Недавние переменные (если есть)
        inventory.setItem(28, new ItemBuilder(Material.BOOK)
            .name("§6Недавние переменные")
            .lore(Arrays.asList(
                "§7Переменные, которые вы",
                "§7использовали недавно",
                "",
                "§eКлик для просмотра"
            ))
            .build());

        // Математические переменные
        if (!isForSetting) {
            inventory.setItem(29, new ItemBuilder(Material.GOLD_NUGGET)
                .name("§6Математические константы")
                .lore(Arrays.asList(
                    "§7Встроенные математические",
                    "§7константы и функции",
                    "",
                    "§7π, e, случайные числа",
                    "",
                    "§eКлик для просмотра"
                ))
                .build());
        }

        // Кнопки управления
        inventory.setItem(40, new ItemBuilder(Material.ARROW)
            .name("§7Назад")
            .build());

        inventory.setItem(44, new ItemBuilder(Material.BARRIER)
            .name("§cОтмена")
            .build());
    }

    public void open() {
        player.openInventory(inventory);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        String title = isForSetting ? "§6Установить переменную" : "§6Выбор переменной";
        String eventTitle = event.getView().getTitle();
        
        // Проверяем основное меню или подменю
        if (!eventTitle.equals(title) && 
            !eventTitle.equals("§6Системные переменные") &&
            !eventTitle.equals("§6Игровые переменные") &&
            !eventTitle.equals("§6Временные переменные") &&
            !eventTitle.equals("§6Мировые переменные") &&
            !eventTitle.equals("§6Математические константы")) return;
        if (!(event.getWhoClicked() instanceof Player)) return;
        if (!event.getWhoClicked().getUniqueId().equals(player.getUniqueId())) return;

        event.setCancelled(true);

        int slot = event.getSlot();
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) return;

        // Обработка подменю
        if (eventTitle.equals("§6Системные переменные")) {
            handleSystemVariableClick(slot);
            return;
        } else if (eventTitle.equals("§6Игровые переменные")) {
            handlePlayerVariableClick(slot);
            return;
        } else if (eventTitle.equals("§6Временные переменные")) {
            handleTimeVariableClick(slot);
            return;
        } else if (eventTitle.equals("§6Мировые переменные")) {
            handleWorldVariableClick(slot);
            return;
        } else if (eventTitle.equals("§6Математические константы")) {
            handleMathConstantClick(slot);
            return;
        }

        // Обработка основного меню
        switch (slot) {
            case 10: // Глобальная переменная
                player.closeInventory();
                AnvilGUIHelper.openTextInput(plugin, player, "Имя глобальной переменной", "", (name) -> {
                    callback.accept(new VariableValue("global:" + name));
                });
                break;

            case 11: // Локальная переменная
                player.closeInventory();
                AnvilGUIHelper.openTextInput(plugin, player, "Имя локальной переменной", "", (name) -> {
                    callback.accept(new VariableValue("local:" + name));
                });
                break;

            case 12: // Системная переменная
                player.closeInventory();
                showSystemVariables();
                break;

            case 19: // Игровые переменные
                player.closeInventory();
                showPlayerVariables();
                break;

            case 20: // Временные переменные
                player.closeInventory();
                showTimeVariables();
                break;

            case 21: // Мировые переменные
                player.closeInventory();
                showWorldVariables();
                break;

            case 28: // Недавние переменные
                player.closeInventory();
                showRecentVariables();
                break;

            case 29: // Математические константы
                if (!isForSetting) {
                    player.closeInventory();
                    showMathConstants();
                }
                break;

            case 40: // Назад
                player.closeInventory();
                break;

            case 44: // Отмена
                player.closeInventory();
                break;
        }
    }

    private void showSystemVariables() {
        Inventory systemInventory = Bukkit.createInventory(null, 54, "§6Системные переменные");

        // Системные переменные
        systemInventory.setItem(10, new ItemBuilder(Material.CLOCK)
            .name("§esystem:time")
            .lore(Arrays.asList(
                "§7Текущее время в мире",
                "§7(тики с начала дня)",
                "",
                "§eКлик для выбора"
            ))
            .build());

        systemInventory.setItem(11, new ItemBuilder(Material.SUNFLOWER)
            .name("§esystem:weather")
            .lore(Arrays.asList(
                "§7Текущая погода в мире",
                "§7(clear, rain, storm)",
                "",
                "§eКлик для выбора"
            ))
            .build());

        systemInventory.setItem(12, new ItemBuilder(Material.COMPASS)
            .name("§esystem:world")
            .lore(Arrays.asList(
                "§7Название текущего мира",
                "",
                "§eКлик для выбора"
            ))
            .build());

        systemInventory.setItem(13, new ItemBuilder(Material.PLAYER_HEAD)
            .name("§esystem:player_count")
            .lore(Arrays.asList(
                "§7Количество игроков онлайн",
                "",
                "§eКлик для выбора"
            ))
            .build());

        // Кнопка назад
        systemInventory.setItem(49, new ItemBuilder(Material.ARROW)
            .name("§7Назад")
            .build());

        player.openInventory(systemInventory);
        
        // Регистрируем временный обработчик для этого инвентаря
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            // Обработка кликов в системных переменных будет через основной обработчик
        }, 1L);
    }

    private void showPlayerVariables() {
        Inventory playerInventory = Bukkit.createInventory(null, 54, "§6Игровые переменные");

        playerInventory.setItem(10, new ItemBuilder(Material.PLAYER_HEAD)
            .name("§eplayer:name")
            .lore(Arrays.asList(
                "§7Имя текущего игрока",
                "",
                "§eКлик для выбора"
            ))
            .build());

        playerInventory.setItem(11, new ItemBuilder(Material.EXPERIENCE_BOTTLE)
            .name("§eplayer:level")
            .lore(Arrays.asList(
                "§7Уровень игрока",
                "",
                "§eКлик для выбора"
            ))
            .build());

        playerInventory.setItem(12, new ItemBuilder(Material.GOLDEN_APPLE)
            .name("§eplayer:health")
            .lore(Arrays.asList(
                "§7Здоровье игрока",
                "",
                "§eКлик для выбора"
            ))
            .build());

        playerInventory.setItem(13, new ItemBuilder(Material.BREAD)
            .name("§eplayer:food")
            .lore(Arrays.asList(
                "§7Сытость игрока",
                "",
                "§eКлик для выбора"
            ))
            .build());

        // Кнопка назад
        playerInventory.setItem(49, new ItemBuilder(Material.ARROW)
            .name("§7Назад")
            .build());

        player.openInventory(playerInventory);
    }

    private void showTimeVariables() {
        Inventory timeInventory = Bukkit.createInventory(null, 54, "§6Временные переменные");

        timeInventory.setItem(10, new ItemBuilder(Material.CLOCK)
            .name("§etime:hour")
            .lore(Arrays.asList(
                "§7Текущий час (0-23)",
                "",
                "§eКлик для выбора"
            ))
            .build());

        timeInventory.setItem(11, new ItemBuilder(Material.CLOCK)
            .name("§etime:minute")
            .lore(Arrays.asList(
                "§7Текущая минута (0-59)",
                "",
                "§eКлик для выбора"
            ))
            .build());

        timeInventory.setItem(12, new ItemBuilder(Material.PAPER)
            .name("§etime:day")
            .lore(Arrays.asList(
                "§7Текущий день",
                "",
                "§eКлик для выбора"
            ))
            .build());

        // Кнопка назад
        timeInventory.setItem(49, new ItemBuilder(Material.ARROW)
            .name("§7Назад")
            .build());

        player.openInventory(timeInventory);
    }

    private void showWorldVariables() {
        Inventory worldInventory = Bukkit.createInventory(null, 54, "§6Мировые переменные");

        worldInventory.setItem(10, new ItemBuilder(Material.GRASS_BLOCK)
            .name("§eworld:spawn_x")
            .lore(Arrays.asList(
                "§7X координата спавна мира",
                "",
                "§eКлик для выбора"
            ))
            .build());

        worldInventory.setItem(11, new ItemBuilder(Material.GRASS_BLOCK)
            .name("§eworld:spawn_y")
            .lore(Arrays.asList(
                "§7Y координата спавна мира",
                "",
                "§eКлик для выбора"
            ))
            .build());

        worldInventory.setItem(12, new ItemBuilder(Material.GRASS_BLOCK)
            .name("§eworld:spawn_z")
            .lore(Arrays.asList(
                "§7Z координата спавна мира",
                "",
                "§eКлик для выбора"
            ))
            .build());

        // Кнопка назад
        worldInventory.setItem(49, new ItemBuilder(Material.ARROW)
            .name("§7Назад")
            .build());

        player.openInventory(worldInventory);
    }

    private void showRecentVariables() {
        // TODO: Реализовать сохранение недавних переменных
        player.sendMessage("§eФункция недавних переменных будет добавлена в следующих обновлениях");
    }

    private void showMathConstants() {
        Inventory mathInventory = Bukkit.createInventory(null, 54, "§6Математические константы");

        mathInventory.setItem(10, new ItemBuilder(Material.GOLD_NUGGET)
            .name("§emath:pi")
            .lore(Arrays.asList(
                "§7Число π (3.14159...)",
                "",
                "§eКлик для выбора"
            ))
            .build());

        mathInventory.setItem(11, new ItemBuilder(Material.GOLD_NUGGET)
            .name("§emath:e")
            .lore(Arrays.asList(
                "§7Число e (2.71828...)",
                "",
                "§eКлик для выбора"
            ))
            .build());

        mathInventory.setItem(12, new ItemBuilder(Material.SNOWBALL)
            .name("§emath:random")
            .lore(Arrays.asList(
                "§7Случайное число от 0 до 1",
                "",
                "§eКлик для выбора"
            ))
            .build());

        // Кнопка назад
        mathInventory.setItem(49, new ItemBuilder(Material.ARROW)
            .name("§7Назад")
            .build());

        player.openInventory(mathInventory);
    }

    // Методы обработки кликов в подменю
    private void handleSystemVariableClick(int slot) {
        player.closeInventory();
        switch (slot) {
            case 10: // system:time
                callback.accept(new VariableValue("system:time"));
                break;
            case 11: // system:weather
                callback.accept(new VariableValue("system:weather"));
                break;
            case 12: // system:world
                callback.accept(new VariableValue("system:world"));
                break;
            case 13: // system:player_count
                callback.accept(new VariableValue("system:player_count"));
                break;
            case 49: // Назад
                this.open();
                break;
        }
    }

    private void handlePlayerVariableClick(int slot) {
        player.closeInventory();
        switch (slot) {
            case 10: // player:name
                callback.accept(new VariableValue("player:name"));
                break;
            case 11: // player:level
                callback.accept(new VariableValue("player:level"));
                break;
            case 12: // player:health
                callback.accept(new VariableValue("player:health"));
                break;
            case 13: // player:food
                callback.accept(new VariableValue("player:food"));
                break;
            case 49: // Назад
                this.open();
                break;
        }
    }

    private void handleTimeVariableClick(int slot) {
        player.closeInventory();
        switch (slot) {
            case 10: // time:hour
                callback.accept(new VariableValue("time:hour"));
                break;
            case 11: // time:minute
                callback.accept(new VariableValue("time:minute"));
                break;
            case 12: // time:day
                callback.accept(new VariableValue("time:day"));
                break;
            case 49: // Назад
                this.open();
                break;
        }
    }

    private void handleWorldVariableClick(int slot) {
        player.closeInventory();
        switch (slot) {
            case 10: // world:spawn_x
                callback.accept(new VariableValue("world:spawn_x"));
                break;
            case 11: // world:spawn_y
                callback.accept(new VariableValue("world:spawn_y"));
                break;
            case 12: // world:spawn_z
                callback.accept(new VariableValue("world:spawn_z"));
                break;
            case 49: // Назад
                this.open();
                break;
        }
    }

    private void handleMathConstantClick(int slot) {
        player.closeInventory();
        switch (slot) {
            case 10: // math:pi
                callback.accept(new VariableValue("math:pi"));
                break;
            case 11: // math:e
                callback.accept(new VariableValue("math:e"));
                break;
            case 12: // math:random
                callback.accept(new VariableValue("math:random"));
                break;
            case 49: // Назад
                this.open();
                break;
        }
    }
}