package ru.openhousing.housing;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.*;

/**
 * Класс дома игрока
 */
public class House {
    
    private final int id;
    private final UUID ownerId;
    private String ownerName;
    private String name;
    private final Location location;
    private final HouseSize size;
    private boolean isPublic;
    private boolean visitorsAllowed;
    private final Set<UUID> allowedPlayers;
    private final Set<UUID> bannedPlayers;
    private final Map<String, Object> settings;
    private long createdAt;
    private long lastModified;
    
    public House(int id, UUID ownerId, String ownerName, String name, Location location, HouseSize size) {
        this.id = id;
        this.ownerId = ownerId;
        this.ownerName = ownerName;
        this.name = name;
        this.location = location.clone();
        this.size = size;
        this.isPublic = false;
        this.visitorsAllowed = true;
        this.allowedPlayers = new HashSet<>();
        this.bannedPlayers = new HashSet<>();
        this.settings = new HashMap<>();
        this.createdAt = System.currentTimeMillis();
        this.lastModified = createdAt;
    }
    
    /**
     * Проверка, может ли игрок зайти в дом
     */
    public boolean canVisit(Player player) {
        UUID playerId = player.getUniqueId();
        
        // Владелец всегда может зайти
        if (playerId.equals(ownerId)) {
            return true;
        }
        
        // Забаненные игроки не могут
        if (bannedPlayers.contains(playerId)) {
            return false;
        }
        
        // Если дом публичный
        if (isPublic) {
            return true;
        }
        
        // Если разрешены посетители и игрок в списке разрешенных
        if (visitorsAllowed && allowedPlayers.contains(playerId)) {
            return true;
        }
        
        // Проверка прав администратора
        return player.hasPermission("openhousing.admin.bypass");
    }
    
    /**
     * Получение точки спавна в доме
     */
    public Location getSpawnLocation() {
        // Центр дома + немного выше для безопасности
        double x = location.getX() + size.getWidth() / 2.0;
        double y = location.getY() + 1;
        double z = location.getZ() + size.getLength() / 2.0;
        
        return new Location(location.getWorld(), x, y, z);
    }
    
    /**
     * Проверка, находится ли локация в доме
     */
    public boolean isInside(Location loc) {
        if (!loc.getWorld().equals(location.getWorld())) {
            return false;
        }
        
        double minX = location.getX();
        double maxX = minX + size.getWidth();
        double minY = location.getY();
        double maxY = minY + size.getHeight();
        double minZ = location.getZ();
        double maxZ = minZ + size.getLength();
        
        return loc.getX() >= minX && loc.getX() <= maxX &&
               loc.getY() >= minY && loc.getY() <= maxY &&
               loc.getZ() >= minZ && loc.getZ() <= maxZ;
    }
    
    /**
     * Добавление игрока в список разрешенных
     */
    public void allowPlayer(UUID playerId) {
        allowedPlayers.add(playerId);
        bannedPlayers.remove(playerId); // Убираем из бана если был
        updateModified();
    }
    
    /**
     * Добавление игрока в список разрешенных по имени
     */
    public void allowPlayer(String playerName) {
        Player player = Bukkit.getPlayer(playerName);
        if (player != null) {
            allowPlayer(player.getUniqueId());
        }
    }
    
    /**
     * Удаление игрока из списка разрешенных
     */
    public void disallowPlayer(UUID playerId) {
        allowedPlayers.remove(playerId);
        updateModified();
    }
    
    /**
     * Бан игрока
     */
    public void banPlayer(UUID playerId) {
        bannedPlayers.add(playerId);
        allowedPlayers.remove(playerId); // Убираем из разрешенных
        updateModified();
        
        // Кикаем игрока из дома, если он там
        Player player = Bukkit.getPlayer(playerId);
        if (player != null && isInside(player.getLocation())) {
            player.teleport(player.getWorld().getSpawnLocation());
            player.sendMessage("§cВы были исключены из дома!");
        }
    }
    
    /**
     * Разбан игрока
     */
    public void unbanPlayer(UUID playerId) {
        bannedPlayers.remove(playerId);
        updateModified();
    }
    
    /**
     * Получение всех игроков в доме
     */
    public List<Player> getPlayersInside() {
        List<Player> playersInside = new ArrayList<>();
        
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (isInside(player.getLocation())) {
                playersInside.add(player);
            }
        }
        
        return playersInside;
    }
    
    /**
     * Кик всех игроков из дома
     */
    public void kickAllPlayers(String reason) {
        List<Player> playersInside = getPlayersInside();
        
        for (Player player : playersInside) {
            if (!player.getUniqueId().equals(ownerId)) { // Не кикаем владельца
                player.teleport(player.getWorld().getSpawnLocation());
                if (reason != null && !reason.isEmpty()) {
                    player.sendMessage("§cВы были исключены из дома: " + reason);
                } else {
                    player.sendMessage("§cВы были исключены из дома!");
                }
            }
        }
    }
    
    /**
     * Расширение дома
     */
    public boolean expandHouse(int newWidth, int newHeight, int newLength) {
        if (newWidth < size.getWidth() || newHeight < size.getHeight() || newLength < size.getLength()) {
            return false; // Нельзя уменьшать
        }
        
        size.setWidth(newWidth);
        size.setHeight(newHeight);
        size.setLength(newLength);
        updateModified();
        
        return true;
    }
    
    /**
     * Получение границ дома
     */
    public HouseBounds getBounds() {
        return new HouseBounds(
            location.clone(),
            location.clone().add(size.getWidth(), size.getHeight(), size.getLength())
        );
    }
    
    private void updateModified() {
        this.lastModified = System.currentTimeMillis();
    }
    
    // Геттеры и сеттеры
    public int getId() {
        return id;
    }
    
    public UUID getOwnerId() {
        return ownerId;
    }
    
    public String getOwnerName() {
        return ownerName;
    }
    
    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
        updateModified();
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
        updateModified();
    }
    
    public Location getLocation() {
        return location.clone();
    }
    
    public HouseSize getSize() {
        return size;
    }
    
    public boolean isPublic() {
        return isPublic;
    }
    
    public void setPublic(boolean isPublic) {
        this.isPublic = isPublic;
        updateModified();
    }
    
    public boolean isVisitorsAllowed() {
        return visitorsAllowed;
    }
    
    public void setVisitorsAllowed(boolean visitorsAllowed) {
        this.visitorsAllowed = visitorsAllowed;
        updateModified();
    }
    
    public Set<UUID> getAllowedPlayers() {
        return new HashSet<>(allowedPlayers);
    }
    
    public Set<UUID> getBannedPlayers() {
        return new HashSet<>(bannedPlayers);
    }
    
    public Map<String, Object> getSettings() {
        return new HashMap<>(settings);
    }
    
    public Object getSetting(String key) {
        return settings.get(key);
    }
    
    public void setSetting(String key, Object value) {
        settings.put(key, value);
        updateModified();
    }
    
    public long getCreatedAt() {
        return createdAt;
    }
    
    public long getLastModified() {
        return lastModified;
    }
    
    /**
     * Класс размера дома
     */
    public static class HouseSize {
        private int width;
        private int height;
        private int length;
        
        public HouseSize(int width, int height, int length) {
            this.width = width;
            this.height = height;
            this.length = length;
        }
        
        public int getWidth() { return width; }
        public int getHeight() { return height; }
        public int getLength() { return length; }
        
        public void setWidth(int width) { this.width = width; }
        public void setHeight(int height) { this.height = height; }
        public void setLength(int length) { this.length = length; }
        
        public int getVolume() {
            return width * height * length;
        }
        
        @Override
        public String toString() {
            return width + "x" + height + "x" + length;
        }
    }
    
    /**
     * Границы дома
     */
    public static class HouseBounds {
        private final Location min;
        private final Location max;
        
        public HouseBounds(Location min, Location max) {
            this.min = min;
            this.max = max;
        }
        
        public Location getMin() { return min; }
        public Location getMax() { return max; }
        
        public boolean contains(Location location) {
            return location.getX() >= min.getX() && location.getX() <= max.getX() &&
                   location.getY() >= min.getY() && location.getY() <= max.getY() &&
                   location.getZ() >= min.getZ() && location.getZ() <= max.getZ();
        }
    }
}
