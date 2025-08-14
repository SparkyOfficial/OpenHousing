package ru.openhousing.coding;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import ru.openhousing.OpenHousing;
import ru.openhousing.coding.blocks.CodeBlock;
import ru.openhousing.coding.blocks.events.PlayerEventBlock;
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
    private final Map<Class<?>, List<PlayerEventBlock>> eventHandlers;
    
    public CodeManager(OpenHousing plugin) {
        this.plugin = plugin;
        this.playerScripts = new ConcurrentHashMap<>();
        this.openEditors = new ConcurrentHashMap<>();
        this.eventHandlers = new ConcurrentHashMap<>();
    }
    
    /**
     * Инициализация менеджера
     */
    public void initialize() {
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
        
        // Перерегистрация обработчиков событий
        unregisterEventHandlers(player.getUniqueId());
        registerEventHandlers(script);
        
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
            registerEventHandlers(script);
        }
    }
    
    /**
     * Регистрация скрипта и его обработчиков событий
     */
    public void registerScript(Player player, CodeScript script) {
        playerScripts.put(player.getUniqueId(), script);
        registerEventHandlers(script);
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
     * Обработка события из CodeListener
     */
    public void handleEvent(Object event, Player player) {
        CodeScript script = getScript(player);
        if (script == null) return;
        
        // Проходим по всем строкам скрипта
        for (CodeLine line : script.getLines()) {
            if (!line.isEnabled()) continue;
            
            // Проверяем первый блок строки - должен быть событием
            List<CodeBlock> blocks = line.getBlocks();
            if (blocks.isEmpty()) continue;
            
            CodeBlock firstBlock = blocks.get(0);
            
            // Проверяем, соответствует ли событие первому блоку
            if (firstBlock.matchesEvent(event)) {
                // Создаем контекст выполнения с данными события
                CodeBlock.ExecutionContext context = firstBlock.createContextFromEvent(event);
                if (context != null) {
                    // Выполняем всю строку
                    line.execute(context);
                }
            }
        }
    }
    

    
    /**
     * Регистрация обработчиков событий из скрипта
     */
    private void registerEventHandlers(CodeScript script) {
        for (CodeBlock block : script.getBlocks()) {
            if (block instanceof PlayerEventBlock) {
                PlayerEventBlock eventBlock = (PlayerEventBlock) block;
                
                // Регистрируем обработчик для каждого типа события
                registerEventHandler(eventBlock);
            }
        }
    }
    
    /**
     * Регистрация конкретного обработчика события
     */
    private void registerEventHandler(PlayerEventBlock eventBlock) {
        // Определяем класс события на основе типа блока
        Class<?>[] eventClasses = getEventClasses(eventBlock);
        
        for (Class<?> eventClass : eventClasses) {
            eventHandlers.computeIfAbsent(eventClass, k -> new ArrayList<>()).add(eventBlock);
        }
    }
    
    /**
     * Получение классов событий для блока
     */
    private Class<?>[] getEventClasses(PlayerEventBlock eventBlock) {
        Object eventTypeParam = eventBlock.getParameter("eventType");
        PlayerEventBlock.PlayerEventType eventType = null;
        
        if (eventTypeParam instanceof PlayerEventBlock.PlayerEventType) {
            eventType = (PlayerEventBlock.PlayerEventType) eventTypeParam;
        } else if (eventTypeParam instanceof String) {
            try {
                eventType = PlayerEventBlock.PlayerEventType.valueOf((String) eventTypeParam);
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Invalid event type: " + eventTypeParam);
                return new Class<?>[0];
            }
        }
        
        if (eventType == null) return new Class<?>[0];
        
        switch (eventType) {
            case JOIN:
                return new Class<?>[]{org.bukkit.event.player.PlayerJoinEvent.class};
            case QUIT:
                return new Class<?>[]{org.bukkit.event.player.PlayerQuitEvent.class};
            case CHAT:
                return new Class<?>[]{org.bukkit.event.player.AsyncPlayerChatEvent.class};
            case MOVE:
                return new Class<?>[]{org.bukkit.event.player.PlayerMoveEvent.class};
            case INTERACT:
                return new Class<?>[]{org.bukkit.event.player.PlayerInteractEvent.class};
            case INTERACT_ENTITY:
                return new Class<?>[]{org.bukkit.event.player.PlayerInteractEntityEvent.class};
            case DAMAGE:
                return new Class<?>[]{org.bukkit.event.entity.EntityDamageEvent.class};
            case DEATH:
                return new Class<?>[]{org.bukkit.event.entity.PlayerDeathEvent.class};
            case RESPAWN:
                return new Class<?>[]{org.bukkit.event.player.PlayerRespawnEvent.class};
            case DROP_ITEM:
                return new Class<?>[]{org.bukkit.event.player.PlayerDropItemEvent.class};
            case INVENTORY_CLICK:
                return new Class<?>[]{org.bukkit.event.inventory.InventoryClickEvent.class};
            case COMMAND:
                return new Class<?>[]{org.bukkit.event.player.PlayerCommandPreprocessEvent.class};
            case TELEPORT:
                return new Class<?>[]{org.bukkit.event.player.PlayerTeleportEvent.class};
            case WORLD_CHANGE:
                return new Class<?>[]{org.bukkit.event.player.PlayerChangedWorldEvent.class};
            case BREAK_BLOCK:
                return new Class<?>[]{org.bukkit.event.block.BlockBreakEvent.class};
            case PLACE_BLOCK:
                return new Class<?>[]{org.bukkit.event.block.BlockPlaceEvent.class};
            default:
                return new Class<?>[0];
        }
    }
    
    /**
     * Удаление обработчиков событий игрока
     */
    private void unregisterEventHandlers(UUID playerId) {
        for (List<PlayerEventBlock> handlers : eventHandlers.values()) {
            handlers.removeIf(handler -> {
                CodeScript script = playerScripts.get(playerId);
                return script != null && script.getBlocks().contains(handler);
            });
        }
    }
    
    /**
     * Обработка события Bukkit
     */
    public void handleEvent(Event event, Player player) {
        List<PlayerEventBlock> handlers = eventHandlers.get(event.getClass());
        if (handlers == null || handlers.isEmpty()) return;
        
        for (PlayerEventBlock handler : handlers) {
            // Проверяем, принадлежит ли обработчик этому игроку
            CodeScript script = getScript(player);
            if (script == null || !script.getBlocks().contains(handler)) continue;
            
            // Проверяем соответствие события
            if (handler.matchesEvent(event.getClass())) {
                // Создаем контекст и выполняем
                CodeBlock.ExecutionContext context = handler.createContextFromEvent(player, event);
                
                // Проверяем, является ли событие асинхронным
                if (event.isAsynchronous()) {
                    // Для асинхронных событий выполняем в основном потоке
                    org.bukkit.Bukkit.getScheduler().runTask(plugin, () -> {
                        try {
                            handler.execute(context);
                        } catch (Exception e) {
                            plugin.getLogger().severe("Error executing async event handler: " + e.getMessage());
                            e.printStackTrace();
                        }
                    });
                } else {
                    // Для синхронных событий выполняем сразу
                    try {
                        handler.execute(context);
                    } catch (Exception e) {
                        plugin.getLogger().severe("Error executing sync event handler: " + e.getMessage());
                        e.printStackTrace();
                    }
                }
            }
        }
    }
    
    /**
     * Очистка данных при отключении игрока
     */
    public void onPlayerQuit(Player player) {
        closeCodeEditor(player);
        unregisterEventHandlers(player.getUniqueId());
        
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
        // Очищаем обработчики событий
        eventHandlers.clear();
        
        // Перерегистрируем все скрипты
        for (CodeScript script : playerScripts.values()) {
            registerEventHandlers(script);
        }
        
        plugin.getLogger().info("All code scripts reloaded!");
    }
}
