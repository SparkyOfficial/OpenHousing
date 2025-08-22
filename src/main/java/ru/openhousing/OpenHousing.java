package ru.openhousing;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import ru.openhousing.api.OpenHousingAPI;
import ru.openhousing.commands.CodeCommand;
import ru.openhousing.commands.HubCommand;
import ru.openhousing.commands.HouseModeCommand;
import ru.openhousing.commands.HousingCommand;
import ru.openhousing.config.ConfigManager;
import ru.openhousing.database.DatabaseManager;
import ru.openhousing.coding.CodeManager;
import ru.openhousing.housing.House;
import ru.openhousing.housing.HousingManager;
import org.bukkit.World;
import ru.openhousing.listeners.*;
import ru.openhousing.coding.listeners.CodeListener;
import ru.openhousing.placeholders.OpenHousingPlaceholders;
import ru.openhousing.utils.MessageUtil;

/**
 * OpenHousing - Hypixel Housing аналог с кодингом в инвентаре
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
    private ru.openhousing.listeners.ChatListener chatListener;
    
    // Economy
    private Economy economy;
    private ru.openhousing.economy.EconomyManager economyManager;
    
    // Integrations
    private ru.openhousing.integrations.WorldGuardIntegration worldGuardIntegration;
    private ru.openhousing.teleportation.TeleportationManager teleportationManager;
    private ru.openhousing.notifications.NotificationManager notificationManager;
    private ru.openhousing.utils.SoundEffects soundEffects;
    
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
    public void onDisable() {
        // Сохраняем все миры домов
        if (housingManager != null) {
            getLogger().info("Saving all house worlds...");
            for (House house : housingManager.getAllHouses()) {
                World world = house.getWorld();
                if (world != null) {
                    world.save();
                    getLogger().info("Saved world: " + world.getName());
                }
            }
        }
        
        // Сохраняем все активные коды
        if (codeManager != null) {
            getLogger().info("Saving all code blocks...");
            codeManager.saveAllCodes();
        }
        
        // Закрываем соединение с базой данных
        if (databaseManager != null) {
            databaseManager.closeConnection();
        }
        
        getLogger().info("OpenHousing disabled successfully!");
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
        boolean debugMode = configManager.getMainConfig().getBoolean("general.debug", false);
        
        try {
            // База данных
            if (debugMode) getLogger().info("[DEBUG] Initializing DatabaseManager...");
            databaseManager = new DatabaseManager(this);
            databaseManager.initialize();
            if (debugMode) getLogger().info("[DEBUG] DatabaseManager initialized successfully");
            
            // Экономический менеджер
            if (debugMode) getLogger().info("[DEBUG] Initializing EconomyManager...");
            economyManager = new ru.openhousing.economy.EconomyManager(this);
            if (debugMode) getLogger().info("[DEBUG] EconomyManager initialized successfully");
            
            // WorldGuard интеграция
            if (debugMode) getLogger().info("[DEBUG] Initializing WorldGuardIntegration...");
            worldGuardIntegration = new ru.openhousing.integrations.WorldGuardIntegration(this);
            if (debugMode) getLogger().info("[DEBUG] WorldGuardIntegration initialized successfully");
            
            // Менеджер телепортации
            if (debugMode) getLogger().info("[DEBUG] Initializing TeleportationManager...");
            teleportationManager = new ru.openhousing.teleportation.TeleportationManager(this);
            if (debugMode) getLogger().info("[DEBUG] TeleportationManager initialized successfully");
            
            // Менеджер уведомлений
            if (debugMode) getLogger().info("[DEBUG] Initializing NotificationManager...");
            notificationManager = new ru.openhousing.notifications.NotificationManager(this);
            if (debugMode) getLogger().info("[DEBUG] NotificationManager initialized successfully");
            
            // Звуковые эффекты
            if (debugMode) getLogger().info("[DEBUG] Initializing SoundEffects...");
            soundEffects = new ru.openhousing.utils.SoundEffects(this);
            if (debugMode) getLogger().info("[DEBUG] SoundEffects initialized successfully");
            
            // Менеджер домов
            if (debugMode) getLogger().info("[DEBUG] Initializing HousingManager...");
            housingManager = new HousingManager(this);
            housingManager.initialize();
            if (debugMode) getLogger().info("[DEBUG] HousingManager initialized successfully");
            
            // Загружаем миры домов с задержкой для стабильности
            getServer().getScheduler().runTaskLater(this, () -> {
                if (debugMode) getLogger().info("[DEBUG] Starting delayed world loading...");
                loadHouseWorlds();
            }, 100L); // 5 секунд задержки
            
            // Менеджер кода
            if (debugMode) getLogger().info("[DEBUG] Initializing CodeManager...");
            codeManager = new CodeManager(this);
            codeManager.initialize();
            if (debugMode) getLogger().info("[DEBUG] CodeManager initialized successfully");
            
            // Автосохранение кода каждые 5 минут (асинхронно)
            getServer().getScheduler().runTaskTimerAsynchronously(this, () -> {
                try {
                    if (debugMode) getLogger().info("[DEBUG] Auto-saving all codes...");
                    codeManager.saveAllCodes();
                    if (debugMode) getLogger().info("[DEBUG] Auto-save completed");
                } catch (Exception e) {
                    getLogger().severe("[ERROR] Auto-save failed: " + e.getMessage());
                    if (debugMode) e.printStackTrace();
                }
            }, 20L * 60 * 5, 20L * 60 * 5);
            
            getLogger().info("All managers initialized successfully!");
            
        } catch (Exception e) {
            getLogger().severe("[CRITICAL] Failed to initialize managers: " + e.getMessage());
            e.printStackTrace();
            getServer().getPluginManager().disablePlugin(this);
        }
    }
    
    /**
     * Регистрация команд
     */
    private void registerCommands() {
        getCommand("housing").setExecutor(new HousingCommand(this));
        getCommand("code").setExecutor(new CodeCommand(this));
        getCommand("hub").setExecutor(new HubCommand(this));
        
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
        // Слушатели событий
        getServer().getPluginManager().registerEvents(new ru.openhousing.listeners.PlayerListener(this), this);
        // ReActionsBlocker удален - он блокировал наши собственные GUI
        getServer().getPluginManager().registerEvents(new ru.openhousing.listeners.BlockListener(this), this);
        getServer().getPluginManager().registerEvents(new ru.openhousing.listeners.InventoryListener(this), this);
        getServer().getPluginManager().registerEvents(new ru.openhousing.listeners.HouseMovementListener(this), this);
        // HouseSettingsGUI регистрируется динамически при создании
        chatListener = new ru.openhousing.listeners.ChatListener(this);
        getServer().getPluginManager().registerEvents(chatListener, this);
        
        // Регистрация слушателя взаимодействия с переменными
        getServer().getPluginManager().registerEvents(new ru.openhousing.coding.listeners.VariableInteractionListener(this), this);
        
        // Регистрация EventManager для обработки событий кода
        getServer().getPluginManager().registerEvents(codeManager.getEventManager(), this);
        
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
    
    public ru.openhousing.utils.SoundEffects getSoundEffects() {
        return soundEffects;
    }
    
    public ru.openhousing.listeners.ChatListener getChatListener() {
        return chatListener;
    }

    
    public Economy getEconomy() {
        return economy;
    }
    
    public ru.openhousing.economy.EconomyManager getEconomyManager() {
        return economyManager;
    }
    
    public ru.openhousing.integrations.WorldGuardIntegration getWorldGuardIntegration() {
        return worldGuardIntegration;
    }
    
    public ru.openhousing.teleportation.TeleportationManager getTeleportationManager() {
        return teleportationManager;
    }
    
    public ru.openhousing.notifications.NotificationManager getNotificationManager() {
        return notificationManager;
    }
    
    public OpenHousingAPI getAPI() {
        return api;
    }
    
    /**
     * Загрузка миров домов при старте сервера
     */
    private void loadHouseWorlds() {
        boolean debugMode = configManager.getMainConfig().getBoolean("general.debug", false);
        
        if (housingManager == null) {
            getLogger().warning("HousingManager is null, cannot load house worlds");
            return;
        }
        
        try {
            if (debugMode) getLogger().info("[DEBUG] Loading all house worlds...");
            
            // Получаем все дома и загружаем их миры
            for (House house : housingManager.getAllHouses()) {
                try {
                    World world = house.getWorld();
                    if (world != null) {
                        if (debugMode) getLogger().info("[DEBUG] Successfully loaded world for house: " + 
                            house.getName() + " (" + house.getWorldName() + ")");
                    } else {
                        getLogger().warning("Failed to load world for house: " + 
                            house.getName() + " (" + house.getWorldName() + ")");
                    }
                } catch (Exception e) {
                    getLogger().severe("Error loading world for house " + 
                        house.getName() + ": " + e.getMessage());
                    if (debugMode) e.printStackTrace();
                }
            }
            
            getLogger().info("House world loading completed");
            
        } catch (Exception e) {
            getLogger().severe("Error during house world loading: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
