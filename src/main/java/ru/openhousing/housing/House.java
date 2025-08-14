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
    private final ru.openhousing.OpenHousing plugin;
    private boolean isPublic;
    private boolean visitorsAllowed;
    private final Set<UUID> allowedPlayers;
    private final Set<UUID> bannedPlayers;
    private final Map<String, Object> settings;
    private HouseMode mode;
    private long createdAt;
    private long lastModified;
    
    public House(int id, UUID ownerId, String ownerName, String name, String worldName, HouseSize size, ru.openhousing.OpenHousing plugin) {
        this.id = id;
        this.ownerId = ownerId;
        this.ownerName = ownerName;
        this.name = name;
        this.worldName = worldName;
        this.world = null; // Загружается по требованию
        this.size = size;
        this.plugin = plugin;
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
     * Получение мира дома с автозагрузкой
     */
    public World getWorld() {
        if (world == null) {
            loadWorld();
        }
        return world;
    }
    
    /**
     * Загрузка мира дома
     */
    private void loadWorld() {
        world = Bukkit.getWorld(worldName);
        if (world == null) {
            // Создаем новый мир если не существует
            WorldCreator creator = new WorldCreator(worldName);
            creator.type(WorldType.FLAT);
            creator.generateStructures(false);
            world = creator.createWorld();
            
            if (world != null) {
                // Устанавливаем спавн в центре
                world.setSpawnLocation(0, 64, 0);
                plugin.getLogger().info("Мир дома '" + name + "' создан: " + worldName);
            }
        }
    }
    
    /**
     * Получение точки спавна в доме
     */
    public Location getSpawnLocation() {
        World houseWorld = getWorld(); // Это загрузит мир если нужно
        if (houseWorld == null) {
            return null;
        }
        // Возвращаем центр дома на уровне земли
        return new Location(houseWorld, 0, 64, 0);
    }
    
    /**
     * Получение списка игроков внутри дома
     */
    public List<Player> getPlayersInside() {
        List<Player> playersInside = new ArrayList<>();
        World houseWorld = getWorld();
        
        if (houseWorld != null) {
            for (Player player : houseWorld.getPlayers()) {
                playersInside.add(player);
            }
        }
        
        return playersInside;
    }
    
    /**
     * Проверка, находится ли локация внутри дома
     */
    public boolean isInside(Location location) {
        World houseWorld = getWorld();
        if (houseWorld == null || location.getWorld() == null || !location.getWorld().equals(houseWorld)) {
            return false;
        }
        
        // Если игрок в мире дома, значит он внутри
        return true;
    }
    
    /**
     * Проверка, может ли игрок посетить дом
     */
    public boolean canVisit(Player player) {
        // Владелец всегда может войти
        if (player.getUniqueId().equals(ownerId)) {
            return true;
        }
        
        // Проверяем бан
        if (bannedPlayers.contains(player.getUniqueId())) {
            return false;
        }
        
        // Публичный дом может посетить каждый
        if (isPublic && visitorsAllowed) {
            return true;
        }
        
        // Проверяем список разрешенных игроков
        return allowedPlayers.contains(player.getUniqueId());
    }
    
    /**
     * Разрешить доступ игроку
     */
    public void allowPlayer(UUID playerId) {
        allowedPlayers.add(playerId);
        bannedPlayers.remove(playerId); // Убираем из бана если был
        updateModified();
    }
    
    /**
     * Разрешить доступ игроку по имени
     */
    public void allowPlayer(String playerName) {
        Player player = Bukkit.getPlayer(playerName);
        if (player != null) {
            allowPlayer(player.getUniqueId());
        }
    }
    
    /**
     * Запретить доступ игроку
     */
    public void disallowPlayer(UUID playerId) {
        allowedPlayers.remove(playerId);
        updateModified();
    }
    
    /**
     * Заблокировать игрока
     */
    public void banPlayer(UUID playerId) {
        bannedPlayers.add(playerId);
        allowedPlayers.remove(playerId); // Убираем из разрешенных
        
        // Кикаем игрока из дома если он там
        Player player = Bukkit.getPlayer(playerId);
        if (player != null && isInside(player.getLocation())) {
            World mainWorld = Bukkit.getWorlds().get(0);
            player.teleport(mainWorld.getSpawnLocation());
            player.sendMessage("§cВы были исключены из дома!");
        }
        
        updateModified();
    }
    
    /**
     * Разблокировать игрока
     */
    public void unbanPlayer(UUID playerId) {
        bannedPlayers.remove(playerId);
        updateModified();
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
