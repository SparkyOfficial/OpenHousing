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
        eventManager.initialize();
        plugin.getLogger().info("CodeManager initialized successfully!");
    }
    
    /**
     * Открытие редактора кода для игрока
     */
    public void openCodeEditor(Player player) {
        try {
            CodeScript script = getOrCreateScript(player);
            
            // Автоматически привязываем код к миру дома, если игрок находится в доме
            String currentWorld = player.getWorld().getName();
            if (currentWorld.startsWith("house_") && (script.getBoundWorld() == null || !script.getBoundWorld().equals(currentWorld))) {
                script.setBoundWorld(currentWorld);
                plugin.getLogger().info("Code bound to world: " + currentWorld + " for player: " + player.getName());
            }
            
            // Переиспользуем один экземпляр GUI на игрока, чтобы исключить спам переоткрытий
            CodeEditorGUI editor = openEditors.computeIfAbsent(player.getUniqueId(),
                id -> new CodeEditorGUI(plugin, player, script));
            editor.updateInventory();
            editor.open();
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
        return playerScripts.computeIfAbsent(player.getUniqueId(), 
            uuid -> new CodeScript(uuid, player.getName()));
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
        playerScripts.put(player.getUniqueId(), script);
        // Перерегистрация обработчиков событий через EventManager
        eventManager.unregisterPlayer(player);
        eventManager.registerPlayer(player, script);
        // Асинхронное сохранение в БД
        plugin.getDatabaseManager().saveCodeScriptAsync(script, () -> {});
    }
    
    /**
     * Загрузка скрипта игрока из базы данных
     */
    public void loadScript(Player player) {
        CodeScript script = plugin.getDatabaseManager().loadCodeScript(player.getUniqueId());
        if (script != null) {
            playerScripts.put(player.getUniqueId(), script);
            eventManager.registerPlayer(player, script);
        }
    }
    
    /**
     * Регистрация скрипта и его обработчиков событий
     */
    public void registerScript(Player player, CodeScript script) {
        playerScripts.put(player.getUniqueId(), script);
        eventManager.registerPlayer(player, script);
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
