package ru.openhousing;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import ru.openhousing.api.OpenHousingAPI;
import ru.openhousing.coding.CodeManager;
import ru.openhousing.commands.*;
import ru.openhousing.config.ConfigManager;
import ru.openhousing.database.DatabaseManager;
import ru.openhousing.games.GameManager;
import ru.openhousing.housing.HousingManager;
import ru.openhousing.listeners.*;
import ru.openhousing.placeholders.OpenHousingPlaceholders;
import ru.openhousing.utils.MessageUtil;

/**
 * OpenHousing - Hypixel Housing аналог с визуальным кодингом
 * 
 * @author YourName
 * @version 1.0.0
 */
public class OpenHousing extends JavaPlugin {
    
    private static OpenHousing instance;
    
    // Managers
    private ConfigManager configManager;
    private DatabaseManager databaseManager;
    private HousingManager housingManager;
    private CodeManager codeManager;
    private GameManager gameManager;
    
    // Economy
    private Economy economy;
    
    // API
    private OpenHousingAPI api;
    
    @Override
    public void onLoad() {
        instance = this;
        
        // Инициализация конфигурации
        configManager = new ConfigManager(this);
        configManager.loadConfigs();
        
        getLogger().info("OpenHousing plugin loaded successfully!");
    }
    
    @Override
    public void onEnable() {
        long startTime = System.currentTimeMillis();
        
        // Проверка зависимостей
        if (!checkDependencies()) {
            getLogger().severe("Missing required dependencies! Disabling plugin...");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        
        // Инициализация менеджеров
        initializeManagers();
        
        // Регистрация команд
        registerCommands();
        
        // Регистрация листенеров
        registerListeners();
        
        // Инициализация API
        api = new OpenHousingAPI(this);
        
        // PlaceholderAPI интеграция
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new OpenHousingPlaceholders(this).register();
            getLogger().info("PlaceholderAPI integration enabled!");
        }
        
        long loadTime = System.currentTimeMillis() - startTime;
        MessageUtil.sendConsole("&a[OpenHousing] &fPlugin enabled successfully in &e" + loadTime + "ms&f!");
        MessageUtil.sendConsole("&a[OpenHousing] &fVersion: &e" + getDescription().getVersion());
        MessageUtil.sendConsole("&a[OpenHousing] &fAuthor: &e" + String.join(", ", getDescription().getAuthors()));
    }
    
    @Override
    public void onDisable() {
        // Сохранение всех данных
        if (housingManager != null) {
            housingManager.saveAll();
        }
        
        if (gameManager != null) {
            gameManager.stopAllGames();
        }
        
        if (databaseManager != null) {
            databaseManager.closeConnection();
        }
        
        MessageUtil.sendConsole("&c[OpenHousing] &fPlugin disabled successfully!");
    }
    
    /**
     * Проверка зависимостей
     */
    private boolean checkDependencies() {
        // Проверка Vault (опциональная)
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            getLogger().info("Vault plugin not found! Economy features will be disabled.");
            economy = null;
        } else {
            // Настройка экономики
            if (!setupEconomy()) {
                getLogger().warning("Failed to setup economy! Economy features will be disabled.");
                economy = null;
            }
        }
        
        return true;
    }
    
    /**
     * Настройка экономики через Vault
     */
    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        
        economy = rsp.getProvider();
        return economy != null;
    }
    
    /**
     * Инициализация менеджеров
     */
    private void initializeManagers() {
        // База данных
        databaseManager = new DatabaseManager(this);
        databaseManager.initialize();
        
        // Менеджер домов
        housingManager = new HousingManager(this);
        housingManager.initialize();
        
        // Менеджер кода
        codeManager = new CodeManager(this);
        codeManager.initialize();
        
        // Менеджер игр
        gameManager = new GameManager(this);
        gameManager.initialize();
        
        getLogger().info("All managers initialized successfully!");
    }
    
    /**
     * Регистрация команд
     */
    private void registerCommands() {
        getCommand("housing").setExecutor(new HousingCommand(this));
        getCommand("code").setExecutor(new CodeCommand(this));
        getCommand("ohgame").setExecutor(new GameCommand(this));
        
        // Команды режимов дома
        HouseModeCommand modeCommand = new HouseModeCommand(this);
        getCommand("play").setExecutor(modeCommand);
        getCommand("build").setExecutor(modeCommand);
        
        getLogger().info("Commands registered successfully!");
    }
    
    /**
     * Регистрация листенеров
     */
    private void registerListeners() {
        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
        getServer().getPluginManager().registerEvents(new HousingListener(this), this);
        getServer().getPluginManager().registerEvents(new CodeListener(this), this);
        getServer().getPluginManager().registerEvents(new GameListener(this), this);
        getServer().getPluginManager().registerEvents(new InventoryListener(this), this);
        
        getLogger().info("Listeners registered successfully!");
    }
    
    // Геттеры
    public static OpenHousing getInstance() {
        return instance;
    }
    
    public ConfigManager getConfigManager() {
        return configManager;
    }
    
    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }
    
    public HousingManager getHousingManager() {
        return housingManager;
    }
    
    public CodeManager getCodeManager() {
        return codeManager;
    }
    
    public GameManager getGameManager() {
        return gameManager;
    }
    
    public Economy getEconomy() {
        return economy;
    }
    
    public OpenHousingAPI getAPI() {
        return api;
    }
}
