package ru.openhousing.housing;

import org.bukkit.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import ru.openhousing.OpenHousing;
import ru.openhousing.utils.MessageUtil;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Менеджер системы Housing
 */
public class HousingManager {
    
    private final OpenHousing plugin;
    private final Map<Integer, House> houses;
    private final Map<UUID, List<House>> playerHouses;
    private final Map<String, House> housesByName;
    private World housingWorld;
    private int nextHouseId;
    private Location nextHouseLocation;
    
    // Настройки из конфига
    private int maxHousesPerPlayer;
    private House.HouseSize defaultSize;
    private House.HouseSize minSize;
    private House.HouseSize maxSize;
    private double creationCost;
    private double expansionCostPerBlock;
    private int houseSpacing;
    
    public HousingManager(OpenHousing plugin) {
        this.plugin = plugin;
        this.houses = new ConcurrentHashMap<>();
        this.playerHouses = new ConcurrentHashMap<>();
        this.housesByName = new ConcurrentHashMap<>();
        this.nextHouseId = 1;
    }
    
    /**
     * Инициализация менеджера
     */
    public void initialize() {
        loadConfiguration();
        setupHousingWorld();
        
        // Асинхронно загружаем дома из базы данных
        plugin.getDatabaseManager().loadAllHousesAsync(loadedHouses -> {
            for (House house : loadedHouses) {
                houses.put(house.getId(), house);
                housesByName.put(house.getName().toLowerCase(), house);
                
                playerHouses.computeIfAbsent(house.getOwnerId(), k -> new ArrayList<>()).add(house);
                
                if (house.getId() >= nextHouseId) {
                    nextHouseId = house.getId() + 1;
                }
            }
            
            plugin.getLogger().info("Loaded " + houses.size() + " houses from database");
        });
        
        plugin.getLogger().info("HousingManager initialized successfully!");
    }
    
    /**
     * Загрузка конфигурации
     */
    private void loadConfiguration() {
        FileConfiguration config = plugin.getConfigManager().getHousingConfig();
        
        maxHousesPerPlayer = config.getInt("max-houses-per-player", 1);
        
        // Размеры домов
        defaultSize = new House.HouseSize(
            config.getInt("default-size.width", 64),
            config.getInt("default-size.height", 64),
            config.getInt("default-size.length", 64)
        );
        
        minSize = new House.HouseSize(
            config.getInt("min-size.width", 32),
            config.getInt("min-size.height", 32),
            config.getInt("min-size.length", 32)
        );
        
        maxSize = new House.HouseSize(
            config.getInt("max-size.width", 128),
            config.getInt("max-size.height", 128),
            config.getInt("max-size.length", 128)
        );
        
        creationCost = config.getDouble("creation-cost", 10000.0);
        expansionCostPerBlock = config.getDouble("expansion-cost-per-block", 100.0);
        houseSpacing = config.getInt("house-spacing", 200);
    }
    
    /**
     * Настройка мира для домов
     */
    private void setupHousingWorld() {
        FileConfiguration config = plugin.getConfigManager().getHousingConfig();
        String worldName = config.getString("world-name", "housing_world");
        boolean autoCreate = config.getBoolean("auto-create-world", true);
        
        housingWorld = Bukkit.getWorld(worldName);
        
        if (housingWorld == null && autoCreate) {
            plugin.getLogger().info("Creating housing world: " + worldName);
            
            WorldCreator creator = new WorldCreator(worldName);
            creator.type(WorldType.FLAT);
            creator.generateStructures(false);
            housingWorld = creator.createWorld();
            
            if (housingWorld != null) {
                housingWorld.setSpawnFlags(false, false);
                housingWorld.setGameRule(GameRule.DO_MOB_SPAWNING, false);
                housingWorld.setGameRule(GameRule.DO_WEATHER_CYCLE, false);
                housingWorld.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
                housingWorld.setTime(6000); // День
                
                plugin.getLogger().info("Housing world created successfully!");
            } else {
                plugin.getLogger().severe("Failed to create housing world!");
            }
        }
        
        if (housingWorld != null) {
            nextHouseLocation = new Location(housingWorld, 0, 100, 0);
        }
    }
    
    /**
     * Загрузка домов из базы данных
     */
    private void loadHousesFromDatabase() {
        List<House> loadedHouses = plugin.getDatabaseManager().loadAllHouses();
        
        for (House house : loadedHouses) {
            houses.put(house.getId(), house);
            housesByName.put(house.getName().toLowerCase(), house);
            
            playerHouses.computeIfAbsent(house.getOwnerId(), k -> new ArrayList<>()).add(house);
            
            if (house.getId() >= nextHouseId) {
                nextHouseId = house.getId() + 1;
            }
        }
        
        plugin.getLogger().info("Loaded " + houses.size() + " houses from database");
    }
    
    /**
     * Создание нового дома
     */
    public CreateHouseResult createHouse(Player player, String houseName) {
        // Проверка разрешений
        if (!player.hasPermission("openhousing.housing.create")) {
            return new CreateHouseResult(false, "У вас нет разрешения на создание домов!");
        }
        
        // Проверка лимита домов
        List<House> playerHouseList = playerHouses.get(player.getUniqueId());
        if (playerHouseList != null && playerHouseList.size() >= maxHousesPerPlayer) {
            return new CreateHouseResult(false, "Достигнут лимит домов: " + maxHousesPerPlayer);
        }
        
        // Проверка имени
        if (houseName == null || houseName.trim().isEmpty()) {
            houseName = player.getName() + "'s House";
        }
        
        if (housesByName.containsKey(houseName.toLowerCase())) {
            return new CreateHouseResult(false, "Дом с таким именем уже существует!");
        }
        
        // Проверка экономики
        if (plugin.getEconomy() != null) {
            if (!plugin.getEconomy().has(player, creationCost)) {
                return new CreateHouseResult(false, "Недостаточно средств! Нужно: " + creationCost);
            }
        }
        
        // Поиск свободного места
        Location houseLocation = findNextHouseLocation();
        if (houseLocation == null) {
            return new CreateHouseResult(false, "Не удалось найти место для дома!");
        }
        
        // Списание денег
        if (plugin.getEconomy() != null) {
            plugin.getEconomy().withdrawPlayer(player, creationCost);
        }
        
        // Создание дома
        House house = new House(
            nextHouseId++,
            player.getUniqueId(),
            player.getName(),
            houseName,
            houseLocation,
            new House.HouseSize(defaultSize.getWidth(), defaultSize.getHeight(), defaultSize.getLength())
        );
        
        // Сохранение в память
        houses.put(house.getId(), house);
        housesByName.put(houseName.toLowerCase(), house);
        playerHouses.computeIfAbsent(player.getUniqueId(), k -> new ArrayList<>()).add(house);
        
        // Сохранение в базу данных
        plugin.getDatabaseManager().saveHouse(house);
        
        // Подготовка территории дома
        prepareHouseArea(house);
        
        return new CreateHouseResult(true, "Дом '" + houseName + "' успешно создан!", house);
    }
    
    /**
     * Поиск следующего свободного места для дома
     */
    private Location findNextHouseLocation() {
        if (housingWorld == null) {
            return null;
        }
        
        // Спиральный поиск места
        int x = (int) nextHouseLocation.getX();
        int z = (int) nextHouseLocation.getZ();
        int y = (int) nextHouseLocation.getY();
        
        for (int radius = 0; radius < 100; radius++) {
            for (int angle = 0; angle < 360; angle += 45) {
                double radians = Math.toRadians(angle);
                int newX = x + (int) (radius * houseSpacing * Math.cos(radians));
                int newZ = z + (int) (radius * houseSpacing * Math.sin(radians));
                
                Location testLocation = new Location(housingWorld, newX, y, newZ);
                if (isLocationFree(testLocation)) {
                    nextHouseLocation = testLocation;
                    return testLocation;
                }
            }
        }
        
        return null;
    }
    
    /**
     * Проверка, свободно ли место
     */
    private boolean isLocationFree(Location location) {
        House.HouseSize testSize = defaultSize;
        
        for (House house : houses.values()) {
            if (house.getLocation().getWorld().equals(location.getWorld())) {
                double distance = house.getLocation().distance(location);
                if (distance < houseSpacing) {
                    return false;
                }
            }
        }
        
        return true;
    }
    
    /**
     * Подготовка территории дома
     */
    private void prepareHouseArea(House house) {
        Location loc = house.getLocation();
        House.HouseSize size = house.getSize();
        World world = loc.getWorld();
        
        // Очистка области и создание платформы
        for (int x = 0; x < size.getWidth(); x++) {
            for (int z = 0; z < size.getLength(); z++) {
                for (int y = 0; y < size.getHeight(); y++) {
                    Location blockLoc = loc.clone().add(x, y, z);
                    
                    if (y == 0) {
                        // Платформа
                        world.getBlockAt(blockLoc).setType(Material.GRASS_BLOCK);
                    } else if (y == 1 && (x == 0 || x == size.getWidth() - 1 || z == 0 || z == size.getLength() - 1)) {
                        // Границы дома
                        world.getBlockAt(blockLoc).setType(Material.BARRIER);
                    } else {
                        // Воздух
                        world.getBlockAt(blockLoc).setType(Material.AIR);
                    }
                }
            }
        }
        
        // Табличка с информацией
        Location signLoc = loc.clone().add(1, 2, 1);
        world.getBlockAt(signLoc).setType(Material.OAK_SIGN);
        // TODO: Установить текст на табличке
    }
    
    /**
     * Удаление дома
     */
    public boolean deleteHouse(House house, Player deleter) {
        // Проверка прав
        if (!house.getOwnerId().equals(deleter.getUniqueId()) && 
            !deleter.hasPermission("openhousing.admin.delete")) {
            return false;
        }
        
        // Кик всех игроков
        house.kickAllPlayers("Дом удален");
        
        // Удаление из памяти
        houses.remove(house.getId());
        housesByName.remove(house.getName().toLowerCase());
        
        List<House> playerHouseList = playerHouses.get(house.getOwnerId());
        if (playerHouseList != null) {
            playerHouseList.remove(house);
            if (playerHouseList.isEmpty()) {
                playerHouses.remove(house.getOwnerId());
            }
        }
        
        // Удаление из базы данных
        plugin.getDatabaseManager().deleteHouse(house.getId());
        
        // Очистка территории
        clearHouseArea(house);
        
        return true;
    }
    
    /**
     * Очистка территории дома
     */
    private void clearHouseArea(House house) {
        Location loc = house.getLocation();
        House.HouseSize size = house.getSize();
        World world = loc.getWorld();
        
        for (int x = 0; x < size.getWidth(); x++) {
            for (int z = 0; z < size.getLength(); z++) {
                for (int y = 0; y < size.getHeight(); y++) {
                    Location blockLoc = loc.clone().add(x, y, z);
                    world.getBlockAt(blockLoc).setType(Material.AIR);
                }
            }
        }
    }
    
    /**
     * Телепортация в дом
     */
    public boolean teleportToHouse(Player player, House house) {
        if (!house.canVisit(player)) {
            return false;
        }
        
        Location spawnLocation = house.getSpawnLocation();
        player.teleport(spawnLocation);
        
        // Приветственное сообщение
        if (house.getOwnerId().equals(player.getUniqueId())) {
            MessageUtil.send(player, "&aДобро пожаловать домой!");
        } else {
            MessageUtil.send(player, "&aВы посетили дом игрока &e" + house.getOwnerName());
        }
        
        return true;
    }
    
    /**
     * Получение дома по ID
     */
    public House getHouse(int id) {
        return houses.get(id);
    }
    
    /**
     * Получение дома по имени
     */
    public House getHouse(String name) {
        return housesByName.get(name.toLowerCase());
    }
    
    /**
     * Получение домов игрока
     */
    public List<House> getPlayerHouses(UUID playerId) {
        return new ArrayList<>(playerHouses.getOrDefault(playerId, new ArrayList<>()));
    }
    
    /**
     * Получение дома по локации
     */
    public House getHouseAt(Location location) {
        for (House house : houses.values()) {
            if (house.isInside(location)) {
                return house;
            }
        }
        return null;
    }
    
    /**
     * Получение всех публичных домов
     */
    public List<House> getPublicHouses() {
        return houses.values().stream()
            .filter(House::isPublic)
            .toList();
    }
    
    /**
     * Сохранение всех данных
     */
    public void saveAll() {
        for (House house : houses.values()) {
            plugin.getDatabaseManager().saveHouse(house);
        }
        plugin.getLogger().info("All housing data saved!");
    }
    
    /**
     * Результат создания дома
     */
    public static class CreateHouseResult {
        private final boolean success;
        private final String message;
        private final House house;
        
        public CreateHouseResult(boolean success, String message) {
            this(success, message, null);
        }
        
        public CreateHouseResult(boolean success, String message, House house) {
            this.success = success;
            this.message = message;
            this.house = house;
        }
        
        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
        public House getHouse() { return house; }
    }
    
    // Геттеры для конфигурации
    public int getMaxHousesPerPlayer() { return maxHousesPerPlayer; }
    public House.HouseSize getDefaultSize() { return defaultSize; }
    public House.HouseSize getMinSize() { return minSize; }
    public House.HouseSize getMaxSize() { return maxSize; }
    public double getCreationCost() { return creationCost; }
    public double getExpansionCostPerBlock() { return expansionCostPerBlock; }
    public World getHousingWorld() { return housingWorld; }
}
