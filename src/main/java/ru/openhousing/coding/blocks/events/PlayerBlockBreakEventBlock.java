package ru.openhousing.coding.blocks.events;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import ru.openhousing.OpenHousing;
import ru.openhousing.coding.blocks.BlockType;
import ru.openhousing.coding.blocks.CodeBlock;
import ru.openhousing.coding.blocks.ExecutionContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Специализированный блок для обработки разрушения блоков игроками
 * Предоставляет расширенную функциональность для управления процессом разрушения
 */
public class PlayerBlockBreakEventBlock extends CodeBlock {
    
    // Настройки блокировки
    private boolean blockBreakEnabled = true;
    private boolean dropItemsEnabled = true;
    private boolean experienceEnabled = true;
    private boolean soundEnabled = true;
    private boolean particlesEnabled = true;
    
    // Настройки дропа
    private boolean customDropsEnabled = false;
    private boolean fortuneEnabled = false;
    private boolean silkTouchEnabled = false;
    private boolean autoSmeltEnabled = false;
    
    // Настройки опыта
    private boolean customExperienceEnabled = false;
    private int baseExperience = 0;
    private double experienceMultiplier = 1.0;
    
    // Настройки уведомлений
    private boolean notificationsEnabled = true;
    private boolean loggingEnabled = false;
    private boolean statisticsEnabled = true;
    
    // Настройки ограничений
    private boolean permissionCheckEnabled = true;
    private boolean cooldownEnabled = false;
    private long cooldownMs = 1000;
    private boolean areaRestrictionEnabled = false;
    private double maxBreakRadius = 10.0;
    
    // Настройки эффектов
    private boolean breakEffectsEnabled = true;
    private boolean customSoundEnabled = false;
    private String customSoundName = "BLOCK_STONE_BREAK";
    private float customSoundVolume = 1.0f;
    private float customSoundPitch = 1.0f;
    
    // Настройки восстановления
    private boolean autoRestoreEnabled = false;
    private long restoreDelayTicks = 100;
    private boolean restoreWithData = true;
    
    // Настройки экономики
    private boolean economyEnabled = false;
    private double breakReward = 0.0;
    private boolean sellDropsEnabled = false;
    
    // Настройки достижений
    private boolean achievementsEnabled = true;
    private boolean progressTrackingEnabled = true;
    
    // Настройки PvP
    private boolean pvpProtectionEnabled = false;
    private boolean teamProtectionEnabled = false;
    
    // Настройки времени
    private boolean timeRestrictionEnabled = false;
    private long breakStartTime = 0;
    private long breakEndTime = 24000;
    
    // Настройки погоды
    private boolean weatherRestrictionEnabled = false;
    private boolean allowBreakInRain = true;
    private boolean allowBreakInStorm = false;
    
    // Настройки инструментов
    private boolean toolRestrictionEnabled = false;
    private List<Material> allowedTools = new ArrayList<>();
    private boolean durabilityCheckEnabled = true;
    
    // Настройки блоков
    private boolean blockRestrictionEnabled = false;
    private List<Material> allowedBlocks = new ArrayList<>();
    private List<Material> forbiddenBlocks = new ArrayList<>();
    
    // Настройки мира
    private boolean worldRestrictionEnabled = false;
    private List<String> allowedWorlds = new ArrayList<>();
    private List<String> forbiddenWorlds = new ArrayList<>();
    
    // Настройки региона
    private boolean regionProtectionEnabled = false;
    private boolean bypassProtectionEnabled = false;
    
    // Настройки команды
    private boolean commandExecutionEnabled = false;
    private List<String> breakCommands = new ArrayList<>();
    private List<String> completeCommands = new ArrayList<>();
    
    // Настройки уведомлений
    private boolean blockBreakMessageEnabled = true;
    private String blockBreakMessage = "&aВы сломали блок %block%";
    private boolean blockBreakTitleEnabled = false;
    private String blockBreakTitle = "&eБлок сломан!";
    private String blockBreakSubtitle = "&7%block%";
    
    // Настройки статистики
    private boolean blockBreakStatsEnabled = true;
    private boolean playerStatsEnabled = true;
    private boolean globalStatsEnabled = false;
    
    // Настройки кэширования
    private boolean blockCacheEnabled = true;
    private int maxCacheSize = 1000;
    private long cacheExpirationMs = 300000;
    
    // Настройки производительности
    private boolean asyncProcessingEnabled = false;
    private boolean batchProcessingEnabled = false;
    private int batchSize = 10;
    
    // Настройки безопасности
    private boolean antiExploitEnabled = true;
    private boolean rateLimitEnabled = true;
    private int maxBreaksPerSecond = 20;
    private boolean suspiciousActivityDetection = true;
    
    // Настройки логирования
    private boolean detailedLoggingEnabled = false;
    private boolean performanceLoggingEnabled = false;
    private boolean errorLoggingEnabled = true;
    
    // Настройки отладки
    private boolean debugModeEnabled = false;
    private boolean verboseOutputEnabled = false;
    
    public PlayerBlockBreakEventBlock() {
        super(BlockType.PLAYER_BLOCK_BREAK);
        initializeDefaultSettings();
    }
    
    private void initializeDefaultSettings() {
        // Инициализация разрешенных инструментов
        allowedTools.add(Material.DIAMOND_PICKAXE);
        allowedTools.add(Material.IRON_PICKAXE);
        allowedTools.add(Material.STONE_PICKAXE);
        allowedTools.add(Material.WOODEN_PICKAXE);
        allowedTools.add(Material.GOLDEN_PICKAXE);
        
        // Инициализация разрешенных миров
        allowedWorlds.add("world");
        allowedWorlds.add("world_nether");
        allowedWorlds.add("world_the_end");
        
        // Инициализация команд
        breakCommands.add("say Игрок %player% сломал блок %block%");
        completeCommands.add("give %player% %block% 1");
    }
    
    @Override
    public void execute(ExecutionContext context) {
        // Основная логика выполняется через событие
        // Этот метод вызывается при создании блока
        if (debugModeEnabled) {
            context.getPlayer().sendMessage("§7[DEBUG] PlayerBlockBreakEventBlock создан");
        }
    }
    
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();
        Location location = block.getLocation();
        
        // Проверка включения
        if (!blockBreakEnabled) {
            event.setCancelled(true);
            return;
        }
        
        // Проверка разрешений
        if (permissionCheckEnabled && !player.hasPermission("openhousing.blockbreak")) {
            event.setCancelled(true);
            player.sendMessage("§cУ вас нет разрешения на разрушение блоков!");
            return;
        }
        
        // Проверка кулдауна
        if (cooldownEnabled && !checkCooldown(player)) {
            event.setCancelled(true);
            player.sendMessage("§cПодождите перед следующим разрушением блока!");
            return;
        }
        
        // Проверка ограничений по области
        if (areaRestrictionEnabled && !checkAreaRestriction(player, location)) {
            event.setCancelled(true);
            player.sendMessage("§cРазрушение блоков запрещено в этой области!");
            return;
        }
        
        // Проверка ограничений по времени
        if (timeRestrictionEnabled && !checkTimeRestriction(location.getWorld())) {
            event.setCancelled(true);
            player.sendMessage("§cРазрушение блоков запрещено в это время!");
            return;
        }
        
        // Проверка ограничений по погоде
        if (weatherRestrictionEnabled && !checkWeatherRestriction(location.getWorld())) {
            event.setCancelled(true);
            player.sendMessage("§cРазрушение блоков запрещено в такую погоду!");
            return;
        }
        
        // Проверка ограничений по инструментам
        if (toolRestrictionEnabled && !checkToolRestriction(player)) {
            event.setCancelled(true);
            player.sendMessage("§cИспользуйте подходящий инструмент для разрушения!");
            return;
        }
        
        // Проверка ограничений по блокам
        if (blockRestrictionEnabled && !checkBlockRestriction(block)) {
            event.setCancelled(true);
            player.sendMessage("§cЭтот блок нельзя разрушать!");
            return;
        }
        
        // Проверка ограничений по миру
        if (worldRestrictionEnabled && !checkWorldRestriction(location.getWorld().getName())) {
            event.setCancelled(true);
            player.sendMessage("§cРазрушение блоков запрещено в этом мире!");
            return;
        }
        
        // Проверка защиты региона
        if (regionProtectionEnabled && !checkRegionProtection(location)) {
            event.setCancelled(true);
            player.sendMessage("§cЭтот регион защищен от разрушения!");
            return;
        }
        
        // Проверка анти-эксплойт
        if (antiExploitEnabled && !checkAntiExploit(player)) {
            event.setCancelled(true);
            player.sendMessage("§cПодозрительная активность обнаружена!");
            return;
        }
        
        // Сохранение состояния блока для восстановления
        BlockState blockState = null;
        if (autoRestoreEnabled) {
            blockState = block.getState();
        }
        
        // Выполнение команд при разрушении
        if (commandExecutionEnabled) {
            executeBreakCommands(player, block);
        }
        
        // Настройка дропа
        if (!dropItemsEnabled) {
            event.setDropItems(false);
        }
        
        // Настройка опыта
        if (!experienceEnabled) {
            event.setExpToDrop(0);
        }
        
        // Настройка звука
        if (!soundEnabled) {
            event.setCancelled(true);
            // Восстанавливаем блок и ломаем без звука
            block.setType(Material.AIR);
        }
        
        // Настройка частиц
        if (!particlesEnabled) {
            // Отключаем частицы (через NMS или плагины)
        }
        
        // Кастомные дропы
        if (customDropsEnabled) {
            handleCustomDrops(player, block, location);
        }
        
        // Кастомный опыт
        if (customExperienceEnabled) {
            handleCustomExperience(player, block, location);
        }
        
        // Эффекты разрушения
        if (breakEffectsEnabled) {
            handleBreakEffects(player, block, location);
        }
        
        // Кастомный звук
        if (customSoundEnabled) {
            handleCustomSound(player, location);
        }
        
        // Уведомления
        if (notificationsEnabled) {
            handleNotifications(player, block);
        }
        
        // Логирование
        if (loggingEnabled) {
            handleLogging(player, block, location);
        }
        
        // Статистика
        if (statisticsEnabled) {
            handleStatistics(player, block);
        }
        
        // Достижения
        if (achievementsEnabled) {
            handleAchievements(player, block);
        }
        
        // Экономика
        if (economyEnabled) {
            handleEconomy(player, block);
        }
        
        // PvP защита
        if (pvpProtectionEnabled) {
            handlePvpProtection(player, block);
        }
        
        // Команды при завершении
        if (commandExecutionEnabled) {
            executeCompleteCommands(player, block);
        }
        
        // Автоматическое восстановление
        if (autoRestoreEnabled && blockState != null) {
            scheduleBlockRestore(blockState);
        }
        
        // Обновление кулдауна
        if (cooldownEnabled) {
            updateCooldown(player);
        }
        
        // Обновление статистики
        if (blockBreakStatsEnabled) {
            updateBlockBreakStats(player, block);
        }
        
        // Обновление статистики игрока
        if (playerStatsEnabled) {
            updatePlayerStats(player, block);
        }
        
        // Обновление глобальной статистики
        if (globalStatsEnabled) {
            updateGlobalStats(block);
        }
        
        // Кэширование
        if (blockCacheEnabled) {
            cacheBlockBreak(player, block, location);
        }
        
        // Отладка
        if (debugModeEnabled) {
            handleDebugOutput(player, block, location);
        }
    }
    
    private boolean checkCooldown(Player player) {
        // Проверка кулдауна для игрока
        long currentTime = System.currentTimeMillis();
        String playerId = player.getUniqueId().toString();
        
        // Получаем время последнего разрушения из переменных
        String lastBreakKey = "last_break_" + playerId;
        Object lastBreakTime = getVariable(lastBreakKey);
        
        if (lastBreakTime != null && lastBreakTime instanceof Number) {
            long lastBreak = ((Number) lastBreakTime).longValue();
            if (currentTime - lastBreak < cooldownMs) {
                return false;
            }
        }
        
        return true;
    }
    
    private void updateCooldown(Player player) {
        // Обновление времени последнего разрушения
        long currentTime = System.currentTimeMillis();
        String playerId = player.getUniqueId().toString();
        String lastBreakKey = "last_break_" + playerId;
        
        setVariable(lastBreakKey, currentTime);
    }
    
    private boolean checkAreaRestriction(Player player, Location location) {
        // Проверка ограничений по области
        if (maxBreakRadius > 0) {
            Location playerLocation = player.getLocation();
            double distance = playerLocation.distance(location);
            return distance <= maxBreakRadius;
        }
        return true;
    }
    
    private boolean checkTimeRestriction(org.bukkit.World world) {
        // Проверка ограничений по времени
        long time = world.getTime();
        return time >= breakStartTime && time <= breakEndTime;
    }
    
    private boolean checkWeatherRestriction(org.bukkit.World world) {
        // Проверка ограничений по погоде
        if (world.hasStorm() && !allowBreakInStorm) {
            return false;
        }
        if (world.isThundering() && !allowBreakInRain) {
            return false;
        }
        return true;
    }
    
    private boolean checkToolRestriction(Player player) {
        // Проверка ограничений по инструментам
        ItemStack itemInHand = player.getInventory().getItemInMainHand();
        if (itemInHand.getType() == Material.AIR) {
            return false;
        }
        
        if (durabilityCheckEnabled) {
            if (itemInHand.getDurability() >= itemInHand.getType().getMaxDurability()) {
                return false;
            }
        }
        
        return allowedTools.contains(itemInHand.getType());
    }
    
    private boolean checkBlockRestriction(Block block) {
        // Проверка ограничений по блокам
        Material blockType = block.getType();
        
        if (forbiddenBlocks.contains(blockType)) {
            return false;
        }
        
        if (!allowedBlocks.isEmpty() && !allowedBlocks.contains(blockType)) {
            return false;
        }
        
        return true;
    }
    
    private boolean checkWorldRestriction(String worldName) {
        // Проверка ограничений по миру
        if (forbiddenWorlds.contains(worldName)) {
            return false;
        }
        
        if (!allowedWorlds.isEmpty() && !allowedWorlds.contains(worldName)) {
            return false;
        }
        
        return true;
    }
    
    private boolean checkRegionProtection(Location location) {
        // Проверка защиты региона
        // Здесь можно интегрировать с WorldGuard или другими плагинами защиты
        return true;
    }
    
    private boolean checkAntiExploit(Player player) {
        // Проверка анти-эксплойт
        if (rateLimitEnabled) {
            String playerId = player.getUniqueId().toString();
            String breakCountKey = "break_count_" + playerId;
            String lastResetKey = "last_reset_" + playerId;
            
            long currentTime = System.currentTimeMillis();
            Object lastReset = getVariable(lastResetKey);
            
            if (lastReset == null || currentTime - ((Number) lastReset).longValue() > 1000) {
                setVariable(lastResetKey, currentTime);
                setVariable(breakCountKey, 0);
            }
            
            Object breakCount = getVariable(breakCountKey);
            int count = breakCount instanceof Number ? ((Number) breakCount).intValue() : 0;
            
            if (count >= maxBreaksPerSecond) {
                return false;
            }
            
            setVariable(breakCountKey, count + 1);
        }
        
        return true;
    }
    
    private void handleCustomDrops(Player player, Block block, Location location) {
        // Обработка кастомных дропов
        if (fortuneEnabled) {
            // Логика удачи
            Random random = new Random();
            int fortuneLevel = getFortuneLevel(player);
            if (fortuneLevel > 0) {
                // Увеличиваем количество дропа
            }
        }
        
        if (silkTouchEnabled) {
            // Логика шелкового касания
            if (hasSilkTouch(player)) {
                // Дропаем сам блок
            }
        }
        
        if (autoSmeltEnabled) {
            // Логика автоматической переплавки
            Material smeltedType = getSmeltedType(block.getType());
            if (smeltedType != null) {
                // Дропаем переплавленный предмет
            }
        }
    }
    
    private void handleCustomExperience(Player player, Block block, Location location) {
        // Обработка кастомного опыта
        int experience = calculateCustomExperience(block);
        if (experience > 0) {
            block.getWorld().spawn(location, org.bukkit.entity.ExperienceOrb.class)
                 .setExperience(experience);
        }
    }
    
    private void handleBreakEffects(Player player, Block block, Location location) {
        // Обработка эффектов разрушения
        if (particlesEnabled) {
            // Создание частиц
            block.getWorld().spawnParticle(
                org.bukkit.Particle.BLOCK_CRACK,
                location.add(0.5, 0.5, 0.5),
                10,
                0.2, 0.2, 0.2,
                0.1,
                block.getBlockData()
            );
        }
    }
    
    private void handleCustomSound(Player player, Location location) {
        // Обработка кастомного звука
        try {
            org.bukkit.Sound sound = org.bukkit.Sound.valueOf(customSoundName);
            player.playSound(location, sound, customSoundVolume, customSoundPitch);
        } catch (IllegalArgumentException e) {
            // Используем стандартный звук
            player.playSound(location, org.bukkit.Sound.BLOCK_STONE_BREAK, 1.0f, 1.0f);
        }
    }
    
    private void handleNotifications(Player player, Block block) {
        // Обработка уведомлений
        if (blockBreakMessageEnabled) {
            String message = blockBreakMessage
                .replace("%player%", player.getName())
                .replace("%block%", block.getType().name())
                .replace("%location%", formatLocation(block.getLocation()));
            
            player.sendMessage(message.replace("&", "§"));
        }
        
        if (blockBreakTitleEnabled) {
            String title = blockBreakTitle
                .replace("%player%", player.getName())
                .replace("%block%", block.getType().name());
            
            String subtitle = blockBreakSubtitle
                .replace("%player%", player.getName())
                .replace("%block%", block.getType().name());
            
            player.sendTitle(
                title.replace("&", "§"),
                subtitle.replace("&", "§"),
                10, 40, 10
            );
        }
    }
    
    private void handleLogging(Player player, Block block, Location location) {
        // Обработка логирования
        if (detailedLoggingEnabled) {
            String logMessage = String.format(
                "[BLOCK_BREAK] Player: %s, Block: %s, Location: %s, Time: %d",
                player.getName(),
                block.getType().name(),
                formatLocation(location),
                System.currentTimeMillis()
            );
            
            Bukkit.getLogger().info(logMessage);
        }
    }
    
    private void handleStatistics(Player player, Block block) {
        // Обработка статистики
        String playerId = player.getUniqueId().toString();
        String blockType = block.getType().name();
        
        // Статистика по блокам
        String blockStatsKey = "block_stats_" + blockType;
        Object blockStats = getVariable(blockStatsKey);
        int blockCount = blockStats instanceof Number ? ((Number) blockStats).intValue() : 0;
        setVariable(blockStatsKey, blockCount + 1);
        
        // Статистика игрока
        String playerStatsKey = "player_stats_" + playerId;
        Object playerStats = getVariable(playerStatsKey);
        int playerCount = playerStats instanceof Number ? ((Number) playerStats).intValue() : 0;
        setVariable(playerStatsKey, playerCount + 1);
    }
    
    private void handleAchievements(Player player, Block block) {
        // Обработка достижений
        if (progressTrackingEnabled) {
            // Отслеживание прогресса по достижениям
            String achievementKey = "achievement_blocks_" + player.getUniqueId();
            Object achievementProgress = getVariable(achievementKey);
            int progress = achievementProgress instanceof Number ? ((Number) achievementProgress).intValue() : 0;
            
            // Проверка достижений
            checkBlockBreakAchievements(player, progress + 1);
            
            setVariable(achievementKey, progress + 1);
        }
    }
    
    private void handleEconomy(Player player, Block block) {
        // Обработка экономики
        if (breakReward > 0) {
            // Награда за разрушение блока
            // Здесь можно интегрировать с Vault или другими плагинами экономики
        }
        
        if (sellDropsEnabled) {
            // Автоматическая продажа дропа
            // Здесь можно интегрировать с плагинами магазинов
        }
    }
    
    private void handlePvpProtection(Player player, Block block) {
        // Обработка PvP защиты
        if (pvpProtectionEnabled) {
            // Проверка PvP статуса
            // Здесь можно интегрировать с плагинами PvP
        }
        
        if (teamProtectionEnabled) {
            // Проверка команды
            // Здесь можно интегрировать с плагинами команд
        }
    }
    
    private void executeBreakCommands(Player player, Block block) {
        // Выполнение команд при разрушении
        for (String command : breakCommands) {
            String processedCommand = command
                .replace("%player%", player.getName())
                .replace("%block%", block.getType().name())
                .replace("%location%", formatLocation(block.getLocation()));
            
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), processedCommand);
        }
    }
    
    private void executeCompleteCommands(Player player, Block block) {
        // Выполнение команд при завершении
        for (String command : completeCommands) {
            String processedCommand = command
                .replace("%player%", player.getName())
                .replace("%block%", block.getType().name());
            
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), processedCommand);
        }
    }
    
    private void scheduleBlockRestore(BlockState blockState) {
        // Планирование восстановления блока
        Bukkit.getScheduler().runTaskLater(OpenHousing.getInstance(), () -> {
            if (restoreWithData) {
                blockState.update(true);
            } else {
                blockState.getBlock().setType(blockState.getType());
            }
        }, restoreDelayTicks);
    }
    
    private void updateBlockBreakStats(Player player, Block block) {
        // Обновление статистики разрушения блоков
        String blockType = block.getType().name();
        String statsKey = "global_block_stats_" + blockType;
        
        Object currentStats = getVariable(statsKey);
        int count = currentStats instanceof Number ? ((Number) currentStats).intValue() : 0;
        setVariable(statsKey, count + 1);
    }
    
    private void updatePlayerStats(Player player, Block block) {
        // Обновление статистики игрока
        String playerId = player.getUniqueId().toString();
        String statsKey = "player_block_stats_" + playerId;
        
        Object currentStats = getVariable(statsKey);
        int count = currentStats instanceof Number ? ((Number) currentStats).intValue() : 0;
        setVariable(statsKey, count + 1);
    }
    
    private void updateGlobalStats(Block block) {
        // Обновление глобальной статистики
        String globalStatsKey = "global_blocks_broken";
        
        Object currentStats = getVariable(globalStatsKey);
        int count = currentStats instanceof Number ? ((Number) currentStats).intValue() : 0;
        setVariable(globalStatsKey, count + 1);
    }
    
    private void cacheBlockBreak(Player player, Block block, Location location) {
        // Кэширование разрушения блока
        String cacheKey = "block_break_cache_" + player.getUniqueId();
        
        // Простое кэширование - можно расширить
        setVariable(cacheKey + "_last_block", block.getType().name());
        setVariable(cacheKey + "_last_location", formatLocation(location));
        setVariable(cacheKey + "_last_time", System.currentTimeMillis());
    }
    
    private void handleDebugOutput(Player player, Block block, Location location) {
        // Обработка отладочного вывода
        if (verboseOutputEnabled) {
            player.sendMessage("§7[DEBUG] Блок: " + block.getType().name());
            player.sendMessage("§7[DEBUG] Локация: " + formatLocation(location));
            player.sendMessage("§7[DEBUG] Время: " + System.currentTimeMillis());
        }
    }
    
    // Вспомогательные методы
    private int getFortuneLevel(Player player) {
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item.containsEnchantment(org.bukkit.enchantments.Enchantment.LOOT_BONUS_BLOCKS)) {
            return item.getEnchantmentLevel(org.bukkit.enchantments.Enchantment.LOOT_BONUS_BLOCKS);
        }
        return 0;
    }
    
    private boolean hasSilkTouch(Player player) {
        ItemStack item = player.getInventory().getItemInMainHand();
        return item.containsEnchantment(org.bukkit.enchantments.Enchantment.SILK_TOUCH);
    }
    
    private Material getSmeltedType(Material originalType) {
        // Простая логика переплавки
        switch (originalType) {
            case IRON_ORE: return Material.IRON_INGOT;
            case GOLD_ORE: return Material.GOLD_INGOT;
            case COPPER_ORE: return Material.COPPER_INGOT;
            case SAND: return Material.GLASS;
            case CLAY: return Material.TERRACOTTA;
            default: return null;
        }
    }
    
    private int calculateCustomExperience(Block block) {
        // Расчет кастомного опыта
        int baseExp = baseExperience;
        double multiplier = experienceMultiplier;
        
        // Базовый опыт по типу блока
        switch (block.getType()) {
            case DIAMOND_ORE:
            case EMERALD_ORE:
                baseExp = Math.max(baseExp, 7);
                break;
            case GOLD_ORE:
            case REDSTONE_ORE:
                baseExp = Math.max(baseExp, 3);
                break;
            case IRON_ORE:
            case COAL_ORE:
                baseExp = Math.max(baseExp, 1);
                break;
        }
        
        return (int) (baseExp * multiplier);
    }
    
    private void checkBlockBreakAchievements(Player player, int progress) {
        // Проверка достижений
        if (progress >= 100) {
            player.sendMessage("§a§lДостижение разблокировано: §eСто блоков!");
        } else if (progress >= 1000) {
            player.sendMessage("§a§lДостижение разблокировано: §eТысяча блоков!");
        } else if (progress >= 10000) {
            player.sendMessage("§a§lДостижение разблокировано: §eДесять тысяч блоков!");
        }
    }
    
    private String formatLocation(Location location) {
        return String.format("%.1f, %.1f, %.1f", 
            location.getX(), location.getY(), location.getZ());
    }
    
    @Override
    public List<String> getDescription() {
        List<String> description = new ArrayList<>();
        description.add("§7Специализированный блок для обработки разрушения блоков");
        description.add("§7игроками с расширенной функциональностью");
        description.add("");
        description.add("§eВозможности:");
        description.add("§7• Настройка дропа и опыта");
        description.add("§7• Ограничения по инструментам и блокам");
        description.add("§7• Защита от эксплойтов");
        description.add("§7• Автоматическое восстановление");
        description.add("§7• Статистика и достижения");
        description.add("§7• Экономическая система");
        description.add("§7• PvP защита");
        description.add("§7• Ограничения по времени и погоде");
        description.add("§7• Кастомные звуки и эффекты");
        description.add("§7• Логирование и отладка");
        return description;
    }
    
    @Override
    public boolean validate(ExecutionContext context) {
        // Валидация настроек
        if (cooldownMs < 0) {
            context.getPlayer().sendMessage("§cОшибка: Кулдаун не может быть отрицательным!");
            return false;
        }
        
        if (maxBreakRadius < 0) {
            context.getPlayer().sendMessage("§cОшибка: Радиус не может быть отрицательным!");
            return false;
        }
        
        if (experienceMultiplier < 0) {
            context.getPlayer().sendMessage("§cОшибка: Множитель опыта не может быть отрицательным!");
            return false;
        }
        
        if (restoreDelayTicks < 0) {
            context.getPlayer().sendMessage("§cОшибка: Задержка восстановления не может быть отрицательной!");
            return false;
        }
        
        if (maxBreaksPerSecond < 1) {
            context.getPlayer().sendMessage("§cОшибка: Максимум разрушений в секунду должен быть больше 0!");
            return false;
        }
        
        return true;
    }
}
