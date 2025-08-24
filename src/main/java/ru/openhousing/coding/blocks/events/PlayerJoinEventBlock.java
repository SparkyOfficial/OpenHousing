package ru.openhousing.coding.blocks.events;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
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
 * Специализированный блок для события входа игрока на сервер
 * Обрабатывает вход с детальным логированием, уведомлениями, эффектами и статистикой
 * 
 * @author OpenHousing Team
 * @version 1.0.0
 */
public class PlayerJoinEventBlock extends CodeBlock implements Listener {
    
    // Статические поля для глобального управления
    private static final Map<UUID, JoinRecord> joinHistory = new ConcurrentHashMap<>();
    private static final Map<String, Integer> joinReasonStats = new ConcurrentHashMap<>();
    private static final Map<String, Integer> worldJoinStats = new ConcurrentHashMap<>();
    private static final AtomicInteger totalJoins = new AtomicInteger(0);
    private static final AtomicInteger totalOnlineTime = new AtomicInteger(0);
    private static final Map<UUID, Long> playerJoinTimes = new ConcurrentHashMap<>();
    
    // Переменные блока (настраиваются через drag-n-drop)
    private BlockVariable welcomeMessageVar;
    private BlockVariable showWelcomeTitleVar;
    private BlockVariable playJoinSoundVar;
    private BlockVariable spawnParticlesVar;
    private BlockVariable giveWelcomeItemsVar;
    private BlockVariable setWelcomeEffectsVar;
    private BlockVariable teleportToSpawnVar;
    private BlockVariable logJoinEventVar;
    private BlockVariable trackStatisticsVar;
    private BlockVariable notifyOnlinePlayersVar;
    private BlockVariable showOnlineCountVar;
    private BlockVariable autoSaveEnabledVar;
    private BlockVariable welcomeDelayVar;
    private BlockVariable firstTimeBonusVar;
    private BlockVariable joinCommandsVar;
    private BlockVariable welcomeInventoryVar;
    private BlockVariable joinPermissionsVar;
    private BlockVariable antiBotProtectionVar;
    private BlockVariable sessionTrackingVar;
    private BlockVariable performanceModeVar;
    
    // Внутренние кэши и состояния
    private final Map<UUID, PlayerJoinStats> playerStats = new ConcurrentHashMap<>();
    private final Map<String, GlobalJoinStats> globalStats = new ConcurrentHashMap<>();
    private final Queue<JoinEventRequest> pendingJoins = new LinkedList<>();
    private final Map<UUID, List<String>> playerCommands = new ConcurrentHashMap<>();
    
    public enum JoinReason {
        NORMAL("Обычный вход", "Игрок вошел обычным способом"),
        FIRST_TIME("Первый раз", "Игрок впервые на сервере"),
        RETURNING("Возвращение", "Игрок вернулся после долгого отсутствия"),
        VIP("VIP вход", "VIP игрок с особыми привилегиями"),
        STAFF("Персонал", "Вход сотрудника сервера"),
        BOT("Бот", "Подозрительный вход (анти-бот защита)"),
        UNKNOWN("Неизвестно", "Причина входа не определена");
        
        private final String displayName;
        private final String description;
        
        JoinReason(String displayName, String description) {
            this.displayName = displayName;
            this.description = description;
        }
        
        public String getDisplayName() { return displayName; }
        public String getDescription() { return description; }
    }
    
    public PlayerJoinEventBlock() {
        super(BlockType.PLAYER_JOIN);
        initializeDefaultSettings();
        registerListener();
    }
    
    /**
     * Инициализация настроек по умолчанию
     */
    private void initializeDefaultSettings() {
        // Создаем переменные с значениями по умолчанию
        welcomeMessageVar = new BlockVariable("welcomeMessage", "Приветственное сообщение", 
            BlockVariable.VariableType.STRING, "§aДобро пожаловать на сервер, %player%!");
        showWelcomeTitleVar = new BlockVariable("showWelcomeTitle", "Показывать приветственный заголовок", 
            BlockVariable.VariableType.BOOLEAN, true);
        playJoinSoundVar = new BlockVariable("playJoinSound", "Воспроизводить звук входа", 
            BlockVariable.VariableType.BOOLEAN, true);
        spawnParticlesVar = new BlockVariable("spawnParticles", "Создавать частицы при входе", 
            BlockVariable.VariableType.BOOLEAN, false);
        giveWelcomeItemsVar = new BlockVariable("giveWelcomeItems", "Выдавать приветственные предметы", 
            BlockVariable.VariableType.BOOLEAN, true);
        setWelcomeEffectsVar = new BlockVariable("setWelcomeEffects", "Устанавливать приветственные эффекты", 
            BlockVariable.VariableType.BOOLEAN, false);
        teleportToSpawnVar = new BlockVariable("teleportToSpawn", "Телепортировать на спавн", 
            BlockVariable.VariableType.BOOLEAN, true);
        logJoinEventVar = new BlockVariable("logJoinEvent", "Логировать событие входа", 
            BlockVariable.VariableType.BOOLEAN, true);
        trackStatisticsVar = new BlockVariable("trackStatistics", "Отслеживать статистику", 
            BlockVariable.VariableType.BOOLEAN, true);
        notifyOnlinePlayersVar = new BlockVariable("notifyOnlinePlayers", "Уведомлять игроков онлайн", 
            BlockVariable.VariableType.BOOLEAN, true);
        showOnlineCountVar = new BlockVariable("showOnlineCount", "Показывать количество игроков", 
            BlockVariable.VariableType.BOOLEAN, true);
        autoSaveEnabledVar = new BlockVariable("autoSaveEnabled", "Автосохранение при входе", 
            BlockVariable.VariableType.BOOLEAN, true);
        welcomeDelayVar = new BlockVariable("welcomeDelay", "Задержка приветствия (мс)", 
            BlockVariable.VariableType.INTEGER, 1000);
        firstTimeBonusVar = new BlockVariable("firstTimeBonus", "Бонус для новичков", 
            BlockVariable.VariableType.BOOLEAN, true);
        joinCommandsVar = new BlockVariable("joinCommands", "Команды при входе", 
            BlockVariable.VariableType.LIST, Arrays.asList("spawn", "kit starter"));
        welcomeInventoryVar = new BlockVariable("welcomeInventory", "Приветственный инвентарь", 
            BlockVariable.VariableType.LIST, Arrays.asList("STONE_PICKAXE", "BREAD:16"));
        joinPermissionsVar = new BlockVariable("joinPermissions", "Разрешения при входе", 
            BlockVariable.VariableType.LIST, Arrays.asList("essentials.home", "essentials.kit"));
        antiBotProtectionVar = new BlockVariable("antiBotProtection", "Анти-бот защита", 
            BlockVariable.VariableType.BOOLEAN, true);
        sessionTrackingVar = new BlockVariable("sessionTracking", "Отслеживание сессий", 
            BlockVariable.VariableType.BOOLEAN, true);
        performanceModeVar = new BlockVariable("performanceMode", "Режим производительности", 
            BlockVariable.VariableType.BOOLEAN, false);
        
        // Инициализация статистики
        for (JoinReason reason : JoinReason.values()) {
            joinReasonStats.put(reason.name(), 0);
        }
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
            System.err.println("Failed to register PlayerJoinEventBlock listener: " + e.getMessage());
        }
    }
    
    /**
     * Обработка события входа игрока
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        
        if (player == null) {
            return;
        }
        
        // Создание контекста выполнения
        ExecutionContext context = new ExecutionContext(player);
        context.setVariable("playerName", player.getName());
        context.setVariable("playerUUID", player.getUniqueId().toString());
        context.setVariable("world", player.getWorld().getName());
        context.setVariable("location", formatLocation(player.getLocation()));
        context.setVariable("timestamp", System.currentTimeMillis());
        context.setVariable("joinReason", detectJoinReason(player));
        context.setVariable("onlinePlayers", Bukkit.getOnlinePlayers().size());
        context.setVariable("maxPlayers", Bukkit.getMaxPlayers());
        context.setVariable("isFirstTime", isFirstTimePlayer(player));
        context.setVariable("lastJoinTime", getLastJoinTime(player));
        
        // Выполнение блока
        ExecutionResult result = execute(context);
        
        // Обработка результата
        handleExecutionResult(event, result, context);
        
        // Обновление статистики
        updateStatistics(player, context);
        
        // Логирование
        logJoinEvent(player, context);
        
        // Уведомления
        sendNotifications(player, context);
        
        // Сохранение данных
        savePlayerData(player, context);
        
        // Запуск отложенных действий
        scheduleDelayedActions(player, context);
    }
    
    /**
     * Определение причины входа
     */
    private JoinReason detectJoinReason(Player player) {
        if (isFirstTimePlayer(player)) {
            return JoinReason.FIRST_TIME;
        }
        
        long lastJoin = getLastJoinTime(player);
        long currentTime = System.currentTimeMillis();
        long timeDiff = currentTime - lastJoin;
        
        // Если прошло больше 24 часов
        if (timeDiff > 24 * 60 * 60 * 1000) {
            return JoinReason.RETURNING;
        }
        
        // Проверка VIP статуса
        if (player.hasPermission("openhousing.vip")) {
            return JoinReason.VIP;
        }
        
        // Проверка персонала
        if (player.hasPermission("openhousing.staff")) {
            return JoinReason.STAFF;
        }
        
        // Анти-бот защита
        if (getBooleanValue(antiBotProtectionVar) && isSuspiciousJoin(player)) {
            return JoinReason.BOT;
        }
        
        return JoinReason.NORMAL;
    }
    
    /**
     * Проверка подозрительного входа (анти-бот)
     */
    private boolean isSuspiciousJoin(Player player) {
        // Простая проверка: если игрок зашел слишком быстро после регистрации
        long joinTime = System.currentTimeMillis();
        long registrationTime = getPlayerRegistrationTime(player);
        
        if (registrationTime > 0) {
            long timeDiff = joinTime - registrationTime;
            // Если прошло меньше 5 секунд - подозрительно
            return timeDiff < 5000;
        }
        
        return false;
    }
    
    /**
     * Получение времени регистрации игрока
     */
    private long getPlayerRegistrationTime(Player player) {
        // TODO: Реализовать получение времени регистрации из базы данных
        return 0;
    }
    
    /**
     * Проверка, является ли игрок новичком
     */
    private boolean isFirstTimePlayer(Player player) {
        return !joinHistory.containsKey(player.getUniqueId());
    }
    
    /**
     * Получение времени последнего входа
     */
    private long getLastJoinTime(Player player) {
        JoinRecord record = joinHistory.get(player.getUniqueId());
        return record != null ? record.getTimestamp() : 0;
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
    private void handleExecutionResult(PlayerJoinEvent event, ExecutionResult result, ExecutionContext context) {
        if (result == null) {
            return;
        }
        
        if (!result.isSuccess()) {
            // Логируем ошибку
            OpenHousing.getInstance().getLogger().warning(
                "PlayerJoinEventBlock execution failed for " + 
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
        
        String joinReason = context.getVariable("joinReason").toString();
        String world = context.getVariable("world").toString();
        
        // Обновление статистики причин входа
        joinReasonStats.merge(joinReason, 1, Integer::sum);
        
        // Обновление статистики по мирам
        worldJoinStats.merge(world, 1, Integer::sum);
        
        // Обновление общей статистики
        totalJoins.incrementAndGet();
        
        // Обновление статистики игрока
        PlayerJoinStats playerStats = this.playerStats.computeIfAbsent(
            player.getUniqueId(), k -> new PlayerJoinStats());
        playerStats.addJoin(joinReason, System.currentTimeMillis());
        
        // Запись времени входа
        playerJoinTimes.put(player.getUniqueId(), System.currentTimeMillis());
        
        // Создание записи о входе
        JoinRecord record = new JoinRecord(
            player.getUniqueId(),
            joinReason,
            System.currentTimeMillis(),
            world,
            formatLocation(player.getLocation())
        );
        joinHistory.put(player.getUniqueId(), record);
    }
    
    /**
     * Логирование события входа
     */
    private void logJoinEvent(Player player, ExecutionContext context) {
        if (!getBooleanValue(logJoinEventVar)) {
            return;
        }
        
        String logMessage = String.format("[PlayerJoin] Player: %s, World: %s, Location: %s, Reason: %s, Online: %d/%d",
            player.getName(),
            context.getVariable("world"),
            context.getVariable("location"),
            context.getVariable("joinReason"),
            context.getVariable("onlinePlayers"),
            context.getVariable("maxPlayers"));
        
        OpenHousing.getInstance().getLogger().info(logMessage);
    }
    
    /**
     * Отправка уведомлений
     */
    private void sendNotifications(Player player, ExecutionContext context) {
        // Приветственное сообщение
        if (getBooleanValue(welcomeMessageVar)) {
            String message = getStringValue(welcomeMessageVar)
                .replace("%s", player.getName())
                .replace("%player", player.getName())
                .replace("%world", context.getVariable("world").toString())
                .replace("%online", context.getVariable("onlinePlayers").toString())
                .replace("%max", context.getVariable("maxPlayers").toString());
            
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
        }
        
        // Приветственный заголовок
        if (getBooleanValue(showWelcomeTitleVar)) {
            String title = "§aДобро пожаловать!";
            String subtitle = "§e" + player.getName();
            
            player.sendTitle(title, subtitle, 10, 40, 10);
        }
        
        // Уведомление игроков онлайн
        if (getBooleanValue(notifyOnlinePlayersVar)) {
            String notifyMessage = String.format("§e%s §7присоединился к серверу", player.getName());
            Bukkit.getOnlinePlayers().forEach(p -> {
                if (p != player) {
                    p.sendMessage(notifyMessage);
                }
            });
        }
        
        // Сообщение о количестве игроков
        if (getBooleanValue(showOnlineCountVar)) {
            int onlineCount = Bukkit.getOnlinePlayers().size();
            int maxPlayers = Bukkit.getMaxPlayers();
            
            String onlineMessage = String.format("§7Игроков онлайн: §e%d§7/§e%d", onlineCount, maxPlayers);
            Bukkit.getOnlinePlayers().forEach(p -> p.sendMessage(onlineMessage));
        }
    }
    
    /**
     * Сохранение данных игрока
     */
    private void savePlayerData(Player player, ExecutionContext context) {
        // Автосохранение
        if (getBooleanValue(autoSaveEnabledVar)) {
            // TODO: Реализовать автосохранение
        }
        
        // Отслеживание сессий
        if (getBooleanValue(sessionTrackingVar)) {
            // TODO: Реализовать отслеживание сессий
        }
    }
    
    /**
     * Планирование отложенных действий
     */
    private void scheduleDelayedActions(Player player, ExecutionContext context) {
        int delay = getIntegerValue(welcomeDelayVar);
        
        Bukkit.getScheduler().runTaskLater(OpenHousing.getInstance(), () -> {
            // Звук входа
            if (getBooleanValue(playJoinSoundVar)) {
                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
            }
            
            // Частицы
            if (getBooleanValue(spawnParticlesVar)) {
                // TODO: Реализовать создание частиц
            }
            
            // Приветственные предметы
            if (getBooleanValue(giveWelcomeItemsVar)) {
                giveWelcomeItems(player);
            }
            
            // Приветственные эффекты
            if (getBooleanValue(setWelcomeEffectsVar)) {
                setWelcomeEffects(player);
            }
            
            // Телепортация на спавн
            if (getBooleanValue(teleportToSpawnVar)) {
                teleportToSpawn(player);
            }
            
            // Выполнение команд
            executeJoinCommands(player);
            
            // Бонус для новичков
            if (getBooleanValue(firstTimeBonusVar) && isFirstTimePlayer(player)) {
                giveFirstTimeBonus(player);
            }
            
        }, delay / 50); // Конвертируем миллисекунды в тики
    }
    
    /**
     * Выдача приветственных предметов
     */
    private void giveWelcomeItems(Player player) {
        List<String> items = getListValue(welcomeInventoryVar);
        
        for (String itemStr : items) {
            try {
                String[] parts = itemStr.split(":");
                Material material = Material.valueOf(parts[0].toUpperCase());
                int amount = parts.length > 1 ? Integer.parseInt(parts[1]) : 1;
                
                ItemStack item = new ItemStack(material, amount);
                player.getInventory().addItem(item);
                
            } catch (Exception e) {
                OpenHousing.getInstance().getLogger().warning(
                    "Failed to give welcome item: " + itemStr + " to " + player.getName());
            }
        }
    }
    
    /**
     * Установка приветственных эффектов
     */
    private void setWelcomeEffects(Player player) {
        // Эффект скорости на 30 секунд
        PotionEffect speedEffect = new PotionEffect(PotionEffectType.SPEED, 600, 0);
        player.addPotionEffect(speedEffect);
        
        // Эффект ночного зрения на 1 минуту
        PotionEffect nightVisionEffect = new PotionEffect(PotionEffectType.NIGHT_VISION, 1200, 0);
        player.addPotionEffect(nightVisionEffect);
    }
    
    /**
     * Телепортация на спавн
     */
    private void teleportToSpawn(Player player) {
        // TODO: Реализовать телепортацию на спавн
        // Это может быть через WorldGuard API или собственные точки спавна
    }
    
    /**
     * Выполнение команд при входе
     */
    private void executeJoinCommands(Player player) {
        List<String> commands = getListValue(joinCommandsVar);
        
        for (String command : commands) {
            try {
                String processedCommand = command
                    .replace("%player%", player.getName())
                    .replace("%uuid%", player.getUniqueId().toString())
                    .replace("%world%", player.getWorld().getName());
                
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), processedCommand);
                
            } catch (Exception e) {
                OpenHousing.getInstance().getLogger().warning(
                    "Failed to execute join command: " + command + " for " + player.getName());
            }
        }
    }
    
    /**
     * Выдача бонуса для новичков
     */
    private void giveFirstTimeBonus(Player player) {
        // Дополнительные предметы для новичков
        player.getInventory().addItem(new ItemStack(Material.DIAMOND, 3));
        player.getInventory().addItem(new ItemStack(Material.EXPERIENCE_BOTTLE, 10));
        
        // Сообщение о бонусе
        player.sendMessage("§6§l🎁 Добро пожаловать на сервер!");
        player.sendMessage("§eВы получили приветственный бонус для новичков!");
        
        // Звук получения бонуса
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 2.0f);
    }
    
    @Override
    public ExecutionResult execute(ExecutionContext context) {
        try {
            Player player = context.getPlayer();
            if (player == null) {
                return ExecutionResult.error("Игрок не найден в контексте");
            }
            
            // Проверяем задержку
            int welcomeDelay = getIntegerValue(welcomeDelayVar);
            if (welcomeDelay > 0) {
                try {
                    Thread.sleep(welcomeDelay);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            
            // Проверяем производительность
            if (getBooleanValue(performanceModeVar)) {
                // Режим производительности - минимальные действия
                return ExecutionResult.success("Вход обработан в режиме производительности");
            }
            
            // Основная логика выполняется в scheduleDelayedActions
            return ExecutionResult.success("Вход игрока обработан успешно");
            
        } catch (Exception e) {
            return ExecutionResult.error("Ошибка выполнения блока входа игрока: " + e.getMessage());
        }
    }
    
    @Override
    public boolean validate() {
        // Проверяем базовые параметры
        return getIntegerValue(welcomeDelayVar) >= 0;
    }
    
    @Override
    public List<String> getDescription() {
        List<String> description = new ArrayList<>();
        description.add("§6Блок события входа игрока");
        description.add("§7Обрабатывает вход с детальным");
        description.add("§7логированием и уведомлениями");
        description.add("");
        description.add("§eПеременные:");
        description.add("§7• Приветствие: " + (getBooleanValue(showWelcomeTitleVar) ? "§aВключено" : "§cВыключено"));
        description.add("§7• Звуки: " + (getBooleanValue(playJoinSoundVar) ? "§aВключены" : "§cВыключены"));
        description.add("§7• Предметы: " + (getBooleanValue(giveWelcomeItemsVar) ? "§aВключены" : "§cВыключены"));
        description.add("§7• Эффекты: " + (getBooleanValue(setWelcomeEffectsVar) ? "§aВключены" : "§cВыключены"));
        description.add("§7• Задержка: " + getIntegerValue(welcomeDelayVar) + "мс");
        description.add("§7• Анти-бот: " + (getBooleanValue(antiBotProtectionVar) ? "§aВключена" : "§cВыключена"));
        
        return description;
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
    
    @SuppressWarnings("unchecked")
    private List<String> getListValue(BlockVariable variable) {
        Object value = variable.getValue();
        if (value instanceof List) {
            return (List<String>) value;
        }
        return new ArrayList<>();
    }
    
    // Геттеры для переменных (для внешнего доступа)
    public BlockVariable getWelcomeMessageVar() { return welcomeMessageVar; }
    public BlockVariable getShowWelcomeTitleVar() { return showWelcomeTitleVar; }
    public BlockVariable getPlayJoinSoundVar() { return playJoinSoundVar; }
    public BlockVariable getSpawnParticlesVar() { return spawnParticlesVar; }
    public BlockVariable getGiveWelcomeItemsVar() { return giveWelcomeItemsVar; }
    public BlockVariable getSetWelcomeEffectsVar() { return setWelcomeEffectsVar; }
    public BlockVariable getTeleportToSpawnVar() { return teleportToSpawnVar; }
    public BlockVariable getLogJoinEventVar() { return logJoinEventVar; }
    public BlockVariable getTrackStatisticsVar() { return trackStatisticsVar; }
    public BlockVariable getNotifyOnlinePlayersVar() { return notifyOnlinePlayersVar; }
    public BlockVariable getShowOnlineCountVar() { return showOnlineCountVar; }
    public BlockVariable getAutoSaveEnabledVar() { return autoSaveEnabledVar; }
    public BlockVariable getWelcomeDelayVar() { return welcomeDelayVar; }
    public BlockVariable getFirstTimeBonusVar() { return firstTimeBonusVar; }
    public BlockVariable getJoinCommandsVar() { return joinCommandsVar; }
    public BlockVariable getWelcomeInventoryVar() { return welcomeInventoryVar; }
    public BlockVariable getJoinPermissionsVar() { return joinPermissionsVar; }
    public BlockVariable getAntiBotProtectionVar() { return antiBotProtectionVar; }
    public BlockVariable getSessionTrackingVar() { return sessionTrackingVar; }
    public BlockVariable getPerformanceModeVar() { return performanceModeVar; }
    
    // Внутренние классы для статистики и кэширования
    private static class JoinRecord {
        private final UUID playerId;
        private final String reason;
        private final long timestamp;
        private final String world;
        private final String location;
        
        public JoinRecord(UUID playerId, String reason, long timestamp, String world, String location) {
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
    
    private static class PlayerJoinStats {
        private final List<JoinRecord> joins = new ArrayList<>();
        private long lastJoinTime = 0;
        private long totalOnlineTime = 0;
        
        public void addJoin(String reason, long timestamp) {
            joins.add(new JoinRecord(null, reason, timestamp, "", ""));
            lastJoinTime = timestamp;
        }
        
        public void setLastJoinTime(long time) { this.lastJoinTime = time; }
        public long getLastJoinTime() { return lastJoinTime; }
        public List<JoinRecord> getJoins() { return joins; }
    }
    
    private static class GlobalJoinStats {
        private int totalJoins = 0;
        private int normalJoins = 0;
        private int firstTimeJoins = 0;
        private int returningJoins = 0;
        private int vipJoins = 0;
        private int staffJoins = 0;
        private int botJoins = 0;
        private long totalProcessingTime = 0;
        
        public void addJoin(String reason, long processingTime) {
            totalJoins++;
            totalProcessingTime += processingTime;
            
            switch (reason.toUpperCase()) {
                case "NORMAL": normalJoins++; break;
                case "FIRST_TIME": firstTimeJoins++; break;
                case "RETURNING": returningJoins++; break;
                case "VIP": vipJoins++; break;
                case "STAFF": staffJoins++; break;
                case "BOT": botJoins++; break;
            }
        }
        
        // Геттеры
        public int getTotalJoins() { return totalJoins; }
        public int getNormalJoins() { return normalJoins; }
        public int getFirstTimeJoins() { return firstTimeJoins; }
        public int getReturningJoins() { return returningJoins; }
        public int getVipJoins() { return vipJoins; }
        public int getStaffJoins() { return staffJoins; }
        public int getBotJoins() { return botJoins; }
        public long getTotalProcessingTime() { return totalProcessingTime; }
        public double getAverageProcessingTime() { return totalJoins > 0 ? (double) totalProcessingTime / totalJoins : 0.0; }
    }
    
    private static class JoinEventRequest {
        private final UUID playerId;
        private final long requestTime;
        private final boolean priority;
        
        public JoinEventRequest(UUID playerId, long requestTime, boolean priority) {
            this.playerId = playerId;
            this.requestTime = requestTime;
            this.priority = priority;
        }
        
        public UUID getPlayerId() { return playerId; }
        public long getRequestTime() { return requestTime; }
        public boolean isPriority() { return priority; }
    }
}
