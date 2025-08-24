package ru.openhousing.coding.blocks.events;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import ru.openhousing.OpenHousing;
import ru.openhousing.coding.blocks.BlockType;
import ru.openhousing.coding.blocks.CodeBlock;
import ru.openhousing.coding.blocks.CodeBlock.ExecutionContext;
import ru.openhousing.coding.blocks.CodeBlock.ExecutionResult;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;
import org.bukkit.Location;

/**
 * Специализированный блок для события чата игрока
 * Обрабатывает чат с детальным контролем, фильтрацией и модерацией
 * 
 * @author OpenHousing Team
 * @version 1.0.0
 */
public class PlayerChatEventBlock extends CodeBlock implements Listener {
    
    // Статические поля для глобального управления
    private static final Map<UUID, ChatHistory> chatHistory = new ConcurrentHashMap<>();
    private static final Map<String, Integer> chatStats = new ConcurrentHashMap<>();
    private static final Set<UUID> mutedPlayers = ConcurrentHashMap.newKeySet();
    private static final Set<UUID> slowChatPlayers = ConcurrentHashMap.newKeySet();
    private static final AtomicInteger totalMessages = new AtomicInteger(0);
    private static final AtomicInteger filteredMessages = new AtomicInteger(0);
    
    // Настройки блока
    private boolean chatEnabled = true;
    private boolean requirePermission = false;
    private String requiredPermission = "openhousing.chat";
    private boolean cooldownEnabled = true;
    private long cooldownTime = 1000; // 1 секунда
    private boolean rateLimitEnabled = true;
    private int maxMessagesPerMinute = 10;
    private boolean antiSpamEnabled = true;
    private boolean antiFloodEnabled = true;
    
    // Настройки фильтрации
    private boolean filterEnabled = true;
    private List<String> blockedWords = new ArrayList<>();
    private List<Pattern> blockedPatterns = new ArrayList<>();
    private List<String> allowedWords = new ArrayList<>();
    private List<Pattern> allowedPatterns = new ArrayList<>();
    private boolean filterCaps = true;
    private int maxCapsPercent = 70;
    private boolean filterUrls = true;
    private boolean filterColors = false;
    private boolean filterEmojis = false;
    
    // Настройки модерации
    private boolean autoModeration = true;
    private boolean warnOnViolation = true;
    private boolean muteOnViolation = false;
    private long muteDuration = 300000; // 5 минут
    private boolean kickOnViolation = false;
    private boolean banOnViolation = false;
    private long banDuration = 3600000; // 1 час
    
    // Настройки форматирования
    private boolean customFormat = false;
    private String chatFormat = "§7[{world}] §f{player}§7: §f{message}";
    private boolean showWorld = true;
    private boolean showRank = false;
    private boolean showBalance = false;
    private boolean showLevel = false;
    
    // Настройки уведомлений
    private boolean notifyAdmins = false;
    private String adminNotificationFormat = "&c[Chat] &f{player} &7said: &e{message}";
    private boolean notifyStaff = false;
    private String staffNotificationFormat = "&a[Chat] &f{player} &7in &e{world}";
    private boolean logChat = true;
    
    // Настройки логирования
    private boolean logToConsole = true;
    private boolean logToFile = false;
    private String logFormat = "[{timestamp}] {player}: {message}";
    private boolean logLocation = true;
    private boolean logWorld = true;
    private boolean logIP = false;
    
    // Настройки статистики
    private boolean collectStats = true;
    private boolean trackPlayerStats = true;
    private boolean trackGlobalStats = true;
    private boolean exportStats = false;
    private String statsExportPath = "plugins/OpenHousing/chat_stats/";
    
    // Настройки производительности
    private boolean asyncProcessing = true;
    private boolean cacheResults = true;
    private int cacheSize = 1000;
    private long cacheExpiry = 300000; // 5 минут
    
    // Внутренние кэши и состояния
    private final Map<String, CachedChatResult> chatCache = new ConcurrentHashMap<>();
    private final Map<UUID, PlayerChatStats> playerStats = new ConcurrentHashMap<>();
    private final Map<String, GlobalChatStats> globalStats = new ConcurrentHashMap<>();
    private final Queue<ChatRequest> pendingChat = new LinkedList<>();
    
    public PlayerChatEventBlock() {
        super(BlockType.PLAYER_CHAT);
        initializeDefaultSettings();
        registerListener();
    }
    
    /**
     * Инициализация настроек по умолчанию
     */
    private void initializeDefaultSettings() {
        // Базовые настройки
        chatEnabled = true;
        cooldownEnabled = true;
        rateLimitEnabled = true;
        antiSpamEnabled = true;
        
        // Настройки фильтрации
        filterEnabled = true;
        filterCaps = true;
        maxCapsPercent = 70;
        filterUrls = true;
        
        // Заблокированные слова по умолчанию
        blockedWords.addAll(Arrays.asList(
            "spam", "advertisement", "scam", "hack", "cheat"
        ));
        
        // Заблокированные паттерны
        blockedPatterns.add(Pattern.compile("\\b(?:https?://|www\\.)\\S+", Pattern.CASE_INSENSITIVE));
        blockedPatterns.add(Pattern.compile("\\b[A-Z]{5,}\\b")); // Слишком много заглавных букв
        
        // Инициализация статистики
        chatStats.put("TOTAL", 0);
        chatStats.put("FILTERED", 0);
        chatStats.put("BLOCKED", 0);
        chatStats.put("MODERATED", 0);
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
            System.err.println("Failed to register PlayerChatEventBlock listener: " + e.getMessage());
        }
    }
    
    /**
     * Обработка события чата
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        String message = event.getMessage();
        
        if (player == null || message == null || message.trim().isEmpty()) {
            return;
        }
        
        // Проверка базовых условий
        if (!shouldProcessChat(player, message)) {
            return;
        }
        
        // Создание контекста выполнения
        ExecutionContext context = new ExecutionContext(player);
        context.setVariable("timestamp", System.currentTimeMillis());
        context.setVariable("playerName", player.getName());
        context.setVariable("playerUUID", player.getUniqueId().toString());
        context.setVariable("message", message);
        context.setVariable("world", player.getWorld().getName());
        context.setVariable("location", formatLocation(player.getLocation()));
        context.setVariable("chatTime", System.currentTimeMillis());
        context.setVariable("recipients", event.getRecipients().size());
        
        // Выполнение блока
        ExecutionResult result = execute(context);
        
        // Обработка результата
        handleExecutionResult(event, result, context);
        
        // Обновление статистики
        updateStatistics(player, message, result);
        
        // Логирование
        logPlayerChat(player, message, result, context);
        
        // Уведомления
        sendNotifications(player, message, result, context);
    }
    
    /**
     * Проверка, следует ли обрабатывать чат
     */
    private boolean shouldProcessChat(Player player, String message) {
        // Проверка базовых условий
        if (!chatEnabled || player == null || message == null) {
            return false;
        }
        
        // Проверка разрешений
        if (requirePermission && !player.hasPermission(requiredPermission)) {
            return false;
        }
        
        // Проверка мута
        if (mutedPlayers.contains(player.getUniqueId())) {
            return false;
        }
        
        // Проверка кулдауна
        if (cooldownEnabled && isOnCooldown(player)) {
            return false;
        }
        
        // Проверка лимита сообщений
        if (rateLimitEnabled && isRateLimited(player)) {
            return false;
        }
        
        // Проверка спама
        if (antiSpamEnabled && isSpamDetected(player, message)) {
            return false;
        }
        
        // Проверка флуда
        if (antiFloodEnabled && isFloodDetected(player, message)) {
            return false;
        }
        
        return true;
    }
    
    /**
     * Проверка кулдауна
     */
    private boolean isOnCooldown(Player player) {
        PlayerChatStats stats = playerStats.get(player.getUniqueId());
        if (stats == null) {
            return false;
        }
        
        long lastMessageTime = stats.getLastMessageTime();
        long currentTime = System.currentTimeMillis();
        
        return (currentTime - lastMessageTime) < cooldownTime;
    }
    
    /**
     * Проверка лимита сообщений
     */
    private boolean isRateLimited(Player player) {
        PlayerChatStats stats = playerStats.get(player.getUniqueId());
        if (stats == null) {
            return false;
        }
        
        long currentTime = System.currentTimeMillis();
        long oneMinuteAgo = currentTime - 60000;
        
        // Удаляем старые сообщения
        stats.getRecentMessages().removeIf(time -> time < oneMinuteAgo);
        
        return stats.getRecentMessages().size() >= maxMessagesPerMinute;
    }
    
    /**
     * Проверка спама
     */
    private boolean isSpamDetected(Player player, String message) {
        PlayerChatStats stats = playerStats.get(player.getUniqueId());
        if (stats == null) {
            return false;
        }
        
        long currentTime = System.currentTimeMillis();
        long fiveSecondsAgo = currentTime - 5000;
        
        // Удаляем старые сообщения
        stats.getRecentMessages().removeIf(time -> time < fiveSecondsAgo);
        
        // Проверяем количество одинаковых сообщений
        long sameMessageCount = stats.getRecentMessages().stream()
            .filter(time -> time >= fiveSecondsAgo)
            .count();
        
        return sameMessageCount > 3; // Максимум 3 сообщения за 5 секунд
    }
    
    /**
     * Проверка флуда
     */
    private boolean isFloodDetected(Player player, String message) {
        PlayerChatStats stats = playerStats.get(player.getUniqueId());
        if (stats == null) {
            return false;
        }
        
        long currentTime = System.currentTimeMillis();
        long tenSecondsAgo = currentTime - 10000;
        
        // Удаляем старые сообщения
        stats.getRecentMessages().removeIf(time -> time < tenSecondsAgo);
        
        // Проверяем общее количество сообщений
        return stats.getRecentMessages().size() > 5; // Максимум 5 сообщений за 10 секунд
    }
    
    @Override
    public ExecutionResult execute(ExecutionContext context) {
        try {
            totalMessages.incrementAndGet();
            
            // Асинхронная обработка
            if (asyncProcessing) {
                return executeAsync(context);
            } else {
                return executeSync(context);
            }
        } catch (Exception e) {
            return ExecutionResult.error("Ошибка выполнения блока чата: " + e.getMessage());
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
            CachedChatResult cached = chatCache.get(player.getUniqueId().toString());
            if (cached != null && !cached.isExpired()) {
                return cached.getResult();
            }
        }
        
        // Выполнение логики чата
        ExecutionResult result = processChatLogic(context);
        
        // Кэширование результата
        if (cacheResults && result != null) {
            String cacheKey = player.getUniqueId().toString();
            chatCache.put(cacheKey, new CachedChatResult(result, System.currentTimeMillis() + cacheExpiry));
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
     * Основная логика обработки чата
     */
    private ExecutionResult processChatLogic(ExecutionContext context) {
        Player player = context.getPlayer();
        String message = context.getVariable("message").toString();
        
        try {
            // Фильтрация сообщения
            if (filterEnabled) {
                FilterResult filterResult = filterMessage(message);
                if (!filterResult.isAllowed()) {
                    filteredMessages.incrementAndGet();
                    return ExecutionResult.error("Сообщение заблокировано: " + filterResult.getReason());
                }
            }
            
            // Модерация сообщения
            if (autoModeration) {
                ModerationResult moderationResult = moderateMessage(player, message);
                if (moderationResult.requiresAction()) {
                    applyModerationAction(player, moderationResult);
                }
            }
            
            // Форматирование сообщения
            if (customFormat) {
                String formattedMessage = formatChatMessage(player, message, context);
                context.setVariable("formattedMessage", formattedMessage);
            }
            
            return ExecutionResult.success("Сообщение успешно обработано");
            
        } catch (Exception e) {
            return ExecutionResult.error("Ошибка обработки чата: " + e.getMessage());
        }
    }
    
    /**
     * Фильтрация сообщения
     */
    private FilterResult filterMessage(String message) {
        // Проверка заблокированных слов
        for (String blockedWord : blockedWords) {
            if (message.toLowerCase().contains(blockedWord.toLowerCase())) {
                return new FilterResult(false, "Содержит заблокированное слово: " + blockedWord);
            }
        }
        
        // Проверка заблокированных паттернов
        for (Pattern pattern : blockedPatterns) {
            if (pattern.matcher(message).find()) {
                return new FilterResult(false, "Соответствует заблокированному паттерну");
            }
        }
        
        // Проверка заглавных букв
        if (filterCaps) {
            int capsCount = 0;
            int totalChars = 0;
            
            for (char c : message.toCharArray()) {
                if (Character.isLetter(c)) {
                    totalChars++;
                    if (Character.isUpperCase(c)) {
                        capsCount++;
                    }
                }
            }
            
            if (totalChars > 0 && (double) capsCount / totalChars * 100 > maxCapsPercent) {
                return new FilterResult(false, "Слишком много заглавных букв");
            }
        }
        
        // Проверка URL
        if (filterUrls) {
            if (message.contains("http://") || message.contains("https://") || message.contains("www.")) {
                return new FilterResult(false, "Содержит URL");
            }
        }
        
        // Проверка цветов
        if (filterColors) {
            if (message.contains("§") || message.contains("&")) {
                return new FilterResult(false, "Содержит цветовые коды");
            }
        }
        
        return new FilterResult(true, "Сообщение прошло фильтрацию");
    }
    
    /**
     * Модерация сообщения
     */
    private ModerationResult moderateMessage(Player player, String message) {
        ModerationResult result = new ModerationResult();
        
        // Проверка на спам
        if (isSpamDetected(player, message)) {
            result.setAction(ModerationAction.WARN);
            result.setReason("Спам в чате");
        }
        
        // Проверка на оскорбления
        if (containsOffensiveLanguage(message)) {
            result.setAction(ModerationAction.MUTE);
            result.setReason("Оскорбительный язык");
            result.setDuration(muteDuration);
        }
        
        // Проверка на рекламу
        if (containsAdvertisement(message)) {
            result.setAction(ModerationAction.MUTE);
            result.setReason("Реклама");
            result.setDuration(muteDuration);
        }
        
        return result;
    }
    
    /**
     * Проверка на оскорбительный язык
     */
    private boolean containsOffensiveLanguage(String message) {
        // TODO: Реализовать проверку на оскорбительный язык
        // Это может быть через API или собственный словарь
        return false;
    }
    
    /**
     * Проверка на рекламу
     */
    private boolean containsAdvertisement(String message) {
        // TODO: Реализовать проверку на рекламу
        // Это может быть через API или собственные паттерны
        return false;
    }
    
    /**
     * Применение действия модерации
     */
    private void applyModerationAction(Player player, ModerationResult result) {
        switch (result.getAction()) {
            case WARN:
                if (warnOnViolation) {
                    player.sendMessage("§c[Модерация] " + result.getReason());
                }
                break;
                
            case MUTE:
                if (muteOnViolation) {
                    mutePlayer(player, result.getDuration());
                    player.sendMessage("§c[Модерация] Вы получили мут: " + result.getReason());
                }
                break;
                
            case KICK:
                if (kickOnViolation) {
                    player.kickPlayer("§c[Модерация] " + result.getReason());
                }
                break;
                
            case BAN:
                if (banOnViolation) {
                    // TODO: Реализовать бан через Bukkit API
                    player.sendMessage("§c[Модерация] Вы получили бан: " + result.getReason());
                }
                break;
        }
    }
    
    /**
     * Мут игрока
     */
    private void mutePlayer(Player player, long duration) {
        mutedPlayers.add(player.getUniqueId());
        
        // Автоматическое снятие мута
        Bukkit.getScheduler().runTaskLater(OpenHousing.getInstance(), () -> {
            mutedPlayers.remove(player.getUniqueId());
            if (player.isOnline()) {
                player.sendMessage("§a[Модерация] Ваш мут снят");
            }
        }, duration / 50L); // Конвертируем в тики
    }
    
    /**
     * Форматирование сообщения чата
     */
    private String formatChatMessage(Player player, String message, ExecutionContext context) {
        String formatted = chatFormat
            .replace("{player}", player.getName())
            .replace("{message}", message)
            .replace("{world}", context.getVariable("world").toString())
            .replace("{location}", context.getVariable("location").toString());
        
        // Добавление ранга
        if (showRank) {
            String rank = getPlayerRank(player);
            formatted = formatted.replace("{rank}", rank);
        }
        
        // Добавление баланса
        if (showBalance) {
            String balance = getPlayerBalance(player);
            formatted = formatted.replace("{balance}", balance);
        }
        
        // Добавление уровня
        if (showLevel) {
            String level = String.valueOf(player.getLevel());
            formatted = formatted.replace("{level}", level);
        }
        
        return ChatColor.translateAlternateColorCodes('&', formatted);
    }
    
    /**
     * Получение ранга игрока
     */
    private String getPlayerRank(Player player) {
        // TODO: Реализовать через Vault или другой плагин
        return "Player";
    }
    
    /**
     * Получение баланса игрока
     */
    private String getPlayerBalance(Player player) {
        // TODO: Реализовать через Vault или другой плагин
        return "0.0";
    }
    
    /**
     * Обработка результата выполнения
     */
    private void handleExecutionResult(AsyncPlayerChatEvent event, ExecutionResult result, ExecutionContext context) {
        if (result == null) {
            return;
        }
        
        if (!result.isSuccess()) {
            // Отменяем сообщение при ошибке
            event.setCancelled(true);
            
            // Отправляем сообщение об ошибке
            Player player = event.getPlayer();
            if (player != null && player.isOnline()) {
                player.sendMessage("§c" + result.getMessage());
            }
        } else {
            // Сообщение обработано успешно
            Player player = event.getPlayer();
            if (player != null) {
                // Обновляем время последнего сообщения
                PlayerChatStats stats = playerStats.computeIfAbsent(
                    player.getUniqueId(), k -> new PlayerChatStats());
                stats.setLastMessageTime(System.currentTimeMillis());
                stats.addMessage(System.currentTimeMillis());
            }
        }
    }
    
    /**
     * Обновление статистики
     */
    private void updateStatistics(Player player, String message, ExecutionResult result) {
        if (!collectStats) {
            return;
        }
        
        // Обновление глобальной статистики
        if (trackGlobalStats) {
            chatStats.merge("TOTAL", 1, Integer::sum);
            
            if (result != null && !result.isSuccess()) {
                chatStats.merge("BLOCKED", 1, Integer::sum);
            }
        }
        
        // Обновление статистики игрока
        if (trackPlayerStats) {
            PlayerChatStats playerStats = this.playerStats.computeIfAbsent(
                player.getUniqueId(), k -> new PlayerChatStats());
            
            playerStats.addMessage(System.currentTimeMillis());
        }
    }
    
    /**
     * Логирование чата игрока
     */
    private void logPlayerChat(Player player, String message, ExecutionResult result, ExecutionContext context) {
        if (!logChat) {
            return;
        }
        
        String logMessage = logFormat
            .replace("{timestamp}", new java.util.Date().toString())
            .replace("{player}", player.getName())
            .replace("{message}", message)
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
        saveToHistory(player.getUniqueId(), message, result, context);
    }
    
    /**
     * Отправка уведомлений
     */
    private void sendNotifications(Player player, String message, ExecutionResult result, ExecutionContext context) {
        // Уведомление администраторов
        if (notifyAdmins) {
            String adminMessage = adminNotificationFormat
                .replace("{player}", player.getName())
                .replace("{message}", message)
                .replace("{world}", context.getVariable("world").toString())
                .replace("{location}", context.getVariable("location").toString());
            
            Bukkit.getOnlinePlayers().stream()
                .filter(p -> p.hasPermission("openhousing.admin.notify"))
                .forEach(p -> p.sendMessage(ChatColor.translateAlternateColorCodes('&', adminMessage)));
        }
        
        // Уведомление персонала
        if (notifyStaff) {
            String staffMessage = staffNotificationFormat
                .replace("{player}", player.getName())
                .replace("{world}", context.getVariable("world").toString());
            
            Bukkit.getOnlinePlayers().stream()
                .filter(p -> p.hasPermission("openhousing.staff.notify"))
                .forEach(p -> p.sendMessage(ChatColor.translateAlternateColorCodes('&', staffMessage)));
        }
    }
    
    /**
     * Сохранение в историю
     */
    private void saveToHistory(UUID playerId, String message, ExecutionResult result, ExecutionContext context) {
        ChatRecord record = new ChatRecord(
            playerId,
            System.currentTimeMillis(),
            message,
            result != null ? result.isSuccess() : false,
            result != null ? result.getMessage() : null,
            context.getVariable("world").toString(),
            context.getVariable("location").toString()
        );
        
        chatHistory.computeIfAbsent(playerId, k -> new ChatHistory())
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
        if (maxMessagesPerMinute < 1) return false;
        if (maxCapsPercent < 0 || maxCapsPercent > 100) return false;
        if (muteDuration < 0) return false;
        if (banDuration < 0) return false;
        if (cacheSize < 1) return false;
        if (cacheExpiry < 0) return false;
        
        return true;
    }
    
    @Override
    public List<String> getDescription() {
        List<String> description = new ArrayList<>();
        description.add("§6Блок события чата игрока");
        description.add("§7Обрабатывает чат с детальным контролем");
        description.add("§7и расширенными возможностями");
        description.add("");
        description.add("§eНастройки:");
        description.add("§7• Фильтрация: " + (filterEnabled ? "§aВключена" : "§cВыключена"));
        description.add("§7• Модерация: " + (autoModeration ? "§aВключена" : "§cВыключена"));
        description.add("§7• Кулдаун: " + (cooldownEnabled ? "§a" + cooldownTime + "мс" : "§cВыключен"));
        description.add("§7• Лимит сообщений: " + (rateLimitEnabled ? "§a" + maxMessagesPerMinute + "/мин" : "§cВыключен"));
        description.add("§7• Анти-спам: " + (antiSpamEnabled ? "§aВключен" : "§cВыключен"));
        
        return description;
    }
    
    // Внутренние классы для фильтрации и модерации
    private static class FilterResult {
        private final boolean allowed;
        private final String reason;
        
        public FilterResult(boolean allowed, String reason) {
            this.allowed = allowed;
            this.reason = reason;
        }
        
        public boolean isAllowed() { return allowed; }
        public String getReason() { return reason; }
    }
    
    private static class ModerationResult {
        private ModerationAction action = ModerationAction.NONE;
        private String reason = "";
        private long duration = 0;
        
        public boolean requiresAction() { return action != ModerationAction.NONE; }
        public ModerationAction getAction() { return action; }
        public String getReason() { return reason; }
        public long getDuration() { return duration; }
        
        public void setAction(ModerationAction action) { this.action = action; }
        public void setReason(String reason) { this.reason = reason; }
        public void setDuration(long duration) { this.duration = duration; }
    }
    
    private enum ModerationAction {
        NONE, WARN, MUTE, KICK, BAN
    }
    
    // Внутренние классы для кэширования и статистики
    private static class CachedChatResult {
        private final ExecutionResult result;
        private final long expiryTime;
        
        public CachedChatResult(ExecutionResult result, long expiryTime) {
            this.result = result;
            this.expiryTime = expiryTime;
        }
        
        public ExecutionResult getResult() { return result; }
        public boolean isExpired() { return System.currentTimeMillis() > expiryTime; }
    }
    
    private static class ChatRecord {
        private final UUID playerId;
        private final long timestamp;
        private final String message;
        private final boolean success;
        private final String resultMessage;
        private final String world;
        private final String location;
        
        public ChatRecord(UUID playerId, long timestamp, String message, 
                         boolean success, String resultMessage, String world, String location) {
            this.playerId = playerId;
            this.timestamp = timestamp;
            this.message = message;
            this.success = success;
            this.resultMessage = resultMessage;
            this.world = world;
            this.location = location;
        }
        
        // Геттеры
        public UUID getPlayerId() { return playerId; }
        public long getTimestamp() { return timestamp; }
        public String getMessage() { return message; }
        public boolean isSuccess() { return success; }
        public String getResultMessage() { return resultMessage; }
        public String getWorld() { return world; }
        public String getLocation() { return location; }
    }
    
    private static class ChatHistory {
        private final List<ChatRecord> records = new ArrayList<>();
        
        public void addRecord(ChatRecord record) {
            records.add(record);
        }
        
        public List<ChatRecord> getRecords() { return records; }
    }
    
    private static class PlayerChatStats {
        private final List<Long> recentMessages = new ArrayList<>();
        private long lastMessageTime = 0;
        
        public void addMessage(long timestamp) {
            recentMessages.add(timestamp);
        }
        
        public void setLastMessageTime(long time) { this.lastMessageTime = time; }
        public long getLastMessageTime() { return lastMessageTime; }
        public List<Long> getRecentMessages() { return recentMessages; }
    }
    
    private static class GlobalChatStats {
        private int totalMessages = 0;
        private int filteredMessages = 0;
        private int moderatedMessages = 0;
        private long totalProcessingTime = 0;
        
        public void addMessage(boolean filtered, long processingTime) {
            totalMessages++;
            if (filtered) {
                filteredMessages++;
            }
            totalProcessingTime += processingTime;
        }
        
        // Геттеры
        public int getTotalMessages() { return totalMessages; }
        public int getFilteredMessages() { return filteredMessages; }
        public int getModeratedMessages() { return moderatedMessages; }
        public long getTotalProcessingTime() { return totalProcessingTime; }
        public double getFilterRate() { return totalMessages > 0 ? (double) filteredMessages / totalMessages : 0.0; }
        public double getAverageProcessingTime() { return totalMessages > 0 ? (double) totalProcessingTime / totalMessages : 0.0; }
    }
    
    private static class ChatRequest {
        private final UUID playerId;
        private final long requestTime;
        private final boolean priority;
        
        public ChatRequest(UUID playerId, long requestTime, boolean priority) {
            this.playerId = playerId;
            this.requestTime = requestTime;
            this.priority = priority;
        }
        
        public UUID getPlayerId() { return playerId; }
        public long getRequestTime() { return requestTime; }
        public boolean isPriority() { return priority; }
    }
}
