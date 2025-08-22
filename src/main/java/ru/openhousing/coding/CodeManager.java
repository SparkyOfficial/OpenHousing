package ru.openhousing.coding;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import ru.openhousing.OpenHousing;
import ru.openhousing.coding.blocks.CodeBlock;
import ru.openhousing.coding.blocks.events.PlayerEventBlock;
import ru.openhousing.coding.blocks.events.EntityEventBlock;
import ru.openhousing.coding.events.EventManager;
import ru.openhousing.coding.gui.CodeEditorGUI;
import ru.openhousing.coding.script.CodeScript;
import ru.openhousing.coding.script.CodeLine;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Менеджер системы визуального кодинга
 */
public class CodeManager {
    
    private final OpenHousing plugin;
    private final Map<UUID, CodeScript> playerScripts;
    private final Map<UUID, CodeEditorGUI> openEditors;
    private final EventManager eventManager;
    
    public CodeManager(OpenHousing plugin) {
        this.plugin = plugin;
        this.playerScripts = new ConcurrentHashMap<>();
        this.openEditors = new ConcurrentHashMap<>();
        this.eventManager = new EventManager(plugin);
    }
    
    /**
     * Инициализация менеджера
     */
    public void initialize() {
        boolean debugMode = plugin.getConfigManager().getMainConfig().getBoolean("general.debug", false);
        
        try {
            if (debugMode) plugin.getLogger().info("[DEBUG] Initializing EventManager...");
            eventManager.initialize();
            if (debugMode) plugin.getLogger().info("[DEBUG] EventManager initialized successfully");
            
            if (debugMode) plugin.getLogger().info("[DEBUG] CodeManager playerScripts map size: " + playerScripts.size());
            if (debugMode) plugin.getLogger().info("[DEBUG] CodeManager openEditors map size: " + openEditors.size());
            
            plugin.getLogger().info("CodeManager initialized successfully!");
        } catch (Exception e) {
            plugin.getLogger().severe("[CRITICAL] CodeManager initialization failed: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
    
    /**
     * Открытие редактора кода для игрока
     */
    public void openCodeEditor(Player player) {
        boolean debugMode = plugin.getConfigManager().getMainConfig().getBoolean("general.debug", false);
        
        try {
            if (debugMode) plugin.getLogger().info("[DEBUG] Opening code editor for player: " + player.getName());
            
            CodeScript script = getOrCreateScript(player);
            if (script == null) {
                plugin.getLogger().severe("[ERROR] Failed to get or create script for player: " + player.getName());
                player.sendMessage("§cОшибка создания скрипта!");
                return;
            }
            
            if (debugMode) plugin.getLogger().info("[DEBUG] Script obtained for player: " + player.getName() + ", lines: " + script.getLines().size());
            
            // Автоматически привязываем код к миру дома, если игрок находится в доме
            String currentWorld = player.getWorld().getName();
            if (currentWorld.startsWith("house_") && (script.getBoundWorld() == null || !script.getBoundWorld().equals(currentWorld))) {
                script.setBoundWorld(currentWorld);
                if (debugMode) plugin.getLogger().info("[DEBUG] Code bound to world: " + currentWorld + " for player: " + player.getName());
            }
            
            // Переиспользуем один экземпляр GUI на игрока, чтобы исключить спам переоткрытий
            CodeEditorGUI editor = openEditors.computeIfAbsent(player.getUniqueId(),
                id -> {
                    if (debugMode) plugin.getLogger().info("[DEBUG] Creating new CodeEditorGUI for player: " + player.getName());
                    return new CodeEditorGUI(plugin, player, script);
                });
            
            if (debugMode) plugin.getLogger().info("[DEBUG] Updating inventory for CodeEditorGUI...");
            editor.updateInventory();
            
            if (debugMode) plugin.getLogger().info("[DEBUG] Opening CodeEditorGUI...");
            editor.open();
            
            // Показываем информацию о коде
            CodeScript.ScriptStats stats = script.getStats();
            plugin.getSoundEffects().showCodeScoreboard(player, script.getLines().size(), stats.getTotalBlocks(), stats.getFunctionCount(), script.isEnabled());
            
            plugin.getLogger().info("Code editor opened for player: " + player.getName());
        } catch (Exception e) {
            plugin.getLogger().severe("Error opening code editor for " + player.getName() + ": " + e.getMessage());
            e.printStackTrace();
            player.sendMessage("§cОшибка открытия редактора кода!");
        }
    }
    
    /**
     * Закрытие редактора кода
     */
    public void closeCodeEditor(Player player) {
        CodeEditorGUI editor = openEditors.remove(player.getUniqueId());
        if (editor != null) {
            // Автоматически сохраняем код при закрытии редактора
            CodeScript script = editor.getScript();
            if (script != null) {
                saveScript(player, script);
                player.sendMessage("§aКод автоматически сохранен!");
                plugin.getSoundEffects().playSave(player);
                plugin.getSoundEffects().showCodeSavedTitle(player);
            }
            editor.close();
        }
    }
    
    /**
     * Получение открытого редактора для игрока
     */
    public CodeEditorGUI getEditorGUI(Player player) {
        return openEditors.get(player.getUniqueId());
    }
    
    /**
     * Получение или создание скрипта игрока
     */
    public CodeScript getOrCreateScript(Player player) {
        boolean debugMode = plugin.getConfigManager().getMainConfig().getBoolean("general.debug", false);
        
        CodeScript script = playerScripts.computeIfAbsent(player.getUniqueId(), 
            uuid -> {
                if (debugMode) plugin.getLogger().info("[DEBUG] Creating new CodeScript for player: " + player.getName());
                CodeScript newScript = new CodeScript(uuid, player.getName());
                if (debugMode) plugin.getLogger().info("[DEBUG] New CodeScript created with " + newScript.getLines().size() + " lines");
                return newScript;
            });
        
        if (debugMode && script != null) {
            plugin.getLogger().info("[DEBUG] Retrieved script for player: " + player.getName() + ", lines: " + script.getLines().size());
        }
        
        return script;
    }
    
    /**
     * Получение скрипта игрока
     */
    public CodeScript getScript(Player player) {
        return playerScripts.get(player.getUniqueId());
    }
    
    /**
     * Сохранение скрипта игрока
     */
    public void saveScript(Player player, CodeScript script) {
        boolean debugMode = plugin.getConfigManager().getMainConfig().getBoolean("general.debug", false);
        
        if (player == null) {
            plugin.getLogger().severe("[ERROR] Cannot save script: player is null");
            return;
        }
        
        if (script == null) {
            plugin.getLogger().severe("[ERROR] Cannot save script: script is null for player " + player.getName());
            return;
        }
        
        if (debugMode) plugin.getLogger().info("[DEBUG] Saving script for player: " + player.getName());
        try {
            playerScripts.put(player.getUniqueId(), script);
            
            // Перерегистрация обработчиков событий через EventManager
            if (eventManager != null) {
                eventManager.unregisterPlayerScript(player.getUniqueId());
                eventManager.registerPlayerScript(player, script);
                if (debugMode) plugin.getLogger().info("[DEBUG] Script re-registered in EventManager");
            } else {
                plugin.getLogger().warning("[WARNING] EventManager is null during script save");
            }
            
            // Асинхронное сохранение в БД
            if (plugin.getDatabaseManager() != null) {
                plugin.getDatabaseManager().saveCodeScriptAsync(script, () -> {
                    if (debugMode) plugin.getLogger().info("[DEBUG] Script saved to database for player: " + player.getName());
                });
            } else {
                plugin.getLogger().warning("[WARNING] DatabaseManager is null during script save");
            }
        } catch (Exception e) {
            plugin.getLogger().severe("[ERROR] Failed to save script for player " + player.getName() + ": " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Загрузка скрипта игрока из базы данных
     */
    public void loadScript(Player player) {
        boolean debugMode = plugin.getConfigManager().getMainConfig().getBoolean("general.debug", false);
        
        if (player == null) {
            plugin.getLogger().severe("[ERROR] Cannot load script: player is null");
            return;
        }
        
        try {
            if (debugMode) plugin.getLogger().info("[DEBUG] Loading script for player: " + player.getName());
            
            CodeScript script = plugin.getDatabaseManager().loadCodeScript(player.getUniqueId());
            if (script != null) {
                playerScripts.put(player.getUniqueId(), script);
                if (eventManager != null) {
                    eventManager.registerPlayerScript(player, script);
                    if (debugMode) plugin.getLogger().info("[DEBUG] Script loaded and registered for player: " + player.getName());
                } else {
                    plugin.getLogger().warning("[WARNING] EventManager is null during script load");
                }
            } else {
                if (debugMode) plugin.getLogger().info("[DEBUG] No script found in database for player: " + player.getName());
            }
        } catch (Exception e) {
            plugin.getLogger().severe("[ERROR] Failed to load script for player " + player.getName() + ": " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Регистрация скрипта и его обработчиков событий
     */
    public void registerScript(Player player, CodeScript script) {
        boolean debugMode = plugin.getConfigManager().getMainConfig().getBoolean("general.debug", false);
        
        if (player == null || script == null) {
            plugin.getLogger().severe("[ERROR] Cannot register script: player or script is null");
            return;
        }
        
        try {
            playerScripts.put(player.getUniqueId(), script);
            if (eventManager != null) {
                eventManager.registerPlayerScript(player, script);
                if (debugMode) plugin.getLogger().info("[DEBUG] Script registered for player: " + player.getName());
            } else {
                plugin.getLogger().warning("[WARNING] EventManager is null during script registration");
            }
        } catch (Exception e) {
            plugin.getLogger().severe("[ERROR] Failed to register script for player " + player.getName() + ": " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Выполнение скрипта
     */
    public void executeScript(Player player) {
        CodeScript script = getScript(player);
        if (script != null) {
            script.execute(new CodeBlock.ExecutionContext(player));
        }
    }
    
    /**
     * Обработка события Bukkit через EventManager
     */
    public void handleEvent(Event event, Player player) {
        eventManager.handleEvent(event, player);
    }
    

    
    /**
     * Очистка данных при отключении игрока
     */
    public void onPlayerQuit(Player player) {
        closeCodeEditor(player);
        eventManager.unregisterPlayer(player);
        
        // Сохраняем скрипт перед удалением из памяти
        CodeScript script = playerScripts.get(player.getUniqueId());
        if (script != null) {
            plugin.getDatabaseManager().saveCodeScriptAsync(script, () -> {});
        }
        
        playerScripts.remove(player.getUniqueId());
    }
    
    /**
     * Получение всех скриптов
     */
    public Map<UUID, CodeScript> getAllScripts() {
        return new HashMap<>(playerScripts);
    }
    
    /**
     * Получение открытых редакторов
     */
    public Map<UUID, CodeEditorGUI> getOpenEditors() {
        return new HashMap<>(openEditors);
    }
    
    /**
     * Проверка, открыт ли редактор у игрока
     */
    public boolean hasOpenEditor(Player player) {
        return openEditors.containsKey(player.getUniqueId());
    }
    
    /**
     * Перезагрузка всех скриптов
     */
    public void reloadAll() {
        // Очищаем все регистрации событий
        eventManager.unregisterAll();
        
        // Перерегистрируем все скрипты через EventManager
        for (Map.Entry<UUID, CodeScript> entry : playerScripts.entrySet()) {
            Player player = plugin.getServer().getPlayer(entry.getKey());
            if (player != null && player.isOnline()) {
                eventManager.registerPlayer(player, entry.getValue());
            }
        }
        
        plugin.getLogger().info("All code scripts reloaded!");
    }
    
    /**
     * Сохранение всех кодов в базу данных
     */
    public void saveAllCodes() {
        for (Map.Entry<UUID, CodeScript> entry : playerScripts.entrySet()) {
            UUID playerId = entry.getKey();
            CodeScript script = entry.getValue();
            
            // Асинхронное сохранение в базу данных
            plugin.getDatabaseManager().saveCodeScriptAsync(script, () -> {});
        }
        plugin.getLogger().info("Saved " + playerScripts.size() + " code scripts to database");
    }
    
    /**
     * Получение EventManager для внешнего использования
     */
    public EventManager getEventManager() {
        return eventManager;
    }
}
