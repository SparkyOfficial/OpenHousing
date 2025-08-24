package ru.openhousing.coding.blocks.events;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.inventory.ItemStack;
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
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Специализированный блок для события использования команды игроком
 * Обрабатывает команды с детальной фильтрацией и логированием
 * 
 * @author OpenHousing Team
 * @version 1.0.0
 */
public class PlayerCommandEventBlock extends CodeBlock implements Listener {
    
    // Статические поля для глобального управления
    private static final Map<UUID, List<CommandExecution>> commandHistory = new ConcurrentHashMap<>();
    private static final Map<String, Integer> commandUsageStats = new ConcurrentHashMap<>();
    private static final Set<String> blockedCommands = ConcurrentHashMap.newKeySet();
    private static final Set<String> allowedCommands = ConcurrentHashMap.newKeySet();
    private static final Map<String, String> commandAliases = new ConcurrentHashMap<>();
    
    // Настройки блока
    private boolean logCommands = true;
    private boolean blockUnsafeCommands = true;
    private boolean requirePermission = false;
    private String requiredPermission = "";
    private boolean cooldownEnabled = false;
    private long cooldownTime = 1000; // 1 секунда
    private boolean rateLimitEnabled = false;
    private int maxCommandsPerMinute = 10;
    private boolean customResponseEnabled = false;
    private String customResponseMessage = "";
    private boolean commandValidationEnabled = true;
    private boolean antiSpamEnabled = true;
    private boolean commandHistoryEnabled = true;
    private int maxHistorySize = 100;
    
    // Фильтры команд
    private Set<String> allowedCommandPatterns = new HashSet<>();
    private Set<String> blockedCommandPatterns = new HashSet<>();
    private Set<String> whitelistedCommands = new HashSet<>();
    private Set<String> blacklistedCommands = new HashSet<>();
    
    // Настройки логирования
    private boolean logToConsole = true;
    private boolean logToFile = false;
    private String logFormat = "[{timestamp}] {player} used command: {command}";
    private boolean logArguments = true;
    private boolean logLocation = true;
    private boolean logWorld = true;
    
    // Настройки безопасности
    private boolean preventCommandInjection = true;
    private boolean validateArguments = true;
    private boolean checkCommandPermissions = true;
    private boolean preventSpam = true;
    private boolean preventFlood = true;
    
    // Настройки уведомлений
    private boolean notifyAdmins = false;
    private String adminNotificationFormat = "&c[Command] &f{player} &7used: &e{command}";
    private boolean notifyPlayer = false;
    private String playerNotificationFormat = "&aКоманда выполнена: &e{command}";
    
    // Настройки статистики
    private boolean collectStats = true;
    private boolean trackPlayerStats = true;
    private boolean trackGlobalStats = true;
    private boolean exportStats = false;
    private String statsExportPath = "plugins/OpenHousing/stats/";
    
    // Настройки производительности
    private boolean asyncProcessing = true;
    private boolean cacheResults = true;
    private int cacheSize = 1000;
    private long cacheExpiry = 300000; // 5 минут
    
    // Внутренние кэши
    private final Map<String, CachedCommand> commandCache = new ConcurrentHashMap<>();
    private final Map<UUID, PlayerCommandStats> playerStats = new ConcurrentHashMap<>();
    private final Map<String, GlobalCommandStats> globalStats = new ConcurrentHashMap<>();
    
    public PlayerCommandEventBlock() {
        super(BlockType.PLAYER_COMMAND);
        initializeDefaultSettings();
        registerListener();
    }
    
    /**
     * Инициализация настроек по умолчанию
     */
    private void initializeDefaultSettings() {
        // Базовые настройки безопасности
        blockedCommands.addAll(Arrays.asList(
            "op", "deop", "stop", "restart", "reload", "save-all",
            "save-off", "save-on", "whitelist", "ban", "ban-ip",
            "pardon", "pardon-ip", "kick", "tp", "tphere", "tppos"
        ));
        
        // Разрешенные команды
        allowedCommands.addAll(Arrays.asList(
            "help", "list", "me", "msg", "tell", "reply",
            "home", "sethome", "delhome", "spawn", "tpa", "tpahere"
        ));
        
        // Паттерны для фильтрации
        allowedCommandPatterns.addAll(Arrays.asList(
            "^[a-zA-Z0-9_]+$", // Только буквы, цифры и подчеркивания
            "^[a-zA-Z]+$",     // Только буквы
            "^[a-zA-Z]+\\s+[a-zA-Z0-9_\\s]*$" // Команда + аргументы
        ));
        
        // Заблокированные паттерны
        blockedCommandPatterns.addAll(Arrays.asList(
            ".*[<>\"'&].*",    // HTML-теги и специальные символы
            ".*\\|.*",         // Пайпы
            ".*;.*",           // Точки с запятой
            ".*\\$.*",         // Доллары
            ".*\\{.*",         // Фигурные скобки
            ".*\\}.*"          // Закрывающие скобки
        ));
        
        // Алиасы команд
        commandAliases.put("h", "help");
        commandAliases.put("l", "list");
        commandAliases.put("m", "msg");
        commandAliases.put("t", "tell");
        commandAliases.put("r", "reply");
        commandAliases.put("s", "spawn");
        commandAliases.put("tp", "teleport");
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
            System.err.println("Failed to register PlayerCommandEventBlock listener: " + e.getMessage());
        }
    }
    
    /**
     * Обработка события использования команды
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        String command = event.getMessage();
        
        if (player == null || command == null || command.trim().isEmpty()) {
            return;
        }
        
        // Проверка базовых условий
        if (!shouldProcessCommand(player, command)) {
            return;
        }
        
        // Создание контекста выполнения
        ExecutionContext context = new ExecutionContext(player);
        context.setVariable("command", command);
        context.setVariable("commandName", extractCommandName(command));
        context.setVariable("commandArgs", extractCommandArguments(command));
        context.setVariable("timestamp", System.currentTimeMillis());
        context.setVariable("playerName", player.getName());
        context.setVariable("playerUUID", player.getUniqueId().toString());
        context.setVariable("world", player.getWorld().getName());
        context.setVariable("location", formatLocation(player.getLocation()));
        
        // Выполнение блока
        ExecutionResult result = execute(context);
        
        // Обработка результата
        handleExecutionResult(event, result, context);
        
        // Обновление статистики
        updateStatistics(player, command, result);
        
        // Логирование
        logCommandExecution(player, command, result, context);
        
        // Уведомления
        sendNotifications(player, command, result, context);
    }
    
    /**
     * Проверка, следует ли обрабатывать команду
     */
    private boolean shouldProcessCommand(Player player, String command) {
        // Проверка разрешений
        if (requirePermission && !player.hasPermission(requiredPermission)) {
            return false;
        }
        
        // Проверка кулдауна
        if (cooldownEnabled && isOnCooldown(player)) {
            return false;
        }
        
        // Проверка лимита команд
        if (rateLimitEnabled && isRateLimited(player)) {
            return false;
        }
        
        // Проверка блокировки
        if (blockUnsafeCommands && isCommandBlocked(command)) {
            return false;
        }
        
        // Проверка валидации
        if (commandValidationEnabled && !isCommandValid(command)) {
            return false;
        }
        
        // Проверка анти-спама
        if (antiSpamEnabled && isSpamDetected(player, command)) {
            return false;
        }
        
        return true;
    }
    
    /**
     * Проверка кулдауна
     */
    private boolean isOnCooldown(Player player) {
        PlayerCommandStats stats = playerStats.get(player.getUniqueId());
        if (stats == null) {
            return false;
        }
        
        long lastCommandTime = stats.getLastCommandTime();
        long currentTime = System.currentTimeMillis();
        
        return (currentTime - lastCommandTime) < cooldownTime;
    }
    
    /**
     * Проверка лимита команд
     */
    private boolean isRateLimited(Player player) {
        PlayerCommandStats stats = playerStats.get(player.getUniqueId());
        if (stats == null) {
            return false;
        }
        
        long currentTime = System.currentTimeMillis();
        long oneMinuteAgo = currentTime - 60000;
        
        // Удаляем старые команды
        stats.getRecentCommands().removeIf(time -> time < oneMinuteAgo);
        
        return stats.getRecentCommands().size() >= maxCommandsPerMinute;
    }
    
    /**
     * Проверка блокировки команды
     */
    private boolean isCommandBlocked(String command) {
        String commandName = extractCommandName(command).toLowerCase();
        
        // Проверка черного списка
        if (blacklistedCommands.contains(commandName)) {
            return true;
        }
        
        // Проверка заблокированных команд
        if (blockedCommands.contains(commandName)) {
            return true;
        }
        
        // Проверка паттернов
        for (String pattern : blockedCommandPatterns) {
            if (command.matches(pattern)) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Проверка валидности команды
     */
    private boolean isCommandValid(String command) {
        // Проверка длины
        if (command.length() > 256) {
            return false;
        }
        
        // Проверка паттернов
        for (String pattern : allowedCommandPatterns) {
            if (command.matches(pattern)) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Проверка спама
     */
    private boolean isSpamDetected(Player player, String command) {
        PlayerCommandStats stats = playerStats.get(player.getUniqueId());
        if (stats == null) {
            return false;
        }
        
        long currentTime = System.currentTimeMillis();
        long fiveSecondsAgo = currentTime - 5000;
        
        // Удаляем старые команды
        stats.getRecentCommands().removeIf(time -> time < fiveSecondsAgo);
        
        // Проверяем количество одинаковых команд
        long sameCommandCount = stats.getRecentCommands().stream()
            .filter(time -> time >= fiveSecondsAgo)
            .count();
        
        return sameCommandCount > 3; // Максимум 3 команды за 5 секунд
    }
    
    /**
     * Извлечение имени команды
     */
    private String extractCommandName(String command) {
        if (command.startsWith("/")) {
            command = command.substring(1);
        }
        
        int spaceIndex = command.indexOf(' ');
        if (spaceIndex == -1) {
            return command.toLowerCase();
        }
        
        return command.substring(0, spaceIndex).toLowerCase();
    }
    
    /**
     * Извлечение аргументов команды
     */
    private String[] extractCommandArguments(String command) {
        if (command.startsWith("/")) {
            command = command.substring(1);
        }
        
        int spaceIndex = command.indexOf(' ');
        if (spaceIndex == -1) {
            return new String[0];
        }
        
        String args = command.substring(spaceIndex + 1);
        return args.split("\\s+");
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
    
    /**
     * Обработка результата выполнения
     */
    private void handleExecutionResult(PlayerCommandPreprocessEvent event, ExecutionResult result, ExecutionContext context) {
        if (result == null) {
            return;
        }
        
        if (!result.isSuccess()) {
            // Блокируем команду при ошибке
            event.setCancelled(true);
            
            // Отправляем сообщение об ошибке
            if (notifyPlayer) {
                String message = customResponseMessage.isEmpty() ? 
                    "§cОшибка выполнения команды: " + result.getMessage() :
                    customResponseMessage.replace("{error}", result.getMessage());
                event.getPlayer().sendMessage(message);
            }
        } else {
            // Команда выполнена успешно
            if (notifyPlayer) {
                String message = playerNotificationFormat
                    .replace("{command}", context.getVariable("commandName").toString())
                    .replace("{player}", event.getPlayer().getName());
                event.getPlayer().sendMessage(message);
            }
        }
    }
    
    /**
     * Обновление статистики
     */
    private void updateStatistics(Player player, String command, ExecutionResult result) {
        if (!collectStats) {
            return;
        }
        
        String commandName = extractCommandName(command);
        
        // Обновление глобальной статистики
        globalStats.computeIfAbsent(commandName, k -> new GlobalCommandStats())
            .incrementUsage();
        
        if (result != null && result.isSuccess()) {
            globalStats.get(commandName).incrementSuccess();
        } else {
            globalStats.get(commandName).incrementFailure();
        }
        
        // Обновление статистики игрока
        if (trackPlayerStats) {
            PlayerCommandStats playerStats = this.playerStats.computeIfAbsent(
                player.getUniqueId(), k -> new PlayerCommandStats());
            
            playerStats.addCommand(commandName, System.currentTimeMillis());
            playerStats.setLastCommandTime(System.currentTimeMillis());
        }
        
        // Обновление счетчика использования
        commandUsageStats.merge(commandName, 1, Integer::sum);
    }
    
    /**
     * Логирование выполнения команды
     */
    private void logCommandExecution(Player player, String command, ExecutionResult result, ExecutionContext context) {
        if (!logCommands) {
            return;
        }
        
        String logMessage = logFormat
            .replace("{timestamp}", new java.util.Date().toString())
            .replace("{player}", player.getName())
            .replace("{command}", command)
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
        
        // Логирование в файл
        if (logToFile) {
            // TODO: Реализовать логирование в файл
        }
        
        // Сохранение в историю
        if (commandHistoryEnabled) {
            saveToHistory(player.getUniqueId(), command, result, context);
        }
    }
    
    /**
     * Отправка уведомлений
     */
    private void sendNotifications(Player player, String command, ExecutionResult result, ExecutionContext context) {
        // Уведомление администраторов
        if (notifyAdmins) {
            String adminMessage = adminNotificationFormat
                .replace("{player}", player.getName())
                .replace("{command}", command)
                .replace("{world}", context.getVariable("world").toString())
                .replace("{location}", context.getVariable("location").toString());
            
            Bukkit.getOnlinePlayers().stream()
                .filter(p -> p.hasPermission("openhousing.admin.notify"))
                .forEach(p -> p.sendMessage(ChatColor.translateAlternateColorCodes('&', adminMessage)));
        }
    }
    
    /**
     * Сохранение в историю
     */
    private void saveToHistory(UUID playerId, String command, ExecutionResult result, ExecutionContext context) {
        CommandExecution execution = new CommandExecution(
            playerId,
            command,
            System.currentTimeMillis(),
            result != null ? result.isSuccess() : false,
            result != null ? result.getMessage() : null,
            context.getVariable("world").toString(),
            context.getVariable("location").toString()
        );
        
        commandHistory.computeIfAbsent(playerId, k -> new ArrayList<>())
            .add(execution);
        
        // Ограничение размера истории
        List<CommandExecution> history = commandHistory.get(playerId);
        if (history.size() > maxHistorySize) {
            history.remove(0);
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
            return ExecutionResult.error("Ошибка выполнения блока команды: " + e.getMessage());
        }
    }
    
    @Override
    public boolean validate() {
        // Проверяем базовые параметры
        if (cooldownTime < 0) return false;
        if (maxCommandsPerMinute < 1) return false;
        if (maxHistorySize < 1) return false;
        if (cacheSize < 1) return false;
        if (cacheExpiry < 0) return false;
        
        return true;
    }
    
    @Override
    public List<String> getDescription() {
        List<String> description = new ArrayList<>();
        description.add("§6Блок события использования команды");
        description.add("§7Обрабатывает команды игроков с детальной");
        description.add("§7фильтрацией и логированием");
        description.add("");
        description.add("§eНастройки:");
        description.add("§7• Логирование: " + (logCommands ? "§aВключено" : "§cВыключено"));
        description.add("§7• Блокировка: " + (blockUnsafeCommands ? "§aВключена" : "§cВыключена"));
        description.add("§7• Кулдаун: " + (cooldownEnabled ? "§a" + cooldownTime + "мс" : "§cВыключен"));
        description.add("§7• Лимит: " + (rateLimitEnabled ? "§a" + maxCommandsPerMinute + "/мин" : "§cВыключен"));
        description.add("§7• Анти-спам: " + (antiSpamEnabled ? "§aВключен" : "§cВыключен"));
        description.add("§7• История: " + (commandHistoryEnabled ? "§a" + maxHistorySize + " записей" : "§cВыключена"));
        
        return description;
    }
    
    /**
     * Синхронное выполнение
     */
    private ExecutionResult executeSync(ExecutionContext context) {
        // Проверка кэша
        if (cacheResults) {
            CachedCommand cached = commandCache.get(context.getVariable("commandName").toString());
            if (cached != null && !cached.isExpired()) {
                return cached.getResult();
            }
        }
        
        // Выполнение логики
        ExecutionResult result = processCommandLogic(context);
        
        // Кэширование результата
        if (cacheResults && result != null) {
            commandCache.put(
                context.getVariable("commandName").toString(),
                new CachedCommand(result, System.currentTimeMillis() + cacheExpiry)
            );
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
     * Основная логика обработки команды
     */
    private ExecutionResult processCommandLogic(ExecutionContext context) {
        String commandName = context.getVariable("commandName").toString();
        String[] args = (String[]) context.getVariable("commandArgs");
        
        // Проверка специальных команд
        if (commandName.equals("help")) {
            return processHelpCommand(context, args);
        } else if (commandName.equals("list")) {
            return processListCommand(context, args);
        } else if (commandName.equals("me")) {
            return processMeCommand(context, args);
        }
        
        // Обработка алиасов
        String actualCommand = commandAliases.get(commandName);
        if (actualCommand != null) {
            context.setVariable("actualCommand", actualCommand);
        }
        
        // Стандартная обработка
        return ExecutionResult.success();
    }
    
    /**
     * Обработка команды help
     */
    private ExecutionResult processHelpCommand(ExecutionContext context, String[] args) {
        Player player = context.getPlayer();
        if (player == null) {
            return ExecutionResult.error("Игрок не найден");
        }
        
        // Отправка справки
        player.sendMessage("§6=== Справка по командам ===");
        player.sendMessage("§7/help - показать эту справку");
        player.sendMessage("§7/list - список игроков онлайн");
        player.sendMessage("§7/me <действие> - описать действие");
        player.sendMessage("§7/msg <игрок> <сообщение> - личное сообщение");
        player.sendMessage("§7/home - телепортация домой");
        player.sendMessage("§7/spawn - телепортация на спавн");
        
        return ExecutionResult.success();
    }
    
    /**
     * Обработка команды list
     */
    private ExecutionResult processListCommand(ExecutionContext context, String[] args) {
        Player player = context.getPlayer();
        if (player == null) {
            return ExecutionResult.error("Игрок не найден");
        }
        
        int onlineCount = Bukkit.getOnlinePlayers().size();
        int maxPlayers = Bukkit.getMaxPlayers();
        
        player.sendMessage("§6=== Игроки онлайн ===");
        player.sendMessage("§7Всего: §e" + onlineCount + "§7/§e" + maxPlayers);
        
        if (onlineCount > 0) {
            String playerList = Bukkit.getOnlinePlayers().stream()
                .map(Player::getName)
                .collect(Collectors.joining("§7, §e"));
            player.sendMessage("§7Игроки: §e" + playerList);
        }
        
        return ExecutionResult.success();
    }
    
    /**
     * Обработка команды me
     */
    private ExecutionResult processMeCommand(ExecutionContext context, String[] args) {
        Player player = context.getPlayer();
        if (player == null) {
            return ExecutionResult.error("Игрок не найден");
        }
        
        if (args.length == 0) {
            return ExecutionResult.error("Укажите действие");
        }
        
        String action = String.join(" ", args);
        String message = "§d* " + player.getName() + " " + action;
        
        // Отправка всем игрокам в радиусе
        player.getNearbyEntities(50, 50, 50).stream()
            .filter(entity -> entity instanceof Player)
            .map(entity -> (Player) entity)
            .forEach(p -> p.sendMessage(message));
        
        // Отправка самому игроку
        player.sendMessage(message);
        
        return ExecutionResult.success();
    }
    
    // Геттеры и сеттеры для настройки блока
    public void setLogCommands(boolean logCommands) { this.logCommands = logCommands; }
    public void setBlockUnsafeCommands(boolean blockUnsafeCommands) { this.blockUnsafeCommands = blockUnsafeCommands; }
    public void setRequirePermission(boolean requirePermission) { this.requirePermission = requirePermission; }
    public void setRequiredPermission(String requiredPermission) { this.requiredPermission = requiredPermission; }
    public void setCooldownEnabled(boolean cooldownEnabled) { this.cooldownEnabled = cooldownEnabled; }
    public void setCooldownTime(long cooldownTime) { this.cooldownTime = cooldownTime; }
    public void setRateLimitEnabled(boolean rateLimitEnabled) { this.rateLimitEnabled = rateLimitEnabled; }
    public void setMaxCommandsPerMinute(int maxCommandsPerMinute) { this.maxCommandsPerMinute = maxCommandsPerMinute; }
    public void setCustomResponseEnabled(boolean customResponseEnabled) { this.customResponseEnabled = customResponseEnabled; }
    public void setCustomResponseMessage(String customResponseMessage) { this.customResponseMessage = customResponseMessage; }
    public void setCommandValidationEnabled(boolean commandValidationEnabled) { this.commandValidationEnabled = commandValidationEnabled; }
    public void setAntiSpamEnabled(boolean antiSpamEnabled) { this.antiSpamEnabled = antiSpamEnabled; }
    public void setCommandHistoryEnabled(boolean commandHistoryEnabled) { this.commandHistoryEnabled = commandHistoryEnabled; }
    public void setMaxHistorySize(int maxHistorySize) { this.maxHistorySize = maxHistorySize; }
    
    // Внутренние классы для статистики и кэширования
    private static class CommandExecution {
        private final UUID playerId;
        private final String command;
        private final long timestamp;
        private final boolean success;
        private final String errorMessage;
        private final String world;
        private final String location;
        
        public CommandExecution(UUID playerId, String command, long timestamp, boolean success, 
                              String errorMessage, String world, String location) {
            this.playerId = playerId;
            this.command = command;
            this.timestamp = timestamp;
            this.success = success;
            this.errorMessage = errorMessage;
            this.world = world;
            this.location = location;
        }
        
        // Геттеры
        public UUID getPlayerId() { return playerId; }
        public String getCommand() { return command; }
        public long getTimestamp() { return timestamp; }
        public boolean isSuccess() { return success; }
        public String getErrorMessage() { return errorMessage; }
        public String getWorld() { return world; }
        public String getLocation() { return location; }
    }
    
    private static class CachedCommand {
        private final ExecutionResult result;
        private final long expiryTime;
        
        public CachedCommand(ExecutionResult result, long expiryTime) {
            this.result = result;
            this.expiryTime = expiryTime;
        }
        
        public ExecutionResult getResult() { return result; }
        public boolean isExpired() { return System.currentTimeMillis() > expiryTime; }
    }
    
    private static class PlayerCommandStats {
        private final List<Long> recentCommands = new ArrayList<>();
        private long lastCommandTime = 0;
        
        public void addCommand(String command, long timestamp) {
            recentCommands.add(timestamp);
        }
        
        public void setLastCommandTime(long time) { this.lastCommandTime = time; }
        public long getLastCommandTime() { return lastCommandTime; }
        public List<Long> getRecentCommands() { return recentCommands; }
    }
    
    private static class GlobalCommandStats {
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
}
