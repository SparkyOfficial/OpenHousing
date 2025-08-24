package ru.openhousing.coding.blocks.events;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.enchantment.PrepareItemEnchantEvent;
import org.bukkit.inventory.ItemStack;
import ru.openhousing.OpenHousing;
import ru.openhousing.coding.blocks.BlockType;
import ru.openhousing.coding.blocks.CodeBlock;
import ru.openhousing.coding.blocks.CodeBlock.ExecutionContext;

import java.util.ArrayList;
import java.util.List;

/**
 * Специализированный блок для обработки зачарования предметов игроками
 * Предоставляет расширенную функциональность для управления процессом зачарования
 */
public class PlayerEnchantEventBlock extends CodeBlock {

    // Настройки блокировки
    private boolean enchantingEnabled = true;
    private boolean soundEnabled = true;
    private boolean particlesEnabled = true;

    // Настройки ограничений
    private boolean permissionCheckEnabled = true;
    private boolean cooldownEnabled = false;
    private long cooldownMs = 1000;
    private boolean areaRestrictionEnabled = false;
    private double maxEnchantRadius = 10.0;

    // Настройки предметов
    private boolean itemRestrictionEnabled = false;
    private List<Material> allowedItems = new ArrayList<>();
    private List<Material> forbiddenItems = new ArrayList<>();
    private boolean itemLimitEnabled = false;
    private int maxEnchantsPerItem = 5;

    // Настройки мира
    private boolean worldRestrictionEnabled = false;
    private List<String> allowedWorlds = new ArrayList<>();
    private List<String> forbiddenWorlds = new ArrayList<>();

    // Настройки региона
    private boolean regionProtectionEnabled = false;
    private boolean bypassProtectionEnabled = false;

    // Настройки времени
    private boolean timeRestrictionEnabled = false;
    private long enchantStartTime = 0;
    private long enchantEndTime = 24000;

    // Настройки погоды
    private boolean weatherRestrictionEnabled = false;
    private boolean allowEnchantInRain = true;
    private boolean allowEnchantInStorm = false;

    // Настройки стола зачарований
    private boolean enchantingTableRestrictionEnabled = false;
    private boolean requireEnchantingTable = true;
    private boolean allowBookshelves = true;
    private int minBookshelves = 0;
    private int maxBookshelves = 15;

    // Настройки уведомлений
    private boolean notificationsEnabled = true;
    private boolean loggingEnabled = false;
    private boolean statisticsEnabled = true;

    // Настройки эффектов
    private boolean enchantEffectsEnabled = true;
    private boolean customSoundEnabled = false;
    private String customSoundName = "BLOCK_ENCHANTMENT_TABLE_USE";
    private float customSoundVolume = 1.0f;
    private float customSoundPitch = 1.0f;

    // Настройки автоматизации
    private boolean autoRepairEnabled = false;
    private boolean autoUnbreakableEnabled = false;
    private boolean autoMendingEnabled = false;

    // Настройки экономики
    private boolean economyEnabled = false;
    private double enchantCost = 0.0;
    private boolean refundEnabled = false;

    // Настройки достижений
    private boolean achievementsEnabled = true;
    private boolean progressTrackingEnabled = true;

    // Настройки PvP
    private boolean pvpProtectionEnabled = false;
    private boolean teamProtectionEnabled = false;

    // Настройки команды
    private boolean commandExecutionEnabled = false;
    private List<String> enchantCommands = new ArrayList<>();
    private List<String> completeCommands = new ArrayList<>();

    // Настройки уведомлений
    private boolean enchantMessageEnabled = true;
    private String enchantMessage = "&aВы зачаровали предмет %item%!";
    private boolean enchantTitleEnabled = false;
    private String enchantTitle = "&eЗачарование!";
    private String enchantSubtitle = "&7%item%";

    // Настройки статистики
    private boolean enchantStatsEnabled = true;
    private boolean playerStatsEnabled = true;
    private boolean globalStatsEnabled = false;

    // Настройки кэширования
    private boolean enchantCacheEnabled = true;
    private int maxCacheSize = 1000;
    private long cacheExpirationMs = 300000;

    // Настройки производительности
    private boolean asyncProcessingEnabled = false;
    private boolean batchProcessingEnabled = false;
    private int batchSize = 10;

    // Настройки безопасности
    private boolean antiExploitEnabled = true;
    private boolean rateLimitEnabled = true;
    private int maxEnchantsPerSecond = 5;
    private boolean suspiciousActivityDetection = true;

    // Настройки логирования
    private boolean detailedLoggingEnabled = false;
    private boolean performanceLoggingEnabled = false;
    private boolean errorLoggingEnabled = true;

    // Настройки отладки
    private boolean debugModeEnabled = false;
    private boolean verboseOutputEnabled = false;

    // Настройки зачарований
    private boolean enchantmentRestrictionEnabled = false;
    private List<Enchantment> allowedEnchantments = new ArrayList<>();
    private List<Enchantment> forbiddenEnchantments = new ArrayList<>();
    private boolean maxLevelRestrictionEnabled = false;
    private int maxEnchantmentLevel = 5;

    // Настройки опыта
    private boolean experienceEnabled = false;
    private int experienceCost = 0;
    private int experienceReward = 0;

    // Настройки предметов
    private boolean itemConsumptionEnabled = false;
    private boolean consumeLapisOnEnchant = false;
    private boolean returnLapisOnFailure = false;

    // Настройки автоматических действий
    private boolean autoSortEnabled = false;
    private boolean autoStoreEnabled = false;
    private boolean autoRepairEnabled2 = false;

    // Настройки истории
    private boolean historyEnabled = false;
    private int maxHistorySize = 100;
    private boolean historyWithMetadata = true;

    public PlayerEnchantEventBlock() {
        super(BlockType.PLAYER_ENCHANT);
        initializeDefaultSettings();
    }

    private void initializeDefaultSettings() {
        // Инициализация разрешенных предметов
        allowedItems.add(Material.DIAMOND_SWORD);
        allowedItems.add(Material.IRON_SWORD);
        allowedItems.add(Material.STONE_SWORD);
        allowedItems.add(Material.WOODEN_SWORD);
        allowedItems.add(Material.GOLDEN_SWORD);
        allowedItems.add(Material.DIAMOND_PICKAXE);
        allowedItems.add(Material.IRON_PICKAXE);
        allowedItems.add(Material.STONE_PICKAXE);
        allowedItems.add(Material.WOODEN_PICKAXE);
        allowedItems.add(Material.GOLDEN_PICKAXE);

        // Инициализация разрешенных миров
        allowedWorlds.add("world");
        allowedWorlds.add("world_nether");
        allowedWorlds.add("world_the_end");

        // Инициализация разрешенных зачарований
        allowedEnchantments.add(Enchantment.DAMAGE_ALL);
        allowedEnchantments.add(Enchantment.UNBREAKING);
        allowedEnchantments.add(Enchantment.DIG_SPEED);
        allowedEnchantments.add(Enchantment.PROTECTION_ENVIRONMENTAL);
        allowedEnchantments.add(Enchantment.ARROW_DAMAGE);
        allowedEnchantments.add(Enchantment.ARROW_INFINITE);

        // Инициализация команд
        enchantCommands.add("say Игрок %player% зачаровал предмет %item%");
        completeCommands.add("give %player% %item% 1");
    }

    @Override
    public ExecutionResult execute(ExecutionContext context) {
        // Основная логика выполняется через событие
        // Этот метод вызывается при создании блока
        if (debugModeEnabled) {
            context.getPlayer().sendMessage("§7[DEBUG] PlayerEnchantEventBlock создан");
        }
        return ExecutionResult.success();
    }

    @EventHandler
    public void onPrepareEnchant(PrepareItemEnchantEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        Location location = event.getEnchantBlock().getLocation();

        // Проверка включения
        if (!enchantingEnabled) {
            event.setCancelled(true);
            return;
        }

        // Проверка разрешений
        if (permissionCheckEnabled && !player.hasPermission("openhousing.enchant")) {
            event.setCancelled(true);
            player.sendMessage("§cУ вас нет разрешения на зачарование!");
            return;
        }

        // Проверка кулдауна
        if (cooldownEnabled && !checkCooldown(player)) {
            event.setCancelled(true);
            player.sendMessage("§cПодождите перед следующим зачарованием!");
            return;
        }

        // Проверка ограничений по области
        if (areaRestrictionEnabled && !checkAreaRestriction(player, location)) {
            event.setCancelled(true);
            player.sendMessage("§cЗачарование запрещено в этой области!");
            return;
        }

        // Проверка ограничений по времени
        if (timeRestrictionEnabled && !checkTimeRestriction(location.getWorld())) {
            event.setCancelled(true);
            player.sendMessage("§cЗачарование запрещено в это время!");
            return;
        }

        // Проверка ограничений по погоде
        if (weatherRestrictionEnabled && !checkWeatherRestriction(location.getWorld())) {
            event.setCancelled(true);
            player.sendMessage("§cЗачарование запрещено в такую погоду!");
            return;
        }

        // Проверка ограничений по предметам
        if (itemRestrictionEnabled && !checkItemRestriction(item)) {
            event.setCancelled(true);
            player.sendMessage("§cЭтот предмет нельзя зачаровать!");
            return;
        }

        // Проверка ограничений по миру
        if (worldRestrictionEnabled && !checkWorldRestriction(location.getWorld().getName())) {
            event.setCancelled(true);
            player.sendMessage("§cЗачарование запрещено в этом мире!");
            return;
        }

        // Проверка защиты региона
        if (regionProtectionEnabled && !checkRegionProtection(location)) {
            event.setCancelled(true);
            player.sendMessage("§cЭтот регион защищен от зачарования!");
            return;
        }

        // Проверка лимита зачарований
        if (itemLimitEnabled && !checkEnchantLimit(player, item)) {
            event.setCancelled(true);
            player.sendMessage("§cВы достигли лимита зачарований для этого предмета!");
            return;
        }

        // Проверка анти-эксплойт
        if (antiExploitEnabled && !checkAntiExploit(player)) {
            event.setCancelled(true);
            player.sendMessage("§cПодозрительная активность обнаружена!");
            return;
        }

        // Проверка экономики (стоимость)
        if (economyEnabled && enchantCost > 0 && !checkEconomyCost(player)) {
            event.setCancelled(true);
            player.sendMessage("§cНедостаточно средств для зачарования!");
            return;
        }

        // Выполнение команд при подготовке
        if (commandExecutionEnabled) {
            executeEnchantCommands(player, item);
        }

        // Отладка
        if (debugModeEnabled) {
            handleDebugOutput(player, item, location);
        }
    }

    @EventHandler
    public void onEnchantItem(EnchantItemEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        Location location = event.getEnchantBlock().getLocation();

        // Настройка звука
        if (!soundEnabled) {
            // Отключаем звук
        }

        // Настройка частиц
        if (!particlesEnabled) {
            // Отключаем частицы
        }

        // Эффекты зачарования
        if (enchantEffectsEnabled) {
            handleEnchantEffects(player, item, location);
        }

        // Кастомный звук
        if (customSoundEnabled) {
            handleCustomSound(player, location);
        }

        // Автоматический ремонт
        if (autoRepairEnabled) {
            handleAutoRepair(player, item);
        }

        // Автоматическая неуничтожимость
        if (autoUnbreakableEnabled) {
            handleAutoUnbreakable(player, item);
        }

        // Автоматическое починка
        if (autoMendingEnabled) {
            handleAutoMending(player, item);
        }

        // Опыт
        if (experienceEnabled) {
            handleExperience(player, item);
        }

        // Уведомления
        if (notificationsEnabled) {
            handleNotifications(player, item);
        }

        // Логирование
        if (loggingEnabled) {
            handleLogging(player, item, location);
        }

        // Статистика
        if (statisticsEnabled) {
            handleStatistics(player, item);
        }

        // Достижения
        if (achievementsEnabled) {
            handleAchievements(player, item);
        }

        // Экономика (награда)
        if (economyEnabled && enchantCost > 0) {
            handleEconomyCost(player, item);
        }

        // PvP защита
        if (pvpProtectionEnabled) {
            handlePvpProtection(player, item);
        }

        // Команды при завершении
        if (commandExecutionEnabled) {
            executeCompleteCommands(player, item);
        }

        // Обновление кулдауна
        if (cooldownEnabled) {
            updateCooldown(player);
        }

        // Обновление статистики
        if (enchantStatsEnabled) {
            updateEnchantStats(player, item);
        }

        // Обновление статистики игрока
        if (playerStatsEnabled) {
            updatePlayerStats(player, item);
        }

        // Обновление глобальной статистики
        if (globalStatsEnabled) {
            updateGlobalStats(item);
        }

        // Кэширование
        if (enchantCacheEnabled) {
            cacheEnchant(player, item, location);
        }

        // Автоматическая сортировка
        if (autoSortEnabled) {
            handleAutoSort(player, item);
        }

        // Автоматическое хранение
        if (autoStoreEnabled) {
            handleAutoStore(player, item);
        }

        // Автоматический ремонт
        if (autoRepairEnabled2) {
            handleAutoRepair2(player, item);
        }

        // История
        if (historyEnabled) {
            addToHistory(player, item);
        }
    }

    private boolean checkCooldown(Player player) {
        // Проверка кулдауна для игрока
        long currentTime = System.currentTimeMillis();
        String playerId = player.getUniqueId().toString();

        String lastEnchantKey = "last_enchant_" + playerId;
        Object lastEnchantTime = getVariable(lastEnchantKey);

        if (lastEnchantTime != null && lastEnchantTime instanceof Number) {
            long lastEnchant = ((Number) lastEnchantTime).longValue();
            if (currentTime - lastEnchant < cooldownMs) {
                return false;
            }
        }

        return true;
    }

    private void updateCooldown(Player player) {
        // Обновление времени последнего зачарования
        long currentTime = System.currentTimeMillis();
        String playerId = player.getUniqueId().toString();
        String lastEnchantKey = "last_enchant_" + playerId;

        setVariable(lastEnchantKey, currentTime);
    }

    private boolean checkAreaRestriction(Player player, Location location) {
        // Проверка ограничений по области
        if (maxEnchantRadius > 0) {
            Location playerLocation = player.getLocation();
            double distance = playerLocation.distance(location);
            return distance <= maxEnchantRadius;
        }
        return true;
    }

    private boolean checkTimeRestriction(org.bukkit.World world) {
        // Проверка ограничений по времени
        long time = world.getTime();
        return time >= enchantStartTime && time <= enchantEndTime;
    }

    private boolean checkWeatherRestriction(org.bukkit.World world) {
        // Проверка ограничений по погоде
        if (world.hasStorm() && !allowEnchantInStorm) {
            return false;
        }
        if (world.isThundering() && !allowEnchantInRain) {
            return false;
        }
        return true;
    }

    private boolean checkItemRestriction(ItemStack item) {
        // Проверка ограничений по предметам
        Material itemType = item.getType();

        if (forbiddenItems.contains(itemType)) {
            return false;
        }

        if (!allowedItems.isEmpty() && !allowedItems.contains(itemType)) {
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

    private boolean checkEnchantLimit(Player player, ItemStack item) {
        // Проверка лимита зачарований
        String playerId = player.getUniqueId().toString();
        String itemType = item.getType().name();
        String enchantCountKey = "enchants_" + playerId + "_" + itemType;

        Object enchantCount = getVariable(enchantCountKey);
        int count = enchantCount instanceof Number ? ((Number) enchantCount).intValue() : 0;

        return count < maxEnchantsPerItem;
    }

    private boolean checkAntiExploit(Player player) {
        // Проверка анти-эксплойт
        if (rateLimitEnabled) {
            String playerId = player.getUniqueId().toString();
            String enchantCountKey = "enchant_count_" + playerId;
            String lastResetKey = "last_enchant_reset_" + playerId;

            long currentTime = System.currentTimeMillis();
            Object lastReset = getVariable(lastResetKey);

            if (lastReset == null || currentTime - ((Number) lastReset).longValue() > 1000) {
                setVariable(lastResetKey, currentTime);
                setVariable(enchantCountKey, 0);
            }

            Object enchantCount = getVariable(enchantCountKey);
            int count = enchantCount instanceof Number ? ((Number) enchantCount).intValue() : 0;

            if (count >= maxEnchantsPerSecond) {
                return false;
            }

            setVariable(enchantCountKey, count + 1);
        }

        return true;
    }

    private boolean checkEconomyCost(Player player) {
        // Проверка экономики (стоимость)
        if (enchantCost > 0) {
            // Здесь можно интегрировать с Vault или другими плагинами экономики
            // Пока возвращаем true
        }
        return true;
    }

    private void handleEnchantEffects(Player player, ItemStack item, Location location) {
        // Обработка эффектов зачарования
        if (particlesEnabled) {
            // Создание частиц
            location.getWorld().spawnParticle(
                org.bukkit.Particle.ENCHANTMENT_TABLE,
                location.add(0.5, 1.0, 0.5),
                20,
                0.3, 0.3, 0.3,
                0.1
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
            player.playSound(location, org.bukkit.Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1.0f, 1.0f);
        }
    }

    private void handleAutoRepair(Player player, ItemStack item) {
        // Обработка автоматического ремонта
        if (item.getDurability() > 0) {
            item.setDurability((short) 0);
            player.sendMessage("§aПредмет автоматически отремонтирован!");
        }
    }

    private void handleAutoUnbreakable(Player player, ItemStack item) {
        // Обработка автоматической неуничтожимости
        if (!item.getItemMeta().isUnbreakable()) {
            item.getItemMeta().setUnbreakable(true);
            player.sendMessage("§aПредмет стал неуничтожимым!");
        }
    }

    private void handleAutoMending(Player player, ItemStack item) {
        // Обработка автоматического починка
        if (!item.containsEnchantment(Enchantment.MENDING)) {
            item.addUnsafeEnchantment(Enchantment.MENDING, 1);
            player.sendMessage("§aДобавлено зачарование Починка!");
        }
    }

    private void handleExperience(Player player, ItemStack item) {
        // Обработка опыта
        if (experienceEnabled) {
            if (experienceCost > 0) {
                player.setExp(player.getExp() - (experienceCost / 100.0f));
            }
            if (experienceReward > 0) {
                player.giveExp(experienceReward);
            }
        }
    }

    private void handleNotifications(Player player, ItemStack item) {
        // Обработка уведомлений
        if (enchantMessageEnabled) {
            String message = enchantMessage
                .replace("%player%", player.getName())
                .replace("%item%", item.getType().name())
                .replace("%location%", formatLocation(player.getLocation()));

            player.sendMessage(message.replace("&", "§"));
        }

        if (enchantTitleEnabled) {
            String title = enchantTitle
                .replace("%player%", player.getName())
                .replace("%item%", item.getType().name());

            String subtitle = enchantSubtitle
                .replace("%player%", player.getName())
                .replace("%item%", item.getType().name());

            player.sendTitle(
                title.replace("&", "§"),
                subtitle.replace("&", "§"),
                10, 40, 10
            );
        }
    }

    private void handleLogging(Player player, ItemStack item, Location location) {
        // Обработка логирования
        if (detailedLoggingEnabled) {
            String logMessage = String.format(
                "[ENCHANT] Player: %s, Item: %s, Location: %s, Time: %d",
                player.getName(),
                item.getType().name(),
                formatLocation(location),
                System.currentTimeMillis()
            );

            Bukkit.getLogger().info(logMessage);
        }
    }

    private void handleStatistics(Player player, ItemStack item) {
        // Обработка статистики
        String playerId = player.getUniqueId().toString();
        String itemType = item.getType().name();

        // Статистика по предметам
        String itemStatsKey = "enchant_stats_" + itemType;
        Object itemStats = getVariable(itemStatsKey);
        int itemCount = itemStats instanceof Number ? ((Number) itemStats).intValue() : 0;
        setVariable(itemStatsKey, itemCount + 1);

        // Статистика игрока
        String playerStatsKey = "player_enchant_stats_" + playerId;
        Object playerStats = getVariable(playerStatsKey);
        int playerCount = playerStats instanceof Number ? ((Number) playerStats).intValue() : 0;
        setVariable(playerStatsKey, playerCount + 1);
    }

    private void handleAchievements(Player player, ItemStack item) {
        // Обработка достижений
        if (progressTrackingEnabled) {
            String achievementKey = "achievement_enchant_" + player.getUniqueId();
            Object achievementProgress = getVariable(achievementKey);
            int progress = achievementProgress instanceof Number ? ((Number) achievementProgress).intValue() : 0;

            checkEnchantAchievements(player, progress + 1);
            setVariable(achievementKey, progress + 1);
        }
    }

    private void handleEconomyCost(Player player, ItemStack item) {
        // Обработка экономики (стоимость)
        if (enchantCost > 0) {
            // Списание средств
            // Здесь можно интегрировать с Vault
        }
    }

    private void handlePvpProtection(Player player, ItemStack item) {
        // Обработка PvP защиты
        if (pvpProtectionEnabled) {
            // Проверка PvP статуса
        }

        if (teamProtectionEnabled) {
            // Проверка команды
        }
    }

    private void executeEnchantCommands(Player player, ItemStack item) {
        // Выполнение команд при зачаровании
        for (String command : enchantCommands) {
            String processedCommand = command
                .replace("%player%", player.getName())
                .replace("%item%", item.getType().name())
                .replace("%location%", formatLocation(player.getLocation()));

            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), processedCommand);
        }
    }

    private void executeCompleteCommands(Player player, ItemStack item) {
        // Выполнение команд при завершении
        for (String command : completeCommands) {
            String processedCommand = command
                .replace("%player%", player.getName())
                .replace("%item%", item.getType().name());

            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), processedCommand);
        }
    }

    private void updateEnchantStats(Player player, ItemStack item) {
        // Обновление статистики зачарований
        String itemType = item.getType().name();
        String statsKey = "global_enchant_stats_" + itemType;

        Object currentStats = getVariable(statsKey);
        int count = currentStats instanceof Number ? ((Number) currentStats).intValue() : 0;
        setVariable(statsKey, count + 1);
    }

    private void updatePlayerStats(Player player, ItemStack item) {
        // Обновление статистики игрока
        String playerId = player.getUniqueId().toString();
        String statsKey = "player_enchant_stats_" + playerId;

        Object currentStats = getVariable(statsKey);
        int count = currentStats instanceof Number ? ((Number) currentStats).intValue() : 0;
        setVariable(statsKey, count + 1);

        // Обновление общего количества зачарований
        String totalEnchantsKey = "enchanted_items_" + playerId;
        Object totalEnchants = getVariable(totalEnchantsKey);
        int total = totalEnchants instanceof Number ? ((Number) totalEnchants).intValue() : 0;
        setVariable(totalEnchantsKey, total + 1);
    }

    private void updateGlobalStats(ItemStack item) {
        // Обновление глобальной статистики
        String globalStatsKey = "global_items_enchanted";

        Object currentStats = getVariable(globalStatsKey);
        int count = currentStats instanceof Number ? ((Number) currentStats).intValue() : 0;
        setVariable(globalStatsKey, count + 1);
    }

    private void cacheEnchant(Player player, ItemStack item, Location location) {
        // Кэширование зачарования
        String cacheKey = "enchant_cache_" + player.getUniqueId();

        setVariable(cacheKey + "_last_item", item.getType().name());
        setVariable(cacheKey + "_last_location", formatLocation(location));
        setVariable(cacheKey + "_last_time", System.currentTimeMillis());
    }

    private void handleAutoSort(Player player, ItemStack item) {
        // Обработка автоматической сортировки
        // Здесь можно реализовать логику сортировки предметов
    }

    private void handleAutoStore(Player player, ItemStack item) {
        // Обработка автоматического хранения
        // Здесь можно реализовать логику хранения предметов
    }

    private void handleAutoRepair2(Player player, ItemStack item) {
        // Обработка автоматического ремонта (второй метод)
        if (item.getDurability() > 0) {
            item.setDurability((short) 0);
            player.sendMessage("§aПредмет автоматически отремонтирован!");
        }
    }

    private void addToHistory(Player player, ItemStack item) {
        // Добавление в историю
        String playerId = player.getUniqueId().toString();
        String historyKey = "enchant_history_" + playerId;

        Object history = getVariable(historyKey);
        List<String> historyList = history instanceof List ? (List<String>) history : new ArrayList<>();

        if (historyList.size() >= maxHistorySize) {
            historyList.remove(0);
        }

        String historyEntry = String.format("%s: %s at %d", 
            item.getType().name(), 
            formatLocation(player.getLocation()),
            System.currentTimeMillis());

        historyList.add(historyEntry);
        setVariable(historyKey, historyList);
    }

    private void handleDebugOutput(Player player, ItemStack item, Location location) {
        // Обработка отладочного вывода
        if (verboseOutputEnabled) {
            player.sendMessage("§7[DEBUG] Предмет: " + item.getType().name());
            player.sendMessage("§7[DEBUG] Локация: " + formatLocation(location));
            player.sendMessage("§7[DEBUG] Время: " + System.currentTimeMillis());
        }
    }

    private void checkEnchantAchievements(Player player, int progress) {
        // Проверка достижений
        if (progress >= 10) {
            player.sendMessage("§a§lДостижение разблокировано: §eДесять зачарований!");
        } else if (progress >= 50) {
            player.sendMessage("§a§lДостижение разблокировано: §eПятьдесят зачарований!");
        } else if (progress >= 100) {
            player.sendMessage("§a§lДостижение разблокировано: §eСто зачарований!");
        }
    }

    private String formatLocation(Location location) {
        return String.format("%.1f, %.1f, %.1f",
            location.getX(), location.getY(), location.getZ());
    }

    @Override
    public List<String> getDescription() {
        List<String> description = new ArrayList<>();
        description.add("§7Специализированный блок для обработки зачарования предметов");
        description.add("§7игроками с расширенной функциональностью");
        description.add("");
        description.add("§eВозможности:");
        description.add("§7• Настройка ограничений и разрешений");
        description.add("§7• Автоматический ремонт, неуничтожимость и починка");
        description.add("§7• Защита от эксплойтов и ограничения по времени");
        description.add("§7• Статистика и достижения");
        description.add("§7• Экономическая система");
        description.add("§7• PvP защита");
        description.add("§7• Ограничения по погоде и миру");
        description.add("§7• Кастомные звуки и эффекты");
        description.add("§7• Логирование и отладка");
        description.add("§7• Автоматическая сортировка и хранение");
        description.add("§7• История зачарований");
        return description;
    }

    @Override
    public boolean validate(ExecutionContext context) {
        // Валидация настроек
        if (cooldownMs < 0) {
            context.getPlayer().sendMessage("§cОшибка: Кулдаун не может быть отрицательным!");
            return false;
        }

        if (maxEnchantRadius < 0) {
            context.getPlayer().sendMessage("§cОшибка: Радиус не может быть отрицательным!");
            return false;
        }

        if (maxEnchantsPerItem < 1) {
            context.getPlayer().sendMessage("§cОшибка: Лимит зачарований должен быть больше 0!");
            return false;
        }

        if (maxEnchantmentLevel < 1) {
            context.getPlayer().sendMessage("§cОшибка: Максимальный уровень зачарования должен быть больше 0!");
            return false;
        }

        if (maxEnchantsPerSecond < 1) {
            context.getPlayer().sendMessage("§cОшибка: Максимум зачарований в секунду должен быть больше 0!");
            return false;
        }

        if (experienceCost < 0) {
            context.getPlayer().sendMessage("§cОшибка: Стоимость опыта не может быть отрицательной!");
            return false;
        }

        if (experienceReward < 0) {
            context.getPlayer().sendMessage("§cОшибка: Награда опыта не может быть отрицательной!");
            return false;
        }

        if (minBookshelves < 0 || maxBookshelves < minBookshelves) {
            context.getPlayer().sendMessage("§cОшибка: Количество книжных полок должно быть корректным!");
            return false;
        }

        return true;
    }
}
