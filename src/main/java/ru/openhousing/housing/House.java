package ru.openhousing.housing;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;
import org.bukkit.entity.Player;

import java.util.*;

/**
 * Класс дома игрока - каждый дом это отдельный мир
 */
public class House {
    
    private final int id;
    private final UUID ownerId;
    private String ownerName;
    private String name;
    private final String worldName; // Имя мира дома
    private World world; // Кэшированная ссылка на мир
    private final HouseSize size;
    private boolean isPublic;
    private boolean visitorsAllowed;
    private final Set<UUID> allowedPlayers;
    private final Set<UUID> bannedPlayers;
    private final Map<String, Object> settings;
    private HouseMode mode;
    private long createdAt;
    private long lastModified;
    
    public House(int id, UUID ownerId, String ownerName, String name, String worldName, HouseSize size) {
        this.id = id;
        this.ownerId = ownerId;
        this.ownerName = ownerName;
        this.name = name;
        this.worldName = worldName;
        this.world = null; // Загружается по требованию
        this.size = size;
        this.isPublic = false;
        this.visitorsAllowed = true;
        this.allowedPlayers = new HashSet<>();
        this.bannedPlayers = new HashSet<>();
        this.settings = new HashMap<>();
        this.mode = HouseMode.PLAY; // По умолчанию режим игры
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
     * Получение мира дома (создание или загрузка)
     */
    public World getWorld() {
        if (world == null || !world.getName().equals(worldName)) {
            world = Bukkit.getWorld(worldName);
            if (world == null) {
                // Создаем мир если он не существует
                WorldCreator creator = new WorldCreator(worldName);
                creator.type(WorldType.FLAT);
                creator.generateStructures(false);
                world = creator.createWorld();
                
                if (world != null) {
                    // Настройки мира дома
                    world.setDifficulty(org.bukkit.Difficulty.PEACEFUL);
                    world.setSpawnFlags(false, false); // Отключаем спавн мобов
                    world.setKeepSpawnInMemory(true);
                }
            }
        }
        return world;
    }
    
    /**
     * Получение точки спавна в доме
     */
    public Location getSpawnLocation() {
        World houseWorld = getWorld();
        if (houseWorld == null) {
            return null;
        }
        
        // Центр мира + немного выше для безопасности
        return new Location(houseWorld, 0, 65, 0);
    }
    
    /**
     * Проверка, находится ли игрок в доме
     */
    public boolean isInside(Location loc) {
        return loc.getWorld() != null && loc.getWorld().getName().equals(worldName);
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
            // Телепортируем в главный мир
            World mainWorld = Bukkit.getWorlds().get(0);
            player.teleport(mainWorld.getSpawnLocation());
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
        World mainWorld = Bukkit.getWorlds().get(0);
        
        for (Player player : playersInside) {
            if (!player.getUniqueId().equals(ownerId)) { // Не кикаем владельца
                player.teleport(mainWorld.getSpawnLocation());
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
     * Удаление мира дома
     */
    public boolean deleteWorld() {
        World houseWorld = getWorld();
        if (houseWorld != null) {
            // Кикаем всех игроков
            kickAllPlayers("Дом удаляется");
            
            // Выгружаем мир
            return Bukkit.unloadWorld(houseWorld, false);
        }
        return true;
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
    
    public String getWorldName() {
        return worldName;
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
    
    public HouseMode getMode() {
        return mode;
    }
    
    public void setMode(HouseMode mode) {
        this.mode = mode;
        updateModified();
    }
    
    /**
     * Получение списка игроков внутри дома
     */
    public List<Player> getPlayersInside() {
        List<Player> playersInside = new ArrayList<>();
        if (world != null) {
            for (Player player : world.getPlayers()) {
                if (isInside(player.getLocation())) {
                    playersInside.add(player);
                }
            }
        }
        return playersInside;
    }
    
    /**
     * Проверка, находится ли локация внутри дома
     */
    public boolean isInside(Location location) {
        if (world == null || location.getWorld() == null || !location.getWorld().equals(world)) {
            return false;
        }
        
        // Проверяем границы дома
        Location spawn = getSpawnLocation();
        if (spawn == null) return false;
        
        double halfWidth = size.getWidth() / 2.0;
        double halfLength = size.getLength() / 2.0;
        
        return location.getX() >= spawn.getX() - halfWidth &&
               location.getX() <= spawn.getX() + halfWidth &&
               location.getZ() >= spawn.getZ() - halfLength &&
               location.getZ() <= spawn.getZ() + halfLength &&
               location.getY() >= spawn.getY() &&
               location.getY() <= spawn.getY() + size.getHeight();
    }
    
    /**
     * Получение точки спавна в доме
     */
    public Location getSpawnLocation() {
        if (world == null) {
            return null;
        }
        // Возвращаем центр дома на уровне земли
        return new Location(world, 0, 64, 0);
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
        
        public String getDisplayName() {
            return width + "x" + height + "x" + length;
        }
        
        @Override
        public String toString() {
            return getDisplayName();
        }
    }
    

}
