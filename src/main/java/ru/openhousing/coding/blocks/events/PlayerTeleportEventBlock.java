package ru.openhousing.coding.blocks.events;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import ru.openhousing.OpenHousing;
import ru.openhousing.coding.blocks.BlockType;
import ru.openhousing.coding.blocks.CodeBlock;
import ru.openhousing.coding.blocks.CodeBlock.ExecutionContext;
import ru.openhousing.coding.blocks.CodeBlock.ExecutionResult;
import ru.openhousing.coding.constants.BlockParams;
import ru.openhousing.utils.MessageUtil;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import com.github.stefvanschie.inventoryframework.AnvilGUI;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemMeta;

/**
 * Специализированный блок для события телепортации игрока
 * Обрабатывает все виды телепортации с детальным контролем и эффектами
 * 
 * @author OpenHousing Team
 * @version 1.0.0
 */
public class PlayerTeleportEventBlock extends CodeBlock implements Listener {
    
    // Статические поля для глобального управления
    private static final Map<UUID, TeleportHistory> teleportHistory = new ConcurrentHashMap<>();
    private static final Map<UUID, Long> lastTeleportTime = new ConcurrentHashMap<>();
    private static final Map<String, Integer> teleportTypeStats = new ConcurrentHashMap<>();
    private static final Set<UUID> playersInTeleport = ConcurrentHashMap.newKeySet();
    private static final AtomicInteger globalTeleportCount = new AtomicInteger(0);
    
    // Настройки блока
    private boolean logTeleports = true;
    private boolean requirePermission = false;
    private String requiredPermission = "openhousing.teleport";
    private boolean cooldownEnabled = true;
    private long cooldownTime = 3000; // 3 секунды
    private boolean rateLimitEnabled = true;
    private int maxTeleportsPerMinute = 5;
    private boolean preventTeleportSpam = true;
    private boolean validateDestinations = true;
    private boolean preventUnsafeTeleports = true;
    private boolean requireConfirmation = false;
    private boolean showTeleportEffects = true;
    private boolean playTeleportSounds = true;
    private boolean notifyNearbyPlayers = true;
    private boolean trackTeleportHistory = true;
    private int maxHistorySize = 50;
    
    // Настройки безопасности
    private boolean checkWorldPermissions = true;
    private boolean preventCrossWorldTeleport = false;
    private boolean requireWorldPermission = false;
    private String worldPermissionPrefix = "openhousing.world.";
    private boolean preventTeleportToVoid = true;
    private boolean preventTeleportToUnsafe = true;
    private boolean checkBedrockLevel = true;
    private int minSafeY = 0;
    private int maxSafeY = 256;
    
    // Настройки эффектов
    private boolean spawnDepartureParticles = true;
    private boolean spawnArrivalParticles = true;
    private Particle departureParticle = Particle.PORTAL;
    private Particle arrivalParticle = Particle.PORTAL;
    private int particleCount = 50;
    private double particleOffset = 0.5;
    private boolean spawnDepartureSmoke = true;
    private boolean spawnArrivalSmoke = true;
    private boolean spawnDepartureFireworks = false;
    private boolean spawnArrivalFireworks = false;
    
    // Настройки звуков
    private Sound departureSound = Sound.ENTITY_ENDERMAN_TELEPORT;
    private Sound arrivalSound = Sound.ENTITY_ENDERMAN_TELEPORT;
    private float departureVolume = 1.0f;
    private float arrivalVolume = 1.0f;
    private float departurePitch = 1.0f;
    private float arrivalPitch = 1.0f;
    
    // Настройки уведомлений
    private boolean notifyPlayer = true;
    private boolean notifyAdmins = false;
    private String playerNotificationFormat = "§aТелепортация: §e{from} §7→ §e{to}";
    private String adminNotificationFormat = "§c[Teleport] §f{player} §7teleported from §e{from} §7to §e{to}";
    private boolean showCoordinates = false;
    private boolean showDistance = true;
    private boolean showWorldInfo = true;
    
    // Настройки логирования
    private boolean logToConsole = true;
    private boolean logToFile = false;
    private String logFormat = "[{timestamp}] {player} teleported from {from} to {to}";
    private boolean logCoordinates = true;
    private boolean logWorld = true;
    private boolean logDistance = true;
    private boolean logTeleportType = true;
    
    // Настройки статистики
    private boolean collectStats = true;
    private boolean trackPlayerStats = true;
    private boolean trackGlobalStats = true;
    private boolean exportStats = false;
    private String statsExportPath = "plugins/OpenHousing/teleport_stats/";
    
    // Настройки производительности
    private boolean asyncProcessing = true;
    private boolean cacheResults = true;
    private int cacheSize = 1000;
    private long cacheExpiry = 300000; // 5 минут
    
    // Внутренние кэши и состояния
    private final Map<String, CachedTeleport> teleportCache = new ConcurrentHashMap<>();
    private final Map<UUID, PlayerTeleportStats> playerStats = new ConcurrentHashMap<>();
    private final Map<String, GlobalTeleportStats> globalStats = new ConcurrentHashMap<>();
    private final Map<UUID, PendingTeleport> pendingTeleports = new ConcurrentHashMap<>();

    // Enhanced GUI settings
    private boolean safetyChecksEnabled = true;
    private boolean visualEffectsEnabled = true;
    private boolean soundEffectsEnabled = true;
    private long cooldownMs = 3000;
    private int rateLimitMax = 5;
    private long rateLimitWindowMs = 60000;
    private boolean historyTrackingEnabled = true;
    private List<String> allowedWorlds = new ArrayList<>();
    
    public PlayerTeleportEventBlock() {
        super(BlockType.PLAYER_TELEPORT);
        initializeDefaultSettings();
        registerListener();
    }
    
    /**
     * Инициализация настроек по умолчанию
     */
    private void initializeDefaultSettings() {
        // Базовые настройки безопасности
        minSafeY = 0;
        maxSafeY = 256;
        
        // Настройки эффектов по умолчанию
        departureParticle = Particle.PORTAL;
        arrivalParticle = Particle.PORTAL;
        particleCount = 50;
        particleOffset = 0.5;
        
        // Настройки звуков по умолчанию
        departureSound = Sound.ENTITY_ENDERMAN_TELEPORT;
        arrivalSound = Sound.ENTITY_ENDERMAN_TELEPORT;
        departureVolume = 1.0f;
        arrivalVolume = 1.0f;
        departurePitch = 1.0f;
        arrivalPitch = 1.0f;
        
        // Инициализация статистики
        teleportTypeStats.put("COMMAND", 0);
        teleportTypeStats.put("PLUGIN", 0);
        teleportTypeStats.put("PORTAL", 0);
        teleportTypeStats.put("ENDER_PEARL", 0);
        teleportTypeStats.put("CHORUS_FRUIT", 0);
        teleportTypeStats.put("RESPAWN", 0);
        teleportTypeStats.put("SPECTATE", 0);

        // Default enhanced settings
        safetyChecksEnabled = true;
        visualEffectsEnabled = true;
        soundEffectsEnabled = true;
        cooldownMs = 3000;
        rateLimitMax = 5;
        rateLimitWindowMs = 60000;
        historyTrackingEnabled = true;
        allowedWorlds.add("world"); // Default allowed world
    }
    
    /**
     * Регистрация листенера
     */
    private void registerListener() {
        try {
            OpenHousing plugin = OpenHousing.getInstance();
            if (plugin != null && plugin.isEnabled()) {
                Bukkit.getPluginManager().registerEvents(this, plugin);
            }
        } catch (Exception e) {
            System.err.println("Failed to register PlayerTeleportEventBlock listener: " + e.getMessage());
        }
    }
    
    /**
     * Обработка события телепортации
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        Player player = event.getPlayer();
        Location from = event.getFrom();
        Location to = event.getTo();
        PlayerTeleportEvent.TeleportCause cause = event.getCause();
        
        if (player == null || from == null || to == null) {
            return;
        }
        
        // Проверка базовых условий
        if (!shouldProcessTeleport(player, from, to, cause)) {
            return;
        }
        
        // Создание контекста выполнения
        ExecutionContext context = new ExecutionContext(player);
        context.setVariable("from", from);
        context.setVariable("to", to);
        context.setVariable("cause", cause.name());
        context.setVariable("timestamp", System.currentTimeMillis());
        context.setVariable("playerName", player.getName());
        context.setVariable("playerUUID", player.getUniqueId().toString());
        context.setVariable("fromWorld", from.getWorld().getName());
        context.setVariable("toWorld", to.getWorld().getName());
        context.setVariable("fromLocation", formatLocation(from));
        context.setVariable("toLocation", formatLocation(to));
        context.setVariable("distance", calculateDistance(from, to));
        context.setVariable("crossWorld", !from.getWorld().equals(to.getWorld()));
        
        // Выполнение блока
        ExecutionResult result = execute(context);
        
        // Обработка результата
        handleExecutionResult(event, result, context);
        
        // Обновление статистики
        updateStatistics(player, from, to, cause, result);
        
        // Логирование
        logTeleportExecution(player, from, to, cause, result, context);
        
        // Уведомления
        sendNotifications(player, from, to, cause, result, context);
        
        // Эффекты
        if (result != null && result.isSuccess()) {
            spawnTeleportEffects(player, from, to, cause);
        }
    }
    
    /**
     * Проверка, следует ли обрабатывать телепортацию
     */
    private boolean shouldProcessTeleport(Player player, Location from, Location to, PlayerTeleportEvent.TeleportCause cause) {
        // Проверка разрешений
        if (requirePermission && !player.hasPermission(requiredPermission)) {
            return false;
        }
        
        // Проверка кулдауна
        if (cooldownEnabled && isOnCooldown(player)) {
            return false;
        }
        
        // Проверка лимита телепортаций
        if (rateLimitEnabled && isRateLimited(player)) {
            return false;
        }
        
        // Проверка спама
        if (preventTeleportSpam && isSpamDetected(player)) {
            return false;
        }
        
        // Проверка безопасности назначения
        if (preventUnsafeTeleports && !isDestinationSafe(to)) {
            return false;
        }
        
        // Проверка разрешений на мир
        if (checkWorldPermissions && !canTeleportToWorld(player, to.getWorld())) {
            return false;
        }
        
        // Проверка кросс-мира
        if (preventCrossWorldTeleport && !from.getWorld().equals(to.getWorld())) {
            return false;
        }
        
        return true;
    }
    
    /**
     * Проверка кулдауна
     */
    private boolean isOnCooldown(Player player) {
        Long lastTime = lastTeleportTime.get(player.getUniqueId());
        if (lastTime == null) {
            return false;
        }
        
        long currentTime = System.currentTimeMillis();
        return (currentTime - lastTime) < cooldownTime;
    }
    
    /**
     * Проверка лимита телепортаций
     */
    private boolean isRateLimited(Player player) {
        PlayerTeleportStats stats = playerStats.get(player.getUniqueId());
        if (stats == null) {
            return false;
        }
        
        long currentTime = System.currentTimeMillis();
        long oneMinuteAgo = currentTime - 60000;
        
        // Удаляем старые телепортации
        stats.getRecentTeleports().removeIf(time -> time < oneMinuteAgo);
        
        return stats.getRecentTeleports().size() >= maxTeleportsPerMinute;
    }
    
    /**
     * Проверка спама
     */
    private boolean isSpamDetected(Player player) {
        PlayerTeleportStats stats = playerStats.get(player.getUniqueId());
        if (stats == null) {
            return false;
        }
        
        long currentTime = System.currentTimeMillis();
        long fiveSecondsAgo = currentTime - 5000;
        
        // Удаляем старые телепортации
        stats.getRecentTeleports().removeIf(time -> time < fiveSecondsAgo);
        
        // Проверяем количество телепортаций
        long recentCount = stats.getRecentTeleports().stream()
            .filter(time -> time >= fiveSecondsAgo)
            .count();
        
        return recentCount > 2; // Максимум 2 телепортации за 5 секунд
    }
    
    /**
     * Проверка безопасности назначения
     */
    private boolean isDestinationSafe(Location destination) {
        if (destination == null) {
            return false;
        }
        
        // Проверка Y координаты
        if (checkBedrockLevel) {
            if (destination.getY() < minSafeY || destination.getY() > maxSafeY) {
                return false;
            }
        }
        
        // Проверка на void
        if (preventTeleportToVoid) {
            if (destination.getY() < 0) {
                return false;
            }
        }
        
        // Проверка на небезопасные блоки
        if (preventTeleportToUnsafe) {
            Material blockType = destination.getBlock().getType();
            if (blockType == Material.LAVA || blockType == Material.FIRE || 
                blockType == Material.CAMPFIRE || blockType == Material.SOUL_FIRE) {
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * Проверка разрешений на мир
     */
    private boolean canTeleportToWorld(Player player, World world) {
        if (world == null) {
            return false;
        }
        
        if (!requireWorldPermission) {
            return true;
        }
        
        String permission = worldPermissionPrefix + world.getName();
        return player.hasPermission(permission);
    }
    
    /**
     * Форматирование локации
     */
    private String formatLocation(Location location) {
        if (location == null) {
            return "null";
        }
        
        return String.format("%.1f, %.1f, %.1f", 
            location.getX(), location.getY(), location.getZ());
    }
    
    /**
     * Вычисление расстояния
     */
    private double calculateDistance(Location from, Location to) {
        if (from == null || to == null) {
            return 0.0;
        }
        
        if (!from.getWorld().equals(to.getWorld())) {
            return -1.0; // Разные миры
        }
        
        return from.distance(to);
    }
    
    /**
     * Обработка результата выполнения
     */
    private void handleExecutionResult(PlayerTeleportEvent event, ExecutionResult result, ExecutionContext context) {
        if (result == null) {
            return;
        }
        
        if (!result.isSuccess()) {
            // Отменяем телепортацию при ошибке
            event.setCancelled(true);
            
            // Отправляем сообщение об ошибке
            if (notifyPlayer) {
                String message = "§cОшибка телепортации: " + result.getMessage();
                event.getPlayer().sendMessage(message);
            }
        } else {
            // Телепортация разрешена
            if (notifyPlayer) {
                String message = playerNotificationFormat
                    .replace("{from}", context.getVariable("fromLocation").toString())
                    .replace("{to}", context.getVariable("toLocation").toString());
                event.getPlayer().sendMessage(message);
            }
            
            // Обновляем время последней телепортации
            lastTeleportTime.put(event.getPlayer().getUniqueId(), System.currentTimeMillis());
        }
    }
    
    /**
     * Обновление статистики
     */
    private void updateStatistics(Player player, Location from, Location to, 
                                PlayerTeleportEvent.TeleportCause cause, ExecutionResult result) {
        if (!collectStats) {
            return;
        }
        
        String causeName = cause.name();
        
        // Обновление глобальной статистики
        globalStats.computeIfAbsent(causeName, k -> new GlobalTeleportStats())
            .incrementUsage();
        
        if (result != null && result.isSuccess()) {
            globalStats.get(causeName).incrementSuccess();
        } else {
            globalStats.get(causeName).incrementFailure();
        }
        
        // Обновление статистики игрока
        if (trackPlayerStats) {
            PlayerTeleportStats playerStats = this.playerStats.computeIfAbsent(
                player.getUniqueId(), k -> new PlayerTeleportStats());
            
            playerStats.addTeleport(causeName, System.currentTimeMillis());
            playerStats.setLastTeleportTime(System.currentTimeMillis());
        }
        
        // Обновление счетчика использования
        teleportTypeStats.merge(causeName, 1, Integer::sum);
        
        // Обновление глобального счетчика
        globalTeleportCount.incrementAndGet();
    }
    
    /**
     * Логирование телепортации
     */
    private void logTeleportExecution(Player player, Location from, Location to, 
                                    PlayerTeleportEvent.TeleportCause cause, ExecutionResult result, 
                                    ExecutionContext context) {
        if (!logTeleports) {
            return;
        }
        
        String logMessage = logFormat
            .replace("{timestamp}", new java.util.Date().toString())
            .replace("{player}", player.getName())
            .replace("{from}", context.getVariable("fromLocation").toString())
            .replace("{to}", context.getVariable("toLocation").toString())
            .replace("{result}", result != null ? (result.isSuccess() ? "SUCCESS" : "FAILURE") : "UNKNOWN")
            .replace("{world}", context.getVariable("toWorld").toString())
            .replace("{distance}", context.getVariable("distance").toString())
            .replace("{cause}", cause.name());
        
        // Логирование в консоль
        if (logToConsole) {
            if (result != null && !result.isSuccess()) {
                OpenHousing.getInstance().getLogger().warning(logMessage);
            } else {
                OpenHousing.getInstance().getLogger().info(logMessage);
            }
        }
        
        // Сохранение в историю
        if (trackTeleportHistory) {
            saveToHistory(player.getUniqueId(), from, to, cause, result, context);
        }
    }
    
    /**
     * Отправка уведомлений
     */
    private void sendNotifications(Player player, Location from, Location to, 
                                PlayerTeleportEvent.TeleportCause cause, ExecutionResult result, 
                                ExecutionContext context) {
        // Уведомление администраторов
        if (notifyAdmins) {
            String adminMessage = adminNotificationFormat
                .replace("{player}", player.getName())
                .replace("{from}", context.getVariable("fromLocation").toString())
                .replace("{to}", context.getVariable("toLocation").toString())
                .replace("{world}", context.getVariable("toWorld").toString())
                .replace("{distance}", context.getVariable("distance").toString());
            
            Bukkit.getOnlinePlayers().stream()
                .filter(p -> p.hasPermission("openhousing.admin.notify"))
                .forEach(p -> p.sendMessage(ChatColor.translateAlternateColorCodes('&', adminMessage)));
        }
        
        // Уведомление ближайших игроков
        if (notifyNearbyPlayers) {
            String nearbyMessage = "§7" + player.getName() + " §fтелепортировался";
            
            player.getNearbyEntities(30, 30, 30).stream()
                .filter(entity -> entity instanceof Player)
                .map(entity -> (Player) entity)
                .filter(p -> !p.equals(player))
                .forEach(p -> p.sendMessage(nearbyMessage));
        }
    }
    
    /**
     * Спавн эффектов телепортации
     */
    private void spawnTeleportEffects(Player player, Location from, Location to, 
                                    PlayerTeleportEvent.TeleportCause cause) {
        if (!showTeleportEffects) {
            return;
        }
        
        // Эффекты отбытия
        if (spawnDepartureParticles) {
            spawnParticles(from, departureParticle, particleCount, particleOffset);
        }
        
        if (spawnDepartureSmoke) {
            from.getWorld().spawnParticle(Particle.SMOKE, from, 20, 0.5, 0.5, 0.5, 0.1);
        }
        
        if (spawnDepartureFireworks) {
            // TODO: Реализовать фейерверки
        }
        
        // Звуки отбытия
        if (playTeleportSounds) {
            from.getWorld().playSound(from, departureSound, departureVolume, departurePitch);
        }
        
        // Эффекты прибытия
        if (spawnArrivalParticles) {
            spawnParticles(to, arrivalParticle, particleCount, particleOffset);
        }
        
        if (spawnArrivalSmoke) {
            to.getWorld().spawnParticle(Particle.SMOKE, to, 20, 0.5, 0.5, 0.5, 0.1);
        }
        
        if (spawnArrivalFireworks) {
            // TODO: Реализовать фейерверки
        }
        
        // Звуки прибытия
        if (playTeleportSounds) {
            to.getWorld().playSound(to, arrivalSound, arrivalVolume, arrivalPitch);
        }
    }
    
    /**
     * Спавн частиц
     */
    private void spawnParticles(Location location, Particle particle, int count, double offset) {
        if (location == null || location.getWorld() == null) {
            return;
        }
        
        location.getWorld().spawnParticle(particle, location, count, offset, offset, offset, 0.1);
    }
    
    /**
     * Сохранение в историю
     */
    private void saveToHistory(UUID playerId, Location from, Location to, 
                              PlayerTeleportEvent.TeleportCause cause, ExecutionResult result, 
                              ExecutionContext context) {
        TeleportRecord record = new TeleportRecord(
            playerId,
            from,
            to,
            cause,
            System.currentTimeMillis(),
            result != null ? result.isSuccess() : false,
            result != null ? result.getMessage() : null,
            context.getVariable("distance").toString(),
            context.getVariable("crossWorld").toString()
        );
        
        teleportHistory.computeIfAbsent(playerId, k -> new TeleportHistory())
            .addRecord(record);
        
        // Ограничение размера истории
        TeleportHistory history = teleportHistory.get(playerId);
        if (history.getRecords().size() > maxHistorySize) {
            history.getRecords().remove(0);
        }
    }
    
    @Override
    public ExecutionResult execute(ExecutionContext context) {
        try {
            // Асинхронная обработка
            if (asyncProcessing) {
                return executeAsync(context);
            } else {
                return executeSync(context);
            }
        } catch (Exception e) {
            return ExecutionResult.error("Ошибка выполнения блока телепортации: " + e.getMessage());
        }
    }
    
    /**
     * Синхронное выполнение
     */
    private ExecutionResult executeSync(ExecutionContext context) {
        // Проверка кэша
        if (cacheResults) {
            String cacheKey = context.getVariable("playerUUID").toString() + "_" + 
                            context.getVariable("cause").toString();
            CachedTeleport cached = teleportCache.get(cacheKey);
            if (cached != null && !cached.isExpired()) {
                return cached.getResult();
            }
        }
        
        // Выполнение логики
        ExecutionResult result = processTeleportLogic(context);
        
        // Кэширование результата
        if (cacheResults && result != null) {
            String cacheKey = context.getVariable("playerUUID").toString() + "_" + 
                            context.getVariable("cause").toString();
            teleportCache.put(cacheKey, new CachedTeleport(result, System.currentTimeMillis() + cacheExpiry));
        }
        
        return result;
    }
    
    /**
     * Асинхронное выполнение
     */
    private ExecutionResult executeAsync(ExecutionContext context) {
        // TODO: Реализовать асинхронное выполнение
        return executeSync(context);
    }
    
    /**
     * Основная логика обработки телепортации
     */
    private ExecutionResult processTeleportLogic(ExecutionContext context) {
        Player player = context.getPlayer();
        Location from = (Location) context.getVariable("from");
        Location to = (Location) context.getVariable("to");
        String cause = context.getVariable("cause").toString();
        
        if (player == null || from == null || to == null) {
            return ExecutionResult.error("Неверные параметры телепортации");
        }
        
        // Проверка специальных случаев
        if (cause.equals("COMMAND")) {
            return processCommandTeleport(context, from, to);
        } else if (cause.equals("PLUGIN")) {
            return processPluginTeleport(context, from, to);
        } else if (cause.equals("ENDER_PEARL")) {
            return processEnderPearlTeleport(context, from, to);
        }
        
        // Стандартная обработка
        return ExecutionResult.success();
    }
    
    /**
     * Обработка телепортации по команде
     */
    private ExecutionResult processCommandTeleport(ExecutionContext context, Location from, Location to) {
        Player player = context.getPlayer();
        
        // Проверка разрешений на команду телепортации
        if (!player.hasPermission("openhousing.teleport.command")) {
            return ExecutionResult.error("Недостаточно прав для телепортации по команде");
        }
        
        // Проверка расстояния
        double distance = (Double) context.getVariable("distance");
        if (distance > 10000) { // Максимум 10к блоков
            return ExecutionResult.error("Слишком большое расстояние для телепортации");
        }
        
        return ExecutionResult.success();
    }
    
    /**
     * Обработка телепортации плагином
     */
    private ExecutionResult processPluginTeleport(ExecutionContext context, Location from, Location to) {
        Player player = context.getPlayer();
        
        // Проверка разрешений на плагин телепортацию
        if (!player.hasPermission("openhousing.teleport.plugin")) {
            return ExecutionResult.error("Недостаточно прав для телепортации плагином");
        }
        
        return ExecutionResult.success();
    }
    
    /**
     * Обработка телепортации эндер-перлом
     */
    private ExecutionResult processEnderPearlTeleport(ExecutionContext context, Location from, Location to) {
        Player player = context.getPlayer();
        
        // Проверка разрешений на эндер-перл телепортацию
        if (!player.hasPermission("openhousing.teleport.enderpearl")) {
            return ExecutionResult.error("Недостаточно прав для телепортации эндер-перлом");
        }
        
        // Проверка кулдауна эндер-перла
        if (isOnCooldown(player)) {
            return ExecutionResult.error("Эндер-перл на кулдауне");
        }
        
        return ExecutionResult.success();
    }
    
    @Override
    public boolean validate() {
        // Проверяем базовые параметры
        if (cooldownTime < 0) return false;
        if (maxTeleportsPerMinute < 1) return false;
        if (maxHistorySize < 1) return false;
        if (cacheSize < 1) return false;
        if (cacheExpiry < 0) return false;
        if (minSafeY < 0) return false;
        if (maxSafeY < minSafeY) return false;
        if (particleCount < 1) return false;
        if (particleOffset < 0) return false;
        
        return true;
    }
    
    @Override
    public String getDescription() {
        return String.format("Player Teleport Event Block\n" +
                "Settings:\n" +
                "- Safety checks: %s\n" +
                "- Visual effects: %s\n" +
                "- Sound effects: %s\n" +
                "- Cooldown: %dms\n" +
                "- Rate limit: %d teleports per %dms\n" +
                "- History tracking: %s",
                safetyChecksEnabled ? "Enabled" : "Disabled",
                visualEffectsEnabled ? "Enabled" : "Disabled",
                soundEffectsEnabled ? "Enabled" : "Disabled",
                cooldownMs,
                rateLimitMax,
                rateLimitWindowMs,
                historyTrackingEnabled ? "Enabled" : "Disabled");
    }

    /**
     * Opens an enhanced configuration GUI for this block
     */
    public void openConfigurationGUI(Player player) {
        new AnvilGUI.Builder()
                .onComplete((player1, text) -> {
                    if (text == null || text.trim().isEmpty()) {
                        return AnvilGUI.Response.close();
                    }
                    
                    // Parse configuration from text
                    parseConfiguration(text);
                    player1.sendMessage("§aTeleport configuration updated!");
                    return AnvilGUI.Response.close();
                })
                .onClose(player1 -> player1.sendMessage("§cConfiguration cancelled"))
                .text("Configure teleport settings")
                .title("§6Teleport Block Config")
                .plugin(OpenHousing.getInstance())
                .open(player);
    }

    /**
     * Opens a detailed settings GUI with multiple pages
     */
    public void openDetailedSettingsGUI(Player player) {
        // Main settings menu
        Inventory menu = Bukkit.createInventory(null, 36, "§6Teleport Block Settings");
        
        // Safety checks toggle
        ItemStack safetyItem = new ItemStack(safetyChecksEnabled ? Material.SHIELD : Material.BARRIER);
        ItemMeta safetyMeta = safetyItem.getItemMeta();
        safetyMeta.setDisplayName("§eSafety Checks: " + (safetyChecksEnabled ? "§aON" : "§cOFF"));
        safetyMeta.setLore(Arrays.asList(
            "§7Click to toggle",
            "§7Prevent unsafe teleports",
            "§7Current: " + (safetyChecksEnabled ? "§aEnabled" : "§cDisabled")
        ));
        safetyItem.setItemMeta(safetyMeta);
        menu.setItem(10, safetyItem);
        
        // Visual effects toggle
        ItemStack visualItem = new ItemStack(visualEffectsEnabled ? Material.END_ROD : Material.BARRIER);
        ItemMeta visualMeta = visualItem.getItemMeta();
        visualMeta.setDisplayName("§eVisual Effects: " + (visualEffectsEnabled ? "§aON" : "§cOFF"));
        visualMeta.setLore(Arrays.asList(
            "§7Click to toggle",
            "§7Particles and smoke effects",
            "§7Current: " + (visualEffectsEnabled ? "§aEnabled" : "§cDisabled")
        ));
        visualItem.setItemMeta(visualMeta);
        menu.setItem(12, visualItem);
        
        // Sound effects toggle
        ItemStack soundItem = new ItemStack(soundEffectsEnabled ? Material.NOTE_BLOCK : Material.BARRIER);
        ItemMeta soundMeta = soundItem.getItemMeta();
        soundMeta.setDisplayName("§eSound Effects: " + (soundEffectsEnabled ? "§aON" : "§cOFF"));
        soundMeta.setLore(Arrays.asList(
            "§7Click to toggle",
            "§7Teleport sound effects",
            "§7Current: " + (soundEffectsEnabled ? "§aEnabled" : "§cDisabled")
        ));
        soundItem.setItemMeta(soundMeta);
        menu.setItem(14, soundItem);
        
        // Cooldown settings
        ItemStack cooldownItem = new ItemStack(Material.CLOCK);
        ItemMeta cooldownMeta = cooldownItem.getItemMeta();
        cooldownMeta.setDisplayName("§eCooldown: " + cooldownMs + "ms");
        cooldownMeta.setLore(Arrays.asList(
            "§7Click to change",
            "§7Delay between teleports",
            "§7Current: " + cooldownMs + "ms"
        ));
        cooldownItem.setItemMeta(cooldownMeta);
        menu.setItem(16, cooldownItem);
        
        // Rate limiting
        ItemStack rateLimitItem = new ItemStack(Material.HOPPER);
        ItemMeta rateLimitMeta = rateLimitItem.getItemMeta();
        rateLimitMeta.setDisplayName("§eRate Limit: " + rateLimitMax + "/" + rateLimitWindowMs + "ms");
        rateLimitMeta.setLore(Arrays.asList(
            "§7Click to configure",
            "§7Max teleports per time window",
            "§7Current: " + rateLimitMax + " per " + rateLimitWindowMs + "ms"
        ));
        rateLimitItem.setItemMeta(rateLimitMeta);
        menu.setItem(19, rateLimitItem);
        
        // History tracking
        ItemStack historyItem = new ItemStack(historyTrackingEnabled ? Material.BOOK : Material.BARRIER);
        ItemMeta historyMeta = historyItem.getItemMeta();
        historyMeta.setDisplayName("§eHistory Tracking: " + (historyTrackingEnabled ? "§aON" : "§cOFF"));
        historyMeta.setLore(Arrays.asList(
            "§7Click to toggle",
            "§7Track teleport history",
            "§7Current: " + (historyTrackingEnabled ? "§aEnabled" : "§cDisabled")
        ));
        historyItem.setItemMeta(historyMeta);
        menu.setItem(21, historyItem);
        
        // World permissions
        ItemStack worldItem = new ItemStack(Material.GRASS_BLOCK);
        ItemMeta worldMeta = worldItem.getItemMeta();
        worldMeta.setDisplayName("§eWorld Permissions: " + allowedWorlds.size());
        worldMeta.setLore(Arrays.asList(
            "§7Click to edit",
            "§7Configure allowed worlds",
            "§7Current: " + String.join(", ", allowedWorlds)
        ));
        worldItem.setItemMeta(worldMeta);
        menu.setItem(23, worldItem);
        
        // Close button
        ItemStack closeItem = new ItemStack(Material.BARRIER);
        ItemMeta closeMeta = closeItem.getItemMeta();
        closeMeta.setDisplayName("§cClose");
        closeItem.setItemMeta(closeMeta);
        menu.setItem(31, closeItem);
        
        player.openInventory(menu);
    }

    /**
     * Parse configuration from text input
     */
    private void parseConfiguration(String text) {
        // Simple configuration parser
        if (text.startsWith("safety:")) {
            safetyChecksEnabled = text.contains("true");
        } else if (text.startsWith("visual:")) {
            visualEffectsEnabled = text.contains("true");
        } else if (text.startsWith("sound:")) {
            soundEffectsEnabled = text.contains("true");
        } else if (text.startsWith("cooldown:")) {
            try {
                cooldownMs = Long.parseLong(text.split(":")[1]);
            } catch (Exception e) {
                // Ignore invalid input
            }
        } else if (text.startsWith("ratelimit:")) {
            try {
                String[] parts = text.split(":")[1].split("/");
                rateLimitMax = Integer.parseInt(parts[0]);
                rateLimitWindowMs = Long.parseLong(parts[1]);
            } catch (Exception e) {
                // Ignore invalid input
            }
        } else if (text.startsWith("history:")) {
            historyTrackingEnabled = text.contains("true");
        } else if (text.startsWith("worlds:")) {
            allowedWorlds.clear();
            String[] worldList = text.split(":")[1].split(",");
            for (String world : worldList) {
                allowedWorlds.add(world.trim());
            }
        }
    }
    
    // Геттеры и сеттеры для настройки блока
    public void setLogTeleports(boolean logTeleports) { this.logTeleports = logTeleports; }
    public void setRequirePermission(boolean requirePermission) { this.requirePermission = requirePermission; }
    public void setRequiredPermission(String requiredPermission) { this.requiredPermission = requiredPermission; }
    public void setCooldownEnabled(boolean cooldownEnabled) { this.cooldownEnabled = cooldownEnabled; }
    public void setCooldownTime(long cooldownTime) { this.cooldownTime = cooldownTime; }
    public void setRateLimitEnabled(boolean rateLimitEnabled) { this.rateLimitEnabled = rateLimitEnabled; }
    public void setMaxTeleportsPerMinute(int maxTeleportsPerMinute) { this.maxTeleportsPerMinute = maxTeleportsPerMinute; }
    public void setPreventTeleportSpam(boolean preventTeleportSpam) { this.preventTeleportSpam = preventTeleportSpam; }
    public void setValidateDestinations(boolean validateDestinations) { this.validateDestinations = validateDestinations; }
    public void setPreventUnsafeTeleports(boolean preventUnsafeTeleports) { this.preventUnsafeTeleports = preventUnsafeTeleports; }
    public void setRequireConfirmation(boolean requireConfirmation) { this.requireConfirmation = requireConfirmation; }
    public void setShowTeleportEffects(boolean showTeleportEffects) { this.showTeleportEffects = showTeleportEffects; }
    public void setPlayTeleportSounds(boolean playTeleportSounds) { this.playTeleportSounds = playTeleportSounds; }
    public void setNotifyNearbyPlayers(boolean notifyNearbyPlayers) { this.notifyNearbyPlayers = notifyNearbyPlayers; }
    public void setTrackTeleportHistory(boolean trackTeleportHistory) { this.trackTeleportHistory = trackTeleportHistory; }
    public void setMaxHistorySize(int maxHistorySize) { this.maxHistorySize = maxHistorySize; }
    
    // Внутренние классы для статистики и кэширования
    private static class TeleportRecord {
        private final UUID playerId;
        private final Location from;
        private final Location to;
        private final PlayerTeleportEvent.TeleportCause cause;
        private final long timestamp;
        private final boolean success;
        private final String message;
        private final String distance;
        private final String crossWorld;
        
        public TeleportRecord(UUID playerId, Location from, Location to, 
                            PlayerTeleportEvent.TeleportCause cause, long timestamp, 
                            boolean success, String message, String distance, String crossWorld) {
            this.playerId = playerId;
            this.from = from;
            this.to = to;
            this.cause = cause;
            this.timestamp = timestamp;
            this.success = success;
            this.message = message;
            this.distance = distance;
            this.crossWorld = crossWorld;
        }
        
        // Геттеры
        public UUID getPlayerId() { return playerId; }
        public Location getFrom() { return from; }
        public Location getTo() { return to; }
        public PlayerTeleportEvent.TeleportCause getCause() { return cause; }
        public long getTimestamp() { return timestamp; }
        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
        public String getDistance() { return distance; }
        public String getCrossWorld() { return crossWorld; }
    }
    
    private static class TeleportHistory {
        private final List<TeleportRecord> records = new ArrayList<>();
        
        public void addRecord(TeleportRecord record) {
            records.add(record);
        }
        
        public List<TeleportRecord> getRecords() { return records; }
    }
    
    private static class CachedTeleport {
        private final ExecutionResult result;
        private final long expiryTime;
        
        public CachedTeleport(ExecutionResult result, long expiryTime) {
            this.result = result;
            this.expiryTime = expiryTime;
        }
        
        public ExecutionResult getResult() { return result; }
        public boolean isExpired() { return System.currentTimeMillis() > expiryTime; }
    }
    
    private static class PlayerTeleportStats {
        private final List<Long> recentTeleports = new ArrayList<>();
        private long lastTeleportTime = 0;
        
        public void addTeleport(String cause, long timestamp) {
            recentTeleports.add(timestamp);
        }
        
        public void setLastTeleportTime(long time) { this.lastTeleportTime = time; }
        public long getLastTeleportTime() { return lastTeleportTime; }
        public List<Long> getRecentTeleports() { return recentTeleports; }
    }
    
    private static class GlobalTeleportStats {
        private int usageCount = 0;
        private int successCount = 0;
        private int failureCount = 0;
        
        public void incrementUsage() { usageCount++; }
        public void incrementSuccess() { successCount++; }
        public void incrementFailure() { failureCount++; }
        
        public int getUsageCount() { return usageCount; }
        public int getSuccessCount() { return successCount; }
        public int getFailureCount() { return failureCount; }
    }
    
    private static class PendingTeleport {
        private final Location destination;
        private final long requestTime;
        private final boolean confirmed;
        
        public PendingTeleport(Location destination, long requestTime, boolean confirmed) {
            this.destination = destination;
            this.requestTime = requestTime;
            this.confirmed = confirmed;
        }
        
        public Location getDestination() { return destination; }
        public long getRequestTime() { return requestTime; }
        public boolean isConfirmed() { return confirmed; }
    }
}
