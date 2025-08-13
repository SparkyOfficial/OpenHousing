package ru.openhousing.listeners;

import org.bukkit.GameMode;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import ru.openhousing.OpenHousing;
import ru.openhousing.housing.House;
import ru.openhousing.housing.HouseMode;
import ru.openhousing.utils.MessageUtil;

/**
 * Слушатель событий Housing системы
 */
public class HousingListener implements Listener {
    
    private final OpenHousing plugin;
    
    public HousingListener(OpenHousing plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        // Проверяем, находится ли игрок в доме при входе
        checkPlayerHouseMode(event.getPlayer());
    }
    
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        // Здесь можно добавить обработку выхода игрока из дома
    }
    
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        // Проверяем смену дома только при значительном перемещении
        if (event.getFrom().getBlockX() != event.getTo().getBlockX() ||
            event.getFrom().getBlockZ() != event.getTo().getBlockZ()) {
            
            checkPlayerHouseMode(event.getPlayer());
        }
    }
    
    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        // Проверяем режим дома после телепортации
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            checkPlayerHouseMode(event.getPlayer());
        }, 1L);
    }
    
    /**
     * Проверка и установка режима игрока в зависимости от дома
     */
    private void checkPlayerHouseMode(org.bukkit.entity.Player player) {
        House house = plugin.getHousingManager().getHouseAt(player.getLocation());
        
        if (house != null) {
            // Игрок в доме - устанавливаем соответствующий режим
            applyHouseModeToPlayer(player, house);
        } else {
            // Игрок вне дома - устанавливаем обычный режим
            if (player.getGameMode() != GameMode.SPECTATOR && 
                !player.hasPermission("openhousing.admin.keep_gamemode")) {
                player.setGameMode(GameMode.SURVIVAL);
            }
        }
    }
    
    /**
     * Применение режима дома к игроку
     */
    private void applyHouseModeToPlayer(org.bukkit.entity.Player player, House house) {
        if (player.hasPermission("openhousing.admin.keep_gamemode")) {
            return; // Админы сохраняют свой режим
        }
        
        HouseMode mode = house.getMode();
        boolean isOwner = house.getOwnerId().equals(player.getUniqueId());
        
        switch (mode) {
            case PLAY:
                // Режим игры - все получают приключение
                if (player.getGameMode() != GameMode.SPECTATOR) {
                    player.setGameMode(GameMode.ADVENTURE);
                }
                break;
                
            case BUILD:
                // Режим строительства
                if (isOwner) {
                    // Владелец получает креатив
                    if (player.getGameMode() != GameMode.SPECTATOR) {
                        player.setGameMode(GameMode.CREATIVE);
                    }
                } else {
                    // Остальные получают приключение
                    if (player.getGameMode() != GameMode.SPECTATOR) {
                        player.setGameMode(GameMode.ADVENTURE);
                    }
                }
                break;
        }
    }
}
