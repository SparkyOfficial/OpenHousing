package ru.openhousing.coding.events;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.*;
import org.bukkit.event.block.*;
import org.bukkit.event.inventory.*;
import ru.openhousing.OpenHousing;
import ru.openhousing.coding.blocks.CodeBlock;
import ru.openhousing.coding.blocks.events.EntityEventBlock;
import ru.openhousing.coding.blocks.events.PlayerEventBlock;
import ru.openhousing.coding.script.CodeScript;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Централизованный менеджер событий для системы визуального кодинга
 * Обеспечивает высокую производительность и гибкость
 */
public class EventManager implements Listener {
    
    private final OpenHousing plugin;
    
    // Кэш активных обработчиков событий по типам
    private final Map<Class<? extends Event>, List<EventHandlerInfo>> eventHandlers = new ConcurrentHashMap<>();
    
    // Кэш кода игроков для быстрого доступа
    private final Map<UUID, CodeScript> playerScripts = new ConcurrentHashMap<>();
    
    // Статистика производительности
    private final Map<String, Long> executionStats = new ConcurrentHashMap<>();
    
    public EventManager(OpenHousing plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Инициализация менеджера событий
     */
    public void initialize() {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        plugin.getLogger().info("EventManager initialized successfully!");
    }
    
    /**
     * Регистрация кода игрока в системе событий
     */
    public void registerPlayerScript(Player player, CodeScript script) {
        UUID playerId = player.getUniqueId();
        
        // Удаляем старые обработчики
        unregisterPlayerScript(playerId);
        
        // Кэшируем код
        playerScripts.put(playerId, script);
        
        // Регистрируем новые обработчики
        registerEventHandlers(playerId, script);
        
        plugin.getLogger().info("Зарегистрирован код игрока " + player.getName() + 
                               " с " + script.getAllBlocks().size() + " блоками");
    }
    
    /**
     * Отмена регистрации кода игрока
     */
    public void unregisterPlayerScript(UUID playerId) {
        playerScripts.remove(playerId);
        // Очистка обработчиков происходит автоматически при проверке
    }
    
    /**
     * Регистрация обработчиков событий для кода
     */
    private void registerEventHandlers(UUID playerId, CodeScript script) {
        List<CodeBlock> eventBlocks = script.getAllBlocks().stream()
            .filter(block -> block instanceof PlayerEventBlock || block instanceof EntityEventBlock)
            .toList();
            
        for (CodeBlock block : eventBlocks) {
            if (block instanceof PlayerEventBlock) {
                registerPlayerEventHandler(playerId, (PlayerEventBlock) block);
            } else if (block instanceof EntityEventBlock) {
                registerEntityEventHandler(playerId, (EntityEventBlock) block);
            }
        }
    }
    
    /**
     * Регистрация обработчика событий игрока
     */
    private void registerPlayerEventHandler(UUID playerId, PlayerEventBlock eventBlock) {
        Object eventTypeParam = eventBlock.getParameter("eventType");
        if (!(eventTypeParam instanceof PlayerEventBlock.PlayerEventType)) return;
        
        PlayerEventBlock.PlayerEventType eventType = (PlayerEventBlock.PlayerEventType) eventTypeParam;
        Class<? extends Event> eventClass = getEventClass(eventType);
        
        if (eventClass != null) {
            EventHandlerInfo handler = new EventHandlerInfo(eventBlock, null, playerId);
            eventHandlers.computeIfAbsent(eventClass, k -> new CopyOnWriteArrayList<>()).add(handler);
        }
    }
    
    /**
     * Регистрация обработчика событий существ
     */
    private void registerEntityEventHandler(UUID playerId, EntityEventBlock eventBlock) {
        Object eventTypeParam = eventBlock.getParameter("eventType");
        if (!(eventTypeParam instanceof EntityEventBlock.EntityEventType)) return;
        
        EntityEventBlock.EntityEventType eventType = (EntityEventBlock.EntityEventType) eventTypeParam;
        Class<? extends Event> eventClass = getEventClass(eventType);
        
        if (eventClass != null) {
            EventHandlerInfo handler = new EventHandlerInfo(eventBlock, null, playerId);
            eventHandlers.computeIfAbsent(eventClass, k -> new CopyOnWriteArrayList<>()).add(handler);
        }
    }
    
    // =================== ОБРАБОТЧИКИ СОБЫТИЙ ИГРОКОВ ===================
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        processEvent(event, PlayerJoinEvent.class);
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        processEvent(event, PlayerQuitEvent.class);
        // Очищаем кэш при выходе игрока
        unregisterPlayerScript(event.getPlayer().getUniqueId());
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        // Обрабатываем в основном потоке для безопасности
        Bukkit.getScheduler().runTask(plugin, () -> processEvent(event, AsyncPlayerChatEvent.class));
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerMove(PlayerMoveEvent event) {
        // Оптимизация: обрабатываем только значительные перемещения
        if (event.getFrom().distanceSquared(event.getTo()) > 0.01) {
            processEvent(event, PlayerMoveEvent.class);
        }
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerInteract(PlayerInteractEvent event) {
        processEvent(event, PlayerInteractEvent.class);
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        processEvent(event, PlayerInteractEntityEvent.class);
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player) {
            processEvent(event, EntityDamageEvent.class);
        }
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerDeath(PlayerDeathEvent event) {
        processEvent(event, PlayerDeathEvent.class);
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        processEvent(event, PlayerRespawnEvent.class);
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        processEvent(event, PlayerDropItemEvent.class);
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerPickupItem(EntityPickupItemEvent event) {
        if (event.getEntity() instanceof Player) {
            processEvent(event, EntityPickupItemEvent.class);
        }
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getWhoClicked() instanceof Player) {
            processEvent(event, InventoryClickEvent.class);
        }
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        processEvent(event, PlayerCommandPreprocessEvent.class);
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        processEvent(event, PlayerTeleportEvent.class);
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerChangeWorld(PlayerChangedWorldEvent event) {
        processEvent(event, PlayerChangedWorldEvent.class);
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerToggleSneak(PlayerToggleSneakEvent event) {
        processEvent(event, PlayerToggleSneakEvent.class);
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerBreakBlock(BlockBreakEvent event) {
        processEvent(event, BlockBreakEvent.class);
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerPlaceBlock(BlockPlaceEvent event) {
        processEvent(event, BlockPlaceEvent.class);
    }
    
    // =================== ОБРАБОТЧИКИ СОБЫТИЙ СУЩЕСТВ ===================
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onEntitySpawn(EntitySpawnEvent event) {
        processEvent(event, EntitySpawnEvent.class);
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onEntityDeath(EntityDeathEvent event) {
        processEvent(event, EntityDeathEvent.class);
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onEntityDamage(EntityDamageEvent event) {
        processEvent(event, EntityDamageEvent.class);
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onEntityTarget(EntityTargetEvent event) {
        processEvent(event, EntityTargetEvent.class);
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onEntityExplode(EntityExplodeEvent event) {
        processEvent(event, EntityExplodeEvent.class);
    }
    
    // =================== ОСНОВНАЯ ЛОГИКА ОБРАБОТКИ ===================
    
    /**
     * Центральная обработка событий
     */
    private void processEvent(Event event, Class<? extends Event> eventClass) {
        long startTime = System.nanoTime();
        
        List<EventHandlerInfo> handlers = eventHandlers.get(event.getClass());
        if (handlers == null || handlers.isEmpty()) {
            return;
        }
        
        // Удаляем неактивные обработчики
        handlers.removeIf(handler -> !playerScripts.containsKey(handler.getPlayerId()));
        
        // Обрабатываем событие для каждого подходящего обработчика
        for (EventHandlerInfo handler : handlers) {
            try {
                if (handler.getEventBlock().matchesEvent(event)) {
                    Player player = Bukkit.getPlayer(handler.getPlayerId());
                    if (player != null && player.isOnline()) {
                        executeEventHandler(player, handler.getEventBlock(), event);
                    }
                }
            } catch (Exception e) {
                plugin.getLogger().warning("Ошибка при обработке события " + eventClass.getSimpleName() + 
                                         " для игрока " + handler.getPlayerId() + ": " + e.getMessage());
            }
        }
        
        // Статистика производительности
        long executionTime = System.nanoTime() - startTime;
        executionStats.merge(eventClass.getSimpleName(), executionTime, Long::sum);
    }
    
    /**
     * Выполнение обработчика события
     */
    private void executeEventHandler(Player player, CodeBlock eventBlock, Event event) {
        try {
            // Фильтр по привязке мира: если скрипт привязан к конкретному миру дома
            CodeScript scriptForPlayer = playerScripts.get(player.getUniqueId());
            if (scriptForPlayer != null && scriptForPlayer.getBoundWorld() != null) {
                String currentWorld = player.getWorld().getName();
                if (!currentWorld.equalsIgnoreCase(scriptForPlayer.getBoundWorld())) {
                    return; // не запускаем код вне привязанного мира
                }
            }

            CodeBlock.ExecutionContext context = eventBlock.createContextFromEvent(event);
            context.setVariable("player", player);
            context.setVariable("event_type", event.getClass().getSimpleName());
            
            // Выполняем блок события
            eventBlock.execute(context);
            
        } catch (Exception e) {
            plugin.getLogger().warning("Ошибка выполнения кода события для игрока " + player.getName() + 
                                     ": " + e.getMessage());
        }
    }
    
    /**
     * Получение класса события по типу
     */
    private Class<? extends Event> getEventClass(PlayerEventBlock.PlayerEventType eventType) {
        return switch (eventType) {
            case JOIN -> PlayerJoinEvent.class;
            case QUIT -> PlayerQuitEvent.class;
            case CHAT -> AsyncPlayerChatEvent.class;
            case MOVE -> PlayerMoveEvent.class;
            case INTERACT -> PlayerInteractEvent.class;
            case INTERACT_ENTITY -> PlayerInteractEntityEvent.class;
            case DAMAGE -> EntityDamageEvent.class;
            case DEATH -> PlayerDeathEvent.class;
            case RESPAWN -> PlayerRespawnEvent.class;
            case DROP_ITEM -> PlayerDropItemEvent.class;
            case PICKUP_ITEM -> EntityPickupItemEvent.class;
            case INVENTORY_CLICK -> InventoryClickEvent.class;
            case COMMAND -> PlayerCommandPreprocessEvent.class;
            case TELEPORT -> PlayerTeleportEvent.class;
            case WORLD_CHANGE -> PlayerChangedWorldEvent.class;
            case SNEAK -> PlayerToggleSneakEvent.class;
            case BREAK_BLOCK -> BlockBreakEvent.class;
            case PLACE_BLOCK -> BlockPlaceEvent.class;
            default -> null;
        };
    }
    
    private Class<? extends Event> getEventClass(EntityEventBlock.EntityEventType eventType) {
        return switch (eventType) {
            case SPAWN -> EntitySpawnEvent.class;
            case DEATH -> EntityDeathEvent.class;
            case DAMAGE -> EntityDamageEvent.class;
            case TARGET -> EntityTargetEvent.class;
            case EXPLODE -> EntityExplodeEvent.class;
            case INTERACT -> PlayerInteractEntityEvent.class;
            default -> null;
        };
    }
    
    /**
     * Получение статистики производительности
     */
    public Map<String, Long> getPerformanceStats() {
        return new HashMap<>(executionStats);
    }
    
    /**
     * Регистрация игрока и его скрипта
     */
    public void registerPlayer(Player player, CodeScript script) {
        playerScripts.put(player.getUniqueId(), script);
        registerEventHandlers(player.getUniqueId(), script);
    }
    
    /**
     * Разрегистрация игрока
     */
    public void unregisterPlayer(Player player) {
        UUID playerId = player.getUniqueId();
        playerScripts.remove(playerId);
        
        // Удаляем все обработчики этого игрока
        for (List<EventHandlerInfo> handlers : eventHandlers.values()) {
            handlers.removeIf(handler -> handler.getPlayerId().equals(playerId));
        }
    }
    
    /**
     * Разрегистрация всех игроков
     */
    public void unregisterAll() {
        playerScripts.clear();
        eventHandlers.clear();
    }
    
    /**
     * Обработка события для конкретного игрока
     */
    public void handleEvent(Event event, Player player) {
        processEvent(event, event.getClass());
    }
    
    /**
     * Очистка статистики
     */
    public void clearStats() {
        executionStats.clear();
    }
    
    /**
     * Внутренний класс для хранения информации об обработчике события
     */
    public static class EventHandlerInfo {
        private final CodeBlock eventBlock;
        private final EventFilter filter;
        private final UUID playerId;
        
        public EventHandlerInfo(CodeBlock eventBlock, EventFilter filter, UUID playerId) {
            this.eventBlock = eventBlock;
            this.filter = filter;
            this.playerId = playerId;
        }
        
        public CodeBlock getEventBlock() { return eventBlock; }
        public EventFilter getFilter() { return filter; }
        public UUID getPlayerId() { return playerId; }
    }
}
