package ru.openhousing.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import ru.openhousing.OpenHousing;
import ru.openhousing.housing.House;
import ru.openhousing.housing.HousingManager;
import ru.openhousing.utils.MessageUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * Команда для работы с Housing системой
 */
public class HousingCommand implements CommandExecutor, TabCompleter {
    
    private final OpenHousing plugin;
    
    public HousingCommand(OpenHousing plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            MessageUtil.send(sender, "&cЭта команда доступна только игрокам!");
            return true;
        }
        
        Player player = (Player) sender;
        
        if (args.length == 0) {
            showHelp(player);
            return true;
        }
        
        String subCommand = args[0].toLowerCase();
        
        switch (subCommand) {
            case "create":
                createHouse(player, args);
                break;
            case "list":
                listHouses(player, args);
                break;
            case "visit":
                visitHouse(player, args);
                break;
            case "delete":
                deleteHouse(player, args);
                break;
            case "home":
                teleportHome(player);
                break;
            case "homes":
                showAvailableHomes(player);
                break;
            case "tpforce":
                forceTeleport(player, args);
                break;
            case "notifications":
                showNotifications(player);
                break;
            case "clearnotifications":
                clearNotifications(player);
                break;
            case "disallow":
                disallowPlayer(player, args);
                break;
            case "ban":
                banPlayer(player, args);
                break;
            case "unban":
                unbanPlayer(player, args);
                break;
            case "public":
                togglePublic(player, args);
                break;
            case "info":
                houseInfo(player, args);
                break;
            case "buy":
                buyHouse(player, args);
                break;
            case "sell":
                sellHouse(player);
                break;
            case "price":
                showPrices(player);
                break;
            case "help":
            default:
                showHelp(player);
                break;
        }
        
        return true;
    }
    
    /**
     * Покупка дома
     */
    private void buyHouse(Player player, String[] args) {
        // Проверяем, есть ли уже дом у игрока
        if (plugin.getHousingManager().hasHouse(player.getName())) {
            MessageUtil.send(player, "&cУ вас уже есть дом! Продайте его перед покупкой нового.");
            return;
        }
        
        // Создаем дом и проверяем экономику
        String houseName = args.length > 1 ? String.join(" ", Arrays.copyOfRange(args, 1, args.length)) : null;
        
        // Создаем временный дом для расчета цены
        HousingManager.CreateHouseResult result = plugin.getHousingManager().createHouse(player, houseName);
        
        if (!result.isSuccess()) {
            MessageUtil.send(player, "&c" + result.getMessage());
            return;
        }
        
        House house = plugin.getHousingManager().getHouse(player.getName());
        if (house == null) {
            MessageUtil.send(player, "&cОшибка создания дома!");
            return;
        }
        
        // Проверяем и списываем деньги
        if (plugin.getEconomyManager().buyHouse(player, house)) {
            MessageUtil.send(player, "&aДом успешно куплен!");
            MessageUtil.send(player, "&7Используйте &e/housing home &7для телепортации!");
        } else {
            // Удаляем дом если покупка не удалась
            List<House> playerHouses = plugin.getHousingManager().getPlayerHouses(player.getUniqueId());
            if (!playerHouses.isEmpty()) {
                plugin.getHousingManager().deleteHouse(playerHouses.get(0), player);
            }
        }
    }
    
    /**
     * Продажа дома
     */
    private void sellHouse(Player player) {
        House house = plugin.getHousingManager().getHouse(player.getName());
        if (house == null) {
            MessageUtil.send(player, "&cУ вас нет дома для продажи!");
            return;
        }
        
        double sellPrice = plugin.getEconomyManager().calculateSellPrice(house);
        
        MessageUtil.send(player, 
            "&e&l=== ПРОДАЖА ДОМА ===",
            "&7Цена продажи: &e" + plugin.getEconomyManager().format(sellPrice),
            "&cВНИМАНИЕ: Дом будет удален навсегда!",
            "",
            "&eНапишите в чат: &aПРОДАТЬ &eдля подтверждения",
            "&7Или любое другое сообщение для отмены"
        );
        
        // Регистрируем временный слушатель чата
        ru.openhousing.listeners.ChatListener.registerTemporaryInput(player, (input) -> {
            if ("ПРОДАТЬ".equalsIgnoreCase(input.trim())) {
                if (plugin.getEconomyManager().sellHouse(player, house)) {
                    plugin.getHousingManager().deleteHouse(house, player);
                    MessageUtil.send(player, "&aДом успешно продан!");
                } else {
                    MessageUtil.send(player, "&cОшибка продажи дома!");
                }
            } else {
                MessageUtil.send(player, "&7Продажа дома отменена");
            }
        });
    }
    
    /**
     * Показать цены
     */
    private void showPrices(Player player) {
        plugin.getEconomyManager().showPriceInfo(player);
    }
    
    /**
     * Создание дома
     */
    private void createHouse(Player player, String[] args) {
        String houseName = null;
        if (args.length > 1) {
            houseName = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
        }
        
        HousingManager.CreateHouseResult result = plugin.getHousingManager().createHouse(player, houseName);
        
        if (result.isSuccess()) {
            MessageUtil.send(player, "&a" + result.getMessage());
            MessageUtil.send(player, "&7Используйте &e/housing home &7для телепортации домой!");
        } else {
            MessageUtil.send(player, "&c" + result.getMessage());
        }
    }
    
    /**
     * Список домов
     */
    private void listHouses(Player player, String[] args) {
        String type = args.length > 1 ? args[1].toLowerCase() : "my";
        
        switch (type) {
            case "my":
                listMyHouses(player);
                break;
            case "public":
                listPublicHouses(player);
                break;
            default:
                MessageUtil.send(player, "&cИспользование: &e/housing list [my|public]");
                break;
        }
    }
    
    private void listMyHouses(Player player) {
        List<House> houses = plugin.getHousingManager().getPlayerHouses(player.getUniqueId());
        
        if (houses.isEmpty()) {
            MessageUtil.send(player, "&cУ вас нет домов! Создайте дом: &e/housing create");
            return;
        }
        
        MessageUtil.send(player, "&6&l=== Ваши дома ===");
        for (House house : houses) {
            String status = house.isPublic() ? "&aПубличный" : "&7Приватный";
            MessageUtil.send(player, "&e" + house.getName() + " &7- " + status + " &8(ID: " + house.getId() + ")");
        }
        MessageUtil.send(player, "&7Используйте &e/housing visit <дом> &7для телепортации");
    }
    
    /**
     * Телепортация домой
     */
    private void teleportHome(Player player) {
        List<House> houses = plugin.getHousingManager().getPlayerHouses(player.getUniqueId());
        if (houses.isEmpty()) {
            MessageUtil.send(player, "&cУ вас нет домов!");
            return;
        }
        
        House house = houses.get(0); // Первый дом как основной
        if (plugin.getHousingManager().teleportToHouse(player, house)) {
            MessageUtil.send(player, "&aВы телепортированы домой!");
        } else {
            MessageUtil.send(player, "&cОшибка телепортации!");
        }
    }
    
    /**
     * Посещение дома
     */
    private void visitHouse(Player player, String[] args) {
        if (args.length < 2) {
            MessageUtil.send(player, "&cИспользование: &e/housing visit <игрок>");
            return;
        }
        
        String targetPlayer = args[1];
        List<House> houses = plugin.getHousingManager().getPlayerHouses(targetPlayer);
        
        if (houses.isEmpty()) {
            MessageUtil.send(player, "&cУ игрока " + targetPlayer + " нет домов!");
            return;
        }
        
        House house = houses.get(0); // Первый дом
        if (plugin.getHousingManager().teleportToHouse(player, house)) {
            MessageUtil.send(player, "&aВы посетили дом игрока " + targetPlayer + "!");
        } else {
            MessageUtil.send(player, "&cВы не можете посетить этот дом!");
        }
    }
    
    /**
     * Показать доступные дома
     */
    private void showAvailableHomes(Player player) {
        List<House> publicHouses = plugin.getHousingManager().getPublicHouses();
        List<House> myHouses = plugin.getHousingManager().getPlayerHouses(player.getUniqueId());
        
        MessageUtil.send(player, "&6&l=== Доступные дома ===");
        
        if (!myHouses.isEmpty()) {
            MessageUtil.send(player, "&eВаши дома:");
            for (House house : myHouses) {
                MessageUtil.send(player, "&7- " + house.getName());
            }
        }
        
        if (!publicHouses.isEmpty()) {
            MessageUtil.send(player, "&eПубличные дома:");
            for (House house : publicHouses) {
                MessageUtil.send(player, "&7- " + house.getName() + " (владелец: " + house.getOwner() + ")");
            }
        }
        
        if (myHouses.isEmpty() && publicHouses.isEmpty()) {
            MessageUtil.send(player, "&cНет доступных домов!");
        }
    }
    
    /**
     * Принудительная телепортация (админ)
     */
    private void forceTeleport(Player player, String[] args) {
        if (!player.hasPermission("openhousing.admin.teleport")) {
            MessageUtil.send(player, "&cУ вас нет прав для принудительной телепортации!");
            return;
        }
        
        if (args.length < 2) {
            MessageUtil.send(player, "&cИспользование: &e/housing tpforce <дом>");
            return;
        }
        
        House house = plugin.getHousingManager().getHouse(args[1]);
        if (house == null) {
            MessageUtil.send(player, "&cДом не найден!");
            return;
        }
        
        player.teleport(house.getSpawnLocation());
        MessageUtil.send(player, "&aВы телепортированы в дом " + house.getName() + "!");
    }
    
    /**
     * Показать уведомления
     */
    private void showNotifications(Player player) {
        MessageUtil.send(player, "&6Ваши уведомления:");
        MessageUtil.send(player, "&7Система уведомлений активна");
    }
    
    /**
     * Очистить уведомления
     */
    private void clearNotifications(Player player) {
        MessageUtil.send(player, "&aУведомления очищены!");
    }
    
    private void listPublicHouses(Player player) {
        List<House> houses = plugin.getHousingManager().getPublicHouses();
        
        if (houses.isEmpty()) {
            MessageUtil.send(player, "&cНет публичных домов!");
            return;
        }
        
        MessageUtil.send(player, "&6&l=== Публичные дома ===");
        for (House house : houses) {
            MessageUtil.send(player, "&e" + house.getName() + " &7- &f" + house.getOwnerName() + " &8(ID: " + house.getId() + ")");
        }
        MessageUtil.send(player, "&7Используйте &e/housing visit <игрок> &7для телепортации");
    }
    
    /**
     * Удаление дома
     */
    private void deleteHouse(Player player, String[] args) {
        List<House> houses = plugin.getHousingManager().getPlayerHouses(player.getUniqueId());
        
        if (houses.isEmpty()) {
            MessageUtil.send(player, "&cУ вас нет домов!");
            return;
        }
        
        House house = houses.get(0); // Удаляем первый дом
        
        if (plugin.getHousingManager().deleteHouse(house, player)) {
            MessageUtil.send(player, "&aДом &e" + house.getName() + " &aуспешно удален!");
        } else {
            MessageUtil.send(player, "&cНе удалось удалить дом!");
        }
    }
    
    /**
     * Разрешить доступ игроку
     */
    private void allowPlayer(Player player, String[] args) {
        if (args.length < 2) {
            MessageUtil.send(player, "&cИспользование: &e/housing allow <игрок>");
            return;
        }
        
        List<House> houses = plugin.getHousingManager().getPlayerHouses(player.getUniqueId());
        if (houses.isEmpty()) {
            MessageUtil.send(player, "&cУ вас нет домов!");
            return;
        }
        
        House house = houses.get(0);
        String targetName = args[1];
        
        house.allowPlayer(targetName);
        MessageUtil.send(player, "&aИгроку &e" + targetName + " &aразрешен доступ к вашему дому!");
    }
    
    /**
     * Запретить доступ игроку
     */
    private void disallowPlayer(Player player, String[] args) {
        if (args.length < 2) {
            MessageUtil.send(player, "&cИспользование: &e/housing disallow <игрок>");
            return;
        }
        
        List<House> houses = plugin.getHousingManager().getPlayerHouses(player.getUniqueId());
        if (houses.isEmpty()) {
            MessageUtil.send(player, "&cУ вас нет домов!");
            return;
        }
        
        House house = houses.get(0);
        String targetName = args[1];
        Player targetPlayer = plugin.getServer().getPlayer(targetName);
        
        if (targetPlayer != null) {
            house.disallowPlayer(targetPlayer.getUniqueId());
            MessageUtil.send(player, "&cИгроку &e" + targetName + " &cзапрещен доступ к вашему дому!");
        } else {
            MessageUtil.send(player, "&cИгрок не найден!");
        }
    }
    
    /**
     * Заблокировать игрока
     */
    private void banPlayer(Player player, String[] args) {
        if (args.length < 2) {
            MessageUtil.send(player, "&cИспользование: &e/housing ban <игрок>");
            return;
        }
        
        List<House> houses = plugin.getHousingManager().getPlayerHouses(player.getUniqueId());
        if (houses.isEmpty()) {
            MessageUtil.send(player, "&cУ вас нет домов!");
            return;
        }
        
        House house = houses.get(0);
        String targetName = args[1];
        Player targetPlayer = plugin.getServer().getPlayer(targetName);
        
        if (targetPlayer != null) {
            house.banPlayer(targetPlayer.getUniqueId());
            MessageUtil.send(player, "&cИгрок &e" + targetName + " &cзаблокирован в вашем доме!");
        } else {
            MessageUtil.send(player, "&cИгрок не найден!");
        }
    }
    
    /**
     * Разблокировать игрока
     */
    private void unbanPlayer(Player player, String[] args) {
        if (args.length < 2) {
            MessageUtil.send(player, "&cИспользование: &e/housing unban <игрок>");
            return;
        }
        
        List<House> houses = plugin.getHousingManager().getPlayerHouses(player.getUniqueId());
        if (houses.isEmpty()) {
            MessageUtil.send(player, "&cУ вас нет домов!");
            return;
        }
        
        House house = houses.get(0);
        String targetName = args[1];
        Player targetPlayer = plugin.getServer().getPlayer(targetName);
        
        if (targetPlayer != null) {
            house.unbanPlayer(targetPlayer.getUniqueId());
            MessageUtil.send(player, "&aИгрок &e" + targetName + " &aразблокирован!");
        } else {
            MessageUtil.send(player, "&cИгрок не найден!");
        }
    }
    
    /**
     * Переключение публичности дома
     */
    private void togglePublic(Player player, String[] args) {
        List<House> houses = plugin.getHousingManager().getPlayerHouses(player.getUniqueId());
        if (houses.isEmpty()) {
            MessageUtil.send(player, "&cУ вас нет домов!");
            return;
        }
        
        House house = houses.get(0);
        house.setPublic(!house.isPublic());
        
        if (house.isPublic()) {
            MessageUtil.send(player, "&aВаш дом теперь публичный! Все игроки могут его посетить.");
        } else {
            MessageUtil.send(player, "&cВаш дом теперь приватный! Только разрешенные игроки могут его посетить.");
        }
    }
    
    /**
     * Настройки дома
     */
    private void houseSettings(Player player, String[] args) {
        List<House> houses = plugin.getHousingManager().getPlayerHouses(player.getUniqueId());
        if (houses.isEmpty()) {
            MessageUtil.send(player, "&cУ вас нет домов!");
            return;
        }
        
        House house = houses.get(0);
        
        MessageUtil.send(player,
            "&6&l=== Настройки дома ===",
            "&eНазвание: &f" + house.getName(),
            "&eВладелец: &f" + house.getOwnerName(),
            "&eРазмер: &f" + house.getSize().getDisplayName(),
            "&eПубличный: " + (house.isPublic() ? "&aДа" : "&cНет"),
            "&eРазрешенные игроки: &f" + house.getAllowedPlayers().size(),
            "&eЗаблокированные игроки: &f" + house.getBannedPlayers().size(),
            "",
            "&7Доступные команды:",
            "&e/housing public &7- переключить публичность",
            "&e/housing allow <игрок> &7- разрешить доступ",
            "&e/housing ban <игрок> &7- заблокировать игрока",
            "&e/housing buy [имя] &7- купить дом",
            "&e/housing sell &7- продать дом",
            "&e/housing price &7- посмотреть цены"
        );
        
        // Открываем GUI настроек
        ru.openhousing.gui.HouseSettingsGUI settingsGUI = new ru.openhousing.gui.HouseSettingsGUI(plugin, player, house);
        settingsGUI.open();
    }
    
    /**
     * Информация о доме
     */
    private void houseInfo(Player player, String[] args) {
        String target = args.length > 1 ? args[1] : player.getName();
        
        House house = plugin.getHousingManager().getHouse(target);
        if (house == null) {
            Player targetPlayer = plugin.getServer().getPlayer(target);
            if (targetPlayer != null) {
                List<House> houses = plugin.getHousingManager().getPlayerHouses(targetPlayer.getUniqueId());
                if (!houses.isEmpty()) {
                    house = houses.get(0);
                }
            }
        }
        
        if (house == null) {
            MessageUtil.send(player, "&cДом не найден!");
            return;
        }
        
        MessageUtil.send(player,
            "&6&l=== Информация о доме ===",
            "&eНазвание: &f" + house.getName(),
            "&eВладелец: &f" + house.getOwnerName(),
            "&eСтатус: " + (house.isPublic() ? "&aПубличный" : "&7Приватный"),
            "&eРазмер: &f" + house.getSize().toString(),
            "&eИгроков внутри: &f" + house.getPlayersInside().size(),
            "&eСоздан: &f" + MessageUtil.formatTime(System.currentTimeMillis() - house.getCreatedAt()) + " назад"
        );
    }
    
    private void showHelp(Player player) {
        MessageUtil.send(player,
            "&6&l=== OpenHousing - Справка ===",
            "&e/housing create [имя] &7- Создать дом",
            "&e/housing home &7- Телепорт домой",
            "&e/housing list [my|public] &7- Список домов",
            "&e/housing visit <игрок> &7- Посетить дом",
            "&e/housing homes &7- Показать доступные дома",
            "&e/housing settings &7- Настройки дома",
            "&e/housing buy [имя] &7- Купить дом",
            "&e/housing sell &7- Продать дом",
            "&e/housing price &7- Показать цены",
            "&e/housing public &7- Сделать дом публичным/приватным",
            "&e/housing allow <игрок> &7- Разрешить доступ",
            "&e/housing ban <игрок> &7- Заблокировать игрока",
            "&e/housing info [игрок] &7- Информация о доме",
            "&e/housing delete &7- Удалить дом",
            "&e/housing notifications &7- Показать уведомления",
            "&e/housing clearnotifications &7- Очистить уведомления",
            "&e/housing tpforce <игрок> &7- Принудительная телепортация (админ)",
            ""
        );
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return Arrays.asList("create", "home", "list", "visit", "homes", "settings", "public", 
                               "allow", "disallow", "ban", "unban", "info", "delete", "buy", "sell", 
                               "price", "notifications", "clearnotifications", "tpforce", "help");
        }
        
        if (args.length == 2) {
            switch (args[0].toLowerCase()) {
                case "list":
                    return Arrays.asList("my", "public");
                case "visit":
                case "info":
                    // Возвращаем имена онлайн игроков
                    return plugin.getServer().getOnlinePlayers().stream()
                        .map(Player::getName)
                        .toList();
                case "allow":
                case "disallow":
                case "ban":
                case "unban":
                    return plugin.getServer().getOnlinePlayers().stream()
                        .map(Player::getName)
                        .toList();
            }
        }
        
        return new ArrayList<>();
    }
}
