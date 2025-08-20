package ru.openhousing.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.entity.Player;
import ru.openhousing.OpenHousing;
import ru.openhousing.housing.House;

/**
 * Слушатель событий блоков в домах
 */
public class BlockListener implements Listener {
    
    private final OpenHousing plugin;
    
    public BlockListener(OpenHousing plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        House house = plugin.getHousingManager().getHouseAt(event.getBlock().getLocation());
        
        if (house != null) {
            // Проверяем права на разрушение блоков в доме
            if (!house.canVisit(player) && !player.hasPermission("openhousing.admin.break")) {
                event.setCancelled(true);
                player.sendMessage("§cВы не можете разрушать блоки в этом доме!");
            }
        }
    }
    
    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        House house = plugin.getHousingManager().getHouseAt(event.getBlock().getLocation());
        
        if (house != null) {
            // Проверяем права на постройку в доме
            if (!house.canVisit(player) && !player.hasPermission("openhousing.admin.build")) {
                event.setCancelled(true);
                player.sendMessage("§cВы не можете строить в этом доме!");
            }
        }
    }
}
