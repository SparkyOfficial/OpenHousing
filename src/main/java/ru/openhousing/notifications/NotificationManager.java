package ru.openhousing.notifications;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import ru.openhousing.OpenHousing;
import ru.openhousing.housing.House;
import ru.openhousing.utils.MessageUtil;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Менеджер уведомлений и логирования
 */
public class NotificationManager {
    
    private final OpenHousing plugin;
    private final Map<UUID, List<Notification>> playerNotifications = new ConcurrentHashMap<>();
    private final File logFile;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    
    // Настройки
    private boolean enableNotifications = true;
    private boolean enableLogging = true;
    private int maxNotificationsPerPlayer = 50;
    private int notificationDisplayTime = 5; // секунды
    
    public NotificationManager(OpenHousing plugin) {
        this.plugin = plugin;
        this.logFile = new File(plugin.getDataFolder(), "housing.log");
        loadConfig();
        startNotificationCleanupTask();
    }
    
    /**
     * Загрузка конфигурации
     */
    private void loadConfig() {
        enableNotifications = plugin.getConfig().getBoolean("notifications.enabled", true);
        enableLogging = plugin.getConfig().getBoolean("logging.enabled", true);
        maxNotificationsPerPlayer = plugin.getConfig().getInt("notifications.max-per-player", 50);
        notificationDisplayTime = plugin.getConfig().getInt("notifications.display-time", 5);
        
        // Сохраняем значения по умолчанию
        plugin.getConfig().set("notifications.enabled", enableNotifications);
        plugin.getConfig().set("logging.enabled", enableLogging);
        plugin.getConfig().set("notifications.max-per-player", maxNotificationsPerPlayer);
        plugin.getConfig().set("notifications.display-time", notificationDisplayTime);
        plugin.saveConfig();
    }
    
    /**
     * Отправка уведомления игроку
     */
    public void sendNotification(Player player, NotificationType type, String message) {
        if (!enableNotifications || player == null) return;
        
        Notification notification = new Notification(type, message, System.currentTimeMillis());
        
        // Добавляем в список уведомлений игрока
        playerNotifications.computeIfAbsent(player.getUniqueId(), k -> new ArrayList<>()).add(notification);
        
        // Ограничиваем количество уведомлений
        List<Notification> notifications = playerNotifications.get(player.getUniqueId());
        while (notifications.size() > maxNotificationsPerPlayer) {
            notifications.remove(0);
        }
        
        // Отправляем уведомление
        String prefix = getNotificationPrefix(type);
        MessageUtil.send(player, prefix + message);
        
        // Логируем
        logAction(player.getName(), type.name(), message);
    }
    
    /**
     * Отправка уведомления всем игрокам дома
     */
    public void notifyHouseMembers(House house, NotificationType type, String message, Player excludePlayer) {
        for (Player player : house.getPlayersInside()) {
            if (player != null && player.isOnline() && !player.equals(excludePlayer)) {
                sendNotification(player, type, message);
            }
        }
    }
    
    /**
     * Уведомления о действиях с домом
     */
    public void notifyHouseCreated(Player player, House house) {
        sendNotification(player, NotificationType.HOUSE_CREATED, 
            "Дом '" + house.getName() + "' успешно создан!");
        logHouseAction(player.getName(), "CREATE", house);
    }
    
    public void notifyHouseDeleted(Player player, House house) {
        sendNotification(player, NotificationType.HOUSE_DELETED, 
            "Дом '" + house.getName() + "' удален");
        logHouseAction(player.getName(), "DELETE", house);
    }
    
    public void notifyHousePurchased(Player player, House house, double price) {
        sendNotification(player, NotificationType.ECONOMY, 
            "Дом '" + house.getName() + "' куплен за " + plugin.getEconomyManager().format(price));
        logHouseAction(player.getName(), "PURCHASE", house, "Price: " + price);
    }
    
    public void notifyHouseSold(Player player, House house, double price) {
        sendNotification(player, NotificationType.ECONOMY, 
            "Дом '" + house.getName() + "' продан за " + plugin.getEconomyManager().format(price));
        logHouseAction(player.getName(), "SELL", house, "Price: " + price);
    }
    
    public void notifyPlayerEntered(House house, Player player) {
        notifyHouseMembers(house, NotificationType.PLAYER_ACTIVITY, 
            "Игрок " + player.getName() + " зашел в дом", player);
        logHouseAction(player.getName(), "ENTER", house);
    }
    
    public void notifyPlayerLeft(House house, Player player) {
        notifyHouseMembers(house, NotificationType.PLAYER_ACTIVITY, 
            "Игрок " + player.getName() + " покинул дом", player);
        logHouseAction(player.getName(), "LEAVE", house);
    }
    
    public void notifyAccessGranted(House house, Player owner, String targetPlayerName) {
        sendNotification(owner, NotificationType.PERMISSION, 
            "Доступ к дому предоставлен игроку " + targetPlayerName);
        
        Player targetPlayer = Bukkit.getPlayer(targetPlayerName);
        if (targetPlayer != null && targetPlayer.isOnline()) {
            sendNotification(targetPlayer, NotificationType.PERMISSION, 
                "Вам предоставлен доступ к дому '" + house.getName() + "'");
        }
        
        logHouseAction(owner.getName(), "GRANT_ACCESS", house, "Target: " + targetPlayerName);
    }
    
    public void notifyAccessRevoked(House house, Player owner, String targetPlayerName) {
        sendNotification(owner, NotificationType.PERMISSION, 
            "Доступ к дому отозван у игрока " + targetPlayerName);
        
        Player targetPlayer = Bukkit.getPlayer(targetPlayerName);
        if (targetPlayer != null && targetPlayer.isOnline()) {
            sendNotification(targetPlayer, NotificationType.PERMISSION, 
                "Доступ к дому '" + house.getName() + "' отозван");
        }
        
        logHouseAction(owner.getName(), "REVOKE_ACCESS", house, "Target: " + targetPlayerName);
    }
    
    /**
     * Показать уведомления игроку
     */
    public void showNotifications(Player player) {
        List<Notification> notifications = playerNotifications.get(player.getUniqueId());
        if (notifications == null || notifications.isEmpty()) {
            MessageUtil.send(player, "&7У вас нет уведомлений");
            return;
        }
        
        MessageUtil.send(player, "&6&l=== Уведомления ===");
        
        // Показываем последние 10 уведомлений
        int start = Math.max(0, notifications.size() - 10);
        for (int i = start; i < notifications.size(); i++) {
            Notification notification = notifications.get(i);
            String timeAgo = MessageUtil.formatTime(System.currentTimeMillis() - notification.getTimestamp());
            String prefix = getNotificationPrefix(notification.getType());
            
            MessageUtil.send(player, "&7[" + timeAgo + " назад] " + prefix + notification.getMessage());
        }
        
        if (notifications.size() > 10) {
            MessageUtil.send(player, "&7... и еще " + (notifications.size() - 10) + " уведомлений");
        }
    }
    
    /**
     * Очистка уведомлений игрока
     */
    public void clearNotifications(Player player) {
        playerNotifications.remove(player.getUniqueId());
        MessageUtil.send(player, "&aУведомления очищены");
    }
    
    /**
     * Логирование действий с домом
     */
    private void logHouseAction(String playerName, String action, House house) {
        logHouseAction(playerName, action, house, null);
    }
    
    private void logHouseAction(String playerName, String action, House house, String details) {
        if (!enableLogging) return;
        
        String logMessage = String.format("[%s] Player: %s, Action: %s, House: %s (ID: %s), Owner: %s", 
            dateFormat.format(new Date()), playerName, action, house.getName(), 
            house.getId(), house.getOwnerName());
        
        if (details != null) {
            logMessage += ", Details: " + details;
        }
        
        logAction(playerName, action, logMessage);
    }
    
    /**
     * Общее логирование
     */
    public void logAction(String playerName, String action, String message) {
        if (!enableLogging) return;
        
        try {
            if (!logFile.exists()) {
                logFile.getParentFile().mkdirs();
                logFile.createNewFile();
            }
            
            try (FileWriter writer = new FileWriter(logFile, true)) {
                writer.write(message + "\n");
                writer.flush();
            }
            
        } catch (IOException e) {
            plugin.getLogger().warning("Ошибка записи в лог: " + e.getMessage());
        }
    }
    
    /**
     * Получение префикса для типа уведомления
     */
    private String getNotificationPrefix(NotificationType type) {
        switch (type) {
            case HOUSE_CREATED:
                return "&a[Дом создан] &f";
            case HOUSE_DELETED:
                return "&c[Дом удален] &f";
            case ECONOMY:
                return "&e[Экономика] &f";
            case PERMISSION:
                return "&b[Доступ] &f";
            case PLAYER_ACTIVITY:
                return "&7[Активность] &f";
            case TELEPORTATION:
                return "&d[Телепортация] &f";
            case SYSTEM:
                return "&6[Система] &f";
            default:
                return "&f[Инфо] &f";
        }
    }
    
    /**
     * Задача очистки старых уведомлений
     */
    private void startNotificationCleanupTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                long cutoffTime = System.currentTimeMillis() - (24 * 60 * 60 * 1000); // 24 часа
                
                for (List<Notification> notifications : playerNotifications.values()) {
                    notifications.removeIf(notification -> notification.getTimestamp() < cutoffTime);
                }
            }
        }.runTaskTimerAsynchronously(plugin, 20L * 60 * 60, 20L * 60 * 60); // Каждый час
    }
    
    /**
     * Очистка данных игрока при выходе
     */
    public void cleanupPlayer(Player player) {
        // Оставляем уведомления для следующего входа
        // playerNotifications.remove(player.getUniqueId());
    }
    
    // Геттеры и сеттеры
    public boolean isNotificationsEnabled() { return enableNotifications; }
    public boolean isLoggingEnabled() { return enableLogging; }
    
    public void setNotificationsEnabled(boolean enabled) {
        this.enableNotifications = enabled;
        plugin.getConfig().set("notifications.enabled", enabled);
        plugin.saveConfig();
    }
    
    public void setLoggingEnabled(boolean enabled) {
        this.enableLogging = enabled;
        plugin.getConfig().set("logging.enabled", enabled);
        plugin.saveConfig();
    }
}
