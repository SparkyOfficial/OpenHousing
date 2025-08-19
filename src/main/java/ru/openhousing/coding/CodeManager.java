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
        CodeScript script = getOrCreateScript(player);
        
        // Если редактор уже открыт, переиспользуем его
        CodeEditorGUI editor = openEditors.get(player.getUniqueId());
        if (editor == null) {
            editor = new CodeEditorGUI(plugin, player, script);
            openEditors.put(player.getUniqueId(), editor);
        }
        
        editor.open();
    }
    
    /**
     * Закрытие редактора кода
     */
    public void closeCodeEditor(Player player) {
        CodeEditorGUI editor = openEditors.remove(player.getUniqueId());
        if (editor != null) {
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
        
        // Сохранение в базу данных
        plugin.getDatabaseManager().saveCodeScript(script);
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
            plugin.getDatabaseManager().saveCodeScript(script);
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
     * Получение EventManager для внешнего использования
     */
    public EventManager getEventManager() {
        return eventManager;
    }
}
