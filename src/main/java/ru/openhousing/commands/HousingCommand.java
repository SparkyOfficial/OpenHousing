package ru.openhousing.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import ru.openhousing.OpenHousing;
import ru.openhousing.utils.MessageUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Команда для работы с Housing системой (заглушка)
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
                MessageUtil.send(player, "&eHousing система будет добавлена в будущих обновлениях!");
                break;
            case "list":
                MessageUtil.send(player, "&eСписок домов будет доступен в будущих обновлениях!");
                break;
            case "visit":
                MessageUtil.send(player, "&eПосещение домов будет доступно в будущих обновлениях!");
                break;
            default:
                showHelp(player);
                break;
        }
        
        return true;
    }
    
    private void showHelp(Player player) {
        MessageUtil.send(player,
            "&6&l=== OpenHousing - Справка ===",
            "&e/housing create &7- Создать дом",
            "&e/housing list &7- Список домов",
            "&e/housing visit <игрок> &7- Посетить дом",
            "&e/housing help &7- Показать эту справку",
            ""
        );
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return Arrays.asList("create", "list", "visit", "help");
        }
        return new ArrayList<>();
    }
}
