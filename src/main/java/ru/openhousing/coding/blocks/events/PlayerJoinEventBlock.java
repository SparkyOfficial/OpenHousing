package ru.openhousing.coding.blocks.events;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Player;
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
import net.wesjd.anvilgui.AnvilGUI;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Специализированный блок для события входа игрока
 * Обрабатывает вход с детальным контролем, эффектами и настройками
 * 
 * @author OpenHousing Team
 * @version 1.0.0
 */
public class PlayerJoinEventBlock extends CodeBlock {
    
    // Статические поля для глобального управления
    private static final Map<UUID, PlayerJoinRecord> joinHistory = new ConcurrentHashMap<>();
    private static final Map<String, Integer> joinStats = new ConcurrentHashMap<>();
    private static final Set<UUID> recentlyJoinedPlayers = ConcurrentHashMap.newKeySet();
    private static final AtomicInteger totalJoins = new AtomicInteger(0);
    private static final AtomicInteger successfulJoins = new AtomicInteger(0);
    
    // Настройки блока
    private boolean welcomeMessageEnabled = true;
    private String welcomeMessage = "§aДобро пожаловать на сервер, {player}!";
    private boolean broadcastJoin = true;
    private String broadcastFormat = "§e{player} §7присоединился к серверу";
    private boolean privateJoin = false;
    private String privateMessage = "§7Вы тихо присоединились к серверу";
    
    // Настройки телепортации
    private boolean teleportToSpawn = true;
    private Location customSpawnLocation = null;
    private boolean useWorldSpawn = true;
    private boolean randomSpawn = false;
    private double spawnRadius = 100.0;
    
    // Настройки эффектов
    private boolean spawnJoinParticles = true;
    private Particle joinParticle = Particle.PORTAL;
    private int particleCount = 100;
    private double particleOffset = 1.0;
    private boolean playJoinSound = true;
    private Sound joinSound = Sound.ENTITY_PLAYER_LEVELUP;
    private float soundVolume = 1.0f;
    private float soundPitch = 1.0f;
    
    // Настройки предметов
    private boolean giveStarterItems = false;
    private List<ItemStack> starterItems = new ArrayList<>();
    private boolean clearInventory = false;
    private boolean restoreInventory = false;
    private boolean backupInventory = true;
    
    // Настройки эффектов зелий
    private boolean giveJoinEffects = false;
    private List<PotionEffect> joinEffects = new ArrayList<>();
    private boolean removeNegativeEffects = true;
    private boolean giveResistance = false;
    private int resistanceDuration = 300; // 15 секунд
    
    // Настройки здоровья и голода
    private boolean setFullHealth = true;
    private boolean setFullFood = true;
    private boolean setMaxExperience = false;
    private boolean giveAbsorption = false;
    private int absorptionAmount = 4;
    
    // Настройки разрешений
    private boolean requirePermission = false;
    private String requiredPermission = "openhousing.join";
    private boolean checkWhitelist = false;
    private boolean checkBan = true;
    private boolean checkMaintenance = false;
    
    // Настройки уведомлений
    private boolean notifyAdmins = false;
    private String adminNotificationFormat = "&c[Join] &f{player} &7joined from &e{ip}";
    private boolean notifyStaff = false;
    private String staffNotificationFormat = "&a[Join] &f{player} &7joined the server";
    private boolean showJoinInfo = false;
    
    // Настройки логирования
    private boolean logJoins = true;
    private boolean logToConsole = true;
    private boolean logToFile = false;
    private String logFormat = "[{timestamp}] {player} joined from {ip}";
    private boolean logLocation = true;
    private boolean logWorld = true;
    
    // Настройки статистики
    private boolean collectStats = true;
    private boolean trackPlayerStats = true;
    private boolean trackGlobalStats = true;
    private boolean exportStats = false;
    private String statsExportPath = "plugins/OpenHousing/join_stats/";
    
    // Настройки производительности
    private boolean asyncProcessing = true;
    private boolean cacheResults = true;
    private int cacheSize = 1000;
    private long cacheExpiry = 300000; // 5 минут
    
    // Enhanced GUI settings
    private boolean welcomeEnabled = true;
    private boolean effectsEnabled = false;
    private boolean itemsEnabled = false;
    private boolean teleportEnabled = true;
    private boolean notificationsEnabled = true;
    private boolean loggingEnabled = true;
    
    // Внутренние кэши и состояния
    private final Map<String, CachedJoinResult> joinCache = new ConcurrentHashMap<>();
    private final Map<UUID, PlayerJoinStats> playerStats = new ConcurrentHashMap<>();
    private final Map<String, GlobalJoinStats> globalStats = new ConcurrentHashMap<>();
    private final Queue<JoinRequest> pendingJoins = new LinkedList<>();
    
    public PlayerJoinEventBlock() {
        super(BlockType.PLAYER_JOIN);
        initializeDefaultSettings();
    }
    
    /**
     * Инициализация настроек по умолчанию
     */
    private void initializeDefaultSettings() {
        // Базовые настройки
        welcomeMessageEnabled = true;
        broadcastJoin = true;
        teleportToSpawn = true;
        useWorldSpawn = true;
        
        // Настройки эффектов
        spawnJoinParticles = true;
        joinParticle = Particle.PORTAL;
        particleCount = 100;
        particleOffset = 1.0;
        playJoinSound = true;
        joinSound = Sound.ENTITY_PLAYER_LEVELUP;
        
        // Настройки здоровья
        setFullHealth = true;
        setFullFood = true;
        removeNegativeEffects = true;
        
        // Инициализация статистики
        joinStats.put("TOTAL", 0);
        joinStats.put("SUCCESS", 0);
        joinStats.put("FAILED", 0);
        
        // Enhanced GUI settings
        welcomeEnabled = true;
        effectsEnabled = false;
        itemsEnabled = false;
        teleportEnabled = true;
        notificationsEnabled = true;
        loggingEnabled = true;
    }
    
    @Override
    public ExecutionResult execute(ExecutionContext context) {
        try {
            totalJoins.incrementAndGet();
            
            // Асинхронная обработка
            if (asyncProcessing) {
                return executeAsync(context);
            } else {
                return executeSync(context);
            }
        } catch (Exception e) {
            return ExecutionResult.error("Ошибка выполнения блока входа: " + e.getMessage());
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
            CachedJoinResult cached = joinCache.get(player.getUniqueId().toString());
            if (cached != null && !cached.isExpired()) {
                return cached.getResult();
            }
        }
        
        // Выполнение логики входа
        ExecutionResult result = processJoinLogic(context);
        
        // Кэширование результата
        if (cacheResults && result != null) {
            String cacheKey = player.getUniqueId().toString();
            joinCache.put(cacheKey, new CachedJoinResult(result, System.currentTimeMillis() + cacheExpiry));
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
     * Основная логика обработки входа
     */
    private ExecutionResult processJoinLogic(ExecutionContext context) {
        Player player = context.getPlayer();
        
        try {
            // Приветственное сообщение
            if (welcomeMessageEnabled) {
                sendWelcomeMessage(player);
            }
            
            // Телепортация на спавн
            if (teleportToSpawn) {
                teleportToSpawn(player);
            }
            
            // Предметы для новичков
            if (giveStarterItems) {
                giveStarterItems(player);
            }
            
            // Эффекты зелий
            if (giveJoinEffects) {
                applyJoinEffects(player);
            }
        
            // Здоровье и голод
            if (setFullHealth) {
                player.setHealth(player.getMaxHealth());
            }
            
            if (setFullFood) {
                player.setFoodLevel(20);
                player.setSaturation(20.0f);
            }
            
            if (removeNegativeEffects) {
                removeNegativeEffects(player);
            }
            
            if (giveResistance) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, resistanceDuration * 20, 0));
            }
            
            if (giveAbsorption) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 200, absorptionAmount - 1));
            }
            
            // Очистка инвентаря
            if (clearInventory) {
                player.getInventory().clear();
            }
            
            // Восстановление инвентаря
            if (restoreInventory) {
                restorePlayerInventory(player);
            }
            
            return ExecutionResult.success("Игрок успешно обработан при входе");
            
        } catch (Exception e) {
            return ExecutionResult.error("Ошибка обработки входа: " + e.getMessage());
        }
    }
    
    /**
     * Отправка приветственного сообщения
     */
    private void sendWelcomeMessage(Player player) {
        if (welcomeMessage.isEmpty()) {
            return;
        }
        
        String message = welcomeMessage
            .replace("{player}", player.getName())
            .replace("{online}", String.valueOf(Bukkit.getOnlinePlayers().size()))
            .replace("{max}", String.valueOf(Bukkit.getMaxPlayers()));
        
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
    }
    
    /**
     * Телепортация на спавн
     */
    private void teleportToSpawn(Player player) {
        Location spawnLocation = null;
        
        if (customSpawnLocation != null) {
            spawnLocation = customSpawnLocation;
        } else if (useWorldSpawn) {
            spawnLocation = player.getWorld().getSpawnLocation();
        }
        
        if (spawnLocation != null) {
            if (randomSpawn) {
                spawnLocation = getRandomSpawnLocation(spawnLocation, spawnRadius);
            }
            
            // Проверяем безопасность локации
            spawnLocation = findSafeLocation(spawnLocation);
            
            player.teleport(spawnLocation);
        }
    }
    
    /**
     * Получение случайной локации спавна
     */
    private Location getRandomSpawnLocation(Location center, double radius) {
        Random random = new Random();
        double angle = random.nextDouble() * 2 * Math.PI;
        double distance = random.nextDouble() * radius;
        
        double x = center.getX() + distance * Math.cos(angle);
        double z = center.getZ() + distance * Math.sin(angle);
        
        World world = center.getWorld();
        int y = world.getHighestBlockYAt((int) x, (int) z) + 1;
        
        return new Location(world, x, y, z);
    }
    
    /**
     * Поиск безопасной локации
     */
    private Location findSafeLocation(Location location) {
        World world = location.getWorld();
        int x = location.getBlockX();
        int y = location.getBlockY();
        int z = location.getBlockZ();
        
        // Ищем безопасное место сверху
        for (int i = 0; i < 10; i++) {
            Location testLocation = new Location(world, x, y + i, z);
            if (isLocationSafe(testLocation)) {
                return testLocation;
            }
        }
        
        // Ищем безопасное место снизу
        for (int i = 1; i < 10; i++) {
            Location testLocation = new Location(world, x, y - i, z);
            if (isLocationSafe(testLocation)) {
                return testLocation;
            }
        }
        
        return location;
    }
    
    /**
     * Проверка безопасности локации
     */
    private boolean isLocationSafe(Location location) {
        Material blockType = location.getBlock().getType();
        Material blockAbove = location.clone().add(0, 1, 0).getBlock().getType();
        Material blockBelow = location.clone().subtract(0, 1, 0).getBlock().getType();
        
        return blockType == Material.AIR && 
               blockAbove == Material.AIR && 
               blockBelow != Material.AIR && 
               blockBelow != Material.LAVA && 
               blockBelow != Material.FIRE;
    }
    
    /**
     * Выдача стартовых предметов
     */
    private void giveStarterItems(Player player) {
        if (starterItems.isEmpty()) {
            return;
        }
        
        for (ItemStack item : starterItems) {
            HashMap<Integer, ItemStack> notAdded = player.getInventory().addItem(item.clone());
            if (!notAdded.isEmpty()) {
                // Если инвентарь заполнен, дропаем предметы
                for (ItemStack droppedItem : notAdded.values()) {
                    player.getWorld().dropItemNaturally(player.getLocation(), droppedItem);
                }
            }
        }
    }
    
    /**
     * Применение эффектов при входе
     */
    private void applyJoinEffects(Player player) {
        if (joinEffects.isEmpty()) {
            return;
        }
        
        for (PotionEffect effect : joinEffects) {
            player.addPotionEffect(effect);
        }
    }
    
    /**
     * Удаление негативных эффектов
     */
    private void removeNegativeEffects(Player player) {
        for (PotionEffect effect : player.getActivePotionEffects()) {
            PotionEffectType type = effect.getType();
            if (type == PotionEffectType.POISON || 
                type == PotionEffectType.WITHER || 
                type == PotionEffectType.BLINDNESS || 
                type == PotionEffectType.WEAKNESS) {
                player.removePotionEffect(type);
            }
        }
    }
    
    /**
     * Восстановление инвентаря игрока
     */
    private void restorePlayerInventory(Player player) {
        // TODO: Реализовать восстановление инвентаря из сохранения
        // Это может быть через плагин или собственное API
    }
    
    /**
     * Обработка результата выполнения
     */
    private void handleExecutionResult(PlayerJoinEvent event, ExecutionResult result, ExecutionContext context) {
        if (result == null) {
            return;
        }
        
        if (result.isSuccess()) {
            successfulJoins.incrementAndGet();
            
            // Добавляем в список недавно вошедших
            recentlyJoinedPlayers.add(event.getPlayer().getUniqueId());
            
            // Удаляем через 5 минут
            Bukkit.getScheduler().runTaskLater(OpenHousing.getInstance(), () -> {
                recentlyJoinedPlayers.remove(event.getPlayer().getUniqueId());
            }, 6000L); // 5 минут * 20 тиков
            
        } else {
            // Логируем ошибку
            OpenHousing.getInstance().getLogger().warning(
                "Player join processing failed for " + event.getPlayer().getName() + 
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
            joinStats.merge("TOTAL", 1, Integer::sum);
            
            if (result != null && result.isSuccess()) {
                joinStats.merge("SUCCESS", 1, Integer::sum);
            } else {
                joinStats.merge("FAILED", 1, Integer::sum);
            }
        }
        
        // Обновление статистики игрока
        if (trackPlayerStats) {
            PlayerJoinStats playerStats = this.playerStats.computeIfAbsent(
                player.getUniqueId(), k -> new PlayerJoinStats());
            
            playerStats.addJoin(System.currentTimeMillis());
        }
    }
    
    /**
     * Логирование входа игрока
     */
    private void logPlayerJoin(Player player, ExecutionResult result, ExecutionContext context) {
        if (!logJoins) {
            return;
        }
        
        String logMessage = logFormat
            .replace("{timestamp}", new java.util.Date().toString())
            .replace("{player}", player.getName())
            .replace("{ip}", context.getVariable("ip").toString())
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
        saveToHistory(player.getUniqueId(), result, context);
    }
    
    /**
     * Отправка уведомлений
     */
    private void sendNotifications(Player player, ExecutionResult result, ExecutionContext context) {
        // Уведомление администраторов
        if (notifyAdmins) {
            String adminMessage = adminNotificationFormat
                .replace("{player}", player.getName())
                .replace("{ip}", context.getVariable("ip").toString())
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
        
        // Информация о входе
        if (showJoinInfo) {
            String infoMessage = "§7Информация о входе:\n" +
                "§7IP: §e" + context.getVariable("ip") + "\n" +
                "§7Мир: §e" + context.getVariable("world") + "\n" +
                "§7Локация: §e" + context.getVariable("location");
            
            player.sendMessage(infoMessage);
        }
    }
    
    /**
     * Спавн эффектов при входе
     */
    private void spawnJoinEffects(Player player) {
        Location location = player.getLocation();
        
        // Частицы
        if (spawnJoinParticles) {
            location.getWorld().spawnParticle(
                joinParticle, 
                location, 
                particleCount, 
                particleOffset, 
                particleOffset, 
                particleOffset, 
                0.1
            );
        }
        
        // Звук
        if (playJoinSound) {
            location.getWorld().playSound(
                location, 
                joinSound, 
                soundVolume, 
                soundPitch
            );
        }
    }
    
    /**
     * Сохранение в историю
     */
    private void saveToHistory(UUID playerId, ExecutionResult result, ExecutionContext context) {
        PlayerJoinRecord record = new PlayerJoinRecord(
            playerId,
            System.currentTimeMillis(),
            result != null ? result.isSuccess() : false,
            result != null ? result.getMessage() : null,
            context.getVariable("ip").toString(),
            context.getVariable("world").toString(),
            context.getVariable("location").toString()
        );
        
        joinHistory.put(playerId, record);
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
        if (resistanceDuration < 0) return false;
        if (absorptionAmount < 0) return false;
        if (spawnRadius < 0) return false;
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
        description.add("§6Блок события входа игрока");
        description.add("§7Обрабатывает вход с детальным контролем");
        description.add("§7и расширенными возможностями");
        description.add("");
        description.add("§eНастройки:");
        description.add("§7• Приветствие: " + (welcomeEnabled ? "§aВключено" : "§cВыключено"));
        description.add("§7• Эффекты: " + (effectsEnabled ? "§aВключены" : "§cВыключены"));
        description.add("§7• Предметы: " + (itemsEnabled ? "§aВключены" : "§cВыключены"));
        description.add("§7• Телепортация: " + (teleportEnabled ? "§aВключена" : "§cВыключена"));
        description.add("§7• Уведомления: " + (notificationsEnabled ? "§aВключены" : "§cВыключены"));
        description.add("§7• Логирование: " + (loggingEnabled ? "§aВключено" : "§cВыключено"));
        
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
                        player.sendMessage("§aJoin configuration updated!")
                    );
                    return AnvilGUI.Response.close();
                })
                .onClose(player1 -> {
                    Bukkit.getScheduler().runTask(OpenHousing.getInstance(), () -> 
                        player.sendMessage("§cConfiguration cancelled"));
                })
                .text("Configure join settings")
                .title("§6Join Block Config")
                .plugin(OpenHousing.getInstance())
                .open(player);
    }

    /**
     * Shows current settings in chat
     */
    public void showSettings(Player player) {
        player.sendMessage("§6=== Join Block Settings ===");
        player.sendMessage("§eWelcome: " + (welcomeEnabled ? "§aON" : "§cOFF"));
        player.sendMessage("§eEffects: " + (effectsEnabled ? "§aON" : "§cOFF"));
        player.sendMessage("§eItems: " + (itemsEnabled ? "§aON" : "§cOFF"));
        player.sendMessage("§eTeleport: " + (teleportEnabled ? "§aON" : "§cOFF"));
        player.sendMessage("§eNotifications: " + (notificationsEnabled ? "§aON" : "§cOFF"));
        player.sendMessage("§eLogging: " + (loggingEnabled ? "§aON" : "§cOFF"));
        player.sendMessage("§7Use /configure to change settings");
    }

    /**
     * Parse configuration from text input
     */
    private void parseConfiguration(String text) {
        // Simple configuration parser
        if (text.startsWith("welcome:")) {
            welcomeEnabled = text.contains("true");
        } else if (text.startsWith("effects:")) {
            effectsEnabled = text.contains("true");
        } else if (text.startsWith("items:")) {
            itemsEnabled = text.contains("true");
        } else if (text.startsWith("teleport:")) {
            teleportEnabled = text.contains("true");
        } else if (text.startsWith("notifications:")) {
            notificationsEnabled = text.contains("true");
        } else if (text.startsWith("logging:")) {
            loggingEnabled = text.contains("true");
        }
    }
    
    // Геттеры и сеттеры для настройки блока
    public void setWelcomeMessageEnabled(boolean welcomeMessageEnabled) { this.welcomeMessageEnabled = welcomeMessageEnabled; }
    public void setWelcomeMessage(String welcomeMessage) { this.welcomeMessage = welcomeMessage; }
    public void setBroadcastJoin(boolean broadcastJoin) { this.broadcastJoin = broadcastJoin; }
    public void setBroadcastFormat(String broadcastFormat) { this.broadcastFormat = broadcastFormat; }
    public void setPrivateJoin(boolean privateJoin) { this.privateJoin = privateJoin; }
    public void setPrivateMessage(String privateMessage) { this.privateMessage = privateMessage; }
    public void setTeleportToSpawn(boolean teleportToSpawn) { this.teleportToSpawn = teleportToSpawn; }
    public void setCustomSpawnLocation(Location customSpawnLocation) { this.customSpawnLocation = customSpawnLocation; }
    public void setUseWorldSpawn(boolean useWorldSpawn) { this.useWorldSpawn = useWorldSpawn; }
    public void setRandomSpawn(boolean randomSpawn) { this.randomSpawn = randomSpawn; }
    public void setSpawnRadius(double spawnRadius) { this.spawnRadius = spawnRadius; }
    public void setSpawnJoinParticles(boolean spawnJoinParticles) { this.spawnJoinParticles = spawnJoinParticles; }
    public void setJoinParticle(Particle joinParticle) { this.joinParticle = joinParticle; }
    public void setParticleCount(int particleCount) { this.particleCount = particleCount; }
    public void setParticleOffset(double particleOffset) { this.particleOffset = particleOffset; }
    public void setPlayJoinSound(boolean playJoinSound) { this.playJoinSound = playJoinSound; }
    public void setJoinSound(Sound joinSound) { this.joinSound = joinSound; }
    public void setSoundVolume(float soundVolume) { this.soundVolume = soundVolume; }
    public void setSoundPitch(float soundPitch) { this.soundPitch = soundPitch; }
    public void setGiveStarterItems(boolean giveStarterItems) { this.giveStarterItems = giveStarterItems; }
    public void setStarterItems(List<ItemStack> starterItems) { this.starterItems = starterItems; }
    public void setClearInventory(boolean clearInventory) { this.clearInventory = clearInventory; }
    public void setRestoreInventory(boolean restoreInventory) { this.restoreInventory = restoreInventory; }
    public void setBackupInventory(boolean backupInventory) { this.backupInventory = backupInventory; }
    public void setGiveJoinEffects(boolean giveJoinEffects) { this.giveJoinEffects = giveJoinEffects; }
    public void setJoinEffects(List<PotionEffect> joinEffects) { this.joinEffects = joinEffects; }
    public void setRemoveNegativeEffects(boolean removeNegativeEffects) { this.removeNegativeEffects = removeNegativeEffects; }
    public void setGiveResistance(boolean giveResistance) { this.giveResistance = giveResistance; }
    public void setResistanceDuration(int resistanceDuration) { this.resistanceDuration = resistanceDuration; }
    public void setSetFullHealth(boolean setFullHealth) { this.setFullHealth = setFullHealth; }
    public void setSetFullFood(boolean setFullFood) { this.setFullFood = setFullFood; }
    public void setSetMaxExperience(boolean setMaxExperience) { this.setMaxExperience = setMaxExperience; }
    public void setGiveAbsorption(boolean giveAbsorption) { this.giveAbsorption = giveAbsorption; }
    public void setAbsorptionAmount(int absorptionAmount) { this.absorptionAmount = absorptionAmount; }
    public void setRequirePermission(boolean requirePermission) { this.requirePermission = requirePermission; }
    public void setRequiredPermission(String requiredPermission) { this.requiredPermission = requiredPermission; }
    public void setCheckWhitelist(boolean checkWhitelist) { this.checkWhitelist = checkWhitelist; }
    public void setCheckBan(boolean checkBan) { this.checkBan = checkBan; }
    public void setCheckMaintenance(boolean checkMaintenance) { this.checkMaintenance = checkMaintenance; }
    public void setNotifyAdmins(boolean notifyAdmins) { this.notifyAdmins = notifyAdmins; }
    public void setAdminNotificationFormat(String adminNotificationFormat) { this.adminNotificationFormat = adminNotificationFormat; }
    public void setNotifyStaff(boolean notifyStaff) { this.notifyStaff = notifyStaff; }
    public void setStaffNotificationFormat(String staffNotificationFormat) { this.staffNotificationFormat = staffNotificationFormat; }
    public void setShowJoinInfo(boolean showJoinInfo) { this.showJoinInfo = showJoinInfo; }
    public void setLogJoins(boolean logJoins) { this.logJoins = logJoins; }
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
    private static class CachedJoinResult {
        private final ExecutionResult result;
        private final long expiryTime;
        
        public CachedJoinResult(ExecutionResult result, long expiryTime) {
            this.result = result;
            this.expiryTime = expiryTime;
        }
        
        public ExecutionResult getResult() { return result; }
        public boolean isExpired() { return System.currentTimeMillis() > expiryTime; }
    }
    
    private static class PlayerJoinRecord {
        private final UUID playerId;
        private final long joinTime;
        private final boolean success;
        private final String message;
        private final String ip;
        private final String world;
        private final String location;
        
        public PlayerJoinRecord(UUID playerId, long joinTime, boolean success, 
                              String message, String ip, String world, String location) {
            this.playerId = playerId;
            this.joinTime = joinTime;
            this.success = success;
            this.message = message;
            this.ip = ip;
            this.world = world;
            this.location = location;
        }
        
        // Геттеры
        public UUID getPlayerId() { return playerId; }
        public long getJoinTime() { return joinTime; }
        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
        public String getIp() { return ip; }
        public String getWorld() { return world; }
        public String getLocation() { return location; }
    }
    
    private static class PlayerJoinStats {
        private final List<Long> joinTimes = new ArrayList<>();
        private long lastJoinTime = 0;
        
        public void addJoin(long timestamp) {
            joinTimes.add(timestamp);
            lastJoinTime = timestamp;
        }
        
        public void setLastJoinTime(long time) { this.lastJoinTime = time; }
        public long getLastJoinTime() { return lastJoinTime; }
        public List<Long> getJoinTimes() { return joinTimes; }
    }
    
    private static class GlobalJoinStats {
        private int totalJoins = 0;
        private int successfulJoins = 0;
        private int failedJoins = 0;
        private long totalProcessingTime = 0;
        
        public void addJoin(boolean success, long processingTime) {
            totalJoins++;
            if (success) {
                successfulJoins++;
            } else {
                failedJoins++;
            }
            totalProcessingTime += processingTime;
        }
        
        // Геттеры
        public int getTotalJoins() { return totalJoins; }
        public int getSuccessfulJoins() { return successfulJoins; }
        public int getFailedJoins() { return failedJoins; }
        public long getTotalProcessingTime() { return totalProcessingTime; }
        public double getSuccessRate() { return totalJoins > 0 ? (double) successfulJoins / totalJoins : 0.0; }
        public double getAverageProcessingTime() { return totalJoins > 0 ? (double) totalProcessingTime / totalJoins : 0.0; }
    }
    
    private static class JoinRequest {
        private final UUID playerId;
        private final long requestTime;
        private final boolean priority;
        
        public JoinRequest(UUID playerId, long requestTime, boolean priority) {
            this.playerId = playerId;
            this.requestTime = requestTime;
            this.priority = priority;
        }
        
        public UUID getPlayerId() { return playerId; }
        public long getRequestTime() { return requestTime; }
        public boolean isPriority() { return priority; }
    }
}
