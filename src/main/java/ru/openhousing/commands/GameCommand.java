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
 * Команда для работы с игровой системой (заглушка)
 */
public class GameCommand implements CommandExecutor, TabCompleter {
    
    private final OpenHousing plugin;
    
    public GameCommand(OpenHousing plugin) {
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
                MessageUtil.send(player, "&eСистема игр будет добавлена в будущих обновлениях!");
                break;
            case "list":
                MessageUtil.send(player, "&eСписок игр будет доступен в будущих обновлениях!");
                break;
            case "join":
                MessageUtil.send(player, "&eПрисоединение к играм будет доступно в будущих обновлениях!");
                break;
            default:
                showHelp(player);
                break;
        }
        
        return true;
    }
    
    private void showHelp(Player player) {
        MessageUtil.send(player,
            "&6&l=== OpenHousing Games - Справка ===",
            "&e/ohgame create &7- Создать игру",
            "&e/ohgame list &7- Список игр",
            "&e/ohgame join <игра> &7- Присоединиться к игре",
            "&e/ohgame help &7- Показать эту справку",
            ""
        );
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return Arrays.asList("create", "list", "join", "help");
        }
        return new ArrayList<>();
    }
}
