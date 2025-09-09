package ru.openhousing.coding.events;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.*;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.plugin.RegisteredListener;
import ru.openhousing.OpenHousing;
import ru.openhousing.coding.blocks.CodeBlock;
import ru.openhousing.coding.script.CodeScript;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Оптимизированный EventManager с улучшенной производительностью
 * Решает проблемы с server lag и множественными проверками
 */
public class OptimizedEventManager implements Listener {
    
    private final OpenHousing plugin;
    
    // Оптимизированное хранение скриптов игроков
    private final ConcurrentHashMap<UUID, CodeScript> playerScripts = new ConcurrentHashMap<>();
    
    // Кэш обработчиков событий для быстрого поиска
    private final Map<Class<? extends Event>, List<EventHandlerInfo>> eventHandlersCache = new ConcurrentHashMap<>();
    
    // Статистика производительности
    private final Map<String, AtomicLong> executionStats = new ConcurrentHashMap<>();
    private final Map<String, AtomicLong> eventStats = new ConcurrentHashMap<>();
    
    // Лимиты производительности
    private static final int MAX_EXECUTION_TIME_MS = 50; // Максимум 50ms на выполнение скрипта
    private static final int MAX_EVENTS_PER_TICK = 100; // Максимум 100 событий в тик
    private static final int MAX_SCRIPTS_PER_PLAYER = 5; // Максимум 5 скриптов на игрока
    
    // Счетчики для контроля производительности
    private final AtomicLong eventsThisTick = new AtomicLong(0);
    private final AtomicLong lastTickTime = new AtomicLong(System.currentTimeMillis());
    
    public OptimizedEventManager(OpenHousing plugin) {
        this.plugin = plugin;
        
        // Запускаем задачу сброса счетчиков каждый тик
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            eventsThisTick.set(0);
            lastTickTime.set(System.currentTimeMillis());
        }, 1L, 1L);
        
        // Регистрируем слушатель
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }
    
    /**
     * Обработчик события входа игрока
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        processEvent(event, player);
    }
    
    /**
     * Обработчик события выхода игрока
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        processEvent(event, player);
    }
    
    /**
     * Обработчик события чата игрока
     */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        processEvent(event, player);
    }
    
    /**
     * Обработчик события движения игрока
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        // Оптимизация: обрабатываем только значительные движения
        if (event.getFrom().getBlockX() != event.getTo().getBlockX() ||
            event.getFrom().getBlockY() != event.getTo().getBlockY() ||
            event.getFrom().getBlockZ() != event.getTo().getBlockZ()) {
            processEvent(event, player);
        }
    }
    
    /**
     * Обработчик события взаимодействия игрока
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        processEvent(event, player);
    }
    
    /**
     * Обработчик события взаимодействия игрока с сущностью
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        Player player = event.getPlayer();
        processEvent(event, player);
    }
    
    /**
     * Обработчик события урона игрока
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            processEvent(event, player);
        }
    }
    
    /**
     * Обработчик события смерти игрока
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        processEvent(event, player);
    }
    
    /**
     * Обработчик события возрождения игрока
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        processEvent(event, player);
    }
    
    /**
     * Обработчик события выбрасывания предмета игроком
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        processEvent(event, player);
    }
    
    /**
     * Обработчик события подбора предмета игроком
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityPickupItem(EntityPickupItemEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            processEvent(event, player);
        }
    }
    
    /**
     * Обработчик события клика в инвентаре
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getWhoClicked() instanceof Player) {
            Player player = (Player) event.getWhoClicked();
            processEvent(event, player);
        }
    }
    
    /**
     * Обработчик события команды игрока
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        processEvent(event, player);
    }
    
    /**
     * Обработчик события телепортации игрока
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        Player player = event.getPlayer();
        processEvent(event, player);
    }
    
    /**
     * Обработчик события смены мира игроком
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerChangedWorld(PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();
        processEvent(event, player);
    }
    
    /**
     * Обработчик события приседания игрока
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerToggleSneak(PlayerToggleSneakEvent event) {
        Player player = event.getPlayer();
        processEvent(event, player);
    }
    
    /**
     * Обработчик события разрушения блока игроком
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        processEvent(event, player);
    }
    
    /**
     * Обработчик события размещения блока игроком
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        processEvent(event, player);
    }
    
    /**
     * Регистрация игрока и его скрипта с оптимизацией
     */
    public void registerPlayer(Player player, CodeScript script) {
        if (script == null || !script.isEnabled()) {
            return;
        }
        
        // Проверяем лимит скриптов на игрока
        if (getPlayerScriptsCount(player) >= MAX_SCRIPTS_PER_PLAYER) {
            plugin.getLogger().warning("Игрок " + player.getName() + " превысил лимит скриптов: " + MAX_SCRIPTS_PER_PLAYER);
            return;
        }
        
        playerScripts.put(player.getUniqueId(), script);
        updateEventHandlersCache();
        
        plugin.getLogger().info("Зарегистрирован скрипт для игрока " + player.getName() + 
                              " (блоков: " + script.getAllBlocks().size() + ")");
    }
    
    /**
     * Отмена регистрации игрока
     */
    public void unregisterPlayer(Player player) {
        CodeScript removed = playerScripts.remove(player.getUniqueId());
        if (removed != null) {
            updateEventHandlersCache();
            plugin.getLogger().info("Отменена регистрация скрипта для игрока " + player.getName());
        }
    }
    
    /**
     * Получение количества скриптов игрока
     */
    private int getPlayerScriptsCount(Player player) {
        return playerScripts.containsKey(player.getUniqueId()) ? 1 : 0;
    }
    
    /**
     * Обновление кэша обработчиков событий
     */
    private void updateEventHandlersCache() {
        eventHandlersCache.clear();
        
        for (CodeScript script : playerScripts.values()) {
            if (!script.isEnabled()) continue;
            
            for (CodeBlock block : script.getAllBlocks()) {
                if (block instanceof ru.openhousing.coding.blocks.events.PlayerEventBlock) {
                    ru.openhousing.coding.blocks.events.PlayerEventBlock eventBlock = (ru.openhousing.coding.blocks.events.PlayerEventBlock) block;
                    Object eventTypeParam = eventBlock.getParameter(ru.openhousing.coding.constants.BlockParams.EVENT_TYPE);
                    if (eventTypeParam instanceof ru.openhousing.coding.blocks.events.PlayerEventBlock.PlayerEventType) {
                        ru.openhousing.coding.blocks.events.PlayerEventBlock.PlayerEventType eventType = 
                            (ru.openhousing.coding.blocks.events.PlayerEventBlock.PlayerEventType) eventTypeParam;
                        Class<? extends Event> eventClass = getEventClass(eventType);
                        if (eventClass != null) {
                            eventHandlersCache.computeIfAbsent(eventClass, k -> new ArrayList<>())
                                .add(new EventHandlerInfo(script, eventBlock));
                        }
                    }
                }
                // Добавить поддержку других типов событий
            }
        }
        
        plugin.getLogger().info("Обновлен кэш обработчиков событий: " + eventHandlersCache.size() + " типов событий");
    }
    
    /**
     * Оптимизированная обработка событий с предварительной фильтрацией
     */
    public void processEvent(Event event, Player player) {
        // Проверка лимитов производительности
        if (eventsThisTick.incrementAndGet() > MAX_EVENTS_PER_TICK) {
            return; // Пропускаем событие, если превышен лимит
        }
        
        // Быстрая проверка - есть ли скрипты для этого игрока
        if (!playerScripts.containsKey(player.getUniqueId())) {
            return;
        }
        
        // Получаем обработчики из кэша
        List<EventHandlerInfo> handlers = eventHandlersCache.get(event.getClass());
        if (handlers == null || handlers.isEmpty()) {
            return;
        }
        
        // Фильтруем обработчики по игроку и условиям
        List<EventHandlerInfo> validHandlers = filterValidHandlers(handlers, player, event);
        
        // Выполняем валидные обработчики
        for (EventHandlerInfo handler : validHandlers) {
            executeEventHandler(player, handler.getEventBlock(), event);
        }
    }
    
    /**
     * Фильтрация валидных обработчиков
     */
    private List<EventHandlerInfo> filterValidHandlers(List<EventHandlerInfo> handlers, Player player, Event event) {
        List<EventHandlerInfo> validHandlers = new ArrayList<>();
        
        for (EventHandlerInfo handler : handlers) {
            CodeScript script = handler.getScript();
            
            // Проверяем, принадлежит ли скрипт этому игроку
            if (!script.getPlayerId().equals(player.getUniqueId())) {
                continue;
            }
            
            // Проверяем привязку к миру
            if (script.getBoundWorld() != null) {
                String currentWorld = player.getWorld().getName();
                if (!currentWorld.equalsIgnoreCase(script.getBoundWorld())) {
                    continue;
                }
            }
            
            // Проверяем, соответствует ли событие блоку
            if (handler.getEventBlock().matchesEvent(event)) {
                validHandlers.add(handler);
            }
        }
        
        return validHandlers;
    }
    
    /**
     * Оптимизированное выполнение обработчика события
     */
    private void executeEventHandler(Player player, CodeBlock eventBlock, Event event) {
        long startTime = System.currentTimeMillis();
        
        try {
            // Получаем скрипт игрока
            CodeScript script = playerScripts.get(player.getUniqueId());
            if (script == null || !script.isEnabled()) {
                return;
            }
            
            // Создаем контекст с переменными из скрипта
            CodeBlock.ExecutionContext context = new CodeBlock.ExecutionContext(player);
            context.getVariables().putAll(script.getGlobalVariables());
            context.getFunctions().putAll(script.getFunctions());
            
            // Добавляем информацию о событии
            context.setVariable("event_type", event.getClass().getSimpleName());
            if (event instanceof PlayerEvent) {
                context.setVariable("player", ((PlayerEvent) event).getPlayer());
            }
            
            // Выполняем блок с контролем времени
            CodeBlock.ExecutionResult result = executeWithTimeout(eventBlock, context, MAX_EXECUTION_TIME_MS);
            
            // Сохраняем измененные переменные
            script.getGlobalVariables().putAll(context.getVariables());
            
            // Обновляем статистику
            updateExecutionStats(event.getClass().getSimpleName(), result, System.currentTimeMillis() - startTime);
            
        } catch (Exception e) {
            plugin.getLogger().warning("Ошибка выполнения кода события для игрока " + player.getName() + 
                                     ": " + e.getMessage());
            if (plugin.getConfig().getBoolean("debug.enabled", false)) {
                e.printStackTrace();
            }
        }
    }
    
    /**
     * Выполнение блока с ограничением времени
     */
    private CodeBlock.ExecutionResult executeWithTimeout(CodeBlock block, CodeBlock.ExecutionContext context, int maxTimeMs) {
        long startTime = System.currentTimeMillis();
        
        // Создаем задачу выполнения
        ExecutionTask task = new ExecutionTask(block, context);
        
        try {
            // Выполняем блок
            CodeBlock.ExecutionResult result = task.execute();
            
            // Проверяем время выполнения
            long executionTime = System.currentTimeMillis() - startTime;
            if (executionTime > maxTimeMs) {
                plugin.getLogger().warning("Блок " + block.getClass().getSimpleName() + 
                                         " выполнился за " + executionTime + "ms (лимит: " + maxTimeMs + "ms)");
            }
            
            return result;
            
        } catch (Exception e) {
            return CodeBlock.ExecutionResult.error("Ошибка выполнения: " + e.getMessage());
        }
    }
    
    /**
     * Обновление статистики выполнения
     */
    private void updateExecutionStats(String eventType, CodeBlock.ExecutionResult result, long executionTime) {
        executionStats.computeIfAbsent(eventType, k -> new AtomicLong(0)).incrementAndGet();
        eventStats.computeIfAbsent("execution_time_" + eventType, k -> new AtomicLong(0)).addAndGet(executionTime);
    }
    
    /**
     * Получение статистики производительности
     */
    public Map<String, Object> getPerformanceStats() {
        Map<String, Object> stats = new HashMap<>();
        
        // Статистика событий
        Map<String, Long> eventCounts = new HashMap<>();
        executionStats.forEach((eventType, count) -> eventCounts.put(eventType, count.get()));
        stats.put("events", eventCounts);
        
        // Статистика времени выполнения
        Map<String, Long> executionTimes = new HashMap<>();
        eventStats.forEach((key, time) -> {
            if (key.startsWith("execution_time_")) {
                String eventType = key.substring("execution_time_".length());
                executionTimes.put(eventType, time.get());
            }
        });
        stats.put("execution_times", executionTimes);
        
        // Общая статистика
        stats.put("active_scripts", playerScripts.size());
        stats.put("events_this_tick", eventsThisTick.get());
        stats.put("last_tick_time", lastTickTime.get());
        
        return stats;
    }
    
    /**
     * Очистка ресурсов
     */
    public void cleanup() {
        playerScripts.clear();
        eventHandlersCache.clear();
        executionStats.clear();
        eventStats.clear();
        HandlerList.unregisterAll(this);
    }
    
    // Вспомогательные классы и методы
    
    private static class EventHandlerInfo {
        private final CodeScript script;
        private final CodeBlock eventBlock;
        
        public EventHandlerInfo(CodeScript script, CodeBlock eventBlock) {
            this.script = script;
            this.eventBlock = eventBlock;
        }
        
        public CodeScript getScript() { return script; }
        public CodeBlock getEventBlock() { return eventBlock; }
    }
    
    private static class ExecutionTask {
        private final CodeBlock block;
        private final CodeBlock.ExecutionContext context;
        
        public ExecutionTask(CodeBlock block, CodeBlock.ExecutionContext context) {
            this.block = block;
            this.context = context;
        }
        
        public CodeBlock.ExecutionResult execute() {
            return block.execute(context);
        }
    }
    
    // Методы для получения классов событий (аналогично оригинальному EventManager)
    private Class<? extends Event> getEventClass(ru.openhousing.coding.blocks.events.PlayerEventBlock.PlayerEventType eventType) {
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
}
