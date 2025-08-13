package ru.openhousing.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.bukkit.configuration.file.FileConfiguration;
import ru.openhousing.OpenHousing;
import ru.openhousing.coding.script.CodeScript;

import java.sql.*;
import java.util.UUID;

/**
 * Менеджер базы данных
 */
public class DatabaseManager {
    
    private final OpenHousing plugin;
    private HikariDataSource dataSource;
    
    public DatabaseManager(OpenHousing plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Инициализация базы данных
     */
    public void initialize() {
        FileConfiguration config = plugin.getConfigManager().getMainConfig();
        String dbType = config.getString("database.type", "h2");
        
        HikariConfig hikariConfig = new HikariConfig();
        
        if ("mysql".equalsIgnoreCase(dbType)) {
            setupMySQL(hikariConfig, config);
        } else {
            setupH2(hikariConfig, config);
        }
        
        // Настройки пула соединений
        hikariConfig.setMaximumPoolSize(config.getInt("database.pool.maximum-pool-size", 10));
        hikariConfig.setMinimumIdle(config.getInt("database.pool.minimum-idle", 2));
        hikariConfig.setConnectionTimeout(config.getLong("database.pool.connection-timeout", 30000));
        hikariConfig.setIdleTimeout(config.getLong("database.pool.idle-timeout", 600000));
        hikariConfig.setMaxLifetime(config.getLong("database.pool.max-lifetime", 1800000));
        
        dataSource = new HikariDataSource(hikariConfig);
        
        // Создание таблиц
        createTables();
        
        plugin.getLogger().info("Database initialized successfully!");
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
            // Таблица для скриптов кода
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
     * Создание таблицы скриптов
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
                    "world VARCHAR(32) NOT NULL, " +
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
     * Сохранение скрипта кода
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
     * Загрузка скрипта кода
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
     * Сериализация скрипта в JSON (упрощенная версия)
     */
    private String serializeScript(CodeScript script) {
        // Здесь должна быть реализация сериализации скрипта в JSON
        // Для упрощения возвращаем пустую строку
        return "{}";
    }
    
    /**
     * Десериализация скрипта из JSON (упрощенная версия)
     */
    private CodeScript deserializeScript(UUID playerId, String playerName, String scriptData) {
        // Здесь должна быть реализация десериализации скрипта из JSON
        // Для упрощения возвращаем новый пустой скрипт
        return new CodeScript(playerId, playerName);
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
