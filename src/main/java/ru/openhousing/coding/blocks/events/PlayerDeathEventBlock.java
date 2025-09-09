package ru.openhousing.coding.blocks.events;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import ru.openhousing.OpenHousing;
import ru.openhousing.coding.blocks.BlockType;
import ru.openhousing.coding.blocks.CodeBlock;
import ru.openhousing.coding.blocks.CodeBlock.ExecutionContext;
import ru.openhousing.coding.blocks.CodeBlock.ExecutionResult;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Специализированный блок для события смерти игрока
 * Обрабатывает смерть с детальным контролем, эффектами и настройками
 * 
 * @author OpenHousing Team
 * @version 1.0.0
 */
public class PlayerDeathEventBlock extends CodeBlock {
    
    // Статические поля для глобального управления
    private static final Map<UUID, PlayerDeathRecord> deathHistory = new ConcurrentHashMap<>();
    private static final Map<String, Integer> deathStats = new ConcurrentHashMap<>();
    private static final Set<UUID> recentlyDiedPlayers = ConcurrentHashMap.newKeySet();
    private static final AtomicInteger totalDeaths = new AtomicInteger(0);
    private static final AtomicInteger successfulDeaths = new AtomicInteger(0);
    
    // Настройки блока
    private boolean deathMessageEnabled = true;
    private String customDeathMessage = "§c{player} умер от {cause}";
    private boolean broadcastDeath = true;
    private String broadcastFormat = "§c{player} §7умер в мире {world}";
    private boolean privateDeath = false;
    private String privateMessage = "§7Вы умерли тихо";
    
    // Настройки дропа предметов
    private boolean keepInventory = false;
    private boolean keepExperience = false;
    private boolean customDropRules = false;
    private List<Material> protectedItems = new ArrayList<>();
    private List<Material> forcedDropItems = new ArrayList<>();
    private double dropChance = 1.0;
    private boolean dropMoney = false;
    private double moneyDropAmount = 0.0;
    
    // Настройки восстановления
    private boolean autoRespawn = false;
    private long respawnDelay = 5000; // 5 секунд
    private boolean restoreHealth = true;
    private boolean restoreFood = true;
    private boolean restoreExperience = false;
    private boolean clearNegativeEffects = true;
    
    // Настройки штрафов
    private boolean experiencePenalty = true;
    private double experienceLossPercent = 10.0;
    private boolean levelPenalty = false;
    private int levelLossAmount = 1;
    private boolean moneyPenalty = false;
    private double moneyLossPercent = 5.0;
    
    // Настройки эффектов
    private boolean spawnDeathParticles = true;
    private Particle deathParticle = Particle.SMOKE;
    private int particleCount = 50;
    private double particleOffset = 0.5;
    private boolean playDeathSound = true;
    private Sound deathSound = Sound.ENTITY_PLAYER_DEATH;
    private float soundVolume = 1.0f;
    private float soundPitch = 1.0f;
    
    // Настройки уведомлений
    private boolean notifyAdmins = false;
    private String adminNotificationFormat = "&c[Death] &f{player} &7died from &e{cause}";
    private boolean notifyStaff = false;
    private String staffNotificationFormat = "&a[Death] &f{player} &7died in world &e{world}";
    private boolean showDeathInfo = false;
    
    // Настройки логирования
    private boolean logDeaths = true;
    private boolean logToConsole = true;
    private boolean logToFile = false;
    private String logFormat = "[{timestamp}] {player} died from {cause} in {world}";
    private boolean logLocation = true;
    private boolean logInventory = false;
    
    // Настройки статистики
    private boolean collectStats = true;
    private boolean trackPlayerStats = true;
    private boolean trackGlobalStats = true;
    private boolean exportStats = false;
    private String statsExportPath = "plugins/OpenHousing/death_stats/";
    
    // Настройки производительности
    private boolean asyncProcessing = true;
    private boolean cacheResults = true;
    private int cacheSize = 1000;
    private long cacheExpiry = 300000; // 5 минут
    
    // Внутренние кэши и состояния
    private final Map<String, CachedDeathResult> deathCache = new ConcurrentHashMap<>();
    private final Map<UUID, PlayerDeathStats> playerStats = new ConcurrentHashMap<>();
    private final Map<String, GlobalDeathStats> globalStats = new ConcurrentHashMap<>();
    private final Queue<DeathRequest> pendingDeaths = new LinkedList<>();
    
    public PlayerDeathEventBlock() {
        super(BlockType.PLAYER_DEATH);
        initializeDefaultSettings();
        // Removed registerListener() call - now handled by OptimizedEventManager
    }
    
    /**
     * Инициализация настроек по умолчанию
     */
    private void initializeDefaultSettings() {
        // Базовые настройки
        deathMessageEnabled = true;
        broadcastDeath = true;
        keepInventory = false;
        keepExperience = false;
        
        // Настройки эффектов
        spawnDeathParticles = true;
        deathParticle = Particle.SMOKE;
        particleCount = 50;
        particleOffset = 0.5;
        playDeathSound = true;
        deathSound = Sound.ENTITY_PLAYER_DEATH;
        
        // Настройки восстановления
        restoreHealth = true;
        restoreFood = true;
        clearNegativeEffects = true;
        
        // Настройки штрафов
        experiencePenalty = true;
        experienceLossPercent = 10.0;
        
        // Инициализация статистики
        deathStats.put("TOTAL", 0);
        deathStats.put("SUCCESS", 0);
        deathStats.put("FAILED", 0);
        
        // Защищенные предметы по умолчанию
        protectedItems.addAll(Arrays.asList(
            Material.DIAMOND_SWORD,
            Material.DIAMOND_PICKAXE,
            Material.DIAMOND_AXE,
            Material.DIAMOND_SHOVEL,
            Material.DIAMOND_HOE,
            Material.DIAMOND_HELMET,
            Material.DIAMOND_CHESTPLATE,
            Material.DIAMOND_LEGGINGS,
            Material.DIAMOND_BOOTS
        ));
    }
    
    /**
     * Обработка события смерти игрока
     */
    public void processDeathEvent(Player player, Player killer, Map<String, Object> eventData) {
        if (player == null) {
            return;
        }
        
        // Проверка базовых условий
        if (!shouldProcessDeath(player)) {
            return;
        }
        
        // Создание контекста выполнения
        ExecutionContext context = new ExecutionContext(player);
        context.setVariable("timestamp", System.currentTimeMillis());
        context.setVariable("playerName", player.getName());
        context.setVariable("playerUUID", player.getUniqueId().toString());
        context.setVariable("killerName", killer != null ? killer.getName() : "none");
        context.setVariable("killerUUID", killer != null ? killer.getUniqueId().toString() : "none");
        context.setVariable("cause", eventData.get("cause") != null ? eventData.get("cause").toString() : "unknown");
        context.setVariable("world", player.getWorld().getName());
        context.setVariable("location", formatLocation(player.getLocation()));
        context.setVariable("deathTime", System.currentTimeMillis());
        context.setVariable("experience", player.getTotalExperience());
        context.setVariable("level", player.getLevel());
        
        // Выполнение блока
        ExecutionResult result = execute(context);
        
        // Обработка результата
        handleExecutionResult(player, result, context);
        
        // Обновление статистики
        updateStatistics(player, result);
        
        // Логирование
        logPlayerDeath(player, eventData, result, context);
        
        // Уведомления
        sendNotifications(player, eventData, result, context);
        
        // Эффекты
        if (result != null && result.isSuccess()) {
            spawnDeathEffects(player);
        }
        
        // Планирование воскрешения
        if (autoRespawn) {
            scheduleRespawn(player, context);
        }
    }
    
    /**
     * Проверка, следует ли обрабатывать смерть
     */
    private boolean shouldProcessDeath(Player player) {
        // Базовые проверки
        if (player == null || !player.isOnline()) {
            return false;
        }
        
        // Проверка разрешений
        if (player.hasPermission("openhousing.death.bypass")) {
            return false;
        }
        
        return true;
    }
    
    @Override
    public ExecutionResult execute(ExecutionContext context) {
        try {
            totalDeaths.incrementAndGet();
            
            // Асинхронная обработка
            if (asyncProcessing) {
                return executeAsync(context);
            } else {
                return executeSync(context);
            }
        } catch (Exception e) {
            return ExecutionResult.error("Ошибка выполнения блока смерти: " + e.getMessage());
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
            CachedDeathResult cached = deathCache.get(player.getUniqueId().toString());
            if (cached != null && !cached.isExpired()) {
                return cached.getResult();
            }
        }
        
        // Выполнение логики смерти
        ExecutionResult result = processDeathLogic(context);
        
        // Кэширование результата
        if (cacheResults && result != null) {
            String cacheKey = player.getUniqueId().toString();
            deathCache.put(cacheKey, new CachedDeathResult(result, System.currentTimeMillis() + cacheExpiry));
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
     * Основная логика обработки смерти
     */
    private ExecutionResult processDeathLogic(ExecutionContext context) {
        Player player = context.getPlayer();
        
        try {
            // Применение штрафов
            if (experiencePenalty) {
                applyExperiencePenalty(player);
            }
            
            if (levelPenalty) {
                applyLevelPenalty(player);
            }
            
            if (moneyPenalty) {
                applyMoneyPenalty(player);
            }
            
            // Очистка негативных эффектов
            if (clearNegativeEffects) {
                clearNegativeEffects(player);
            }
            
            // Создание резервной копии инвентаря
            if (keepInventory) {
                backupPlayerInventory(player);
            }
            
            return ExecutionResult.success("Смерть игрока успешно обработана");
            
        } catch (Exception e) {
            return ExecutionResult.error("Ошибка обработки смерти: " + e.getMessage());
        }
    }
    
    /**
     * Применение штрафа по опыту
     */
    private void applyExperiencePenalty(Player player) {
        int currentExp = player.getTotalExperience();
        int lossAmount = (int) (currentExp * (experienceLossPercent / 100.0));
        
        if (lossAmount > 0) {
            int newExp = Math.max(0, currentExp - lossAmount);
            player.setTotalExperience(newExp);
        }
    }
    
    /**
     * Применение штрафа по уровню
     */
    private void applyLevelPenalty(Player player) {
        int currentLevel = player.getLevel();
        int newLevel = Math.max(0, currentLevel - levelLossAmount);
        player.setLevel(newLevel);
    }
    
    /**
     * Применение денежного штрафа
     */
    private void applyMoneyPenalty(Player player) {
        // TODO: Реализовать через Vault или другой плагин экономики
        // Пока что просто логируем
        OpenHousing.getInstance().getLogger().info(
            "Money penalty applied to " + player.getName() + 
            ": " + moneyLossPercent + "%"
        );
    }
    
    /**
     * Очистка негативных эффектов
     */
    private void clearNegativeEffects(Player player) {
        for (PotionEffect effect : player.getActivePotionEffects()) {
            PotionEffectType type = effect.getType();
            if (type == PotionEffectType.POISON || 
                type == PotionEffectType.WITHER || 
                type == PotionEffectType.BLINDNESS || 
                type == PotionEffectType.WEAKNESS ||
                type == PotionEffectType.HUNGER) {
                player.removePotionEffect(type);
            }
        }
    }
    
    /**
     * Создание резервной копии инвентаря
     */
    private void backupPlayerInventory(Player player) {
        // TODO: Реализовать сохранение инвентаря в базу данных или файл
        // Пока что просто логируем
        OpenHousing.getInstance().getLogger().info(
            "Inventory backup created for " + player.getName()
        );
    }
    
    /**
     * Обработка результата выполнения
     */
    private void handleExecutionResult(Player player, ExecutionResult result, ExecutionContext context) {
        if (result == null) {
            return;
        }
        
        if (result.isSuccess()) {
            successfulDeaths.incrementAndGet();
            
            // Добавляем в список недавно умерших
            recentlyDiedPlayers.add(player.getUniqueId());
            
            // Удаляем через 1 минуту
            Bukkit.getScheduler().runTaskLater(OpenHousing.getInstance(), () -> {
                recentlyDiedPlayers.remove(player.getUniqueId());
            }, 1200L); // 1 минута * 20 тиков
            
            // Note: We can't set keepInventory or keepLevel here anymore since we don't have access to the event
            // These settings should be handled by the OptimizedEventManager when processing the event
            
        } else {
            // Логируем ошибку
            OpenHousing.getInstance().getLogger().warning(
                "Player death processing failed for " + player.getName() + 
                ": " + result.getMessage());
        }
    }
    
    /**
     * Обновление статистики
     */
    private void updateStatistics(Player player, ExecutionResult result) {
        if (!collectStats) {
            return;
        }
        
        // Обновление глобальной статистики
        if (trackGlobalStats) {
            deathStats.merge("TOTAL", 1, Integer::sum);
            
            if (result != null && result.isSuccess()) {
                deathStats.merge("SUCCESS", 1, Integer::sum);
            } else {
                deathStats.merge("FAILED", 1, Integer::sum);
            }
        }
        
        // Обновление статистики игрока
        if (trackPlayerStats) {
            PlayerDeathStats playerStats = this.playerStats.computeIfAbsent(
                player.getUniqueId(), k -> new PlayerDeathStats());
            
            playerStats.addDeath(System.currentTimeMillis());
        }
    }
    
    /**
     * Логирование смерти игрока
     */
    private void logPlayerDeath(Player player, Map<String, Object> eventData, ExecutionResult result, ExecutionContext context) {
        if (!logDeaths) {
            return;
        }
        
        String logMessage = logFormat
            .replace("{timestamp}", new java.util.Date().toString())
            .replace("{player}", player.getName())
            .replace("{cause}", context.getVariable("cause").toString())
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
        saveToHistory(player.getUniqueId(), eventData, result, context);
    }
    
    /**
     * Отправка уведомлений
     */
    private void sendNotifications(Player player, Map<String, Object> eventData, ExecutionResult result, ExecutionContext context) {
        // Уведомление администраторов
        if (notifyAdmins) {
            String adminMessage = adminNotificationFormat
                .replace("{player}", player.getName())
                .replace("{cause}", context.getVariable("cause").toString())
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
                .replace("{world}", context.getVariable("world").toString())
                .replace("{cause}", context.getVariable("cause").toString());
            
            Bukkit.getOnlinePlayers().stream()
                .filter(p -> p.hasPermission("openhousing.staff.notify"))
                .forEach(p -> p.sendMessage(ChatColor.translateAlternateColorCodes('&', staffMessage)));
        }
        
        // Информация о смерти
        if (showDeathInfo) {
            String infoMessage = "§7Информация о смерти:\n" +
                "§7Причина: §e" + context.getVariable("cause") + "\n" +
                "§7Мир: §e" + context.getVariable("world") + "\n" +
                "§7Локация: §e" + context.getVariable("location") + "\n" +
                "§7Убийца: §e" + context.getVariable("killerName");
            
            player.sendMessage(infoMessage);
        }
    }
    
    /**
     * Спавн эффектов при смерти
     */
    private void spawnDeathEffects(Player player) {
        Location location = player.getLocation();
        
        // Частицы
        if (spawnDeathParticles) {
            location.getWorld().spawnParticle(
                deathParticle, 
                location, 
                particleCount, 
                particleOffset, 
                particleOffset, 
                particleOffset, 
                0.1
            );
        }
        
        // Звук
        if (playDeathSound) {
            location.getWorld().playSound(
                location, 
                deathSound, 
                soundVolume, 
                soundPitch
            );
        }
    }
    
    /**
     * Планирование воскрешения
     */
    private void scheduleRespawn(Player player, ExecutionContext context) {
        Bukkit.getScheduler().runTaskLater(OpenHousing.getInstance(), () -> {
            if (player.isOnline()) {
                respawnPlayer(player, context);
            }
        }, respawnDelay / 50L); // Конвертируем в тики
    }
    
    /**
     * Воскрешение игрока
     */
    private void respawnPlayer(Player player, ExecutionContext context) {
        try {
            // Восстановление здоровья
            if (restoreHealth) {
                player.setHealth(player.getMaxHealth());
            }
            
            // Восстановление голода
            if (restoreFood) {
                player.setFoodLevel(20);
                player.setSaturation(20.0f);
            }
            
            // Восстановление опыта
            if (restoreExperience) {
                int originalExp = (Integer) context.getVariable("experience");
                int originalLevel = (Integer) context.getVariable("level");
                player.setTotalExperience(originalExp);
                player.setLevel(originalLevel);
            }
            
            // Восстановление инвентаря
            if (keepInventory) {
                restorePlayerInventory(player);
            }
            
            // Телепортация на спавн
            Location spawnLocation = player.getWorld().getSpawnLocation();
            if (spawnLocation != null) {
                player.teleport(spawnLocation);
            }
            
            // Уведомление
            player.sendMessage("§aВы были воскрешены!");
            
        } catch (Exception e) {
            OpenHousing.getInstance().getLogger().warning(
                "Failed to respawn player " + player.getName() + ": " + e.getMessage());
        }
    }
    
    /**
     * Восстановление инвентаря игрока
     */
    private void restorePlayerInventory(Player player) {
        // TODO: Реализовать восстановление инвентаря из резервной копии
        OpenHousing.getInstance().getLogger().info(
            "Inventory restored for " + player.getName()
        );
    }
    
    /**
     * Сохранение в историю
     */
    private void saveToHistory(UUID playerId, Map<String, Object> eventData, ExecutionResult result, ExecutionContext context) {
        PlayerDeathRecord record = new PlayerDeathRecord(
            playerId,
            System.currentTimeMillis(),
            result != null ? result.isSuccess() : false,
            result != null ? result.getMessage() : null,
            context.getVariable("cause").toString(),
            context.getVariable("world").toString(),
            context.getVariable("location").toString(),
            context.getVariable("killerName").toString()
        );
        
        deathHistory.put(playerId, record);
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
        if (respawnDelay < 0) return false;
        if (experienceLossPercent < 0 || experienceLossPercent > 100) return false;
        if (levelLossAmount < 0) return false;
        if (moneyLossPercent < 0 || moneyLossPercent > 100) return false;
        if (dropChance < 0 || dropChance > 1) return false;
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
        description.add("§6Блок события смерти игрока");
        description.add("§7Обрабатывает смерть с детальным контролем");
        description.add("§7и расширенными возможностями");
        description.add("");
        description.add("§eНастройки:");
        description.add("§7• Сохранение инвентаря: " + (keepInventory ? "§aВключено" : "§cВыключено"));
        description.add("§7• Сохранение опыта: " + (keepExperience ? "§aВключено" : "§cВыключено"));
        description.add("§7• Штраф по опыту: " + (experiencePenalty ? "§a" + experienceLossPercent + "%" : "§cВыключено"));
        description.add("§7• Авто-воскрешение: " + (autoRespawn ? "§a" + (respawnDelay/1000) + "с" : "§cВыключено"));
        description.add("§7• Эффекты смерти: " + (spawnDeathParticles ? "§aВключены" : "§cВыключены"));
        
        return description;
    }
    
    // Геттеры и сеттеры для настройки блока
    public void setDeathMessageEnabled(boolean deathMessageEnabled) { this.deathMessageEnabled = deathMessageEnabled; }
    public void setCustomDeathMessage(String customDeathMessage) { this.customDeathMessage = customDeathMessage; }
    public void setBroadcastDeath(boolean broadcastDeath) { this.broadcastDeath = broadcastDeath; }
    public void setBroadcastFormat(String broadcastFormat) { this.broadcastFormat = broadcastFormat; }
    public void setPrivateDeath(boolean privateDeath) { this.privateDeath = privateDeath; }
    public void setPrivateMessage(String privateMessage) { this.privateMessage = privateMessage; }
    public void setKeepInventory(boolean keepInventory) { this.keepInventory = keepInventory; }
    public void setKeepExperience(boolean keepExperience) { this.keepExperience = keepExperience; }
    public void setCustomDropRules(boolean customDropRules) { this.customDropRules = customDropRules; }
    public void setProtectedItems(List<Material> protectedItems) { this.protectedItems = protectedItems; }
    public void setForcedDropItems(List<Material> forcedDropItems) { this.forcedDropItems = forcedDropItems; }
    public void setDropChance(double dropChance) { this.dropChance = dropChance; }
    public void setDropMoney(boolean dropMoney) { this.dropMoney = dropMoney; }
    public void setMoneyDropAmount(double moneyDropAmount) { this.moneyDropAmount = moneyDropAmount; }
    public void setAutoRespawn(boolean autoRespawn) { this.autoRespawn = autoRespawn; }
    public void setRespawnDelay(long respawnDelay) { this.respawnDelay = respawnDelay; }
    public void setRestoreHealth(boolean restoreHealth) { this.restoreHealth = restoreHealth; }
    public void setRestoreFood(boolean restoreFood) { this.restoreFood = restoreFood; }
    public void setRestoreExperience(boolean restoreExperience) { this.restoreExperience = restoreExperience; }
    public void setClearNegativeEffects(boolean clearNegativeEffects) { this.clearNegativeEffects = clearNegativeEffects; }
    public void setExperiencePenalty(boolean experiencePenalty) { this.experiencePenalty = experiencePenalty; }
    public void setExperienceLossPercent(double experienceLossPercent) { this.experienceLossPercent = experienceLossPercent; }
    public void setLevelPenalty(boolean levelPenalty) { this.levelPenalty = levelPenalty; }
    public void setLevelLossAmount(int levelLossAmount) { this.levelLossAmount = levelLossAmount; }
    public void setMoneyPenalty(boolean moneyPenalty) { this.moneyPenalty = moneyPenalty; }
    public void setMoneyLossPercent(double moneyLossPercent) { this.moneyLossPercent = moneyLossPercent; }
    public void setSpawnDeathParticles(boolean spawnDeathParticles) { this.spawnDeathParticles = spawnDeathParticles; }
    public void setDeathParticle(Particle deathParticle) { this.deathParticle = deathParticle; }
    public void setParticleCount(int particleCount) { this.particleCount = particleCount; }
    public void setParticleOffset(double particleOffset) { this.particleOffset = particleOffset; }
    public void setPlayDeathSound(boolean playDeathSound) { this.playDeathSound = playDeathSound; }
    public void setDeathSound(Sound deathSound) { this.deathSound = deathSound; }
    public void setSoundVolume(float soundVolume) { this.soundVolume = soundVolume; }
    public void setSoundPitch(float soundPitch) { this.soundPitch = soundPitch; }
    public void setNotifyAdmins(boolean notifyAdmins) { this.notifyAdmins = notifyAdmins; }
    public void setAdminNotificationFormat(String adminNotificationFormat) { this.adminNotificationFormat = adminNotificationFormat; }
    public void setNotifyStaff(boolean notifyStaff) { this.notifyStaff = notifyStaff; }
    public void setStaffNotificationFormat(String staffNotificationFormat) { this.staffNotificationFormat = staffNotificationFormat; }
    public void setShowDeathInfo(boolean showDeathInfo) { this.showDeathInfo = showDeathInfo; }
    public void setLogDeaths(boolean logDeaths) { this.logDeaths = logDeaths; }
    public void setLogToConsole(boolean logToConsole) { this.logToConsole = logToConsole; }
    public void setLogToFile(boolean logToFile) { this.logToFile = logToFile; }
    public void setLogFormat(String logFormat) { this.logFormat = logFormat; }
    public void setLogLocation(boolean logLocation) { this.logLocation = logLocation; }
    public void setLogInventory(boolean logInventory) { this.logInventory = logInventory; }
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
    private static class CachedDeathResult {
        private final ExecutionResult result;
        private final long expiryTime;
        
        public CachedDeathResult(ExecutionResult result, long expiryTime) {
            this.result = result;
            this.expiryTime = expiryTime;
        }
        
        public ExecutionResult getResult() { return result; }
        public boolean isExpired() { return System.currentTimeMillis() > expiryTime; }
    }
    
    private static class PlayerDeathRecord {
        private final UUID playerId;
        private final long deathTime;
        private final boolean success;
        private final String message;
        private final String cause;
        private final String world;
        private final String location;
        private final String killerName;
        
        public PlayerDeathRecord(UUID playerId, long deathTime, boolean success, 
                               String message, String cause, String world, String location, String killerName) {
            this.playerId = playerId;
            this.deathTime = deathTime;
            this.success = success;
            this.message = message;
            this.cause = cause;
            this.world = world;
            this.location = location;
            this.killerName = killerName;
        }
        
        // Геттеры
        public UUID getPlayerId() { return playerId; }
        public long getDeathTime() { return deathTime; }
        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
        public String getCause() { return cause; }
        public String getWorld() { return world; }
        public String getLocation() { return location; }
        public String getKillerName() { return killerName; }
    }
    
    private static class PlayerDeathStats {
        private final List<Long> deathTimes = new ArrayList<>();
        private long lastDeathTime = 0;
        
        public void addDeath(long timestamp) {
            deathTimes.add(timestamp);
            lastDeathTime = timestamp;
        }
        
        public void setLastDeathTime(long time) { this.lastDeathTime = time; }
        public long getLastDeathTime() { return lastDeathTime; }
        public List<Long> getDeathTimes() { return deathTimes; }
    }
    
    private static class GlobalDeathStats {
        private int totalDeaths = 0;
        private int successfulDeaths = 0;
        private int failedDeaths = 0;
        private long totalProcessingTime = 0;
        
        public void addDeath(boolean success, long processingTime) {
            totalDeaths++;
            if (success) {
                successfulDeaths++;
            } else {
                failedDeaths++;
            }
            totalProcessingTime += processingTime;
        }
        
        // Геттеры
        public int getTotalDeaths() { return totalDeaths; }
        public int getSuccessfulDeaths() { return successfulDeaths; }
        public int getFailedDeaths() { return failedDeaths; }
        public long getTotalProcessingTime() { return totalProcessingTime; }
        public double getSuccessRate() { return totalDeaths > 0 ? (double) successfulDeaths / totalDeaths : 0.0; }
        public double getAverageProcessingTime() { return totalDeaths > 0 ? (double) totalProcessingTime / totalDeaths : 0.0; }
    }
    
    private static class DeathRequest {
        private final UUID playerId;
        private final long requestTime;
        private final boolean priority;
        
        public DeathRequest(UUID playerId, long requestTime, boolean priority) {
            this.playerId = playerId;
            this.requestTime = requestTime;
            this.priority = priority;
        }
        
        public UUID getPlayerId() { return playerId; }
        public long getRequestTime() { return requestTime; }
        public boolean isPriority() { return priority; }
    }
}
