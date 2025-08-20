package ru.openhousing.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import ru.openhousing.OpenHousing;
import ru.openhousing.housing.House;
import ru.openhousing.notifications.NotificationType;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Слушатель движения игроков для отслеживания входа/выхода из домов
 */
public class HouseMovementListener implements Listener {
    
    private final OpenHousing plugin;
    private final Map<UUID, String> playerLastHouse = new HashMap<>();
    
    public HouseMovementListener(OpenHousing plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        
        // Проверяем только если игрок действительно переместился в новый блок
        if (event.getFrom().getBlockX() == event.getTo().getBlockX() &&
            event.getFrom().getBlockY() == event.getTo().getBlockY() &&
            event.getFrom().getBlockZ() == event.getTo().getBlockZ()) {
            return;
        }
        
        House currentHouse = plugin.getHousingManager().getHouseAt(event.getTo());
        String currentHouseId = currentHouse != null ? String.valueOf(currentHouse.getId()) : null;
        String lastHouseId = playerLastHouse.get(player.getUniqueId());
        
        // Если игрок сменил дом
        if (!java.util.Objects.equals(currentHouseId, lastHouseId)) {
            
            // Покинул предыдущий дом
            if (lastHouseId != null) {
                House lastHouse = plugin.getHousingManager().getHouseById(lastHouseId);
                if (lastHouse != null) {
                    lastHouse.removePlayer(player.getUniqueId());
                    plugin.getNotificationManager().notifyPlayerLeft(lastHouse, player);
                }
            }
            
            // Вошел в новый дом
            if (currentHouse != null) {
                // Проверяем доступ
                if (canPlayerEnterHouse(player, currentHouse)) {
                    currentHouse.addPlayer(player.getUniqueId());
                    plugin.getNotificationManager().notifyPlayerEntered(currentHouse, player);
                    
                    // Отправляем приветственное сообщение
                    if (!currentHouse.getOwner().equals(player.getName())) {
                        plugin.getNotificationManager().sendNotification(player, NotificationType.PLAYER_ACTIVITY,
                            "Добро пожаловать в дом '" + currentHouse.getName() + "' (владелец: " + currentHouse.getOwnerName() + ")");
                    } else {
                        plugin.getNotificationManager().sendNotification(player, NotificationType.PLAYER_ACTIVITY,
                            "Добро пожаловать домой!");
                    }
                } else {
                    // Игрок не может войти в дом
                    plugin.getNotificationManager().sendNotification(player, NotificationType.SYSTEM,
                        "У вас нет доступа к дому '" + currentHouse.getName() + "'");
                    currentHouseId = null; // Не обновляем последний дом
                }
            }
            
            // Обновляем последний дом игрока
            if (currentHouseId != null) {
                playerLastHouse.put(player.getUniqueId(), currentHouseId);
            } else {
                playerLastHouse.remove(player.getUniqueId());
            }
        }
    }
    
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();
        
        // Убираем игрока из всех домов
        String lastHouseId = playerLastHouse.remove(playerId);
        if (lastHouseId != null) {
            House lastHouse = plugin.getHousingManager().getHouseById(lastHouseId);
            if (lastHouse != null) {
                lastHouse.removePlayer(playerId);
                plugin.getNotificationManager().notifyPlayerLeft(lastHouse, player);
            }
        }
        
        // Очищаем данные телепортации
        plugin.getTeleportationManager().cleanupPlayer(player);
        
        // Очищаем данные уведомлений (опционально)
        plugin.getNotificationManager().cleanupPlayer(player);
    }
    
    /**
     * Проверка доступа игрока к дому
     */
    private boolean canPlayerEnterHouse(Player player, House house) {
        // Проверяем через WorldGuard если доступен
        if (plugin.getWorldGuardIntegration().isWorldGuardEnabled()) {
            return plugin.getWorldGuardIntegration().canPlayerAccess(player, house);
        }
        
        // Внутренняя проверка доступа
        if (house.getOwner().equals(player.getName())) {
            return true; // Владелец всегда может войти
        }
        
        if (house.getBannedPlayers().contains(player.getUniqueId())) {
            return false; // Заблокированный игрок не может войти
        }
        
        if (house.isPublic()) {
            return true; // Публичный дом доступен всем
        }
        
        if (house.getAllowedPlayers().contains(player.getName())) {
            return true; // Разрешенный игрок может войти
        }
        
        if (player.hasPermission("openhousing.admin")) {
            return true; // Администратор может войти везде
        }
        
        return false; // По умолчанию доступ запрещен
    }
}
