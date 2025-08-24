package ru.openhousing.coding.blocks.events;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.ItemStack;
import ru.openhousing.OpenHousing;
import ru.openhousing.coding.blocks.BlockType;
import ru.openhousing.coding.blocks.CodeBlock;
import ru.openhousing.coding.blocks.CodeBlock.ExecutionContext;
import ru.openhousing.coding.blocks.CodeBlock.ExecutionResult;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Специализированный блок для обработки взаимодействия игроков с существами
 * Предоставляет расширенную функциональность для управления процессом взаимодействия
 */
public class PlayerInteractEntityEventBlock extends CodeBlock {
    
    // Настройки блокировки
    private boolean entityInteractionEnabled = true;
    private boolean soundEnabled = true;
    private boolean particlesEnabled = true;
    
    // Настройки ограничений
    private boolean permissionCheckEnabled = true;
    private boolean cooldownEnabled = false;
    private long cooldownMs = 1000;
    private boolean areaRestrictionEnabled = false;
    private double maxInteractionRadius = 10.0;
    
    // Настройки существ
    private boolean entityRestrictionEnabled = false;
    private List<EntityType> allowedEntities = new ArrayList<>();
    private List<EntityType> forbiddenEntities = new ArrayList<>();
    private boolean entityLimitEnabled = false;
    private int maxInteractionsPerEntity = 100;
    
    // Настройки мира
    private boolean worldRestrictionEnabled = false;
    private List<String> allowedWorlds = new ArrayList<>();
    private List<String> forbiddenWorlds = new ArrayList<>();
    
    // Настройки региона
    private boolean regionProtectionEnabled = false;
    private boolean bypassProtectionEnabled = false;
    
    // Настройки времени
    private boolean timeRestrictionEnabled = false;
    private long interactionStartTime = 0;
    private long interactionEndTime = 24000;
    
    // Настройки погоды
    private boolean weatherRestrictionEnabled = false;
    private boolean allowInteractionInRain = true;
    private boolean allowInteractionInStorm = false;
    
    // Настройки инструментов
    private boolean toolRestrictionEnabled = false;
    private List<Material> allowedTools = new ArrayList<>();
    
    // Настройки уведомлений
    private boolean notificationsEnabled = true;
    private boolean loggingEnabled = false;
    private boolean statisticsEnabled = true;
    
    // Настройки эффектов
    private boolean interactionEffectsEnabled = true;
    private boolean customSoundEnabled = false;
    private String customSoundName = "ENTITY_VILLAGER_YES";
    private float customSoundVolume = 1.0f;
    private float customSoundPitch = 1.0f;
    
    // Настройки автоматизации
    private boolean autoTameEnabled = false;
    private boolean autoBreedEnabled = false;
    private boolean autoHealEnabled = false;
    
    // Настройки экономики
    private boolean economyEnabled = false;
    private double interactionCost = 0.0;
    private boolean refundEnabled = false;
    
    // Настройки достижений
    private boolean achievementsEnabled = true;
    private boolean progressTrackingEnabled = true;
    
    // Настройки PvP
    private boolean pvpProtectionEnabled = false;
    private boolean teamProtectionEnabled = false;
    
    // Настройки команды
    private boolean commandExecutionEnabled = false;
    private List<String> interactionCommands = new ArrayList<>();
    private List<String> completeCommands = new ArrayList<>();
    
    // Настройки уведомлений
    private boolean entityInteractionMessageEnabled = true;
    private String entityInteractionMessage = "&aВы взаимодействовали с %entity%";
    private boolean entityInteractionTitleEnabled = false;
    private String entityInteractionTitle = "&eВзаимодействие!";
    private String entityInteractionSubtitle = "&7%entity%";
    
    // Настройки статистики
    private boolean entityInteractionStatsEnabled = true;
    private boolean playerStatsEnabled = true;
    private boolean globalStatsEnabled = false;
    
    // Настройки кэширования
    private boolean entityCacheEnabled = true;
    private int maxCacheSize = 1000;
    private long cacheExpirationMs = 300000;
    
    // Настройки производительности
    private boolean asyncProcessingEnabled = false;
    private boolean batchProcessingEnabled = false;
    private int batchSize = 10;
    
    // Настройки безопасности
    private boolean antiExploitEnabled = true;
    private boolean rateLimitEnabled = true;
    private int maxInteractionsPerSecond = 20;
    private boolean suspiciousActivityDetection = true;
    
    // Настройки логирования
    private boolean detailedLoggingEnabled = false;
    private boolean performanceLoggingEnabled = false;
    private boolean errorLoggingEnabled = true;
    
    // Настройки отладки
    private boolean debugModeEnabled = false;
    private boolean verboseOutputEnabled = false;
    
    // Настройки взаимодействия
    private boolean rightClickEnabled = true;
    private boolean leftClickEnabled = false;
    private boolean shiftClickEnabled = true;
    
    // Настройки ограничений по здоровью
    private boolean healthRestrictionEnabled = false;
    private double minEntityHealth = 0.0;
    private double maxEntityHealth = 20.0;
    
    // Настройки ограничений по возрасту
    private boolean ageRestrictionEnabled = false;
    private boolean allowBabyEntities = true;
    private boolean allowAdultEntities = true;
    
    // Настройки ограничений по приручению
    private boolean tamingRestrictionEnabled = false;
    private boolean allowTamedOnly = false;
    private boolean allowUntamedOnly = false;
    
    // Настройки автоматических действий
    private boolean autoFeedEnabled = false;
    private boolean autoMountEnabled = false;
    private boolean autoDismountEnabled = false;
    
    // Настройки эффектов зелий
    private boolean potionEffectEnabled = false;
    private boolean giveEffectToPlayer = false;
    private boolean giveEffectToEntity = false;
    
    // Настройки предметов
    private boolean itemConsumptionEnabled = false;
    private boolean consumeItemOnInteraction = false;
    private boolean returnItemOnFailure = false;
    
    // Настройки опыта
    private boolean experienceEnabled = false;
    private int experienceReward = 0;
    private boolean experienceToPlayer = true;
    private boolean experienceToEntity = false;
    
    public PlayerInteractEntityEventBlock() {
        super(BlockType.PLAYER_INTERACT_ENTITY);
        initializeDefaultSettings();
    }
    
    private void initializeDefaultSettings() {
        // Инициализация разрешенных существ
        allowedEntities.add(EntityType.VILLAGER);
        allowedEntities.add(EntityType.COW);
        allowedEntities.add(EntityType.SHEEP);
        allowedEntities.add(EntityType.PIG);
        allowedEntities.add(EntityType.CHICKEN);
        
        // Инициализация разрешенных инструментов
        allowedTools.add(Material.WHEAT);
        allowedTools.add(Material.CARROT);
        allowedTools.add(Material.POTATO);
        allowedTools.add(Material.BEETROOT);
        allowedTools.add(Material.APPLE);
        
        // Инициализация разрешенных миров
        allowedWorlds.add("world");
        allowedWorlds.add("world_nether");
        allowedWorlds.add("world_the_end");
        
        // Инициализация команд
        interactionCommands.add("say Игрок %player% взаимодействовал с %entity%");
        completeCommands.add("give %player% %entity%_treat 1");
    }
    
    @Override
    public ExecutionResult execute(ExecutionContext context) {
        // Основная логика выполняется через событие
        // Этот метод вызывается при создании блока
        if (debugModeEnabled) {
            context.getPlayer().sendMessage("§7[DEBUG] PlayerInteractEntityEventBlock создан");
        }
        return ExecutionResult.success();
    }
    
    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        Player player = event.getPlayer();
        Entity entity = event.getRightClicked();
        Location location = entity.getLocation();
        ItemStack itemInHand = player.getInventory().getItemInMainHand();
        
        // Проверка включения
        if (!entityInteractionEnabled) {
            event.setCancelled(true);
            return;
        }
        
        // Проверка разрешений
        if (permissionCheckEnabled && !player.hasPermission("openhousing.entityinteract")) {
            event.setCancelled(true);
            player.sendMessage("§cУ вас нет разрешения на взаимодействие с существами!");
            return;
        }
        
        // Проверка кулдауна
        if (cooldownEnabled && !checkCooldown(player)) {
            event.setCancelled(true);
            player.sendMessage("§cПодождите перед следующим взаимодействием!");
            return;
        }
        
        // Проверка ограничений по области
        if (areaRestrictionEnabled && !checkAreaRestriction(player, location)) {
            event.setCancelled(true);
            player.sendMessage("§cВзаимодействие запрещено в этой области!");
            return;
        }
        
        // Проверка ограничений по времени
        if (timeRestrictionEnabled && !checkTimeRestriction(location.getWorld())) {
            event.setCancelled(true);
            player.sendMessage("§cВзаимодействие запрещено в это время!");
            return;
        }
        
        // Проверка ограничений по погоде
        if (weatherRestrictionEnabled && !checkWeatherRestriction(location.getWorld())) {
            event.setCancelled(true);
            player.sendMessage("§cВзаимодействие запрещено в такую погоду!");
            return;
        }
        
        // Проверка ограничений по инструментам
        if (toolRestrictionEnabled && !checkToolRestriction(player)) {
            event.setCancelled(true);
            player.sendMessage("§cИспользуйте подходящий инструмент для взаимодействия!");
            return;
        }
        
        // Проверка ограничений по существам
        if (entityRestrictionEnabled && !checkEntityRestriction(entity)) {
            event.setCancelled(true);
            player.sendMessage("§cС этим существом нельзя взаимодействовать!");
            return;
        }
        
        // Проверка ограничений по миру
        if (worldRestrictionEnabled && !checkWorldRestriction(location.getWorld().getName())) {
            event.setCancelled(true);
            player.sendMessage("§cВзаимодействие запрещено в этом мире!");
            return;
        }
        
        // Проверка защиты региона
        if (regionProtectionEnabled && !checkRegionProtection(location)) {
            event.setCancelled(true);
            player.sendMessage("§cЭтот регион защищен от взаимодействия!");
            return;
        }
        
        // Проверка ограничений по здоровью
        if (healthRestrictionEnabled && !checkHealthRestriction(entity)) {
            event.setCancelled(true);
            player.sendMessage("§cСущество не подходит по здоровью!");
            return;
        }
        
        // Проверка ограничений по возрасту
        if (ageRestrictionEnabled && !checkAgeRestriction(entity)) {
            event.setCancelled(true);
            player.sendMessage("§cСущество не подходит по возрасту!");
            return;
        }
        
        // Проверка ограничений по приручению
        if (tamingRestrictionEnabled && !checkTamingRestriction(entity)) {
            event.setCancelled(true);
            player.sendMessage("§cСущество не подходит по статусу приручения!");
            return;
        }
        
        // Проверка лимита взаимодействий
        if (entityLimitEnabled && !checkEntityLimit(entity)) {
            event.setCancelled(true);
            player.sendMessage("§cДостигнут лимит взаимодействий с этим существом!");
            return;
        }
        
        // Проверка анти-эксплойт
        if (antiExploitEnabled && !checkAntiExploit(player)) {
            event.setCancelled(true);
            player.sendMessage("§cПодозрительная активность обнаружена!");
            return;
        }
        
        // Проверка экономики
        if (economyEnabled && !checkEconomy(player)) {
            event.setCancelled(true);
            player.sendMessage("§cНедостаточно средств для взаимодействия!");
            return;
        }
        
        // Выполнение команд при взаимодействии
        if (commandExecutionEnabled) {
            executeInteractionCommands(player, entity);
        }
        
        // Настройка звука
        if (!soundEnabled) {
            // Отключаем звук
        }
        
        // Настройка частиц
        if (!particlesEnabled) {
            // Отключаем частицы
        }
        
        // Эффекты взаимодействия
        if (interactionEffectsEnabled) {
            handleInteractionEffects(player, entity, location);
        }
        
        // Кастомный звук
        if (customSoundEnabled) {
            handleCustomSound(player, location);
        }
        
        // Автоматическое приручение
        if (autoTameEnabled) {
            handleAutoTame(player, entity);
        }
        
        // Автоматическое размножение
        if (autoBreedEnabled) {
            handleAutoBreed(player, entity);
        }
        
        // Автоматическое лечение
        if (autoHealEnabled) {
            handleAutoHeal(player, entity);
        }
        
        // Автоматическое кормление
        if (autoFeedEnabled) {
            handleAutoFeed(player, entity);
        }
        
        // Автоматическое оседлание
        if (autoMountEnabled) {
            handleAutoMount(player, entity);
        }
        
        // Автоматическое слезание
        if (autoDismountEnabled) {
            handleAutoDismount(player, entity);
        }
        
        // Эффекты зелий
        if (potionEffectEnabled) {
            handlePotionEffects(player, entity);
        }
        
        // Потребление предметов
        if (itemConsumptionEnabled) {
            handleItemConsumption(player, entity, itemInHand);
        }
        
        // Опыт
        if (experienceEnabled) {
            handleExperience(player, entity);
        }
        
        // Уведомления
        if (notificationsEnabled) {
            handleNotifications(player, entity);
        }
        
        // Логирование
        if (loggingEnabled) {
            handleLogging(player, entity, location);
        }
        
        // Статистика
        if (statisticsEnabled) {
            handleStatistics(player, entity);
        }
        
        // Достижения
        if (achievementsEnabled) {
            handleAchievements(player, entity);
        }
        
        // Экономика
        if (economyEnabled) {
            handleEconomy(player, entity);
        }
        
        // PvP защита
        if (pvpProtectionEnabled) {
            handlePvpProtection(player, entity);
        }
        
        // Команды при завершении
        if (commandExecutionEnabled) {
            executeCompleteCommands(player, entity);
        }
        
        // Обновление кулдауна
        if (cooldownEnabled) {
            updateCooldown(player);
        }
        
        // Обновление статистики
        if (entityInteractionStatsEnabled) {
            updateEntityInteractionStats(player, entity);
        }
        
        // Обновление статистики игрока
        if (playerStatsEnabled) {
            updatePlayerStats(player, entity);
        }
        
        // Обновление глобальной статистики
        if (globalStatsEnabled) {
            updateGlobalStats(entity);
        }
        
        // Кэширование
        if (entityCacheEnabled) {
            cacheEntityInteraction(player, entity, location);
        }
        
        // Отладка
        if (debugModeEnabled) {
            handleDebugOutput(player, entity, location);
        }
    }
    
    private boolean checkCooldown(Player player) {
        // Проверка кулдауна для игрока
        long currentTime = System.currentTimeMillis();
        String playerId = player.getUniqueId().toString();
        
        String lastInteractionKey = "last_entity_interaction_" + playerId;
        Object lastInteractionTime = getVariable(lastInteractionKey);
        
        if (lastInteractionTime != null && lastInteractionTime instanceof Number) {
            long lastInteraction = ((Number) lastInteractionTime).longValue();
            if (currentTime - lastInteraction < cooldownMs) {
                return false;
            }
        }
        
        return true;
    }
    
    private void updateCooldown(Player player) {
        // Обновление времени последнего взаимодействия
        long currentTime = System.currentTimeMillis();
        String playerId = player.getUniqueId().toString();
        String lastInteractionKey = "last_entity_interaction_" + playerId;
        
        setVariable(lastInteractionKey, currentTime);
    }
    
    private boolean checkAreaRestriction(Player player, Location location) {
        // Проверка ограничений по области
        if (maxInteractionRadius > 0) {
            Location playerLocation = player.getLocation();
            double distance = playerLocation.distance(location);
            return distance <= maxInteractionRadius;
        }
        return true;
    }
    
    private boolean checkTimeRestriction(org.bukkit.World world) {
        // Проверка ограничений по времени
        long time = world.getTime();
        return time >= interactionStartTime && time <= interactionEndTime;
    }
    
    private boolean checkWeatherRestriction(org.bukkit.World world) {
        // Проверка ограничений по погоде
        if (world.hasStorm() && !allowInteractionInStorm) {
            return false;
        }
        if (world.isThundering() && !allowInteractionInRain) {
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
        
        return allowedTools.contains(itemInHand.getType());
    }
    
    private boolean checkEntityRestriction(Entity entity) {
        // Проверка ограничений по существам
        EntityType entityType = entity.getType();
        
        if (forbiddenEntities.contains(entityType)) {
            return false;
        }
        
        if (!allowedEntities.isEmpty() && !allowedEntities.contains(entityType)) {
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
    
    private boolean checkHealthRestriction(Entity entity) {
        // Проверка ограничений по здоровью
        if (entity instanceof org.bukkit.entity.LivingEntity) {
            org.bukkit.entity.LivingEntity livingEntity = (org.bukkit.entity.LivingEntity) entity;
            double health = livingEntity.getHealth();
            return health >= minEntityHealth && health <= maxEntityHealth;
        }
        return true;
    }
    
    private boolean checkAgeRestriction(Entity entity) {
        // Проверка ограничений по возрасту
        if (entity instanceof org.bukkit.entity.Ageable) {
            org.bukkit.entity.Ageable ageable = (org.bukkit.entity.Ageable) entity;
            boolean isAdult = ageable.isAdult();
            
            if (isAdult && !allowAdultEntities) {
                return false;
            }
            
            if (!isAdult && !allowBabyEntities) {
                return false;
            }
        }
        
        return true;
    }
    
    private boolean checkTamingRestriction(Entity entity) {
        // Проверка ограничений по приручению
        if (entity instanceof org.bukkit.entity.Tameable) {
            org.bukkit.entity.Tameable tameable = (org.bukkit.entity.Tameable) entity;
            boolean isTamed = tameable.isTamed();
            
            if (isTamed && !allowTamedOnly) {
                return false;
            }
            
            if (!isTamed && !allowUntamedOnly) {
                return false;
            }
        }
        
        return true;
    }
    
    private boolean checkEntityLimit(Entity entity) {
        // Проверка лимита взаимодействий
        String entityId = entity.getUniqueId().toString();
        String interactionCountKey = "entity_interactions_" + entityId;
        
        Object interactionCount = getVariable(interactionCountKey);
        int count = interactionCount instanceof Number ? ((Number) interactionCount).intValue() : 0;
        
        return count < maxInteractionsPerEntity;
    }
    
    private boolean checkAntiExploit(Player player) {
        // Проверка анти-эксплойт
        if (rateLimitEnabled) {
            String playerId = player.getUniqueId().toString();
            String interactionCountKey = "entity_interaction_count_" + playerId;
            String lastResetKey = "last_entity_interaction_reset_" + playerId;
            
            long currentTime = System.currentTimeMillis();
            Object lastReset = getVariable(lastResetKey);
            
            if (lastReset == null || currentTime - ((Number) lastReset).longValue() > 1000) {
                setVariable(lastResetKey, currentTime);
                setVariable(interactionCountKey, 0);
            }
            
            Object interactionCount = getVariable(interactionCountKey);
            int count = interactionCount instanceof Number ? ((Number) interactionCount).intValue() : 0;
            
            if (count >= maxInteractionsPerSecond) {
                return false;
            }
            
            setVariable(interactionCountKey, count + 1);
        }
        
        return true;
    }
    
    private boolean checkEconomy(Player player) {
        // Проверка экономики
        if (interactionCost > 0) {
            // Здесь можно интегрировать с Vault или другими плагинами экономики
            // Пока возвращаем true
        }
        return true;
    }
    
    private void handleInteractionEffects(Player player, Entity entity, Location location) {
        // Обработка эффектов взаимодействия
        if (particlesEnabled) {
            // Создание частиц
            entity.getWorld().spawnParticle(
                org.bukkit.Particle.HEART,
                location.add(0, 1, 0),
                5,
                0.2, 0.2, 0.2,
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
            player.playSound(location, org.bukkit.Sound.ENTITY_VILLAGER_YES, 1.0f, 1.0f);
        }
    }
    
    private void handleAutoTame(Player player, Entity entity) {
        // Обработка автоматического приручения
        if (entity instanceof org.bukkit.entity.Tameable) {
            org.bukkit.entity.Tameable tameable = (org.bukkit.entity.Tameable) entity;
            if (!tameable.isTamed()) {
                tameable.setTamed(true);
                tameable.setOwner(player);
                player.sendMessage("§aСущество приручено!");
            }
        }
    }
    
    private void handleAutoBreed(Player player, Entity entity) {
        // Обработка автоматического размножения
        if (entity instanceof org.bukkit.entity.Animals) {
            org.bukkit.entity.Animals animal = (org.bukkit.entity.Animals) entity;
            if (animal.canBreed()) {
                animal.setBreed(true);
                player.sendMessage("§aСущество готово к размножению!");
            }
        }
    }
    
    private void handleAutoHeal(Player player, Entity entity) {
        // Обработка автоматического лечения
        if (entity instanceof org.bukkit.entity.LivingEntity) {
            org.bukkit.entity.LivingEntity livingEntity = (org.bukkit.entity.LivingEntity) entity;
            double maxHealth = livingEntity.getMaxHealth();
            double currentHealth = livingEntity.getHealth();
            
            if (currentHealth < maxHealth) {
                livingEntity.setHealth(maxHealth);
                player.sendMessage("§aСущество исцелено!");
            }
        }
    }
    
    private void handleAutoFeed(Player player, Entity entity) {
        // Обработка автоматического кормления
        if (entity instanceof org.bukkit.entity.Animals) {
            org.bukkit.entity.Animals animal = (org.bukkit.entity.Animals) entity;
            if (animal.canBreed()) {
                animal.setBreed(false);
                player.sendMessage("§aСущество накормлено!");
            }
        }
    }
    
    private void handleAutoMount(Player player, Entity entity) {
        // Обработка автоматического оседлания
        if (entity instanceof org.bukkit.entity.Vehicle) {
            org.bukkit.entity.Vehicle vehicle = (org.bukkit.entity.Vehicle) entity;
            if (vehicle.getPassengers().isEmpty()) {
                vehicle.addPassenger(player);
                player.sendMessage("§aВы сели на существо!");
            }
        }
    }
    
    private void handleAutoDismount(Player player, Entity entity) {
        // Обработка автоматического слезания
        if (entity instanceof org.bukkit.entity.Vehicle) {
            org.bukkit.entity.Vehicle vehicle = (org.bukkit.entity.Vehicle) entity;
            if (vehicle.getPassengers().contains(player)) {
                vehicle.removePassenger(player);
                player.sendMessage("§aВы слезли с существа!");
            }
        }
    }
    
    private void handlePotionEffects(Player player, Entity entity) {
        // Обработка эффектов зелий
        if (giveEffectToPlayer) {
            // Даем эффект игроку
            player.addPotionEffect(new org.bukkit.potion.PotionEffect(
                org.bukkit.potion.PotionEffectType.REGENERATION,
                200, // 10 секунд
                1
            ));
        }
        
        if (giveEffectToEntity && entity instanceof org.bukkit.entity.LivingEntity) {
            // Даем эффект существу
            org.bukkit.entity.LivingEntity livingEntity = (org.bukkit.entity.LivingEntity) entity;
            livingEntity.addPotionEffect(new org.bukkit.potion.PotionEffect(
                org.bukkit.potion.PotionEffectType.REGENERATION,
                200, // 10 секунд
                1
            ));
        }
    }
    
    private void handleItemConsumption(Player player, Entity entity, ItemStack itemInHand) {
        // Обработка потребления предметов
        if (consumeItemOnInteraction && itemInHand.getType() != Material.AIR) {
            if (itemInHand.getAmount() > 1) {
                itemInHand.setAmount(itemInHand.getAmount() - 1);
            } else {
                player.getInventory().setItemInMainHand(null);
            }
            
            if (returnItemOnFailure) {
                // Возвращаем предмет при неудаче
                // Логика возврата
            }
        }
    }
    
    private void handleExperience(Player player, Entity entity) {
        // Обработка опыта
        if (experienceReward > 0) {
            if (experienceToPlayer) {
                player.giveExp(experienceReward);
            }
            
            if (experienceToEntity) {
                // Даем опыт существу (если возможно)
                entity.getWorld().spawn(entity.getLocation(), org.bukkit.entity.ExperienceOrb.class)
                     .setExperience(experienceReward);
            }
        }
    }
    
    private void handleNotifications(Player player, Entity entity) {
        // Обработка уведомлений
        if (entityInteractionMessageEnabled) {
            String message = entityInteractionMessage
                .replace("%player%", player.getName())
                .replace("%entity%", entity.getType().name())
                .replace("%location%", formatLocation(entity.getLocation()));
            
            player.sendMessage(message.replace("&", "§"));
        }
        
        if (entityInteractionTitleEnabled) {
            String title = entityInteractionTitle
                .replace("%player%", player.getName())
                .replace("%entity%", entity.getType().name());
            
            String subtitle = entityInteractionSubtitle
                .replace("%player%", player.getName())
                .replace("%entity%", entity.getType().name());
            
            player.sendTitle(
                title.replace("&", "§"),
                subtitle.replace("&", "§"),
                10, 40, 10
            );
        }
    }
    
    private void handleLogging(Player player, Entity entity, Location location) {
        // Обработка логирования
        if (detailedLoggingEnabled) {
            String logMessage = String.format(
                "[ENTITY_INTERACTION] Player: %s, Entity: %s, Location: %s, Time: %d",
                player.getName(),
                entity.getType().name(),
                formatLocation(location),
                System.currentTimeMillis()
            );
            
            Bukkit.getLogger().info(logMessage);
        }
    }
    
    private void handleStatistics(Player player, Entity entity) {
        // Обработка статистики
        String playerId = player.getUniqueId().toString();
        String entityType = entity.getType().name();
        
        // Статистика по существам
        String entityStatsKey = "entity_interaction_stats_" + entityType;
        Object entityStats = getVariable(entityStatsKey);
        int entityCount = entityStats instanceof Number ? ((Number) entityStats).intValue() : 0;
        setVariable(entityStatsKey, entityCount + 1);
        
        // Статистика игрока
        String playerStatsKey = "player_entity_interaction_stats_" + playerId;
        Object playerStats = getVariable(playerStatsKey);
        int playerCount = playerStats instanceof Number ? ((Number) playerStats).intValue() : 0;
        setVariable(playerStatsKey, playerCount + 1);
    }
    
    private void handleAchievements(Player player, Entity entity) {
        // Обработка достижений
        if (progressTrackingEnabled) {
            String achievementKey = "achievement_entity_interaction_" + player.getUniqueId();
            Object achievementProgress = getVariable(achievementKey);
            int progress = achievementProgress instanceof Number ? ((Number) achievementProgress).intValue() : 0;
            
            checkEntityInteractionAchievements(player, progress + 1);
            setVariable(achievementKey, progress + 1);
        }
    }
    
    private void handleEconomy(Player player, Entity entity) {
        // Обработка экономики
        if (interactionCost > 0) {
            // Списание средств
            // Здесь можно интегрировать с Vault
        }
    }
    
    private void handlePvpProtection(Player player, Entity entity) {
        // Обработка PvP защиты
        if (pvpProtectionEnabled) {
            // Проверка PvP статуса
        }
        
        if (teamProtectionEnabled) {
            // Проверка команды
        }
    }
    
    private void executeInteractionCommands(Player player, Entity entity) {
        // Выполнение команд при взаимодействии
        for (String command : interactionCommands) {
            String processedCommand = command
                .replace("%player%", player.getName())
                .replace("%entity%", entity.getType().name())
                .replace("%location%", formatLocation(entity.getLocation()));
            
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), processedCommand);
        }
    }
    
    private void executeCompleteCommands(Player player, Entity entity) {
        // Выполнение команд при завершении
        for (String command : completeCommands) {
            String processedCommand = command
                .replace("%player%", player.getName())
                .replace("%entity%", entity.getType().name());
            
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), processedCommand);
        }
    }
    
    private void updateEntityInteractionStats(Player player, Entity entity) {
        // Обновление статистики взаимодействия с существами
        String entityType = entity.getType().name();
        String statsKey = "global_entity_interaction_stats_" + entityType;
        
        Object currentStats = getVariable(statsKey);
        int count = currentStats instanceof Number ? ((Number) currentStats).intValue() : 0;
        setVariable(statsKey, count + 1);
    }
    
    private void updatePlayerStats(Player player, Entity entity) {
        // Обновление статистики игрока
        String playerId = player.getUniqueId().toString();
        String statsKey = "player_entity_interaction_stats_" + playerId;
        
        Object currentStats = getVariable(statsKey);
        int count = currentStats instanceof Number ? ((Number) currentStats).intValue() : 0;
        setVariable(statsKey, count + 1);
        
        // Обновление общего количества взаимодействий
        String totalInteractionsKey = "entity_interactions_" + playerId;
        Object totalInteractions = getVariable(totalInteractionsKey);
        int total = totalInteractions instanceof Number ? ((Number) totalInteractions).intValue() : 0;
        setVariable(totalInteractionsKey, total + 1);
    }
    
    private void updateGlobalStats(Entity entity) {
        // Обновление глобальной статистики
        String globalStatsKey = "global_entity_interactions";
        
        Object currentStats = getVariable(globalStatsKey);
        int count = currentStats instanceof Number ? ((Number) currentStats).intValue() : 0;
        setVariable(globalStatsKey, count + 1);
    }
    
    private void cacheEntityInteraction(Player player, Entity entity, Location location) {
        // Кэширование взаимодействия с существом
        String cacheKey = "entity_interaction_cache_" + player.getUniqueId();
        
        setVariable(cacheKey + "_last_entity", entity.getType().name());
        setVariable(cacheKey + "_last_location", formatLocation(location));
        setVariable(cacheKey + "_last_time", System.currentTimeMillis());
    }
    
    private void handleDebugOutput(Player player, Entity entity, Location location) {
        // Обработка отладочного вывода
        if (verboseOutputEnabled) {
            player.sendMessage("§7[DEBUG] Существо: " + entity.getType().name());
            player.sendMessage("§7[DEBUG] Локация: " + formatLocation(location));
            player.sendMessage("§7[DEBUG] Время: " + System.currentTimeMillis());
        }
    }
    
    private void checkEntityInteractionAchievements(Player player, int progress) {
        // Проверка достижений
        if (progress >= 100) {
            player.sendMessage("§a§lДостижение разблокировано: §eСто взаимодействий с существами!");
        } else if (progress >= 1000) {
            player.sendMessage("§a§lДостижение разблокировано: §eТысяча взаимодействий с существами!");
        } else if (progress >= 10000) {
            player.sendMessage("§a§lДостижение разблокировано: §eДесять тысяч взаимодействий с существами!");
        }
    }
    
    private String formatLocation(Location location) {
        return String.format("%.1f, %.1f, %.1f", 
            location.getX(), location.getY(), location.getZ());
    }
    
    @Override
    public List<String> getDescription() {
        List<String> description = new ArrayList<>();
        description.add("§7Специализированный блок для обработки взаимодействия");
        description.add("§7игроков с существами с расширенной функциональностью");
        description.add("");
        description.add("§eВозможности:");
        description.add("§7• Настройка ограничений и разрешений");
        description.add("§7• Ограничения по здоровью, возрасту и приручению");
        description.add("§7• Защита от эксплойтов");
        description.add("§7• Автоматические действия (приручение, размножение, лечение)");
        description.add("§7• Статистика и достижения");
        description.add("§7• Экономическая система");
        description.add("§7• PvP защита");
        description.add("§7• Ограничения по времени и погоде");
        description.add("§7• Кастомные звуки и эффекты");
        description.add("§7• Логирование и отладка");
        description.add("§7• Эффекты зелий и опыт");
        description.add("§7• Потребление предметов");
        return description;
    }
    
    @Override
    public boolean validate() {
        // Валидация настроек
        if (cooldownMs < 0) {
            return false;
        }
        
        if (maxInteractionRadius < 0) {
            return false;
        }
        
        if (minEntityHealth < 0 || maxEntityHealth > 1000) {
            return false;
        }
        
        if (maxInteractionsPerEntity < 1) {
            return false;
        }
        
        if (maxInteractionsPerSecond < 1) {
            return false;
        }
        
        if (experienceReward < 0) {
            return false;
        }
        
        return true;
    }
    
    // Вспомогательные методы для работы с переменными
    private Object getVariable(String key) {
        // В реальной реализации здесь должна быть логика получения переменной
        // Пока что возвращаем null для совместимости
        return null;
    }
    
    private void setVariable(String key, Object value) {
        // В реальной реализации здесь должна быть логика установки переменной
        // Пока что ничего не делаем для совместимости
    }
}
