package ru.openhousing.config;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import ru.openhousing.OpenHousing;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

/**
 * Менеджер конфигурации
 */
public class ConfigManager {
    
    private final OpenHousing plugin;
    private final Map<String, FileConfiguration> configs;
    private final Map<String, File> configFiles;
    
    public ConfigManager(OpenHousing plugin) {
        this.plugin = plugin;
        this.configs = new HashMap<>();
        this.configFiles = new HashMap<>();
    }
    
    /**
     * Загрузка всех конфигурационных файлов
     */
    public void loadConfigs() {
        loadConfig("config.yml");
        loadConfig("messages.yml");
        loadConfig("housing.yml");
        loadConfig("games.yml");
    }
    
    /**
     * Загрузка конкретного конфигурационного файла
     */
    public void loadConfig(String fileName) {
        File configFile = new File(plugin.getDataFolder(), fileName);
        
        // Создаем файл из ресурсов, если он не существует
        if (!configFile.exists()) {
            createConfigFromResource(fileName, configFile);
        }
        
        FileConfiguration config = YamlConfiguration.loadConfiguration(configFile);
        configs.put(fileName, config);
        configFiles.put(fileName, configFile);
        
        plugin.getLogger().info("Loaded config: " + fileName);
    }
    
    /**
     * Создание конфигурационного файла из ресурсов
     */
    private void createConfigFromResource(String fileName, File configFile) {
        try {
            configFile.getParentFile().mkdirs();
            
            InputStream resource = plugin.getResource(fileName);
            if (resource != null) {
                Files.copy(resource, configFile.toPath());
                resource.close();
            } else {
                // Создаем пустой файл, если ресурс не найден
                configFile.createNewFile();
            }
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to create config file: " + fileName);
            e.printStackTrace();
        }
    }
    
    /**
     * Получение конфигурации
     */
    public FileConfiguration getConfig(String fileName) {
        return configs.get(fileName);
    }
    
    /**
     * Получение основной конфигурации
     */
    public FileConfiguration getMainConfig() {
        return getConfig("config.yml");
    }
    
    /**
     * Получение конфигурации сообщений
     */
    public FileConfiguration getMessagesConfig() {
        return getConfig("messages.yml");
    }
    
    /**
     * Получение конфигурации housing
     */
    public FileConfiguration getHousingConfig() {
        return getConfig("housing.yml");
    }
    
    /**
     * Получение конфигурации игр
     */
    public FileConfiguration getGamesConfig() {
        return getConfig("games.yml");
    }
    
    /**
     * Сохранение конфигурации
     */
    public void saveConfig(String fileName) {
        FileConfiguration config = configs.get(fileName);
        File configFile = configFiles.get(fileName);
        
        if (config != null && configFile != null) {
            try {
                config.save(configFile);
            } catch (IOException e) {
                plugin.getLogger().severe("Failed to save config: " + fileName);
                e.printStackTrace();
            }
        }
    }
    
    /**
     * Сохранение всех конфигураций
     */
    public void saveAllConfigs() {
        for (String fileName : configs.keySet()) {
            saveConfig(fileName);
        }
    }
    
    /**
     * Перезагрузка конфигурации
     */
    public void reloadConfig(String fileName) {
        loadConfig(fileName);
    }
    
    /**
     * Перезагрузка всех конфигураций
     */
    public void reloadAllConfigs() {
        for (String fileName : configs.keySet()) {
            reloadConfig(fileName);
        }
    }
    
    /**
     * Получение значения из конфигурации с дефолтным значением
     */
    public <T> T getValue(String fileName, String path, T defaultValue) {
        FileConfiguration config = getConfig(fileName);
        if (config != null && config.contains(path)) {
            return (T) config.get(path);
        }
        return defaultValue;
    }
    
    /**
     * Установка значения в конфигурацию
     */
    public void setValue(String fileName, String path, Object value) {
        FileConfiguration config = getConfig(fileName);
        if (config != null) {
            config.set(path, value);
        }
    }
    
    /**
     * Получение строки из конфигурации сообщений
     */
    public String getMessage(String key) {
        return getMessage(key, null);
    }
    
    /**
     * Получение строки из конфигурации сообщений с дефолтным значением
     */
    public String getMessage(String key, String defaultValue) {
        FileConfiguration messages = getMessagesConfig();
        if (messages != null && messages.contains(key)) {
            return messages.getString(key, defaultValue);
        }
        return defaultValue != null ? defaultValue : "Сообщение не найдено: " + key;
    }
}
