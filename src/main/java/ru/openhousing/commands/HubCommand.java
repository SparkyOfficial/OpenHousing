package ru.openhousing.commands;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ru.openhousing.OpenHousing;

/**
 * Команда для телепортации в хаб
 */
public class HubCommand implements CommandExecutor {
    
    private final OpenHousing plugin;
    
    public HubCommand(OpenHousing plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cЭта команда доступна только игрокам!");
            return true;
        }
        
        Player player = (Player) sender;
        
        // Получаем мир хаба (первый мир или мир с именем "world")
        World hubWorld = getHubWorld();
        
        if (hubWorld == null) {
            player.sendMessage("§cМир хаба не найден!");
            return true;
        }
        
        // Телепортируем игрока на спавн хаба
        player.teleport(hubWorld.getSpawnLocation());
        player.sendMessage("§aВы телепортированы в хаб!");
        
        return true;
    }
    
    /**
     * Получить мир хаба
     */
    private World getHubWorld() {
        // Сначала пробуем найти мир с именем "world" или "hub"
        World world = Bukkit.getWorld("world");
        if (world != null) {
            return world;
        }
        
        world = Bukkit.getWorld("hub");
        if (world != null) {
            return world;
        }
        
        // Если не найден, берем первый доступный мир
        if (!Bukkit.getWorlds().isEmpty()) {
            return Bukkit.getWorlds().get(0);
        }
        
        return null;
    }
}
