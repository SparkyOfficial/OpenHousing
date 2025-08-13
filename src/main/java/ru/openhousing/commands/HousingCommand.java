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
            case "settings":
                houseSettings(player, args);
                break;
            case "allow":
                allowPlayer(player, args);
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
            case "home":
                goHome(player);
                break;
            case "help":
            default:
                showHelp(player);
                break;
        }
        
        return true;
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
        MessageUtil.send(player, "&7Используйте &e/housing visit <дом> &7для телепортации");
    }
    
    /**
     * Посещение дома
     */
    private void visitHouse(Player player, String[] args) {
        if (args.length < 2) {
            MessageUtil.send(player, "&cИспользование: &e/housing visit <дом|игрок>");
            return;
        }
        
        String target = args[1];
        House house = plugin.getHousingManager().getHouse(target);
        
        // Если дом не найден по имени, попробуем найти по имени игрока
        if (house == null) {
            Player targetPlayer = plugin.getServer().getPlayer(target);
            UUID targetId = targetPlayer != null ? targetPlayer.getUniqueId() : null;
            
            if (targetId != null) {
                List<House> playerHouses = plugin.getHousingManager().getPlayerHouses(targetId);
                if (!playerHouses.isEmpty()) {
                    house = playerHouses.get(0); // Первый дом игрока
                }
            }
        }
        
        if (house == null) {
            MessageUtil.send(player, "&cДом не найден!");
            return;
        }
        
        if (plugin.getHousingManager().teleportToHouse(player, house)) {
            // Сообщение отправляется в HousingManager
        } else {
            MessageUtil.send(player, "&cВы не можете посетить этот дом!");
        }
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
            "&eИмя: &f" + house.getName(),
            "&eСтатус: " + (house.isPublic() ? "&aПубличный" : "&7Приватный"),
            "&eПосетители: " + (house.isVisitorsAllowed() ? "&aРазрешены" : "&cЗапрещены"),
            "&eРазмер: &f" + house.getSize().toString(),
            "",
            "&7Команды:",
            "&e/housing public &7- Переключить публичность",
            "&e/housing allow <игрок> &7- Разрешить доступ",
            "&e/housing ban <игрок> &7- Заблокировать игрока"
        );
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
    
    /**
     * Телепортация домой
     */
    private void goHome(Player player) {
        List<House> houses = plugin.getHousingManager().getPlayerHouses(player.getUniqueId());
        
        if (houses.isEmpty()) {
            MessageUtil.send(player, "&cУ вас нет домов! Создайте дом: &e/housing create");
            return;
        }
        
        House house = houses.get(0);
        plugin.getHousingManager().teleportToHouse(player, house);
    }
    
    private void showHelp(Player player) {
        MessageUtil.send(player,
            "&6&l=== OpenHousing - Справка ===",
            "&e/housing create [имя] &7- Создать дом",
            "&e/housing home &7- Телепорт домой",
            "&e/housing list [my|public] &7- Список домов",
            "&e/housing visit <дом|игрок> &7- Посетить дом",
            "&e/housing settings &7- Настройки дома",
            "&e/housing public &7- Сделать дом публичным/приватным",
            "&e/housing allow <игрок> &7- Разрешить доступ",
            "&e/housing ban <игрок> &7- Заблокировать игрока",
            "&e/housing info [дом|игрок] &7- Информация о доме",
            "&e/housing delete &7- Удалить дом",
            ""
        );
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return Arrays.asList("create", "home", "list", "visit", "settings", "public", 
                               "allow", "disallow", "ban", "unban", "info", "delete", "help");
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
