package ru.openhousing.coding.blocks.events;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import ru.openhousing.OpenHousing;
import ru.openhousing.coding.blocks.BlockType;
import ru.openhousing.coding.blocks.CodeBlock;
import ru.openhousing.coding.blocks.CodeBlock.ExecutionContext;
import ru.openhousing.coding.blocks.CodeBlock.ExecutionResult;
import ru.openhousing.coding.blocks.BlockVariable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Специализированный блок для события выхода игрока с сервера
 * Обрабатывает выход с детальным логированием, уведомлениями и статистикой
 * 
 * @author OpenHousing Team
 * @version 1.0.0
 */
public class PlayerQuitEventBlock extends CodeBlock {
    
    // Статические поля для глобального управления
    private static final Map<UUID, QuitRecord> quitHistory = new ConcurrentHashMap<>();
    private static final Map<String, Integer> quitReasonStats = new ConcurrentHashMap<>();
    private static final Map<String, Integer> worldQuitStats = new ConcurrentHashMap<>();
    private static final AtomicInteger totalQuits = new AtomicInteger(0);
    private static final AtomicInteger totalOnlineTime = new AtomicInteger(0);
    
    // Переменные блока (настраиваются через drag-n-drop)
    private BlockVariable notifyOnlinePlayersVar;
    private BlockVariable showQuitMessageVar;
    private BlockVariable logQuitEventVar;
    private BlockVariable trackStatisticsVar;
    private BlockVariable saveInventoryVar;
    private BlockVariable backupLocationVar;
    private BlockVariable quitMessageFormatVar;
    private BlockVariable onlinePlayersMessageVar;
    private BlockVariable quitSoundVar;
    private BlockVariable quitParticlesVar;
    private BlockVariable quitDelayVar;
    private BlockVariable autoSaveVar;
    private BlockVariable notifyAdminsVar;
    private BlockVariable quitReasonDetectionVar;
    private BlockVariable sessionTrackingVar;
    
    // Внутренние кэши и состояния
    private final Map<UUID, PlayerQuitStats> playerStats = new ConcurrentHashMap<>();
    private final Map<String, GlobalQuitStats> globalStats = new ConcurrentHashMap<>();
    private final Queue<QuitEventRequest> pendingQuits = new LinkedList<>();
    
    public PlayerQuitEventBlock() {
        super(BlockType.PLAYER_QUIT);
        initializeDefaultSettings();
    }
    
    /**
     * Инициализация настроек по умолчанию
     */
    private void initializeDefaultSettings() {
        // Создаем переменные с значениями по умолчанию
        notifyOnlinePlayersVar = new BlockVariable("notifyOnlinePlayers", "Уведомлять игроков онлайн", 
            BlockVariable.VariableType.BOOLEAN, true);
        showQuitMessageVar = new BlockVariable("showQuitMessage", "Показывать сообщение о выходе", 
            BlockVariable.VariableType.BOOLEAN, true);
        logQuitEventVar = new BlockVariable("logQuitEvent", "Логировать событие выхода", 
            BlockVariable.VariableType.BOOLEAN, true);
        trackStatisticsVar = new BlockVariable("trackStatistics", "Отслеживать статистику", 
            BlockVariable.VariableType.BOOLEAN, true);
        saveInventoryVar = new BlockVariable("saveInventory", "Сохранять инвентарь", 
            BlockVariable.VariableType.BOOLEAN, true);
        backupLocationVar = new BlockVariable("backupLocation", "Резервное копирование локации", 
            BlockVariable.VariableType.BOOLEAN, true);
        quitMessageFormatVar = new BlockVariable("quitMessageFormat", "Формат сообщения о выходе", 
            BlockVariable.VariableType.STRING, "§e%s покинул сервер");
        onlinePlayersMessageVar = new BlockVariable("onlinePlayersMessage", "Сообщение о количестве игроков", 
            BlockVariable.VariableType.BOOLEAN, true);
        quitSoundVar = new BlockVariable("quitSound", "Звук при выходе", 
            BlockVariable.VariableType.BOOLEAN, false);
        quitParticlesVar = new BlockVariable("quitParticles", "Частицы при выходе", 
            BlockVariable.VariableType.BOOLEAN, false);
        quitDelayVar = new BlockVariable("quitDelay", "Задержка перед выходом (мс)", 
            BlockVariable.VariableType.INTEGER, 0);
        autoSaveVar = new BlockVariable("autoSave", "Автосохранение при выходе", 
            BlockVariable.VariableType.BOOLEAN, true);
        notifyAdminsVar = new BlockVariable("notifyAdmins", "Уведомлять администраторов", 
            BlockVariable.VariableType.BOOLEAN, false);
        quitReasonDetectionVar = new BlockVariable("quitReasonDetection", "Определять причину выхода", 
            BlockVariable.VariableType.BOOLEAN, true);
        sessionTrackingVar = new BlockVariable("sessionTracking", "Отслеживать сессии", 
            BlockVariable.VariableType.BOOLEAN, true);
        
        // Инициализация статистики
        quitReasonStats.put("NORMAL", 0);
        quitReasonStats.put("TIMEOUT", 0);
        quitReasonStats.put("KICK", 0);
        quitReasonStats.put("BAN", 0);
        quitReasonStats.put("CRASH", 0);
        quitReasonStats.put("UNKNOWN", 0);
    }
    
    /**
     * Обработка события выхода игрока
     */
    public void processQuitEvent(Player player, Map<String, Object> eventData) {
        if (player == null) {
            return;
        }
        
        // Создание контекста выполнения
        ExecutionContext context = new ExecutionContext(player);
        context.setVariable("playerName", player.getName());
        context.setVariable("playerUUID", player.getUniqueId().toString());
        context.setVariable("world", player.getWorld().getName());
        context.setVariable("timestamp", System.currentTimeMillis());
        
        // Добавляем данные события из eventData
        if (eventData != null) {
            context.getVariables().putAll(eventData);
            // Если location не передан в eventData, получаем его из игрока
            if (!context.getVariables().containsKey("location")) {
                context.setVariable("location", formatLocation(player.getLocation()));
            }
        } else {
            // Если eventData null, получаем location из игрока
            context.setVariable("location", formatLocation(player.getLocation()));
        }
        
        // Выполнение блока
        ExecutionResult result = execute(context);
        
        // Обработка результата
        handleExecutionResult(player, result, context);
        
        // Обновление статистики
        updateStatistics(player, context);
        
        // Логирование
        logQuitEvent(player, context);
        
        // Уведомления
        sendNotifications(player, context);
        
        // Сохранение данных
        savePlayerData(player, context);
    }
    
    /**
     * Обработка результата выполнения
     */
    private void handleExecutionResult(Player player, ExecutionResult result, ExecutionContext context) {
        if (result == null) {
            return;
        }
        
        if (!result.isSuccess()) {
            // Логируем ошибку
            OpenHousing.getInstance().getLogger().warning(
                "PlayerQuitEventBlock execution failed for " + 
                context.getVariable("playerName") + ": " + result.getMessage());
        }
    }
    
    /**
     * Обновление статистики
     */
    private void updateStatistics(Player player, ExecutionContext context) {
        if (!getBooleanValue(trackStatisticsVar)) {
            return;
        }
        
        String quitReason = context.getVariable("quitReason").toString();
        String world = context.getVariable("world").toString();
        
        // Обновление статистики причин выхода
        quitReasonStats.merge(quitReason, 1, Integer::sum);
        
        // Обновление статистики по мирам
        worldQuitStats.merge(world, 1, Integer::sum);
        
        // Обновление общей статистики
        totalQuits.incrementAndGet();
        
        // Обновление статистики игрока
        PlayerQuitStats playerStats = this.playerStats.computeIfAbsent(
            player.getUniqueId(), k -> new PlayerQuitStats());
        playerStats.addQuit(quitReason, System.currentTimeMillis());
    }
    
    /**
     * Логирование события выхода
     */
    private void logQuitEvent(Player player, ExecutionContext context) {
        if (!getBooleanValue(logQuitEventVar)) {
            return;
        }
        
        String logMessage = String.format("[PlayerQuit] Player: %s, World: %s, Location: %s, Reason: %s, Online: %d/%d",
            player.getName(),
            context.getVariable("world"),
            context.getVariable("location"),
            context.getVariable("quitReason"),
            context.getVariable("onlinePlayers"),
            context.getVariable("maxPlayers"));
        
        OpenHousing.getInstance().getLogger().info(logMessage);
    }
    
    /**
     * Отправка уведомлений
     */
    private void sendNotifications(Player player, ExecutionContext context) {
        // Уведомление игроков онлайн
        if (getBooleanValue(notifyOnlinePlayersVar)) {
            String message = getStringValue(quitMessageFormatVar)
                .replace("%s", player.getName())
                .replace("%player", player.getName())
                .replace("%world", context.getVariable("world").toString())
                .replace("%online", context.getVariable("onlinePlayers").toString())
                .replace("%max", context.getVariable("maxPlayers").toString());
            
            Bukkit.getOnlinePlayers().forEach(p -> p.sendMessage(ChatColor.translateAlternateColorCodes('&', message)));
        }
        
        // Сообщение о количестве игроков онлайн
        if (getBooleanValue(onlinePlayersMessageVar)) {
            int onlineCount = Bukkit.getOnlinePlayers().size();
            int maxPlayers = Bukkit.getMaxPlayers();
            
            String onlineMessage = String.format("§7Игроков онлайн: §e%d§7/§e%d", onlineCount, maxPlayers);
            Bukkit.getOnlinePlayers().forEach(p -> p.sendMessage(onlineMessage));
        }
        
        // Уведомление администраторов
        if (getBooleanValue(notifyAdminsVar)) {
            String adminMessage = String.format("§c[Quit] §f%s §7вышел с сервера (Мир: %s, Причина: %s)",
                player.getName(),
                context.getVariable("world"),
                context.getVariable("quitReason"));
            
            Bukkit.getOnlinePlayers().stream()
                .filter(p -> p.hasPermission("openhousing.admin.notify"))
                .forEach(p -> p.sendMessage(adminMessage));
        }
    }
    
    /**
     * Сохранение данных игрока
     */
    private void savePlayerData(Player player, ExecutionContext context) {
        // Сохранение инвентаря
        if (getBooleanValue(saveInventoryVar)) {
            // TODO: Реализовать сохранение инвентаря
        }
        
        // Резервное копирование локации
        if (getBooleanValue(backupLocationVar)) {
            // TODO: Реализовать сохранение локации
        }
        
        // Автосохранение
        if (getBooleanValue(autoSaveVar)) {
            // TODO: Реализовать автосохранение
        }
    }
    
    @Override
    public ExecutionResult execute(ExecutionContext context) {
        try {
            Player player = context.getPlayer();
            if (player == null) {
                return ExecutionResult.error("Игрок не найден в контексте");
            }
            
            // Проверяем задержку
            int quitDelay = getIntegerValue(quitDelayVar);
            if (quitDelay > 0) {
                try {
                    Thread.sleep(quitDelay);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            
            // Воспроизводим звук
            if (getBooleanValue(quitSoundVar)) {
                // TODO: Реализовать воспроизведение звука
            }
            
            // Показываем частицы
            if (getBooleanValue(quitParticlesVar)) {
                // TODO: Реализовать показ частиц
            }
            
            return ExecutionResult.success();
            
        } catch (Exception e) {
            return ExecutionResult.error("Ошибка выполнения блока выхода игрока: " + e.getMessage());
        }
    }
    
    @Override
    public boolean validate() {
        // Проверяем базовые параметры
        return getIntegerValue(quitDelayVar) >= 0;
    }
    
    @Override
    public List<String> getDescription() {
        List<String> description = new ArrayList<>();
        description.add("§6Блок события выхода игрока");
        description.add("§7Обрабатывает выход с детальным");
        description.add("§7логированием и уведомлениями");
        description.add("");
        description.add("§eПеременные:");
        description.add("§7• Уведомления: " + (getBooleanValue(notifyOnlinePlayersVar) ? "§aВключены" : "§cВыключены"));
        description.add("§7• Сообщения: " + (getBooleanValue(showQuitMessageVar) ? "§aВключены" : "§cВыключены"));
        description.add("§7• Логирование: " + (getBooleanValue(logQuitEventVar) ? "§aВключено" : "§cВыключено"));
        description.add("§7• Статистика: " + (getBooleanValue(trackStatisticsVar) ? "§aВключена" : "§cВыключена"));
        description.add("§7• Сохранение: " + (getBooleanValue(saveInventoryVar) ? "§aВключено" : "§cВыключено"));
        description.add("§7• Задержка: " + getIntegerValue(quitDelayVar) + "мс");
        
        return description;
    }
    
    /**
     * Определение причины выхода
     */
    private String detectQuitReason(Map<String, Object> eventData) {
        // TODO: Реализовать определение причины выхода
        // Это может быть через API плагина или собственное отслеживание
        return "NORMAL";
    }
    
    /**
     * Форматирование локации
     */
    private String formatLocation(org.bukkit.Location location) {
        if (location == null) {
            return "null";
        }
        
        return String.format("%.1f, %.1f, %.1f", 
            location.getX(), location.getY(), location.getZ());
    }
    
    // Вспомогательные методы для работы с переменными
    private boolean getBooleanValue(BlockVariable variable) {
        Object value = variable.getValue();
        return value instanceof Boolean ? (Boolean) value : false;
    }
    
    private int getIntegerValue(BlockVariable variable) {
        Object value = variable.getValue();
        if (value instanceof Integer) return (Integer) value;
        if (value instanceof String) {
            try { return Integer.parseInt((String) value); } catch (Exception e) { }
        }
        return 0;
    }
    
    private String getStringValue(BlockVariable variable) {
        Object value = variable.getValue();
        return value != null ? value.toString() : "";
    }
    
    // Геттеры для переменных (для внешнего доступа)
    public BlockVariable getNotifyOnlinePlayersVar() { return notifyOnlinePlayersVar; }
    public BlockVariable getShowQuitMessageVar() { return showQuitMessageVar; }
    public BlockVariable getLogQuitEventVar() { return logQuitEventVar; }
    public BlockVariable getTrackStatisticsVar() { return trackStatisticsVar; }
    public BlockVariable getSaveInventoryVar() { return saveInventoryVar; }
    public BlockVariable getBackupLocationVar() { return backupLocationVar; }
    public BlockVariable getQuitMessageFormatVar() { return quitMessageFormatVar; }
    public BlockVariable getOnlinePlayersMessageVar() { return onlinePlayersMessageVar; }
    public BlockVariable getQuitSoundVar() { return quitSoundVar; }
    public BlockVariable getQuitParticlesVar() { return quitParticlesVar; }
    public BlockVariable getQuitDelayVar() { return quitDelayVar; }
    public BlockVariable getAutoSaveVar() { return autoSaveVar; }
    public BlockVariable getNotifyAdminsVar() { return notifyAdminsVar; }
    public BlockVariable getQuitReasonDetectionVar() { return quitReasonDetectionVar; }
    public BlockVariable getSessionTrackingVar() { return sessionTrackingVar; }
    
    // Внутренние классы для статистики и кэширования
    private static class QuitRecord {
        private final UUID playerId;
        private final String reason;
        private final long timestamp;
        private final String world;
        private final String location;
        
        public QuitRecord(UUID playerId, String reason, long timestamp, String world, String location) {
            this.playerId = playerId;
            this.reason = reason;
            this.timestamp = timestamp;
            this.world = world;
            this.location = location;
        }
        
        // Геттеры
        public UUID getPlayerId() { return playerId; }
        public String getReason() { return reason; }
        public long getTimestamp() { return timestamp; }
        public String getWorld() { return world; }
        public String getLocation() { return location; }
    }
    
    private static class PlayerQuitStats {
        private final List<QuitRecord> quits = new ArrayList<>();
        private long lastQuitTime = 0;
        private long totalOnlineTime = 0;
        
        public void addQuit(String reason, long timestamp) {
            quits.add(new QuitRecord(null, reason, timestamp, "", ""));
            lastQuitTime = timestamp;
        }
        
        public void setLastQuitTime(long time) { this.lastQuitTime = time; }
        public long getLastQuitTime() { return lastQuitTime; }
        public List<QuitRecord> getQuits() { return quits; }
    }
    
    private static class GlobalQuitStats {
        private int totalQuits = 0;
        private int normalQuits = 0;
        private int timeoutQuits = 0;
        private int kickQuits = 0;
        private int banQuits = 0;
        private int crashQuits = 0;
        private long totalProcessingTime = 0;
        
        public void addQuit(String reason, long processingTime) {
            totalQuits++;
            totalProcessingTime += processingTime;
            
            switch (reason.toUpperCase()) {
                case "NORMAL": normalQuits++; break;
                case "TIMEOUT": timeoutQuits++; break;
                case "KICK": kickQuits++; break;
                case "BAN": banQuits++; break;
                case "CRASH": crashQuits++; break;
            }
        }
        
        // Геттеры
        public int getTotalQuits() { return totalQuits; }
        public int getNormalQuits() { return normalQuits; }
        public int getTimeoutQuits() { return timeoutQuits; }
        public int getKickQuits() { return kickQuits; }
        public int getBanQuits() { return banQuits; }
        public int getCrashQuits() { return crashQuits; }
        public long getTotalProcessingTime() { return totalProcessingTime; }
        public double getAverageProcessingTime() { return totalQuits > 0 ? (double) totalProcessingTime / totalQuits : 0.0; }
    }
    
    private static class QuitEventRequest {
        private final UUID playerId;
        private final long requestTime;
        private final boolean priority;
        
        public QuitEventRequest(UUID playerId, long requestTime, boolean priority) {
            this.playerId = playerId;
            this.requestTime = requestTime;
            this.priority = priority;
        }
        
        public UUID getPlayerId() { return playerId; }
        public long getRequestTime() { return requestTime; }
        public boolean isPriority() { return priority; }
    }
}
