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
        // Отдельные миры для каждого дома создаются по требованию
        
        // Асинхронно загружаем дома из базы данных
        plugin.getDatabaseManager().loadAllHousesAsync(loadedHouses -> {
            for (House house : loadedHouses) {
                houses.put(house.getId(), house);
                housesByName.put(house.getName().toLowerCase(), house);
                
                playerHouses.computeIfAbsent(house.getOwnerId(), k -> new ArrayList<>()).add(house);
                
                if (house.getId() >= nextHouseId) {
                    nextHouseId = house.getId() + 1;
                }
                
                // Предварительно загружаем мир дома
                try {
                    World houseWorld = house.getWorld();
                    if (houseWorld != null) {
                        plugin.getLogger().info("Preloaded world for house '" + house.getName() + "': " + house.getWorldName());
                    } else {
                        plugin.getLogger().warning("World not found for house: " + house.getId() + " (world: " + house.getWorldName() + ")");
                        // Не игнорируем дом, если мир не загружен - он может быть создан позже
                    }
                } catch (Exception e) {
                    plugin.getLogger().severe("Error preloading world for house '" + house.getName() + "': " + e.getMessage());
                    e.printStackTrace();
                }
            }
            
            plugin.getLogger().info("Loaded " + houses.size() + " houses from database with world preloading");
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
        
        // Проверка экономики (отключена в разработке)
        if (plugin.getEconomy() != null && plugin.getConfigManager().getMainConfig().getBoolean("economy.use-vault", false)) {
            if (!plugin.getEconomy().has(player, creationCost)) {
                return new CreateHouseResult(false, "Недостаточно средств! Нужно: " + creationCost);
            }
        }
        

        
        // Списание денег (отключено в разработке)
        if (plugin.getEconomy() != null && plugin.getConfigManager().getMainConfig().getBoolean("economy.use-vault", false)) {
            plugin.getEconomy().withdrawPlayer(player, creationCost);
        }
        
        // Создание уникального имени мира
        String worldName = "house_" + player.getUniqueId().toString().replace("-", "") + "_" + nextHouseId;
        
        // Создание дома
        House house = new House(
            nextHouseId++,
            player.getUniqueId(),
            player.getName(),
            houseName,
            worldName,
            new House.HouseSize(defaultSize.getWidth(), defaultSize.getHeight(), defaultSize.getLength()),
            plugin
        );
        
        // Сохранение в память
        houses.put(house.getId(), house);
        housesByName.put(houseName.toLowerCase(), house);
        playerHouses.computeIfAbsent(player.getUniqueId(), k -> new ArrayList<>()).add(house);
        
        // Сохранение в базу данных
        plugin.getDatabaseManager().saveHouse(house);
        
        // Создание и подготовка мира дома
        prepareHouseWorld(house);
        
        // Привязываем код игрока к миру дома и сохраняем асинхронно
        try {
            ru.openhousing.coding.script.CodeScript script = plugin.getCodeManager().getOrCreateScript(player);
            script.setBoundWorld(worldName);
            plugin.getDatabaseManager().saveCodeScriptAsync(script, () -> {});
        } catch (Exception ignored) {}
        
        // Создание WorldGuard региона
        if (plugin.getWorldGuardIntegration().isEnabled()) {
            plugin.getWorldGuardIntegration().createHouseRegion(house);
        }
        
        // Звуковые эффекты и уведомления
        plugin.getSoundEffects().playHouseCreate(player);
        plugin.getSoundEffects().showHouseCreatedTitle(player, houseName);
        plugin.getSoundEffects().showHouseBossBar(player, houseName, player.getName());
        
        return new CreateHouseResult(true, "Дом '" + houseName + "' успешно создан!", house);
    }
    
    /**
     * Подготовка мира дома
     */
    private void prepareHouseWorld(House house) {
        try {
            World world = house.getWorld();
            if (world != null) {
                // Создаем базовую платформу
                Location spawnLoc = house.getSpawnLocation();
                if (spawnLoc != null) {
                    // Создаем платформу 5x5 из травы
                    for (int x = -2; x <= 2; x++) {
                        for (int z = -2; z <= 2; z++) {
                            Location blockLoc = spawnLoc.clone().add(x, -1, z);
                            blockLoc.getBlock().setType(org.bukkit.Material.GRASS_BLOCK);
                        }
                    }
                    
                    // Устанавливаем спавн мира
                    world.setSpawnLocation(spawnLoc);
                    
                    plugin.getLogger().info("Мир дома '" + house.getName() + "' создан: " + world.getName());
                }
            }
        } catch (Exception e) {
            plugin.getLogger().severe("Ошибка создания мира дома: " + e.getMessage());
            e.printStackTrace();
        }
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
        
        // Удаление WorldGuard региона
        if (plugin.getWorldGuardIntegration().isEnabled()) {
            plugin.getWorldGuardIntegration().deleteHouseRegion(house);
        }
        
        // Очистка территории
        clearHouseArea(house);
        
        return true;
    }
    
    /**
     * Очистка территории дома
     */
    private void clearHouseArea(House house) {
        World world = house.getWorld();
        if (world == null) return;
        
        House.HouseSize size = house.getSize();
        
        for (int x = 0; x < size.getWidth(); x++) {
            for (int z = 0; z < size.getLength(); z++) {
                for (int y = 0; y < size.getHeight(); y++) {
                    Location blockLoc = new Location(world, x, y, z);
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
            plugin.getSoundEffects().playTeleport(player);
        } else {
            MessageUtil.send(player, "&aВы посетили дом игрока &e" + house.getOwnerName());
            plugin.getSoundEffects().playTeleport(player);
        }
        
        // Показываем информацию о доме
        plugin.getSoundEffects().showDetailedHouseScoreboard(player, house);
        
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
     * Получение домов игрока по UUID
     */
    public List<House> getPlayerHouses(UUID playerId) {
        return new ArrayList<>(playerHouses.getOrDefault(playerId, new ArrayList<>()));
    }
    
    /**
     * Получение домов игрока по имени
     */
    public List<House> getPlayerHouses(String playerName) {
        // Поиск по имени владельца
        return houses.values().stream()
            .filter(house -> house.getOwnerName().equalsIgnoreCase(playerName))
            .collect(java.util.stream.Collectors.toList());
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
    
    // Missing methods needed by other classes
    public List<House> getAllHouses() {
        return new ArrayList<>(houses.values());
    }
    
    public House getHouseById(String id) {
        try {
            int houseId = Integer.parseInt(id);
            return houses.get(houseId);
        } catch (NumberFormatException e) {
            return null;
        }
    }
    
    public boolean hasHouse(String houseName) {
        return housesByName.containsKey(houseName.toLowerCase());
    }
}
