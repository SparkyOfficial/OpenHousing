package ru.openhousing.teleportation;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import ru.openhousing.OpenHousing;
import ru.openhousing.housing.House;
import ru.openhousing.utils.MessageUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Менеджер телепортации между домами
 */
public class TeleportationManager {
    
    private final OpenHousing plugin;
    private final Map<UUID, Long> teleportCooldowns = new HashMap<>();
    private final Map<UUID, BukkitRunnable> pendingTeleports = new HashMap<>();
    
    // Настройки телепортации
    private int teleportDelay = 3; // секунды
    private int teleportCooldown = 10; // секунды
    private boolean allowMovementDuringTeleport = false;
    
    public TeleportationManager(OpenHousing plugin) {
        this.plugin = plugin;
        loadConfig();
    }
    
    /**
     * Загрузка конфигурации
     */
    private void loadConfig() {
        teleportDelay = plugin.getConfig().getInt("teleportation.delay", 3);
        teleportCooldown = plugin.getConfig().getInt("teleportation.cooldown", 10);
        allowMovementDuringTeleport = plugin.getConfig().getBoolean("teleportation.allow-movement", false);
        
        // Сохраняем значения по умолчанию
        plugin.getConfig().set("teleportation.delay", teleportDelay);
        plugin.getConfig().set("teleportation.cooldown", teleportCooldown);
        plugin.getConfig().set("teleportation.allow-movement", allowMovementDuringTeleport);
        plugin.saveConfig();
    }
    
    /**
     * Телепортация игрока к своему дому
     */
    public void teleportToHome(Player player) {
        House house = plugin.getHousingManager().getHouse(player.getName());
        if (house == null) {
            MessageUtil.send(player, "&cУ вас нет дома! Используйте &e/housing buy &cдля покупки.");
            return;
        }
        
        teleportToHouse(player, house, "домой");
    }
    
    /**
     * Телепортация игрока к дому другого игрока
     */
    public void teleportToPlayerHouse(Player player, String targetPlayerName) {
        House house = plugin.getHousingManager().getHouse(targetPlayerName);
        if (house == null) {
            MessageUtil.send(player, "&cДом игрока &e" + targetPlayerName + " &cне найден!");
            return;
        }
        
        // Проверяем доступ
        if (!canPlayerAccess(player, house)) {
            MessageUtil.send(player, "&cУ вас нет доступа к дому игрока &e" + targetPlayerName + "&c!");
            return;
        }
        
        teleportToHouse(player, house, "к дому " + targetPlayerName);
    }
    
    /**
     * Основная логика телепортации к дому
     */
    private void teleportToHouse(Player player, House house, String destination) {
        // Проверяем кулдаун
        if (isOnCooldown(player)) {
            long remainingTime = getRemainingCooldown(player);
            MessageUtil.send(player, "&cПодождите еще &e" + remainingTime + " &cсекунд перед следующей телепортацией!");
            return;
        }
        
        // Отменяем предыдущую телепортацию если есть
        cancelPendingTeleport(player);
        
        Location targetLocation = house.getSpawnLocation() != null ? 
            house.getSpawnLocation() : house.getLocation();
        
        if (teleportDelay <= 0) {
            // Мгновенная телепортация
            performTeleport(player, targetLocation, destination);
        } else {
            // Телепортация с задержкой
            startDelayedTeleport(player, targetLocation, destination);
        }
    }
    
    /**
     * Запуск телепортации с задержкой
     */
    private void startDelayedTeleport(Player player, Location target, String destination) {
        Location startLocation = player.getLocation().clone();
        
        MessageUtil.send(player, "&eТелепортация " + destination + " через &c" + teleportDelay + " &eсекунд...");
        if (!allowMovementDuringTeleport) {
            MessageUtil.send(player, "&7Не двигайтесь во время телепортации!");
        }
        
        BukkitRunnable teleportTask = new BukkitRunnable() {
            int countdown = teleportDelay;
            
            @Override
            public void run() {
                if (!player.isOnline()) {
                    cancel();
                    return;
                }
                
                // Проверяем движение игрока
                if (!allowMovementDuringTeleport && 
                    startLocation.distance(player.getLocation()) > 0.5) {
                    MessageUtil.send(player, "&cТелепортация отменена - вы двигались!");
                    cancel();
                    return;
                }
                
                countdown--;
                
                if (countdown <= 0) {
                    performTeleport(player, target, destination);
                    cancel();
                } else if (countdown <= 3) {
                    MessageUtil.send(player, "&e" + countdown + "...");
                }
            }
        };
        
        pendingTeleports.put(player.getUniqueId(), teleportTask);
        teleportTask.runTaskTimer(plugin, 0L, 20L);
    }
    
    /**
     * Выполнение телепортации
     */
    private void performTeleport(Player player, Location target, String destination) {
        try {
            player.teleport(target);
            MessageUtil.send(player, "&aТелепортация " + destination + " выполнена!");
            
            // Устанавливаем кулдаун
            teleportCooldowns.put(player.getUniqueId(), System.currentTimeMillis() + (teleportCooldown * 1000L));
            
            // Убираем из ожидающих
            pendingTeleports.remove(player.getUniqueId());
            
        } catch (Exception e) {
            MessageUtil.send(player, "&cОшибка телепортации: " + e.getMessage());
            plugin.getLogger().warning("Ошибка телепортации игрока " + player.getName() + ": " + e.getMessage());
        }
    }
    
    /**
     * Отмена ожидающей телепортации
     */
    public void cancelPendingTeleport(Player player) {
        BukkitRunnable task = pendingTeleports.remove(player.getUniqueId());
        if (task != null) {
            task.cancel();
            MessageUtil.send(player, "&7Телепортация отменена");
        }
    }
    
    /**
     * Проверка кулдауна
     */
    private boolean isOnCooldown(Player player) {
        if (player.hasPermission("openhousing.teleport.nocooldown")) {
            return false;
        }
        
        Long cooldownEnd = teleportCooldowns.get(player.getUniqueId());
        return cooldownEnd != null && System.currentTimeMillis() < cooldownEnd;
    }
    
    /**
     * Получение оставшегося времени кулдауна
     */
    private long getRemainingCooldown(Player player) {
        Long cooldownEnd = teleportCooldowns.get(player.getUniqueId());
        if (cooldownEnd == null) return 0;
        
        long remaining = (cooldownEnd - System.currentTimeMillis()) / 1000;
        return Math.max(0, remaining);
    }
    
    /**
     * Проверка доступа к дому
     */
    private boolean canPlayerAccess(Player player, House house) {
        // Проверяем через WorldGuard если доступен
        if (plugin.getWorldGuardIntegration().isWorldGuardEnabled()) {
            return plugin.getWorldGuardIntegration().canPlayerAccess(player, house);
        }
        
        // Внутренняя проверка доступа
        return house.isPublic() || 
               house.getOwner().equals(player.getName()) || 
               house.getAllowedPlayers().contains(player.getName()) ||
               player.hasPermission("openhousing.admin");
    }
    
    /**
     * Получение списка доступных домов для игрока
     */
    public void showAvailableHouses(Player player) {
        MessageUtil.send(player, "&6&l=== Доступные дома ===");
        
        boolean foundAny = false;
        
        // Собственный дом
        House ownHouse = plugin.getHousingManager().getHouse(player.getName());
        if (ownHouse != null) {
            MessageUtil.send(player, "&a• &e" + ownHouse.getName() + " &7(ваш дом)");
            foundAny = true;
        }
        
        // Публичные дома
        for (House house : plugin.getHousingManager().getAllHouses()) {
            if (house.isPublic() && !house.getOwner().equals(player.getName())) {
                MessageUtil.send(player, "&a• &e" + house.getName() + " &7(владелец: " + house.getOwner() + ")");
                foundAny = true;
            }
        }
        
        // Дома с разрешенным доступом
        for (House house : plugin.getHousingManager().getAllHouses()) {
            if (!house.isPublic() && 
                !house.getOwner().equals(player.getName()) && 
                house.getAllowedPlayers().contains(player.getName())) {
                MessageUtil.send(player, "&a• &e" + house.getName() + " &7(разрешен доступ)");
                foundAny = true;
            }
        }
        
        if (!foundAny) {
            MessageUtil.send(player, "&7Нет доступных домов для телепортации");
        } else {
            MessageUtil.send(player, "&7Используйте &e/housing visit <владелец> &7для телепортации");
        }
    }
    
    /**
     * Очистка кулдаунов при выходе игрока
     */
    public void cleanupPlayer(Player player) {
        teleportCooldowns.remove(player.getUniqueId());
        cancelPendingTeleport(player);
    }
    
    /**
     * Принудительная телепортация (для администраторов)
     */
    public void forceTeleport(Player player, String targetPlayerName) {
        if (!player.hasPermission("openhousing.admin")) {
            MessageUtil.send(player, "&cУ вас нет прав для принудительной телепортации!");
            return;
        }
        
        House house = plugin.getHousingManager().getHouse(targetPlayerName);
        if (house == null) {
            MessageUtil.send(player, "&cДом игрока &e" + targetPlayerName + " &cне найден!");
            return;
        }
        
        Location target = house.getSpawnLocation() != null ? 
            house.getSpawnLocation() : house.getLocation();
        
        performTeleport(player, target, "к дому " + targetPlayerName + " (принудительно)");
    }
    
    // Геттеры и сеттеры для настроек
    public int getTeleportDelay() { return teleportDelay; }
    public int getTeleportCooldown() { return teleportCooldown; }
    public boolean isMovementAllowed() { return allowMovementDuringTeleport; }
    
    public void setTeleportDelay(int delay) {
        this.teleportDelay = delay;
        plugin.getConfig().set("teleportation.delay", delay);
        plugin.saveConfig();
    }
    
    public void setTeleportCooldown(int cooldown) {
        this.teleportCooldown = cooldown;
        plugin.getConfig().set("teleportation.cooldown", cooldown);
        plugin.saveConfig();
    }
    
    public void setAllowMovement(boolean allow) {
        this.allowMovementDuringTeleport = allow;
        plugin.getConfig().set("teleportation.allow-movement", allow);
        plugin.saveConfig();
    }
}
