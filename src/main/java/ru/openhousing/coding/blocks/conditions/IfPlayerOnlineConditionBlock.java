package ru.openhousing.coding.blocks.conditions;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
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
import java.util.stream.Collectors;
import net.wesjd.anvilgui.AnvilGUI;

/**
 * Специализированный блок для условия проверки онлайн статуса игрока
 * Проверяет различные аспекты онлайн статуса с детальным контролем
 * 
 * @author OpenHousing Team
 * @version 1.0.0
 */
public class IfPlayerOnlineConditionBlock extends CodeBlock {
    
    // Статические поля для глобального управления
    private static final Map<UUID, PlayerOnlineStatus> playerStatusCache = new ConcurrentHashMap<>();
    private static final Map<String, Integer> onlineStatusStats = new ConcurrentHashMap<>();
    private static final Set<UUID> recentlyOnlinePlayers = ConcurrentHashMap.newKeySet();
    private static final AtomicInteger totalOnlineChecks = new AtomicInteger(0);
    private static final AtomicInteger successfulOnlineChecks = new AtomicInteger(0);
    
    // Настройки блока
    private boolean checkSpecificPlayer = false;
    private String targetPlayerName = "";
    private UUID targetPlayerUUID = null;
    private boolean checkMultiplePlayers = false;
    private List<String> playerNames = new ArrayList<>();
    private List<UUID> playerUUIDs = new ArrayList<>();
    private boolean checkAllPlayers = false;
    private boolean checkServerCapacity = false;
    private int minOnlinePlayers = 1;
    private int maxOnlinePlayers = Integer.MAX_VALUE;
    private boolean checkPlayerActivity = false;
    private long minActivityTime = 300000; // 5 минут
    private boolean checkPlayerWorld = false;
    private String requiredWorld = "";
    private boolean checkPlayerGamemode = false;
    private String requiredGamemode = "";
    private boolean checkPlayerPermission = false;
    private String requiredPermission = "";
    private boolean checkPlayerGroup = false;
    private String requiredGroup = "";
    private boolean checkPlayerRank = false;
    private String requiredRank = "";
    
    // Настройки логики
    private boolean requireAllConditions = false;
    private boolean useAdvancedLogic = false;
    private String customLogicExpression = "";
    private boolean checkInverted = false;
    private boolean checkDelayed = false;
    private long delayTime = 1000; // 1 секунда
    private boolean checkRepeated = false;
    private int repeatCount = 3;
    private long repeatInterval = 5000; // 5 секунд
    
    // Настройки кэширования
    private boolean useCache = true;
    private long cacheExpiry = 30000; // 30 секунд
    private int maxCacheSize = 1000;
    private boolean clearExpiredCache = true;
    private boolean cacheOfflinePlayers = false;
    
    // Настройки уведомлений
    private boolean notifyOnConditionMet = false;
    private boolean notifyOnConditionFailed = false;
    private String successMessage = "§aУсловие выполнено: игрок онлайн";
    private String failureMessage = "§cУсловие не выполнено: игрок оффлайн";
    private boolean showDetailedInfo = false;
    private boolean logConditionChecks = false;
    
    // Настройки статистики
    private boolean collectStats = true;
    private boolean trackPlayerStats = true;
    private boolean trackGlobalStats = true;
    private boolean exportStats = false;
    private String statsExportPath = "plugins/OpenHousing/online_stats/";
    
    // Настройки производительности
    private boolean asyncProcessing = true;
    private boolean batchProcessing = false;
    private int batchSize = 10;
    private boolean optimizeQueries = true;
    private boolean useIndexing = true;
    
    // Enhanced GUI settings
    private String checkType = "SPECIFIC_PLAYERS";
    private List<String> targetPlayers = new ArrayList<>();
    private boolean worldFilterEnabled = false;
    private boolean gamemodeFilterEnabled = false;
    private boolean permissionFilterEnabled = false;
    private boolean groupFilterEnabled = false;
    private boolean cachingEnabled = true;
    private long cacheExpiryMs = 30000;
    
    // Внутренние кэши и состояния
    private final Map<String, CachedOnlineStatus> onlineStatusCache = new ConcurrentHashMap<>();
    private final Map<UUID, PlayerOnlineStats> playerStats = new ConcurrentHashMap<>();
    private final Map<String, GlobalOnlineStats> globalStats = new ConcurrentHashMap<>();
    private final Queue<OnlineCheckRequest> pendingChecks = new LinkedList<>();
    
    public IfPlayerOnlineConditionBlock() {
        super(BlockType.IF_PLAYER_ONLINE);
        initializeDefaultSettings();
        
        // Initialize enhanced GUI settings
        checkType = "SPECIFIC_PLAYERS";
        targetPlayers = new ArrayList<>();
        worldFilterEnabled = false;
        gamemodeFilterEnabled = false;
        permissionFilterEnabled = false;
        groupFilterEnabled = false;
        cachingEnabled = true;
        cacheExpiryMs = 30000;
    }
    
    /**
     * Инициализация настроек по умолчанию
     */
    private void initializeDefaultSettings() {
        // Базовые настройки
        checkSpecificPlayer = false;
        checkMultiplePlayers = false;
        checkAllPlayers = false;
        checkServerCapacity = false;
        
        // Настройки логики
        requireAllConditions = false;
        useAdvancedLogic = false;
        checkInverted = false;
        checkDelayed = false;
        checkRepeated = false;
        
        // Настройки кэширования
        useCache = true;
        cacheExpiry = 30000;
        maxCacheSize = 1000;
        clearExpiredCache = true;
        cacheOfflinePlayers = false;
        
        // Настройки уведомлений
        notifyOnConditionMet = false;
        notifyOnConditionFailed = false;
        showDetailedInfo = false;
        logConditionChecks = false;
        
        // Настройки производительности
        asyncProcessing = true;
        batchProcessing = false;
        batchSize = 10;
        optimizeQueries = true;
        useIndexing = true;
        
        // Инициализация статистики
        onlineStatusStats.put("ONLINE", 0);
        onlineStatusStats.put("OFFLINE", 0);
        onlineStatusStats.put("UNKNOWN", 0);
    }
    
    @Override
    public ExecutionResult execute(ExecutionContext context) {
        try {
            totalOnlineChecks.incrementAndGet();
            
            // Асинхронная обработка
            if (asyncProcessing) {
                return executeAsync(context);
            } else {
                return executeSync(context);
            }
        } catch (Exception e) {
            return ExecutionResult.error("Ошибка выполнения блока проверки онлайн статуса: " + e.getMessage());
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
        if (useCache) {
            CachedOnlineStatus cached = onlineStatusCache.get(player.getUniqueId().toString());
            if (cached != null && !cached.isExpired()) {
                return processCachedResult(cached, context);
            }
        }
        
        // Выполнение проверки
        boolean result = performOnlineCheck(context);
        
        // Кэширование результата
        if (useCache) {
            cacheResult(player.getUniqueId().toString(), result);
        }
        
        // Обновление статистики
        updateStatistics(player, result);
        
        // Обработка результата
        return processCheckResult(result, context);
    }
    
    /**
     * Асинхронное выполнение
     */
    private ExecutionResult executeAsync(ExecutionContext context) {
        // TODO: Реализовать асинхронное выполнение
        return executeSync(context);
    }
    
    /**
     * Выполнение проверки онлайн статуса
     */
    private boolean performOnlineCheck(ExecutionContext context) {
        Player player = context.getPlayer();
        
        // Проверка конкретного игрока
        if (checkSpecificPlayer) {
            return checkSpecificPlayerStatus();
        }
        
        // Проверка нескольких игроков
        if (checkMultiplePlayers) {
            return checkMultiplePlayersStatus();
        }
        
        // Проверка всех игроков
        if (checkAllPlayers) {
            return checkAllPlayersStatus();
        }
        
        // Проверка серверной вместимости
        if (checkServerCapacity) {
            return checkServerCapacityStatus();
        }
        
        // Проверка активности игрока
        if (checkPlayerActivity) {
            return checkPlayerActivityStatus(player);
        }
        
        // Проверка мира игрока
        if (checkPlayerWorld) {
            return checkPlayerWorldStatus(player);
        }
        
        // Проверка режима игры
        if (checkPlayerGamemode) {
            return checkPlayerGamemodeStatus(player);
        }
        
        // Проверка разрешений
        if (checkPlayerPermission) {
            return checkPlayerPermissionStatus(player);
        }
        
        // Проверка группы
        if (checkPlayerGroup) {
            return checkPlayerGroupStatus(player);
        }
        
        // Проверка ранга
        if (checkPlayerRank) {
            return checkPlayerRankStatus(player);
        }
        
        // Стандартная проверка - игрок в контексте онлайн
        return player.isOnline();
    }
    
    /**
     * Проверка статуса конкретного игрока
     */
    private boolean checkSpecificPlayerStatus() {
        if (targetPlayerName.isEmpty() && targetPlayerUUID == null) {
            return false;
        }
        
        Player targetPlayer = null;
        
        if (targetPlayerUUID != null) {
            targetPlayer = Bukkit.getPlayer(targetPlayerUUID);
        } else if (!targetPlayerName.isEmpty()) {
            targetPlayer = Bukkit.getPlayer(targetPlayerName);
        }
        
        return targetPlayer != null && targetPlayer.isOnline();
    }
    
    /**
     * Проверка статуса нескольких игроков
     */
    private boolean checkMultiplePlayersStatus() {
        if (playerNames.isEmpty() && playerUUIDs.isEmpty()) {
            return false;
        }
        
        int onlineCount = 0;
        int totalCount = 0;
        
        // Проверка по именам
        for (String name : playerNames) {
            Player player = Bukkit.getPlayer(name);
            if (player != null && player.isOnline()) {
                onlineCount++;
            }
            totalCount++;
        }
        
        // Проверка по UUID
        for (UUID uuid : playerUUIDs) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null && player.isOnline()) {
                onlineCount++;
            }
            totalCount++;
        }
        
        if (requireAllConditions) {
            return onlineCount == totalCount;
        } else {
            return onlineCount > 0;
        }
    }
    
    /**
     * Проверка статуса всех игроков
     */
    private boolean checkAllPlayersStatus() {
        Collection<? extends Player> onlinePlayers = Bukkit.getOnlinePlayers();
        int onlineCount = onlinePlayers.size();
        
        if (checkServerCapacity) {
            return onlineCount >= minOnlinePlayers && onlineCount <= maxOnlinePlayers;
        }
        
        return onlineCount > 0;
    }
    
    /**
     * Проверка серверной вместимости
     */
    private boolean checkServerCapacityStatus() {
        int onlineCount = Bukkit.getOnlinePlayers().size();
        int maxPlayers = Bukkit.getMaxPlayers();
        
        return onlineCount >= minOnlinePlayers && onlineCount <= maxOnlinePlayers && onlineCount <= maxPlayers;
    }
    
    /**
     * Проверка активности игрока
     */
    private boolean checkPlayerActivityStatus(Player player) {
        if (player == null || !player.isOnline()) {
            return false;
        }
        
        // Получаем время последней активности
        long lastActivity = getPlayerLastActivity(player);
        long currentTime = System.currentTimeMillis();
        
        return (currentTime - lastActivity) >= minActivityTime;
    }
    
    /**
     * Проверка мира игрока
     */
    private boolean checkPlayerWorldStatus(Player player) {
        if (player == null || !player.isOnline()) {
            return false;
        }
        
        if (requiredWorld.isEmpty()) {
            return true;
        }
        
        return player.getWorld().getName().equalsIgnoreCase(requiredWorld);
    }
    
    /**
     * Проверка режима игры
     */
    private boolean checkPlayerGamemodeStatus(Player player) {
        if (player == null || !player.isOnline()) {
            return false;
        }
        
        if (requiredGamemode.isEmpty()) {
            return true;
        }
        
        return player.getGameMode().name().equalsIgnoreCase(requiredGamemode);
    }
    
    /**
     * Проверка разрешений
     */
    private boolean checkPlayerPermissionStatus(Player player) {
        if (player == null || !player.isOnline()) {
            return false;
        }
        
        if (requiredPermission.isEmpty()) {
            return true;
        }
        
        return player.hasPermission(requiredPermission);
    }
    
    /**
     * Проверка группы
     */
    private boolean checkPlayerGroupStatus(Player player) {
        if (player == null || !player.isOnline()) {
            return false;
        }
        
        if (requiredGroup.isEmpty()) {
            return true;
        }
        
        // TODO: Реализовать проверку группы через Vault или другой плагин
        return true;
    }
    
    /**
     * Проверка ранга
     */
    private boolean checkPlayerRankStatus(Player player) {
        if (player == null || !player.isOnline()) {
            return false;
        }
        
        if (requiredRank.isEmpty()) {
            return true;
        }
        
        // TODO: Реализовать проверку ранга через Vault или другой плагин
        return true;
    }
    
    /**
     * Получение времени последней активности игрока
     */
    private long getPlayerLastActivity(Player player) {
        // TODO: Реализовать получение времени последней активности
        // Это может быть через API плагина или собственное отслеживание
        return System.currentTimeMillis();
    }
    
    /**
     * Кэширование результата
     */
    private void cacheResult(String playerId, boolean result) {
        if (!useCache) {
            return;
        }
        
        // Очистка устаревшего кэша
        if (clearExpiredCache) {
            clearExpiredCache();
        }
        
        // Ограничение размера кэша
        if (onlineStatusCache.size() >= maxCacheSize) {
            removeOldestCacheEntry();
        }
        
        // Добавление нового результата
        onlineStatusCache.put(playerId, new CachedOnlineStatus(result, System.currentTimeMillis() + cacheExpiry));
    }
    
    /**
     * Очистка устаревшего кэша
     */
    private void clearExpiredCache() {
        onlineStatusCache.entrySet().removeIf(entry -> entry.getValue().isExpired());
    }
    
    /**
     * Удаление самого старого элемента кэша
     */
    private void removeOldestCacheEntry() {
        if (onlineStatusCache.isEmpty()) {
            return;
        }
        
        String oldestKey = onlineStatusCache.entrySet().stream()
            .min(Comparator.comparingLong(entry -> entry.getValue().getCreationTime()))
            .map(Map.Entry::getKey)
            .orElse(null);
        
        if (oldestKey != null) {
            onlineStatusCache.remove(oldestKey);
        }
    }
    
    /**
     * Обработка кэшированного результата
     */
    private ExecutionResult processCachedResult(CachedOnlineStatus cached, ExecutionContext context) {
        boolean result = cached.getResult();
        
        // Обновление статистики
        Player player = context.getPlayer();
        if (player != null) {
            updateStatistics(player, result);
        }
        
        return processCheckResult(result, context);
    }
    
    /**
     * Обработка результата проверки
     */
    private ExecutionResult processCheckResult(boolean result, ExecutionContext context) {
        // Инвертирование результата если нужно
        if (checkInverted) {
            result = !result;
        }
        
        // Уведомления
        if (result && notifyOnConditionMet) {
            sendNotification(context, successMessage, true);
        } else if (!result && notifyOnConditionFailed) {
            sendNotification(context, failureMessage, false);
        }
        
        // Логирование
        if (logConditionChecks) {
            logConditionCheck(context, result);
        }
        
        // Возврат результата
        if (result) {
            successfulOnlineChecks.incrementAndGet();
            return ExecutionResult.success();
        } else {
            return ExecutionResult.error("Условие не выполнено");
        }
    }
    
    /**
     * Отправка уведомления
     */
    private void sendNotification(ExecutionContext context, String message, boolean success) {
        Player player = context.getPlayer();
        if (player == null || !player.isOnline()) {
            return;
        }
        
        String formattedMessage = message
            .replace("{player}", player.getName())
            .replace("{online}", String.valueOf(Bukkit.getOnlinePlayers().size()))
            .replace("{max}", String.valueOf(Bukkit.getMaxPlayers()));
        
        if (success) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', formattedMessage));
        } else {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', formattedMessage));
        }
    }
    
    /**
     * Логирование проверки условия
     */
    private void logConditionCheck(ExecutionContext context, boolean result) {
        Player player = context.getPlayer();
        if (player == null) {
            return;
        }
        
        String logMessage = String.format("[OnlineCheck] Player: %s, Result: %s, Online: %d/%d",
            player.getName(),
            result ? "SUCCESS" : "FAILURE",
            Bukkit.getOnlinePlayers().size(),
            Bukkit.getMaxPlayers());
        
        OpenHousing.getInstance().getLogger().info(logMessage);
    }
    
    /**
     * Обновление статистики
     */
    private void updateStatistics(Player player, boolean result) {
        if (!collectStats) {
            return;
        }
        
        // Обновление глобальной статистики
        if (trackGlobalStats) {
            String status = result ? "ONLINE" : "OFFLINE";
            onlineStatusStats.merge(status, 1, Integer::sum);
        }
        
        // Обновление статистики игрока
        if (trackPlayerStats) {
            PlayerOnlineStats playerStats = this.playerStats.computeIfAbsent(
                player.getUniqueId(), k -> new PlayerOnlineStats());
            
            playerStats.addCheck(result, System.currentTimeMillis());
        }
    }
    
    @Override
    public boolean validate() {
        // Проверяем базовые параметры
        if (delayTime < 0) return false;
        if (repeatCount < 1) return false;
        if (repeatInterval < 0) return false;
        if (cacheExpiry < 0) return false;
        if (maxCacheSize < 1) return false;
        if (minOnlinePlayers < 0) return false;
        if (maxOnlinePlayers < minOnlinePlayers) return false;
        if (minActivityTime < 0) return false;
        if (batchSize < 1) return false;
        
        return true;
    }
    
    @Override
    public List<String> getDescription() {
        List<String> description = new ArrayList<>();
        description.add("§6Блок условия проверки онлайн статуса");
        description.add("§7Проверяет различные аспекты онлайн");
        description.add("§7статуса игроков с детальным контролем");
        description.add("");
        description.add("§eНастройки:");
        description.add("§7• Конкретный игрок: " + (checkSpecificPlayer ? "§a" + targetPlayerName : "§cВыключено"));
        description.add("§7• Несколько игроков: " + (checkMultiplePlayers ? "§a" + playerNames.size() + " игроков" : "§cВыключено"));
        description.add("§7• Все игроки: " + (checkAllPlayers ? "§aВключено" : "§cВыключено"));
        description.add("§7• Серверная вместимость: " + (checkServerCapacity ? "§a" + minOnlinePlayers + "-" + maxOnlinePlayers : "§cВыключено"));
        description.add("§7• Активность: " + (checkPlayerActivity ? "§a" + (minActivityTime/1000) + "с" : "§cВыключено"));
        description.add("§7• Мир: " + (checkPlayerWorld ? "§a" + requiredWorld : "§cВыключено"));
        description.add("§7• Режим игры: " + (checkPlayerGamemode ? "§a" + requiredGamemode : "§cВыключено"));
        description.add("§7• Разрешения: " + (checkPlayerPermission ? "§a" + requiredPermission : "§cВыключено"));
        description.add("§7• Кэширование: " + (useCache ? "§a" + (cacheExpiry/1000) + "с" : "§cВыключено"));
        description.add("§7• Асинхронность: " + (asyncProcessing ? "§aВключена" : "§cВыключена"));
        
        return description;
    }

    /**
     * Opens an enhanced configuration GUI for this block
     */
    public void openConfigurationGUI(Player player) {
        new AnvilGUI.Builder()
                .onClick((slot, state) -> {
                    if (slot != AnvilGUI.Slot.OUTPUT) {
                        return AnvilGUI.Response.text(state.getText());
                    }
                    String input = state.getText();
                    if (input == null || input.trim().isEmpty()) {
                        return AnvilGUI.Response.text(state.getText());
                    }
                    
                    // Parse configuration from text
                    parseConfiguration(input);
                    Bukkit.getScheduler().runTask(OpenHousing.getInstance(), () -> 
                        player.sendMessage("§aOnline check configuration updated!")
                    );
                    return AnvilGUI.Response.close();
                })
                .onClose(player1 -> {
                    Bukkit.getScheduler().runTask(OpenHousing.getInstance(), () -> 
                        player.sendMessage("§cConfiguration cancelled"));
                })
                .text("Configure online check settings")
                .title("§6Online Check Config")
                .plugin(OpenHousing.getInstance())
                .open(player);
    }

    /**
     * Shows current settings in chat
     */
    public void showSettings(Player player) {
        player.sendMessage("§6=== Online Check Block Settings ===");
        player.sendMessage("§eCheck Type: " + checkType);
        player.sendMessage("§eTarget Players: " + String.join(", ", targetPlayers));
        player.sendMessage("§eWorld Filter: " + (worldFilterEnabled ? "§aON" : "§cOFF"));
        player.sendMessage("§eGamemode Filter: " + (gamemodeFilterEnabled ? "§aON" : "§cOFF"));
        player.sendMessage("§ePermission Filter: " + (permissionFilterEnabled ? "§aON" : "§cOFF"));
        player.sendMessage("§eGroup Filter: " + (groupFilterEnabled ? "§aON" : "§cOFF"));
        player.sendMessage("§eCaching: " + (cachingEnabled ? "§aON" : "§cOFF"));
        player.sendMessage("§eCache Expiry: " + cacheExpiryMs + "ms");
        player.sendMessage("§7Use /configure to change settings");
    }

    /**
     * Parse configuration from text input
     */
    private void parseConfiguration(String text) {
        // Simple configuration parser
        if (text.startsWith("type:")) {
            checkType = text.split(":")[1].toUpperCase();
        } else if (text.startsWith("players:")) {
            targetPlayers.clear();
            String[] playerList = text.split(":")[1].split(",");
            for (String playerName : playerList) {
                targetPlayers.add(playerName.trim());
            }
        } else if (text.startsWith("world:")) {
            worldFilterEnabled = text.contains("true");
        } else if (text.startsWith("gamemode:")) {
            gamemodeFilterEnabled = text.contains("true");
        } else if (text.startsWith("permission:")) {
            permissionFilterEnabled = text.contains("true");
        } else if (text.startsWith("group:")) {
            groupFilterEnabled = text.contains("true");
        } else if (text.startsWith("cache:")) {
            cachingEnabled = text.contains("true");
        } else if (text.startsWith("expiry:")) {
            try {
                cacheExpiryMs = Long.parseLong(text.split(":")[1]);
            } catch (Exception e) {
                // Ignore invalid input
            }
        }
    }
    
    // Геттеры и сеттеры для настройки блока
    public void setCheckSpecificPlayer(boolean checkSpecificPlayer) { this.checkSpecificPlayer = checkSpecificPlayer; }
    public void setTargetPlayerName(String targetPlayerName) { this.targetPlayerName = targetPlayerName; }
    public void setTargetPlayerUUID(UUID targetPlayerUUID) { this.targetPlayerUUID = targetPlayerUUID; }
    public void setCheckMultiplePlayers(boolean checkMultiplePlayers) { this.checkMultiplePlayers = checkMultiplePlayers; }
    public void setPlayerNames(List<String> playerNames) { this.playerNames = playerNames; }
    public void setPlayerUUIDs(List<UUID> playerUUIDs) { this.playerUUIDs = playerUUIDs; }
    public void setCheckAllPlayers(boolean checkAllPlayers) { this.checkAllPlayers = checkAllPlayers; }
    public void setCheckServerCapacity(boolean checkServerCapacity) { this.checkServerCapacity = checkServerCapacity; }
    public void setMinOnlinePlayers(int minOnlinePlayers) { this.minOnlinePlayers = minOnlinePlayers; }
    public void setMaxOnlinePlayers(int maxOnlinePlayers) { this.maxOnlinePlayers = maxOnlinePlayers; }
    public void setCheckPlayerActivity(boolean checkPlayerActivity) { this.checkPlayerActivity = checkPlayerActivity; }
    public void setMinActivityTime(long minActivityTime) { this.minActivityTime = minActivityTime; }
    public void setCheckPlayerWorld(boolean checkPlayerWorld) { this.checkPlayerWorld = checkPlayerWorld; }
    public void setRequiredWorld(String requiredWorld) { this.requiredWorld = requiredWorld; }
    public void setCheckPlayerGamemode(boolean checkPlayerGamemode) { this.checkPlayerGamemode = checkPlayerGamemode; }
    public void setRequiredGamemode(String requiredGamemode) { this.requiredGamemode = requiredGamemode; }
    public void setCheckPlayerPermission(boolean checkPlayerPermission) { this.checkPlayerPermission = checkPlayerPermission; }
    public void setRequiredPermission(String requiredPermission) { this.requiredPermission = requiredPermission; }
    public void setCheckPlayerGroup(boolean checkPlayerGroup) { this.checkPlayerGroup = checkPlayerGroup; }
    public void setRequiredGroup(String requiredGroup) { this.requiredGroup = requiredGroup; }
    public void setCheckPlayerRank(boolean checkPlayerRank) { this.checkPlayerRank = checkPlayerRank; }
    public void setRequiredRank(String requiredRank) { this.requiredRank = requiredRank; }
    
    // Внутренние классы для кэширования и статистики
    private static class CachedOnlineStatus {
        private final boolean result;
        private final long expiryTime;
        private final long creationTime;
        
        public CachedOnlineStatus(boolean result, long expiryTime) {
            this.result = result;
            this.expiryTime = expiryTime;
            this.creationTime = System.currentTimeMillis();
        }
        
        public boolean getResult() { return result; }
        public boolean isExpired() { return System.currentTimeMillis() > expiryTime; }
        public long getCreationTime() { return creationTime; }
    }
    
    private static class PlayerOnlineStatus {
        private boolean isOnline = false;
        private long lastSeen = 0;
        private long totalOnlineTime = 0;
        private int loginCount = 0;
        private int logoutCount = 0;
        
        public void setOnline(boolean online) {
            long currentTime = System.currentTimeMillis();
            
            if (online && !isOnline) {
                // Игрок зашел
                loginCount++;
                lastSeen = currentTime;
            } else if (!online && isOnline) {
                // Игрок вышел
                logoutCount++;
                totalOnlineTime += (currentTime - lastSeen);
            }
            
            isOnline = online;
        }
        
        // Геттеры
        public boolean isOnline() { return isOnline; }
        public long getLastSeen() { return lastSeen; }
        public long getTotalOnlineTime() { return totalOnlineTime; }
        public int getLoginCount() { return loginCount; }
        public int getLogoutCount() { return logoutCount; }
    }
    
    private static class PlayerOnlineStats {
        private final List<OnlineCheckRecord> checks = new ArrayList<>();
        private long lastCheckTime = 0;
        
        public void addCheck(boolean result, long timestamp) {
            checks.add(new OnlineCheckRecord(result, timestamp));
            lastCheckTime = timestamp;
        }
        
        public void setLastCheckTime(long time) { this.lastCheckTime = time; }
        public long getLastCheckTime() { return lastCheckTime; }
        public List<OnlineCheckRecord> getChecks() { return checks; }
    }
    
    private static class OnlineCheckRecord {
        private final boolean result;
        private final long timestamp;
        
        public OnlineCheckRecord(boolean result, long timestamp) {
            this.result = result;
            this.timestamp = timestamp;
        }
        
        public boolean getResult() { return result; }
        public long getTimestamp() { return timestamp; }
    }
    
    private static class GlobalOnlineStats {
        private int totalChecks = 0;
        private int successfulChecks = 0;
        private int failedChecks = 0;
        private long totalProcessingTime = 0;
        
        public void addCheck(boolean success, long processingTime) {
            totalChecks++;
            if (success) {
                successfulChecks++;
            } else {
                failedChecks++;
            }
            totalProcessingTime += processingTime;
        }
        
        // Геттеры
        public int getTotalChecks() { return totalChecks; }
        public int getSuccessfulChecks() { return successfulChecks; }
        public int getFailedChecks() { return failedChecks; }
        public long getTotalProcessingTime() { return totalProcessingTime; }
        public double getSuccessRate() { return totalChecks > 0 ? (double) successfulChecks / totalChecks : 0.0; }
        public double getAverageProcessingTime() { return totalChecks > 0 ? (double) totalProcessingTime / totalChecks : 0.0; }
    }
    
    private static class OnlineCheckRequest {
        private final UUID playerId;
        private final long requestTime;
        private final boolean priority;
        
        public OnlineCheckRequest(UUID playerId, long requestTime, boolean priority) {
            this.playerId = playerId;
            this.requestTime = requestTime;
            this.priority = priority;
        }
        
        public UUID getPlayerId() { return playerId; }
        public long getRequestTime() { return requestTime; }
        public boolean isPriority() { return priority; }
    }
}
