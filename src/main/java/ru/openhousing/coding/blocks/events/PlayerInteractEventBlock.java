package ru.openhousing.coding.blocks.events;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import ru.openhousing.OpenHousing;
import ru.openhousing.coding.blocks.BlockType;
import ru.openhousing.coding.blocks.CodeBlock;
import ru.openhousing.coding.blocks.CodeBlock.ExecutionContext;
import ru.openhousing.coding.blocks.CodeBlock.ExecutionResult;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Специализированный блок для события взаимодействия игрока
 * Обрабатывает взаимодействия с блоками, предметами и сущностями
 * 
 * @author OpenHousing Team
 * @version 1.0.0
 */
public class PlayerInteractEventBlock extends CodeBlock implements Listener {
    
    // Статические поля для глобального управления
    private static final Map<UUID, InteractionHistory> interactionHistory = new ConcurrentHashMap<>();
    private static final Map<String, Integer> interactionStats = new ConcurrentHashMap<>();
    private static final Set<UUID> recentlyInteractedPlayers = ConcurrentHashMap.newKeySet();
    private static final AtomicInteger totalInteractions = new AtomicInteger(0);
    private static final AtomicInteger successfulInteractions = new AtomicInteger(0);
    
    // Настройки блока
    private boolean interactionEnabled = true;
    private boolean requirePermission = false;
    private String requiredPermission = "openhousing.interact";
    private boolean cooldownEnabled = true;
    private long cooldownTime = 500; // 0.5 секунды
    private boolean rateLimitEnabled = true;
    private int maxInteractionsPerMinute = 20;
    
    // Настройки блоков
    private boolean blockInteractionEnabled = true;
    private List<Material> allowedBlocks = new ArrayList<>();
    private List<Material> blockedBlocks = new ArrayList<>();
    private boolean blockPlacementEnabled = true;
    private boolean blockBreakingEnabled = true;
    private boolean blockUseEnabled = true;
    
    // Настройки предметов
    private boolean itemInteractionEnabled = true;
    private List<Material> allowedItems = new ArrayList<>();
    private List<Material> blockedItems = new ArrayList<>();
    private boolean itemUseEnabled = true;
    private boolean itemPlacementEnabled = true;
    
    // Настройки сущностей
    private boolean entityInteractionEnabled = true;
    private List<String> allowedEntities = new ArrayList<>();
    private List<String> blockedEntities = new ArrayList<>();
    private boolean entityRightClickEnabled = true;
    private boolean entityLeftClickEnabled = true;
    
    // Настройки эффектов
    private boolean spawnInteractionParticles = true;
    private Particle interactionParticle = Particle.HEART;
    private int particleCount = 20;
    private double particleOffset = 0.3;
    private boolean playInteractionSound = true;
    private Sound interactionSound = Sound.BLOCK_STONE_PLACE;
    private float soundVolume = 0.5f;
    private float soundPitch = 1.0f;
    
    // Настройки уведомлений
    private boolean notifyPlayer = true;
    private String interactionMessage = "§aВзаимодействие выполнено";
    private boolean notifyAdmins = false;
    private String adminNotificationFormat = "&c[Interact] &f{player} &7interacted with &e{target}";
    
    // Настройки логирования
    private boolean logInteractions = true;
    private boolean logToConsole = true;
    private boolean logToFile = false;
    private String logFormat = "[{timestamp}] {player} interacted with {target} at {location}";
    private boolean logLocation = true;
    private boolean logWorld = true;
    
    // Настройки статистики
    private boolean collectStats = true;
    private boolean trackPlayerStats = true;
    private boolean trackGlobalStats = true;
    private boolean exportStats = false;
    private String statsExportPath = "plugins/OpenHousing/interaction_stats/";
    
    // Настройки производительности
    private boolean asyncProcessing = true;
    private boolean cacheResults = true;
    private int cacheSize = 1000;
    private long cacheExpiry = 300000; // 5 минут
    
    // Внутренние кэши и состояния
    private final Map<String, CachedInteractionResult> interactionCache = new ConcurrentHashMap<>();
    private final Map<UUID, PlayerInteractionStats> playerStats = new ConcurrentHashMap<>();
    private final Map<String, GlobalInteractionStats> globalStats = new ConcurrentHashMap<>();
    private final Queue<InteractionRequest> pendingInteractions = new LinkedList<>();
    
    public PlayerInteractEventBlock() {
        super(BlockType.PLAYER_INTERACT);
        initializeDefaultSettings();
        registerListener();
    }
    
    /**
     * Инициализация настроек по умолчанию
     */
    private void initializeDefaultSettings() {
        // Базовые настройки
        interactionEnabled = true;
        cooldownEnabled = true;
        rateLimitEnabled = true;
        
        // Настройки блоков
        blockInteractionEnabled = true;
        blockPlacementEnabled = true;
        blockBreakingEnabled = true;
        blockUseEnabled = true;
        
        // Настройки предметов
        itemInteractionEnabled = true;
        itemUseEnabled = true;
        itemPlacementEnabled = true;
        
        // Настройки сущностей
        entityInteractionEnabled = true;
        entityRightClickEnabled = true;
        entityLeftClickEnabled = true;
        
        // Настройки эффектов
        spawnInteractionParticles = true;
        interactionParticle = Particle.HEART;
        particleCount = 20;
        particleOffset = 0.3;
        playInteractionSound = true;
        interactionSound = Sound.BLOCK_STONE_PLACE;
        
        // Инициализация статистики
        interactionStats.put("TOTAL", 0);
        interactionStats.put("SUCCESS", 0);
        interactionStats.put("BLOCKED", 0);
        interactionStats.put("FAILED", 0);
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
            System.err.println("Failed to register PlayerInteractEventBlock listener: " + e.getMessage());
        }
    }
    
    /**
     * Обработка события взаимодействия
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Block block = event.getClickedBlock();
        ItemStack item = event.getItem();
        
        if (player == null) {
            return;
        }
        
        // Проверка базовых условий
        if (!shouldProcessInteraction(player, event)) {
            return;
        }
        
        // Создание контекста выполнения
        ExecutionContext context = new ExecutionContext(player);
        context.setVariable("timestamp", System.currentTimeMillis());
        context.setVariable("playerName", player.getName());
        context.setVariable("playerUUID", player.getUniqueId().toString());
        context.setVariable("action", event.getAction().name());
        context.setVariable("hand", event.getHand() != null ? event.getHand().name() : "MAIN_HAND");
        context.setVariable("world", player.getWorld().getName());
        context.setVariable("location", formatLocation(player.getLocation()));
        context.setVariable("interactionTime", System.currentTimeMillis());
        
        if (block != null) {
            context.setVariable("blockType", block.getType().name());
            context.setVariable("blockLocation", formatLocation(block.getLocation()));
        }
        
        if (item != null) {
            context.setVariable("itemType", item.getType().name());
            context.setVariable("itemAmount", item.getAmount());
        }
        
        // Выполнение блока
        ExecutionResult result = execute(context);
        
        // Обработка результата
        handleExecutionResult(event, result, context);
        
        // Обновление статистики
        updateStatistics(player, event, result);
        
        // Логирование
        logPlayerInteraction(player, event, result, context);
        
        // Уведомления
        sendNotifications(player, event, result, context);
        
        // Эффекты
        if (result != null && result.isSuccess()) {
            spawnInteractionEffects(player, event);
        }
    }
    
    /**
     * Проверка, следует ли обрабатывать взаимодействие
     */
    private boolean shouldProcessInteraction(Player player, PlayerInteractEvent event) {
        // Проверка базовых условий
        if (!interactionEnabled || player == null) {
            return false;
        }
        
        // Проверка разрешений
        if (requirePermission && !player.hasPermission(requiredPermission)) {
            return false;
        }
        
        // Проверка кулдауна
        if (cooldownEnabled && isOnCooldown(player)) {
            return false;
        }
        
        // Проверка лимита взаимодействий
        if (rateLimitEnabled && isRateLimited(player)) {
            return false;
        }
        
        // Проверка блоков
        Block block = event.getClickedBlock();
        if (block != null && !isBlockInteractionAllowed(block.getType())) {
            return false;
        }
        
        // Проверка предметов
        ItemStack item = event.getItem();
        if (item != null && !isItemInteractionAllowed(item.getType())) {
            return false;
        }
        
        return true;
    }
    
    /**
     * Проверка кулдауна
     */
    private boolean isOnCooldown(Player player) {
        PlayerInteractionStats stats = playerStats.get(player.getUniqueId());
        if (stats == null) {
            return false;
        }
        
        long lastInteractionTime = stats.getLastInteractionTime();
        long currentTime = System.currentTimeMillis();
        
        return (currentTime - lastInteractionTime) < cooldownTime;
    }
    
    /**
     * Проверка лимита взаимодействий
     */
    private boolean isRateLimited(Player player) {
        PlayerInteractionStats stats = playerStats.get(player.getUniqueId());
        if (stats == null) {
            return false;
        }
        
        long currentTime = System.currentTimeMillis();
        long oneMinuteAgo = currentTime - 60000;
        
        // Удаляем старые взаимодействия
        stats.getRecentInteractions().removeIf(time -> time < oneMinuteAgo);
        
        return stats.getRecentInteractions().size() >= maxInteractionsPerMinute;
    }
    
    /**
     * Проверка разрешения на взаимодействие с блоком
     */
    private boolean isBlockInteractionAllowed(Material blockType) {
        if (!blockInteractionEnabled) {
            return false;
        }
        
        // Проверка заблокированных блоков
        if (blockedBlocks.contains(blockType)) {
            return false;
        }
        
        // Проверка разрешенных блоков
        if (!allowedBlocks.isEmpty() && !allowedBlocks.contains(blockType)) {
            return false;
        }
        
        return true;
    }
    
    /**
     * Проверка разрешения на взаимодействие с предметом
     */
    private boolean isItemInteractionAllowed(Material itemType) {
        if (!itemInteractionEnabled) {
            return false;
        }
        
        // Проверка заблокированных предметов
        if (blockedItems.contains(itemType)) {
            return false;
        }
        
        // Проверка разрешенных предметов
        if (!allowedItems.isEmpty() && !allowedItems.contains(itemType)) {
            return false;
        }
        
        return true;
    }
    
    @Override
    public ExecutionResult execute(ExecutionContext context) {
        try {
            totalInteractions.incrementAndGet();
            
            // Асинхронная обработка
            if (asyncProcessing) {
                return executeAsync(context);
            } else {
                return executeSync(context);
            }
        } catch (Exception e) {
            return ExecutionResult.error("Ошибка выполнения блока взаимодействия: " + e.getMessage());
        }
    }
    
    /**
     * Синхронное выполнение
     */
    private ExecutionResult executeSync(ExecutionContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return ExecutionResult.error("Игрок не найден в контексте");
        }
        
        // Проверка кэша
        if (cacheResults) {
            CachedInteractionResult cached = interactionCache.get(player.getUniqueId().toString());
            if (cached != null && !cached.isExpired()) {
                return cached.getResult();
            }
        }
        
        // Выполнение логики взаимодействия
        ExecutionResult result = processInteractionLogic(context);
        
        // Кэширование результата
        if (cacheResults && result != null) {
            String cacheKey = player.getUniqueId().toString();
            interactionCache.put(cacheKey, new CachedInteractionResult(result, System.currentTimeMillis() + cacheExpiry));
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
     * Основная логика обработки взаимодействия
     */
    private ExecutionResult processInteractionLogic(ExecutionContext context) {
        Player player = context.getPlayer();
        String action = context.getVariable("action").toString();
        
        try {
            // Обработка различных типов взаимодействий
            if (action.contains("RIGHT_CLICK")) {
                return processRightClickInteraction(context);
            } else if (action.contains("LEFT_CLICK")) {
                return processLeftClickInteraction(context);
            } else if (action.contains("PHYSICAL")) {
                return processPhysicalInteraction(context);
            }
            
            return ExecutionResult.success("Взаимодействие успешно обработано");
            
        } catch (Exception e) {
            return ExecutionResult.error("Ошибка обработки взаимодействия: " + e.getMessage());
        }
    }
    
    /**
     * Обработка правого клика
     */
    private ExecutionResult processRightClickInteraction(ExecutionContext context) {
        Player player = context.getPlayer();
        String blockType = (String) context.getVariable("blockType");
        String itemType = (String) context.getVariable("itemType");
        
        // Обработка взаимодействия с блоками
        if (blockType != null) {
            return processBlockRightClick(context, blockType);
        }
        
        // Обработка использования предметов
        if (itemType != null) {
            return processItemRightClick(context, itemType);
        }
        
        return ExecutionResult.success("Правый клик обработан");
    }
    
    /**
     * Обработка правого клика по блоку
     */
    private ExecutionResult processBlockRightClick(ExecutionContext context, String blockType) {
        // TODO: Реализовать специфичную логику для разных типов блоков
        return ExecutionResult.success("Взаимодействие с блоком " + blockType + " обработано");
    }
    
    /**
     * Обработка правого клика предметом
     */
    private ExecutionResult processItemRightClick(ExecutionContext context, String itemType) {
        // TODO: Реализовать специфичную логику для разных типов предметов
        return ExecutionResult.success("Использование предмета " + itemType + " обработано");
    }
    
    /**
     * Обработка левого клика
     */
    private ExecutionResult processLeftClickInteraction(ExecutionContext context) {
        // TODO: Реализовать логику левого клика
        return ExecutionResult.success("Левый клик обработан");
    }
    
    /**
     * Обработка физического взаимодействия
     */
    private ExecutionResult processPhysicalInteraction(ExecutionContext context) {
        // TODO: Реализовать логику физического взаимодействия
        return ExecutionResult.success("Физическое взаимодействие обработано");
    }
    
    /**
     * Обработка результата выполнения
     */
    private void handleExecutionResult(PlayerInteractEvent event, ExecutionResult result, ExecutionContext context) {
        if (result == null) {
            return;
        }
        
        if (result.isSuccess()) {
            successfulInteractions.incrementAndGet();
            
            // Добавляем в список недавно взаимодействовавших
            recentlyInteractedPlayers.add(event.getPlayer().getUniqueId());
            
            // Удаляем через 30 секунд
            Bukkit.getScheduler().runTaskLater(OpenHousing.getInstance(), () -> {
                recentlyInteractedPlayers.remove(event.getPlayer().getUniqueId());
            }, 600L); // 30 секунд * 20 тиков
            
        } else {
            // Отменяем взаимодействие при ошибке
            event.setCancelled(true);
            
            // Логируем ошибку
            OpenHousing.getInstance().getLogger().warning(
                "Player interaction processing failed for " + event.getPlayer().getName() + 
                ": " + result.getMessage());
        }
    }
    
    /**
     * Обновление статистики
     */
    private void updateStatistics(Player player, PlayerInteractEvent event, ExecutionResult result) {
        if (!collectStats) {
            return;
        }
        
        // Обновление глобальной статистики
        if (trackGlobalStats) {
            interactionStats.merge("TOTAL", 1, Integer::sum);
            
            if (result != null && result.isSuccess()) {
                interactionStats.merge("SUCCESS", 1, Integer::sum);
            } else {
                interactionStats.merge("FAILED", 1, Integer::sum);
            }
        }
        
        // Обновление статистики игрока
        if (trackPlayerStats) {
            PlayerInteractionStats playerStats = this.playerStats.computeIfAbsent(
                player.getUniqueId(), k -> new PlayerInteractionStats());
            
            playerStats.addInteraction(System.currentTimeMillis());
        }
    }
    
    /**
     * Логирование взаимодействия игрока
     */
    private void logPlayerInteraction(Player player, PlayerInteractEvent event, ExecutionResult result, ExecutionContext context) {
        if (!logInteractions) {
            return;
        }
        
        String target = "unknown";
        if (event.getClickedBlock() != null) {
            target = event.getClickedBlock().getType().name();
        } else if (event.getItem() != null) {
            target = event.getItem().getType().name();
        }
        
        String logMessage = logFormat
            .replace("{timestamp}", new java.util.Date().toString())
            .replace("{player}", player.getName())
            .replace("{target}", target)
            .replace("{result}", result != null ? (result.isSuccess() ? "SUCCESS" : "FAILURE") : "UNKNOWN")
            .replace("{world}", context.getVariable("world").toString())
            .replace("{location}", context.getVariable("location").toString());
        
        // Логирование в консоль
        if (logToConsole) {
            if (result != null && !result.isSuccess()) {
                OpenHousing.getInstance().getLogger().warning(logMessage);
            } else {
                OpenHousing.getInstance().getLogger().info(logMessage);
            }
        }
        
        // Сохранение в историю
        saveToHistory(player.getUniqueId(), event, result, context);
    }
    
    /**
     * Отправка уведомлений
     */
    private void sendNotifications(Player player, PlayerInteractEvent event, ExecutionResult result, ExecutionContext context) {
        // Уведомление игрока
        if (notifyPlayer && result != null && result.isSuccess()) {
            player.sendMessage(interactionMessage);
        }
        
        // Уведомление администраторов
        if (notifyAdmins) {
            String target = "unknown";
            if (event.getClickedBlock() != null) {
                target = event.getClickedBlock().getType().name();
            } else if (event.getItem() != null) {
                target = event.getItem().getType().name();
            }
            
            String adminMessage = adminNotificationFormat
                .replace("{player}", player.getName())
                .replace("{target}", target)
                .replace("{world}", context.getVariable("world").toString())
                .replace("{location}", context.getVariable("location").toString());
            
            Bukkit.getOnlinePlayers().stream()
                .filter(p -> p.hasPermission("openhousing.admin.notify"))
                .forEach(p -> p.sendMessage(ChatColor.translateAlternateColorCodes('&', adminMessage)));
        }
    }
    
    /**
     * Спавн эффектов взаимодействия
     */
    private void spawnInteractionEffects(Player player, PlayerInteractEvent event) {
        Location location = event.getClickedBlock() != null ? 
            event.getClickedBlock().getLocation() : player.getLocation();
        
        // Частицы
        if (spawnInteractionParticles) {
            location.getWorld().spawnParticle(
                interactionParticle, 
                location, 
                particleCount, 
                particleOffset, 
                particleOffset, 
                particleOffset, 
                0.1
            );
        }
        
        // Звук
        if (playInteractionSound) {
            location.getWorld().playSound(
                location, 
                interactionSound, 
                soundVolume, 
                soundPitch
            );
        }
    }
    
    /**
     * Сохранение в историю
     */
    private void saveToHistory(UUID playerId, PlayerInteractEvent event, ExecutionResult result, ExecutionContext context) {
        String target = "unknown";
        if (event.getClickedBlock() != null) {
            target = event.getClickedBlock().getType().name();
        } else if (event.getItem() != null) {
            target = event.getItem().getType().name();
        }
        
        InteractionRecord record = new InteractionRecord(
            playerId,
            System.currentTimeMillis(),
            target,
            event.getAction().name(),
            result != null ? result.isSuccess() : false,
            result != null ? result.getMessage() : null,
            context.getVariable("world").toString(),
            context.getVariable("location").toString()
        );
        
        interactionHistory.computeIfAbsent(playerId, k -> new InteractionHistory())
            .addRecord(record);
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
    
    @Override
    public boolean validate() {
        // Проверяем базовые параметры
        if (cooldownTime < 0) return false;
        if (maxInteractionsPerMinute < 1) return false;
        if (particleCount < 0) return false;
        if (particleOffset < 0) return false;
        if (soundVolume < 0) return false;
        if (soundPitch < 0) return false;
        if (cacheSize < 1) return false;
        if (cacheExpiry < 0) return false;
        
        return true;
    }
    
    @Override
    public List<String> getDescription() {
        List<String> description = new ArrayList<>();
        description.add("§6Блок события взаимодействия игрока");
        description.add("§7Обрабатывает взаимодействия с блоками,");
        description.add("§7предметами и сущностями");
        description.add("");
        description.add("§eНастройки:");
        description.add("§7• Взаимодействие с блоками: " + (blockInteractionEnabled ? "§aВключено" : "§cВыключено"));
        description.add("§7• Взаимодействие с предметами: " + (itemInteractionEnabled ? "§aВключено" : "§cВыключено"));
        description.add("§7• Взаимодействие с сущностями: " + (entityInteractionEnabled ? "§aВключено" : "§cВыключено"));
        description.add("§7• Кулдаун: " + (cooldownEnabled ? "§a" + cooldownTime + "мс" : "§cВыключен"));
        description.add("§7• Лимит: " + (rateLimitEnabled ? "§a" + maxInteractionsPerMinute + "/мин" : "§cВыключен"));
        
        return description;
    }
    
    // Геттеры и сеттеры для настройки блока
    public void setInteractionEnabled(boolean interactionEnabled) { this.interactionEnabled = interactionEnabled; }
    public void setRequirePermission(boolean requirePermission) { this.requirePermission = requirePermission; }
    public void setRequiredPermission(String requiredPermission) { this.requiredPermission = requiredPermission; }
    public void setCooldownEnabled(boolean cooldownEnabled) { this.cooldownEnabled = cooldownEnabled; }
    public void setCooldownTime(long cooldownTime) { this.cooldownTime = cooldownTime; }
    public void setRateLimitEnabled(boolean rateLimitEnabled) { this.rateLimitEnabled = rateLimitEnabled; }
    public void setMaxInteractionsPerMinute(int maxInteractionsPerMinute) { this.maxInteractionsPerMinute = maxInteractionsPerMinute; }
    public void setBlockInteractionEnabled(boolean blockInteractionEnabled) { this.blockInteractionEnabled = blockInteractionEnabled; }
    public void setAllowedBlocks(List<Material> allowedBlocks) { this.allowedBlocks = allowedBlocks; }
    public void setBlockedBlocks(List<Material> blockedBlocks) { this.blockedBlocks = blockedBlocks; }
    public void setBlockPlacementEnabled(boolean blockPlacementEnabled) { this.blockPlacementEnabled = blockPlacementEnabled; }
    public void setBlockBreakingEnabled(boolean blockBreakingEnabled) { this.blockBreakingEnabled = blockBreakingEnabled; }
    public void setBlockUseEnabled(boolean blockUseEnabled) { this.blockUseEnabled = blockUseEnabled; }
    public void setItemInteractionEnabled(boolean itemInteractionEnabled) { this.itemInteractionEnabled = itemInteractionEnabled; }
    public void setAllowedItems(List<Material> allowedItems) { this.allowedItems = allowedItems; }
    public void setBlockedItems(List<Material> blockedItems) { this.blockedItems = blockedItems; }
    public void setItemUseEnabled(boolean itemUseEnabled) { this.itemUseEnabled = itemUseEnabled; }
    public void setItemPlacementEnabled(boolean itemPlacementEnabled) { this.itemPlacementEnabled = itemPlacementEnabled; }
    public void setEntityInteractionEnabled(boolean entityInteractionEnabled) { this.entityInteractionEnabled = entityInteractionEnabled; }
    public void setAllowedEntities(List<String> allowedEntities) { this.allowedEntities = allowedEntities; }
    public void setBlockedEntities(List<String> blockedEntities) { this.blockedEntities = blockedEntities; }
    public void setEntityRightClickEnabled(boolean entityRightClickEnabled) { this.entityRightClickEnabled = entityRightClickEnabled; }
    public void setEntityLeftClickEnabled(boolean entityLeftClickEnabled) { this.entityLeftClickEnabled = entityLeftClickEnabled; }
    public void setSpawnInteractionParticles(boolean spawnInteractionParticles) { this.spawnInteractionParticles = spawnInteractionParticles; }
    public void setInteractionParticle(Particle interactionParticle) { this.interactionParticle = interactionParticle; }
    public void setParticleCount(int particleCount) { this.particleCount = particleCount; }
    public void setParticleOffset(double particleOffset) { this.particleOffset = particleOffset; }
    public void setPlayInteractionSound(boolean playInteractionSound) { this.playInteractionSound = playInteractionSound; }
    public void setInteractionSound(Sound interactionSound) { this.interactionSound = interactionSound; }
    public void setSoundVolume(float soundVolume) { this.soundVolume = soundVolume; }
    public void setSoundPitch(float soundPitch) { this.soundPitch = soundPitch; }
    public void setNotifyPlayer(boolean notifyPlayer) { this.notifyPlayer = notifyPlayer; }
    public void setInteractionMessage(String interactionMessage) { this.interactionMessage = interactionMessage; }
    public void setNotifyAdmins(boolean notifyAdmins) { this.notifyAdmins = notifyAdmins; }
    public void setAdminNotificationFormat(String adminNotificationFormat) { this.adminNotificationFormat = adminNotificationFormat; }
    public void setLogInteractions(boolean logInteractions) { this.logInteractions = logInteractions; }
    public void setLogToConsole(boolean logToConsole) { this.logToConsole = logToConsole; }
    public void setLogToFile(boolean logToFile) { this.logToFile = logToFile; }
    public void setLogFormat(String logFormat) { this.logFormat = logFormat; }
    public void setLogLocation(boolean logLocation) { this.logLocation = logLocation; }
    public void setLogWorld(boolean logWorld) { this.logWorld = logWorld; }
    public void setCollectStats(boolean collectStats) { this.collectStats = collectStats; }
    public void setTrackPlayerStats(boolean trackPlayerStats) { this.trackPlayerStats = trackPlayerStats; }
    public void setTrackGlobalStats(boolean trackGlobalStats) { this.trackGlobalStats = trackGlobalStats; }
    public void setExportStats(boolean exportStats) { this.exportStats = exportStats; }
    public void setStatsExportPath(String statsExportPath) { this.statsExportPath = statsExportPath; }
    public void setAsyncProcessing(boolean asyncProcessing) { this.asyncProcessing = asyncProcessing; }
    public void setCacheResults(boolean cacheResults) { this.cacheResults = cacheResults; }
    public void setCacheSize(int cacheSize) { this.cacheSize = cacheSize; }
    public void setCacheExpiry(long cacheExpiry) { this.cacheExpiry = cacheExpiry; }
    
    // Внутренние классы для кэширования и статистики
    private static class CachedInteractionResult {
        private final ExecutionResult result;
        private final long expiryTime;
        
        public CachedInteractionResult(ExecutionResult result, long expiryTime) {
            this.result = result;
            this.expiryTime = expiryTime;
        }
        
        public ExecutionResult getResult() { return result; }
        public boolean isExpired() { return System.currentTimeMillis() > expiryTime; }
    }
    
    private static class InteractionRecord {
        private final UUID playerId;
        private final long timestamp;
        private final String target;
        private final String action;
        private final boolean success;
        private final String message;
        private final String world;
        private final String location;
        
        public InteractionRecord(UUID playerId, long timestamp, String target, String action,
                               boolean success, String message, String world, String location) {
            this.playerId = playerId;
            this.timestamp = timestamp;
            this.target = target;
            this.action = action;
            this.success = success;
            this.message = message;
            this.world = world;
            this.location = location;
        }
        
        // Геттеры
        public UUID getPlayerId() { return playerId; }
        public long getTimestamp() { return timestamp; }
        public String getTarget() { return target; }
        public String getAction() { return action; }
        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
        public String getWorld() { return world; }
        public String getLocation() { return location; }
    }
    
    private static class InteractionHistory {
        private final List<InteractionRecord> records = new ArrayList<>();
        
        public void addRecord(InteractionRecord record) {
            records.add(record);
        }
        
        public List<InteractionRecord> getRecords() { return records; }
    }
    
    private static class PlayerInteractionStats {
        private final List<Long> recentInteractions = new ArrayList<>();
        private long lastInteractionTime = 0;
        
        public void addInteraction(long timestamp) {
            recentInteractions.add(timestamp);
        }
        
        public void setLastInteractionTime(long time) { this.lastInteractionTime = time; }
        public long getLastInteractionTime() { return lastInteractionTime; }
        public List<Long> getRecentInteractions() { return recentInteractions; }
    }
    
    private static class GlobalInteractionStats {
        private int totalInteractions = 0;
        private int successfulInteractions = 0;
        private int blockedInteractions = 0;
        private int failedInteractions = 0;
        private long totalProcessingTime = 0;
        
        public void addInteraction(boolean success, boolean blocked, long processingTime) {
            totalInteractions++;
            if (success) {
                successfulInteractions++;
            } else if (blocked) {
                blockedInteractions++;
            } else {
                failedInteractions++;
            }
            totalProcessingTime += processingTime;
        }
        
        // Геттеры
        public int getTotalInteractions() { return totalInteractions; }
        public int getSuccessfulInteractions() { return successfulInteractions; }
        public int getBlockedInteractions() { return blockedInteractions; }
        public int getFailedInteractions() { return failedInteractions; }
        public long getTotalProcessingTime() { return totalProcessingTime; }
        public double getSuccessRate() { return totalInteractions > 0 ? (double) successfulInteractions / totalInteractions : 0.0; }
        public double getAverageProcessingTime() { return totalInteractions > 0 ? (double) totalProcessingTime / totalInteractions : 0.0; }
    }
    
    private static class InteractionRequest {
        private final UUID playerId;
        private final long requestTime;
        private final boolean priority;
        
        public InteractionRequest(UUID playerId, long requestTime, boolean priority) {
            this.playerId = playerId;
            this.requestTime = requestTime;
            this.priority = priority;
        }
        
        public UUID getPlayerId() { return playerId; }
        public long getRequestTime() { return requestTime; }
        public boolean isPriority() { return priority; }
    }
}
