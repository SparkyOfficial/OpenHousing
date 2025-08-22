package ru.openhousing.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.bukkit.configuration.file.FileConfiguration;
import ru.openhousing.OpenHousing;
import ru.openhousing.coding.script.CodeScript;
import ru.openhousing.coding.serialization.ScriptSerializer;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Менеджер базы данных
 */
public class DatabaseManager {
    
    private final OpenHousing plugin;
    private HikariDataSource dataSource;
    private ScriptSerializer scriptSerializer;
    
    public DatabaseManager(OpenHousing plugin) {
        this.plugin = plugin;
        this.scriptSerializer = new ScriptSerializer();
    }
    
    /**
     * Инициализация базы данных
     */
    public void initialize() {
        boolean debugMode = plugin.getConfigManager().getConfig().getBoolean("general.debug", false);
        
        try {
            if (debugMode) plugin.getLogger().info("[DEBUG] Initializing database...");
            
            FileConfiguration config = plugin.getConfigManager().getMainConfig();
            String dbType = config.getString("database.type", "h2");
            
            if (debugMode) plugin.getLogger().info("[DEBUG] Database type: " + dbType);
            
            HikariConfig hikariConfig = new HikariConfig();
            
            if ("mysql".equalsIgnoreCase(dbType)) {
                if (debugMode) plugin.getLogger().info("[DEBUG] Setting up MySQL connection...");
                setupMySQL(hikariConfig, config);
            } else {
                if (debugMode) plugin.getLogger().info("[DEBUG] Setting up H2 connection...");
                setupH2(hikariConfig, config);
            }
            
            // Настройки пула соединений
            hikariConfig.setMaximumPoolSize(config.getInt("database.pool.maximum-pool-size", 10));
            hikariConfig.setMinimumIdle(config.getInt("database.pool.minimum-idle", 2));
            hikariConfig.setConnectionTimeout(config.getLong("database.pool.connection-timeout", 30000));
            hikariConfig.setIdleTimeout(config.getLong("database.pool.idle-timeout", 600000));
            hikariConfig.setMaxLifetime(config.getLong("database.pool.max-lifetime", 1800000));
            
            if (debugMode) plugin.getLogger().info("[DEBUG] Creating HikariDataSource...");
            dataSource = new HikariDataSource(hikariConfig);
            
            // Создание таблиц
            if (debugMode) plugin.getLogger().info("[DEBUG] Creating database tables...");
            createTables();
            if (debugMode) plugin.getLogger().info("[DEBUG] Database tables created successfully");
            
            plugin.getLogger().info("Database initialized successfully!");
        } catch (Exception e) {
            plugin.getLogger().severe("[CRITICAL] Database initialization failed: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Database initialization failed", e);
        }
    }
    
    /**
     * Настройка MySQL
     */
    private void setupMySQL(HikariConfig config, FileConfiguration pluginConfig) {
        String host = pluginConfig.getString("database.mysql.host", "localhost");
        int port = pluginConfig.getInt("database.mysql.port", 3306);
        String database = pluginConfig.getString("database.mysql.database", "openhousing");
        String username = pluginConfig.getString("database.mysql.username", "root");
        String password = pluginConfig.getString("database.mysql.password", "");
        boolean ssl = pluginConfig.getBoolean("database.mysql.ssl", false);
        
        config.setJdbcUrl(String.format("jdbc:mysql://%s:%d/%s?useSSL=%s&autoReconnect=true&useUnicode=true&characterEncoding=utf8",
            host, port, database, ssl));
        config.setUsername(username);
        config.setPassword(password);
        config.setDriverClassName("com.mysql.cj.jdbc.Driver");
    }
    
    /**
     * Настройка H2
     */
    private void setupH2(HikariConfig config, FileConfiguration pluginConfig) {
        String fileName = pluginConfig.getString("database.h2.file", "openhousing.db");
        String dbPath = plugin.getDataFolder().getAbsolutePath() + "/" + fileName;
        
        config.setJdbcUrl("jdbc:h2:" + dbPath + ";MODE=MySQL;DATABASE_TO_LOWER=TRUE");
        config.setDriverClassName("org.h2.Driver");
    }
    
    /**
     * Создание таблиц
     */
    private void createTables() {
        try (Connection connection = getConnection()) {
            // Таблица для кода игроков
            createCodeScriptsTable(connection);
            
            // Таблица для домов
            createHousesTable(connection);
            
            // Таблица для игр
            createGamesTable(connection);
            
            // Таблица для игроков
            createPlayersTable(connection);
            
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to create database tables!");
            e.printStackTrace();
        }
    }
    
    /**
     * Создание таблицы кода
     */
    private void createCodeScriptsTable(Connection connection) throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS code_scripts (" +
                    "player_id VARCHAR(36) PRIMARY KEY, " +
                    "player_name VARCHAR(16) NOT NULL, " +
                    "script_data TEXT, " +
                    "enabled BOOLEAN DEFAULT TRUE, " +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                    "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP" +
                    ")";
        
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.executeUpdate();
        }
    }
    
    /**
     * Создание таблицы домов
     */
    private void createHousesTable(Connection connection) throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS houses (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "owner_id VARCHAR(36) NOT NULL, " +
                    "owner_name VARCHAR(16) NOT NULL, " +
                    "name VARCHAR(32) NOT NULL, " +
                    "world_name VARCHAR(64) NOT NULL, " +
                    "x INT NOT NULL, " +
                    "y INT NOT NULL, " +
                    "z INT NOT NULL, " +
                    "width INT NOT NULL, " +
                    "height INT NOT NULL, " +
                    "length INT NOT NULL, " +
                    "public BOOLEAN DEFAULT FALSE, " +
                    "visitors_allowed BOOLEAN DEFAULT TRUE, " +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                    "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP" +
                    ")";
        
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.executeUpdate();
        }
    }
    
    /**
     * Создание таблицы игр
     */
    private void createGamesTable(Connection connection) throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS games (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "creator_id VARCHAR(36) NOT NULL, " +
                    "creator_name VARCHAR(16) NOT NULL, " +
                    "name VARCHAR(32) NOT NULL, " +
                    "type VARCHAR(16) NOT NULL, " +
                    "description TEXT, " +
                    "max_players INT DEFAULT 8, " +
                    "public BOOLEAN DEFAULT TRUE, " +
                    "active BOOLEAN DEFAULT TRUE, " +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                    "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP" +
                    ")";
        
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.executeUpdate();
        }
    }
    
    /**
     * Создание таблицы игроков
     */
    private void createPlayersTable(Connection connection) throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS players (" +
                    "id VARCHAR(36) PRIMARY KEY, " +
                    "name VARCHAR(16) NOT NULL, " +
                    "first_join TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                    "last_join TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                    "houses_created INT DEFAULT 0, " +
                    "games_created INT DEFAULT 0, " +
                    "scripts_executed INT DEFAULT 0" +
                    ")";
        
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.executeUpdate();
        }
    }
    
    /**
     * Получение соединения с базой данных
     */
    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }
    
    /**
     * Сохранение кода игрока
     */
    public void saveCodeScript(CodeScript script) {
        String sql = "INSERT INTO code_scripts (player_id, player_name, script_data, enabled) " +
                    "VALUES (?, ?, ?, ?) " +
                    "ON DUPLICATE KEY UPDATE " +
                    "player_name = VALUES(player_name), " +
                    "script_data = VALUES(script_data), " +
                    "enabled = VALUES(enabled), " +
                    "updated_at = CURRENT_TIMESTAMP";
        
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            
            statement.setString(1, script.getPlayerId().toString());
            statement.setString(2, script.getPlayerName());
            statement.setString(3, serializeScript(script));
            statement.setBoolean(4, script.isEnabled());
            
            statement.executeUpdate();
            
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to save code script for player: " + script.getPlayerName());
            e.printStackTrace();
        }
    }
    
    /**
     * Загрузка кода игрока
     */
    public CodeScript loadCodeScript(UUID playerId) {
        String sql = "SELECT * FROM code_scripts WHERE player_id = ?";
        
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            
            statement.setString(1, playerId.toString());
            
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    String playerName = resultSet.getString("player_name");
                    String scriptData = resultSet.getString("script_data");
                    boolean enabled = resultSet.getBoolean("enabled");
                    
                    CodeScript script = deserializeScript(playerId, playerName, scriptData);
                    if (script != null) {
                        script.setEnabled(enabled);
                    }
                    
                    return script;
                }
            }
            
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to load code script for player: " + playerId);
            e.printStackTrace();
        }
        
        return null;
    }
    
    /**
     * Асинхронная загрузка кода игрока
     */
    public void loadCodeScriptAsync(UUID playerId, java.util.function.Consumer<CodeScript> callback) {
        org.bukkit.Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            CodeScript script = loadCodeScript(playerId);
            // Возвращаем результат в основной поток
            org.bukkit.Bukkit.getScheduler().runTask(plugin, () -> callback.accept(script));
        });
    }
    
    /**
     * Асинхронное сохранение кода игрока
     */
    public void saveCodeScriptAsync(CodeScript script, java.lang.Runnable callback) {
        org.bukkit.Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            saveCodeScript(script);
            // Вызываем callback в основном потоке
            org.bukkit.Bukkit.getScheduler().runTask(plugin, callback);
        });
    }
    
    /**
     * Сохранение дома
     */
    public void saveHouse(ru.openhousing.housing.House house) {
        String sql = "INSERT INTO houses (id, owner_id, owner_name, name, world_name, x, y, z, width, height, length, public, visitors_allowed) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) " +
                    "ON DUPLICATE KEY UPDATE " +
                    "owner_name = VALUES(owner_name), " +
                    "name = VALUES(name), " +
                    "public = VALUES(public), " +
                    "visitors_allowed = VALUES(visitors_allowed), " +
                    "updated_at = CURRENT_TIMESTAMP";
        
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            
            statement.setInt(1, house.getId());
            statement.setString(2, house.getOwnerId().toString());
            statement.setString(3, house.getOwnerName());
            statement.setString(4, house.getName());
            statement.setString(5, house.getWorldName());
            statement.setInt(6, 0); // X координата (не используется для отдельных миров)
            statement.setInt(7, 65); // Y координата (стандартная высота спавна)
            statement.setInt(8, 0); // Z координата (не используется для отдельных миров)
            statement.setInt(9, house.getSize().getWidth());
            statement.setInt(10, house.getSize().getHeight());
            statement.setInt(11, house.getSize().getLength());
            statement.setBoolean(12, house.isPublic());
            statement.setBoolean(13, house.isVisitorsAllowed());
            
            statement.executeUpdate();
            
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to save house: " + house.getName());
            e.printStackTrace();
        }
    }
    
    /**
     * Асинхронное сохранение дома
     */
    public void saveHouseAsync(ru.openhousing.housing.House house, java.lang.Runnable callback) {
        org.bukkit.Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            saveHouse(house);
            // Вызываем callback в основном потоке
            org.bukkit.Bukkit.getScheduler().runTask(plugin, callback);
        });
    }
    
    /**
     * Загрузка всех домов
     */
    public List<ru.openhousing.housing.House> loadAllHouses() {
        List<ru.openhousing.housing.House> houses = new ArrayList<>();
        String sql = "SELECT * FROM houses";
        
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            
            while (resultSet.next()) {
                try {
                    int id = resultSet.getInt("id");
                    UUID ownerId = UUID.fromString(resultSet.getString("owner_id"));
                    String ownerName = resultSet.getString("owner_name");
                    String name = resultSet.getString("name");
                    String worldName = resultSet.getString("world_name");
                    int x = resultSet.getInt("x");
                    int y = resultSet.getInt("y");
                    int z = resultSet.getInt("z");
                    int width = resultSet.getInt("width");
                    int height = resultSet.getInt("height");
                    int length = resultSet.getInt("length");
                    boolean isPublic = resultSet.getBoolean("public");
                    boolean visitorsAllowed = resultSet.getBoolean("visitors_allowed");
                    
                    org.bukkit.World world = org.bukkit.Bukkit.getWorld(worldName);
                    if (world == null) {
                        plugin.getLogger().warning("World not found for house: " + name + " (world: " + worldName + ")");
                        continue;
                    }
                    
                    org.bukkit.Location location = new org.bukkit.Location(world, x, y, z);
                    ru.openhousing.housing.House.HouseSize size = new ru.openhousing.housing.House.HouseSize(width, height, length);
                    
                    ru.openhousing.housing.House house = new ru.openhousing.housing.House(id, ownerId, ownerName, name, worldName, size, plugin);
                    house.setPublic(isPublic);
                    house.setVisitorsAllowed(visitorsAllowed);
                    
                    houses.add(house);
                    
                } catch (Exception e) {
                    plugin.getLogger().warning("Failed to load house from database: " + e.getMessage());
                }
            }
            
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to load houses from database!");
            e.printStackTrace();
        }
        
        return houses;
    }
    
    /**
     * Асинхронная загрузка всех домов
     */
    public void loadAllHousesAsync(java.util.function.Consumer<java.util.List<ru.openhousing.housing.House>> callback) {
        org.bukkit.Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            java.util.List<ru.openhousing.housing.House> houses = loadAllHouses();
            // Возвращаем результат в основной поток
            org.bukkit.Bukkit.getScheduler().runTask(plugin, () -> callback.accept(houses));
        });
    }
    
    /**
     * Удаление дома
     */
    public void deleteHouse(int houseId) {
        String sql = "DELETE FROM houses WHERE id = ?";
        
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            
            statement.setInt(1, houseId);
            statement.executeUpdate();
            
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to delete house: " + houseId);
            e.printStackTrace();
        }
    }
    
    /**
     * Сериализация кода в JSON
     */
    private String serializeScript(CodeScript script) {
        return scriptSerializer.serialize(script);
    }
    
    /**
     * Десериализация кода из JSON
     */
    private CodeScript deserializeScript(UUID playerId, String playerName, String scriptData) {
        return scriptSerializer.deserialize(scriptData, playerId, playerName);
    }
    
    /**
     * Закрытие соединения с базой данных
     */
    public void closeConnection() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            plugin.getLogger().info("Database connection closed!");
        }
    }
}
